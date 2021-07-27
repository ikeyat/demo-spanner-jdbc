package com.example.demospanner.repository;

import org.springframework.data.repository.query.Param;

import com.example.demospanner.model.Todo;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;
import com.google.cloud.spring.data.spanner.repository.query.Query;

public interface TodoRepository extends SpannerRepository<Todo, String> {
	@Query("SELECT COUNT(*) FROM todo WHERE finished = @finished")
	long countByFinished(@Param("finished") Boolean finished);
}
