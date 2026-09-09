package com.portfolio.wallet.controller;

import com.portfolio.wallet.dto.request.TransferRequest;
import com.portfolio.wallet.dto.response.TransferResponse;
import com.portfolio.wallet.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "Operações de transferências financeiras entre carteiras (P2P)")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Realizar transferência P2P entre carteiras",
            description = "Executa uma transferência monetária atômica entre a carteira de origem e destino, validando saldo, impedindo auto-transferências e aplicando locking pessimista contra concorrência.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (mesma carteira de origem e destino ou valor menor/igual a zero)"),
            @ApiResponse(responseCode = "404", description = "Carteira de origem ou destino não encontrada"),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente na carteira de origem")
    })
    public ResponseEntity<TransferResponse> transfer(@RequestBody @Valid TransferRequest request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }
}
