package com.portfolio.wallet.controller;

import com.portfolio.wallet.dto.request.CreateUserRequest;
import com.portfolio.wallet.dto.response.UserResponse;
import com.portfolio.wallet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operações de cadastro e consulta de usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo usuário",
            description = "Cadastra um novo usuário no sistema e cria automaticamente sua carteira com saldo inicial zerado (0.00).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário e carteira criados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF/CNPJ ou e-mail já cadastrado no sistema")
    })
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        UserResponse response = userService.createUser(request);
        URI location = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes cadastrais do usuário e o identificador de sua carteira.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário localizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Retorna a listagem de todos os usuários registrados.")
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }
}
