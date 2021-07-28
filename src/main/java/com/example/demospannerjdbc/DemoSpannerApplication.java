package com.example.demospannerjdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.demospannerjdbc.mybatis.SpannerLocalDateTimeTypeHandler;

@SpringBootApplication
public class DemoSpannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoSpannerApplication.class, args);
	}

	@Bean
	public SpannerLocalDateTimeTypeHandler spannerLocalDateTimeTypeHandler() {
		return new SpannerLocalDateTimeTypeHandler();
	}
}
