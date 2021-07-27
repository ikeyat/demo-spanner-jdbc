package com.example.demospanner.model;

import java.time.LocalDateTime;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

@Table(name = "todo")
public class Todo {

	@PrimaryKey
	private String id;

	private String title;

	private Boolean finished;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public Todo() {
		super();
	}

	public Todo(String title) {
		super();
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean isFinished() {
		return finished;
	}

	public void setFinished(Boolean finished) {
		this.finished = finished;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
