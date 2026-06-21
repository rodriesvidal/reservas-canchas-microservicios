package com.canchas.auth_service.service;

import com.canchas.auth_service.dto.AuthResponse;
import com.canchas.auth_service.dto.LoginRequest;
import com.canchas.auth_service.dto.RegistroRequest;

public interface AuthService {

    AuthResponse registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);
}