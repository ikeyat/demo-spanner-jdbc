# メモ
## 経緯
- Spring Data Cloud Spannerを試したが、従来のDBアクセスライブラリよりもできることが少ない印象だった。
  - https://github.com/ikeyat/demo-spanner
- 一方、JDBC版だと、コネクションプールがネックになったりしないか、機能制約や動作不安定がないかが気になる。
- SpannerのJDBCを用いて、従来のDBアクセスライブラリでSpannerを利用してみる。

## 前提
- MacOS
- Docker
- インターネット接続可能（Google Container Registryへのアクセス）
- Spring Boot
- MyBatis
- オープンソース版Spanner JDBC
  - 他にSimba版Spanner JDBCもあるが、それはTODO
  - https://cloud.google.com/spanner/docs/jdbc-drivers

## 準備
### Cloud CLIのインストール（Dockerイメージ）
- https://cloud.google.com/sdk/docs/downloads-docker
  - 「指定した Docker イメージのインストール」でpullしてくるだけで良い。
  
### Spannerのエミュレータのインストール（Dockerイメージ）
- https://cloud.google.com/spanner/docs/emulator#docker
- SpannerのDockerは常に使うのでずっと動かしておく。
- 途中「エミュレータで gcloud CLI を使用する」でCloud CLIが必要になるため、
  - Cloud CLIをインストールする（前出）。
  - Cloud CLIをDockerに入れているので、以下に読み替えて、Docker内でエミュレータ切り替え操作を実施。
 
     ```
     docker run --rm -it gcr.io/google.com/cloudsdktool/cloud-sdk:latest
     gcloud config configurations create emulator
     gcloud config set auth/disable_credentials true
     gcloud config set project demo-spanner
     gcloud config set api_endpoint_overrides/spanner http://host.docker.internal:9020/
     ```

    - `host.docker.internal`に変更している理由について。CLIのDockerから見たSpannerエミュレータの宛先はlocalhostではないため。
      - https://qiita.com/ijufumi/items/badde64d530e6bade382#%E3%81%9D%E3%81%AE5hostdockerinternal-%E3%82%92%E4%BD%BF%E3%81%86-20190916%E8%BF%BD%E8%A8%9820200227%E7%B7%A8%E9%9B%86
  - CLI Docker内のまま、インスタンスを作成。

     ```
     gcloud spanner instances create test-instance --config=emulator-config --description="Test Instance" --nodes=1
     ```
  - CLI Docker内のまま、databaseを作成。

     ```
     gcloud spanner databases create test-database --instance=test-instance
     ```
   	 - SpannerにはRDBのスキーマ相当の概念が無い模様
   	     - スキーマという単語 は出てくるが、別の意味ぽい。 

### TBLを準備する
- DDLやDMLはgcloud spannerで行う（サードパーティのspanner-cliもあるが）
  - https://cloud.google.com/spanner/docs/modify-gcloud
  - https://cloud.google.com/sdk/gcloud/reference/spanner
  - https://cloud.google.com/sdk/gcloud/reference/spanner/databases/ddl/update#DATABASE
- todoテーブルの削除と作成。CLIのDocker内で実行。

  ```
  gcloud spanner databases ddl update test-database --ddl="DROP TABLE todo"  --instance=test-instance
  gcloud spanner databases ddl update test-database --ddl="CREATE TABLE todo(id STRING(36), title STRING(30), finished BOOL, created_at TIMESTAMP) PRIMARY KEY (id)"  --instance=test-instance 
  ```

## サンプルAPの作成や実行
###  Spring Data Cloud SpannerのサンプルAPから修正
- https://github.com/ikeyat/demo-spanner
- Spring Data Cloud Spannerと比較するため、Spring Boot 2.4.xのままにする。
- Spring Cloud GCP等の依存関係を削除し、mybatisのstarterと、オープンソース版のSpanner JDBC Driverの依存関係を追加。
  - https://github.com/googleapis/java-spanner-jdbc#quickstart

### Spannerエミュレータ接続設定
- application.propertiesには、エミュレータ準備時に設定した以下の値を入れる。
- 接続文字列はSpanner JDBC Driverのドキュメントに従う。
  - エミュレータの場合は、`sePlainText=true`が必要
  - エミュレータの場合は、`export SPANNER_EMULATOR_HOST=localhost:9010`するのが一般的らしいが、環境変数だと設定漏れしそうなので、接続文字列にエミュレータ接続先を含めてしまう方法を採用。
     - https://cloud.google.com/spanner/docs/use-oss-jdbc#connecting_to_the_emulator

     ```
     spring.datasource.driver-class-name=com.google.cloud.spanner.jdbc.JdbcDriver
     spring.datasource.url=jdbc:cloudspanner:${demo.spanner.host}/projects/${demo.spanner.project-id}/instances/${demo.spanner.instance-id}/databases/${demo.spanner.database-id};${demo.spanner.option}
     
     demo.spanner.host=//localhost:9010
     demo.spanner.project-id=demo-spanner
     demo.spanner.instance-id=test-instance
     demo.spanner.database-id=test-database
     demo.spanner.option=usePlainText=true
     ```

### サンプルAPのRepositoryやロジック修正
- groupidやpackage名は元から変更しておく。
- RepositoryやModelをMyBatisに対応させるため修正。
  - CrudReposiroryの`save()`、すなわちupsert相当をMyBatisでは実現できないため、`insert()`と`update()`に分割。
- オープンソース版Spanner JDBCはJDBC4.2に未対応と思われ（公式に記載なし）、`LocalDateTime`を扱う場合はMyBatisの`TypeHandler`を拡張する必要がある。
  - 本来JDBC4.2で扱える`LocalDateTime`をSQLにパラメータバインドしようとすると、以下のエラー。

     ```
     Caused by: org.springframework.jdbc.UncategorizedSQLException: 
     ### Error updating database.  Cause: com.google.cloud.spanner.jdbc.JdbcSqlExceptionFactory$JdbcSqlExceptionImpl: INVALID_ARGUMENT: Unsupported parameter type: java.time.LocalDateTime - 2021-07-28T21:43:48.744780
     ```
  - MyBatisが、JDBC4.2対応するタイミングで行った修正をもとに戻すような拡張をしてあげる。
     - https://github.com/mybatis/mybatis-3/commit/963a8a577bc1d9a98e5182a7779a85a3ca834984

### サンプルAPの実行
- DemoApplicationをSpringBootで実行（STSからでOK）
  - `ApplicationRunner`で起動時にレコード追加や削除を適当にしている。結果がログに出るので、正常に動いたかを確認。
  - Spring Data Cloud Spannerと異なり、RDB向けの`schema.sql`や`data.sql`が使えるので、そちらでテーブルを起動時に初期化するようにした。 
     - ただし、Spannerは`DROP TABLE xxx IF EXISTS`のような構文に対応していないため、TBLが存在しないと初期化エラーになってしまう。仕方ないので、`spring.datasource.continue-on-error=true` で初期化エラーを無視するようにする。
- オープンソース版Spanner JDBCのせいか、Spanner自体の問題かは不明だが、時々原因不明のSQL失敗エラーが発生し、二度と動かなくなる。SpannerエミュレータのDockerコンテナを再起動し、インスタンスを作り直せば当然直るが、原因は気になる。


