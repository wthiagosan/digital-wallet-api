package com.portfolio.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.wallet.dto.request.DepositRequest;
import com.portfolio.wallet.dto.response.StatementResponse;
import com.portfolio.wallet.dto.response.TransactionResponse;
import com.portfolio.wallet.dto.response.WalletResponse;
import com.portfolio.wallet.exception.GlobalExceptionHandler;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.model.enums.TransactionStatus;
import com.portfolio.wallet.model.enums.TransactionType;
import com.portfolio.wallet.service.WalletService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@Import(GlobalExceptionHandler.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @Test
    @DisplayName("POST /api/v1/wallets/{id}/deposit - Should deposit funds and return 200 OK")
    void shouldDepositFunds() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("150.00"));
        WalletResponse response = new WalletResponse(1L, 10L, new BigDecimal("350.00"), LocalDateTime.now(), LocalDateTime.now());

        when(walletService.deposit(eq(1L), any(DepositRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.balance").value(350.00));
    }

    @Test
    @DisplayName("POST /api/v1/wallets/{id}/deposit - Should return 400 Bad Request when amount is zero or negative")
    void shouldReturn400WhenNonPositiveAmount() throws Exception {
        DepositRequest invalid = new DepositRequest(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/v1/wallets/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_PARAMETERS"));
    }

    @Test
    @DisplayName("GET /api/v1/wallets/{id} - Should return wallet details with 200 OK")
    void shouldFindWalletById() throws Exception {
        WalletResponse response = new WalletResponse(1L, 10L, new BigDecimal("200.00"), LocalDateTime.now(), LocalDateTime.now());
        when(walletService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    @DisplayName("GET /api/v1/wallets/users/{userId} - Should return wallet details with 200 OK")
    void shouldFindByUserId() throws Exception {
        WalletResponse response = new WalletResponse(1L, 10L, new BigDecimal("200.00"), LocalDateTime.now(), LocalDateTime.now());
        when(walletService.findByUserId(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/wallets/{id}/statement - Should return statement with 200 OK")
    void shouldGetStatement() throws Exception {
        TransactionResponse tx = new TransactionResponse(100L, null, 1L, new BigDecimal("100.00"),
                TransactionType.DEPOSIT, TransactionStatus.COMPLETED, LocalDateTime.now());
        StatementResponse statement = new StatementResponse(1L, new BigDecimal("100.00"), List.of(tx));

        when(walletService.getStatement(1L)).thenReturn(statement);

        mockMvc.perform(get("/api/v1/wallets/1/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(100.00))
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].id").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/wallets/{id} - Should return 404 when wallet not found")
    void shouldReturn404WhenWalletNotFound() throws Exception {
        when(walletService.findById(999L)).thenThrow(new WalletNotFoundException(999L));

        mockMvc.perform(get("/api/v1/wallets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"));
    }
}
