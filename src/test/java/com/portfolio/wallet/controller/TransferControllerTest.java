package com.portfolio.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.wallet.dto.request.TransferRequest;
import com.portfolio.wallet.dto.response.TransferResponse;
import com.portfolio.wallet.exception.GlobalExceptionHandler;
import com.portfolio.wallet.exception.InsufficientBalanceException;
import com.portfolio.wallet.exception.SameWalletTransferException;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portfolio.wallet.security.IdempotencyService;

@WebMvcTest(TransferController.class)
@Import({GlobalExceptionHandler.class, IdempotencyService.class})
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @Test
    @DisplayName("POST /api/v1/transfers - Should execute transfer and return 200 OK")
    void shouldExecuteTransfer() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("45.00"));
        TransferResponse response = new TransferResponse(100L, 1L, 2L, new BigDecimal("45.00"),
                TransactionStatus.COMPLETED, LocalDateTime.now());

        when(transferService.transfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(100))
                .andExpect(jsonPath("$.sourceWalletId").value(1))
                .andExpect(jsonPath("$.targetWalletId").value(2))
                .andExpect(jsonPath("$.amount").value(45.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - Should return 422 Unprocessable Entity when insufficient balance")
    void shouldReturn422WhenInsufficientFunds() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("1000.00"));

        when(transferService.transfer(any(TransferRequest.class)))
                .thenThrow(new InsufficientBalanceException(1L, new BigDecimal("100.00"), new BigDecimal("1000.00")));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Saldo Insuficiente"))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.walletId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(100.00))
                .andExpect(jsonPath("$.requiredAmount").value(1000.00));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - Should return 400 Bad Request when self-transfer attempted")
    void shouldReturn400WhenSelfTransfer() throws Exception {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("50.00"));

        when(transferService.transfer(any(TransferRequest.class)))
                .thenThrow(new SameWalletTransferException(1L));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Transferência Para Mesma Carteira Proibida"))
                .andExpect(jsonPath("$.code").value("SELF_TRANSFER_FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - Should return 404 Not Found when wallet does not exist")
    void shouldReturn404WhenWalletNotFound() throws Exception {
        TransferRequest request = new TransferRequest(999L, 2L, new BigDecimal("50.00"));

        when(transferService.transfer(any(TransferRequest.class)))
                .thenThrow(new WalletNotFoundException(999L));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - Should return 400 Bad Request when payload fails Bean Validation")
    void shouldReturn400WhenInvalidPayload() throws Exception {
        TransferRequest invalid = new TransferRequest(null, null, new BigDecimal("-5.00"));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_PARAMETERS"));
    }
}
