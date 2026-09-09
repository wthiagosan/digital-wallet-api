package com.portfolio.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.wallet.dto.request.CreateUserRequest;
import com.portfolio.wallet.dto.response.UserResponse;
import com.portfolio.wallet.exception.DocumentAlreadyExistsException;
import com.portfolio.wallet.exception.GlobalExceptionHandler;
import com.portfolio.wallet.exception.UserNotFoundException;
import com.portfolio.wallet.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/v1/users - Should return 201 Created with Location header and body")
    void shouldCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Margaret Hamilton", "12345678901", "margaret@nasa.gov");
        UserResponse response = new UserResponse(1L, "Margaret Hamilton", "12345678901", "margaret@nasa.gov", 10L, LocalDateTime.now());

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Margaret Hamilton"))
                .andExpect(jsonPath("$.documentNumber").value("12345678901"))
                .andExpect(jsonPath("$.walletId").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/users - Should return 400 Bad Request with ProblemDetail when payload is invalid")
    void shouldReturn400WhenInvalidPayload() throws Exception {
        CreateUserRequest invalid = new CreateUserRequest("", "123", "not-an-email");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetros de Entrada Inválidos"))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_PARAMETERS"))
                .andExpect(jsonPath("$.invalidParams.fullName").exists())
                .andExpect(jsonPath("$.invalidParams.email").exists());
    }

    @Test
    @DisplayName("POST /api/v1/users - Should return 409 Conflict when document already exists")
    void shouldReturn409WhenDocumentExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Alan Turing", "12345678901", "alan@mail.com");

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DocumentAlreadyExistsException("12345678901"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Documento Já Cadastrado"))
                .andExpect(jsonPath("$.code").value("DOCUMENT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.documentNumber").value("12345678901"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should return 200 OK when user exists")
    void shouldFindUserById() throws Exception {
        UserResponse response = new UserResponse(1L, "Margaret Hamilton", "12345678901", "margaret@nasa.gov", 10L, LocalDateTime.now());
        when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Margaret Hamilton"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should return 404 Not Found when user does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findById(999L)).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Usuário Não Encontrado"))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/users - Should return 200 OK with list of users")
    void shouldListAllUsers() throws Exception {
        UserResponse r1 = new UserResponse(1L, "User 1", "111", "u1@mail.com", 10L, LocalDateTime.now());
        UserResponse r2 = new UserResponse(2L, "User 2", "222", "u2@mail.com", 20L, LocalDateTime.now());

        when(userService.findAll()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("User 1"));
    }
}
