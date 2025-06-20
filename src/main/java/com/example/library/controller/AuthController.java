package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.RegisterRequest;
import com.example.library.dto.UserDTO;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> result = userService.login(loginRequest);
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO user = userService.register(registerRequest);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/register-admin")
    public ApiResponse<UserDTO> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest, Authentication authentication) {
        try {
            // 验证当前用户是否为管理员
            if (authentication == null || !authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
                return ApiResponse.forbidden("只有管理员才能创建管理员账户");
            }
            
            UserDTO user = userService.registerAdmin(registerRequest);
            return ApiResponse.success("管理员账户创建成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserDTO user = userService.getCurrentUser(username);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/create-emergency-admin")
    public ApiResponse<UserDTO> createEmergencyAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 临时接口，用于紧急创建管理员账户
            UserDTO user = userService.registerAdmin(registerRequest);
            return ApiResponse.success("紧急管理员账户创建成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/promote-to-admin/{username}")
    public ApiResponse<String> promoteToAdmin(@PathVariable String username) {
        try {
            // 临时接口，用于将用户提升为管理员
            userService.promoteToAdmin(username);
            return ApiResponse.success("用户已提升为管理员", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 