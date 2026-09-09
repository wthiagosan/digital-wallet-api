package com.portfolio.wallet.controller;

import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.response.StatementResponse;
import com.portfolio.wallet.dto.response.WalletResponse;
import com.portfolio.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "Operações de carteira digital, saldo, depósitos e extratos")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Realizar depósito em carteira",
            description = "Adiciona fundos a uma carteira ativa existente e registra no histórico como transação de tipo DEPOSIT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso e saldo atualizado"),
            @ApiResponse(responseCode = "400", description = "Valor de depósito inválido (menor ou igual a zero)"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<WalletResponse> deposit(@PathVariable Long id,
                                                 @RequestBody @Valid DepositRequest request) {
        return ResponseEntity.ok(walletService.deposit(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar carteira por ID", description = "Retorna os detalhes da carteira, usuário associado e saldo atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carteira localizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<WalletResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.findById(id));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Consultar carteira por ID do Usuário", description = "Localiza a carteira vinculada a um determinado usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carteira localizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada para o usuário informado")
    })
    public ResponseEntity<WalletResponse> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.findByUserId(userId));
    }

    @GetMapping("/{id}/statement")
    @Operation(summary = "Consultar extrato da carteira",
            description = "Retorna o saldo atual e a lista de movimentações financeiras (depósitos e transferências enviadas/recebidas) ordenadas da mais recente para a mais antiga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<StatementResponse> getStatement(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getStatement(id));
    }
}
