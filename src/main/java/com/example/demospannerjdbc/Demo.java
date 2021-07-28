package com.example.demospannerjdbc;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.demospannerjdbc.model.Todo;
import com.example.demospannerjdbc.service.TodoService;

@Component
public class Demo implements ApplicationRunner {
	private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

	private TodoService todoService;

	public Demo(TodoService todoService) {
		this.todoService = todoService;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Todo todo1 = new Todo("todo1");
		Todo todo2 = new Todo("todo2");
		Todo todo3 = new Todo("todo3");
		Todo todo4 = new Todo("todo4");
		Todo todo5 = new Todo("todo5");
		Todo todo6 = new Todo("todo6");

		todoService.create(todo1);
		todoService.create(todo2);
		todoService.create(todo3);
		printTodos("+3 created");

		todoService.create(todo4);
		todoService.create(todo5);
		try {
			todoService.create(todo6);
		} catch (RuntimeException e) {
			LOG.error("Business Error", e);
		}
		printTodos("+2 created");

		todo1.setFinished(true);
		printTodos("not updated");

		todoService.finish(todo1.getId());
		printTodos("1 updated");

		todoService.create(todo6);
		printTodos("+1 created");

		todoService.delete(todo1.getId());
		todoService.delete(todo2.getId());
		todoService.delete(todo3.getId());
		todoService.delete(todo4.getId());
		todoService.delete(todo5.getId());
		todoService.delete(todo6.getId());
		printTodos("all deleted");
	}

	private void printTodos(String memo) {
		Iterable<Todo> todos = todoService.findAll();
		LOG.info("--printTodos: " + memo + " --");
		todos.forEach(item -> LOG.info(ToStringBuilder.reflectionToString(item)));
		LOG.info("-------------------------");
	}
}
