package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.TransferRequest;
import com.portfolio.wallet.dto.response.TransferResponse;

public interface TransferService {

    TransferResponse transfer(TransferRequest request);
}
