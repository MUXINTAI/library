package com.example.library.config;

import com.example.library.entity.User;
import com.example.library.entity.Category;
import com.example.library.repository.UserRepository;
import com.example.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 删除现有的admin用户并重新创建
        userRepository.findByUsername("admin").ifPresent(user -> {
            userRepository.delete(user);
            System.out.println("已删除现有的admin用户");
        });
        
        // 创建新的管理员账户
        User admin = new User();
        admin.setUsername("admin");
        String encodedPassword = passwordEncoder.encode("admin123");
        admin.setPassword(encodedPassword);
        admin.setRealName("系统管理员");
        admin.setEmail("admin@library.com");
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        System.out.println("默认管理员账户已创建: admin/admin123");
        System.out.println("加密后的密码: " + encodedPassword);
        
        // 创建默认图书分类
        createDefaultCategories();
    }
    
    private void createDefaultCategories() {
        String[] categories = {
            "文学", "历史", "哲学", "科学", "技术", 
            "艺术", "医学", "法律", "经济", "教育"
        };
        
        for (String categoryName : categories) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category category = new Category();
                category.setName(categoryName);
                category.setDescription(categoryName + "类图书");
                categoryRepository.save(category);
            }
        }
        
        System.out.println("默认图书分类已创建");
    }
} 