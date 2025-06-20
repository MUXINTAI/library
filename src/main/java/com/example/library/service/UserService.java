package com.example.library.service;

import com.example.library.dto.LoginRequest;
import com.example.library.dto.RegisterRequest;
import com.example.library.dto.UserDTO;
import com.example.library.dto.ProfileUpdateRequest;
import com.example.library.dto.PasswordUpdateRequest;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import com.example.library.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Map<String, Object> login(LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        if (!user.getEnabled()) {
            throw new RuntimeException("用户已被禁用");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", UserDTO.fromEntity(user));
        
        return result;
    }
    
    public UserDTO register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRealName(registerRequest.getRealName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.Role.READER); // 默认为读者角色
        
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
    
    public UserDTO registerAdmin(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRealName(registerRequest.getRealName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.Role.ADMIN); // 设置为管理员角色
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
    
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserDTO.fromEntity(user);
    }
    
    public UserDTO updateUser(Long id, RegisterRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户名是否已存在（排除当前用户）
        if (!user.getUsername().equals(updateRequest.getUsername()) && 
            userRepository.existsByUsername(updateRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（排除当前用户）
        if (updateRequest.getEmail() != null && 
            !updateRequest.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        user.setUsername(updateRequest.getUsername());
        user.setRealName(updateRequest.getRealName());
        user.setEmail(updateRequest.getEmail());
        user.setPhone(updateRequest.getPhone());
        
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
    
    public Page<UserDTO> getAllUsers(String keyword, User.Role role, Pageable pageable) {
        Page<User> users;
        
        if (role != null && keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByRoleAndKeyword(role, keyword.trim(), pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(UserDTO::fromEntity);
    }
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserDTO.fromEntity(user);
    }
    
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }
    
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(!user.getEnabled());
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
    
    public void promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
    }
    
    // 读者更新个人信息（不包括密码）
    public UserDTO updateProfile(String username, ProfileUpdateRequest updateRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否已存在（排除当前用户）
        if (updateRequest.getEmail() != null && 
            !updateRequest.getEmail().equals(user.getEmail()) &&
            userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        user.setRealName(updateRequest.getRealName());
        user.setEmail(updateRequest.getEmail());
        user.setPhone(updateRequest.getPhone());
        
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
    
    // 读者更新密码
    public void updatePassword(String username, PasswordUpdateRequest passwordRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证当前密码
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);
    }
    
    // 统计方法
    public long getTotalUserCount() {
        return userRepository.count();
    }
    
    public long getUserCountByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
} 