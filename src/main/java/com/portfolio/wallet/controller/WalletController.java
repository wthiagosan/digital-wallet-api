package com.portfolio.wallet.controller;

import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.response.StatementResponse;
import com.portfolio.wallet.dto.response.WalletResponse;
import com.portfolio.wallet.security.IdempotencyService;
import com.portfolio.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@Validated
@Tag(name = "Wallets", description = "Operações de carteira digital, saldo, depósitos e extratos")
public class WalletController {

    private final WalletService walletService;
    private final IdempotencyService idempotencyService;

    public WalletController(WalletService walletService, IdempotencyService idempotencyService) {
        this.walletService = walletService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Realizar depósito em carteira",
            description = "Adiciona fundos a uma carteira ativa existente e registra no histórico como transação de tipo DEPOSIT. Suporta cabeçalho Idempotency-Key para prevenção de duplicações.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso e saldo atualizado"),
            @ApiResponse(responseCode = "400", description = "Valor de depósito inválido ou parâmetros incorretos"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito de requisição concorrente com mesma chave de idempotência")
    })
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable @Positive(message = "O ID da carteira deve ser maior que zero") Long id,
            @Parameter(description = "Chave única opcional para garantia de idempotência em retentativas de rede")
            @RequestHeader(value = IdempotencyService.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @RequestBody @Valid DepositRequest request) {
        WalletResponse response = idempotencyService.execute(idempotencyKey, () -> walletService.deposit(id, request));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar carteira por ID", description = "Retorna os detalhes da carteira, usuário associado e saldo atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carteira localizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<WalletResponse> findById(
            @PathVariable @Positive(message = "O ID da carteira deve ser maior que zero") Long id) {
        return ResponseEntity.ok(walletService.findById(id));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Consultar carteira por ID do Usuário", description = "Localiza a carteira vinculada a um determinado usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carteira localizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada para o usuário informado")
    })
    public ResponseEntity<WalletResponse> findByUserId(
            @PathVariable @Positive(message = "O ID do usuário deve ser maior que zero") Long userId) {
        return ResponseEntity.ok(walletService.findByUserId(userId));
    }

    @GetMapping("/{id}/statement")
    @Operation(summary = "Consultar extrato da carteira",
            description = "Retorna o saldo atual e a lista de movimentações financeiras (depósitos e transferências enviadas/recebidas) ordenadas da mais recente para a mais antiga.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extrato gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    public ResponseEntity<StatementResponse> getStatement(
            @PathVariable @Positive(message = "O ID da carteira deve ser maior que zero") Long id) {
        return ResponseEntity.ok(walletService.getStatement(id));
    }
}
