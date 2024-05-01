package com.telus.todo;

import com.telus.todo.model.Todo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class TodoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    public static void clearDatabase(@Autowired DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "todo");
    }

    @Test
    public void testGetAllTodos() {
        webTestClient.get().uri("/todos")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Todo.class);
    }

    @Test
    public void testGetTodoById() {
        Todo todo = new Todo();
        todo.setId(1L);
        todo.setDescription("Test Todo");
        // Save the Todo
        webTestClient.post().uri("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(todo)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Query the Todo by its ID
        webTestClient.get().uri("/todos/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Todo.class)
                .value(todoResponse -> {
                    // Assert the returned Todo matches the created Todo
                    assertNotNull(todoResponse);
                    assertEquals(1L, todoResponse.getId());
                    assertEquals("Test Todo", todoResponse.getDescription());
                });
    }

    @Test
    public void testGetTodoByIdNotFound() {
        Long id = 999L;
        webTestClient.get().uri("/todos/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
    }

    @Test
    public void testCreateTodo() {
        Todo todo = new Todo();
        webTestClient.post().uri("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(todo)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Todo.class);
    }

    @Test
    public void testCreateAndUpdateTodo() {
        // Create a Todo
        Todo todo = new Todo();
        todo.setDescription("Test Todo");
        todo.setCompletionStatus(false);

        // Send a POST request to create the Todo
        Todo createdTodo = webTestClient.post().uri("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(todo)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Update the created Todo
        Todo updatedTodo = new Todo();
        updatedTodo.setId(createdTodo.getId());
        updatedTodo.setDescription("Updated Todo");
        updatedTodo.setCompletionStatus(true);

        // Send a PATCH request to update the Todo
        webTestClient.patch().uri("/todos/{id}", updatedTodo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedTodo)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Todo.class)
                .value(todoResponse -> {
                    // Assert the returned Todo matches the updated Todo
                    assertNotNull(todoResponse);
                    assertEquals(updatedTodo.getId(), todoResponse.getId());
                    assertEquals(updatedTodo.getDescription(), todoResponse.getDescription());
                    assertEquals(updatedTodo.isCompletionStatus(), todoResponse.isCompletionStatus());
                });
    }

    @Test
    public void testUpdateTodoNotFound() {
        Long id = 999L;
        Todo updatedTodo = new Todo();
        webTestClient.patch().uri("/todos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedTodo)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testDeleteTodoById() {
        Long id = 1L;
        webTestClient.delete().uri("/todos/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }
}