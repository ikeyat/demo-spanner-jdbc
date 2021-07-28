package com.example.demospannerjdbc.service;

import com.example.demospannerjdbc.model.Todo;

public interface TodoService {
	Iterable<Todo> findAll();

	Todo create(Todo todo);

	Todo finish(String todoId);

	void delete(String todoId);
}
