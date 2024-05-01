package com.telus.todo.controller;

import com.telus.todo.model.Todo;
import com.telus.todo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @GetMapping
    public Flux<Todo> getAllTodos() {
        return Flux.fromIterable(todoRepository.findAll());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Todo>> getTodoById(@PathVariable Long id) {
        return Mono.fromCallable(() -> todoRepository.findById(id))
                .map(todo -> todo.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build()));
    }

    @PostMapping
    public Mono<Todo> createTodo(@RequestBody Todo todo) {
        return Mono.fromCallable(() -> todoRepository.save(todo));
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<Todo>> updateTodo(
            @PathVariable Long id,
            @RequestBody Todo updatedTodo) {
        return Mono.just(todoRepository.findById(id)
                        .map(existingTodo -> {
                            existingTodo.setDescription(updatedTodo.getDescription());
                            existingTodo.setCompletionStatus(updatedTodo.isCompletionStatus());
                            return todoRepository.save(existingTodo);
                        })).map(todo -> ResponseEntity.ok().body(todo.orElse(null)))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteTodoById(@PathVariable Long id) {
        return Mono.fromRunnable(() -> todoRepository.deleteById(id));
    }
}