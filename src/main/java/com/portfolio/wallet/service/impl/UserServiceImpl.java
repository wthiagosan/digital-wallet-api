package com.portfolio.wallet.service.impl;

import com.portfolio.wallet.dto.request.CreateUserRequest;
import com.portfolio.wallet.dto.response.UserResponse;
import com.portfolio.wallet.exception.DocumentAlreadyExistsException;
import com.portfolio.wallet.exception.EmailAlreadyExistsException;
import com.portfolio.wallet.exception.UserNotFoundException;
import com.portfolio.wallet.model.User;
import com.portfolio.wallet.model.Wallet;
import com.portfolio.wallet.repository.UserRepository;
import com.portfolio.wallet.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new DocumentAlreadyExistsException(request.documentNumber());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User(request.fullName(), request.documentNumber(), request.email());
        Wallet wallet = new Wallet(user);
        user.setWallet(wallet);

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
