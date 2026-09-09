package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.CreateUserRequest;
import com.portfolio.wallet.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse findById(Long id);

    List<UserResponse> findAll();
}
