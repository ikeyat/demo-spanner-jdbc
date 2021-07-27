package com.example.demospanner.service;

import com.example.demospanner.model.Todo;

public interface TodoService {
	Iterable<Todo> findAll();

	Todo create(Todo todo);

	Todo finish(String todoId);

	void delete(String todoId);
}
