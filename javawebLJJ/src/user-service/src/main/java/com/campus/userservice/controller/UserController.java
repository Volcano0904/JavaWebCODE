package com.campus.userservice.controller;

import com.campus.userservice.entity.User;
import com.campus.userservice.exception.UserException;
import com.campus.userservice.service.UserService;
import com.campus.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody User user) {
        User savedUser = userService.registerUser(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "注册成功");
        response.put("data", savedUser);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam @Email @NotBlank String email, 
                                                     @RequestParam @NotBlank String password) {
        Optional<User> optionalUser = userService.findByUsername(email);
        if (!optionalUser.isPresent()) {
            throw new UserException("用户不存在");
        }

        User user = optionalUser.get();
        if (!userService.validatePassword(user, password)) {
            throw new UserException("密码错误");
        }

        if (user.getStatus() == 0) {
            throw new UserException("账户已被禁用");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("token", token);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Optional<User> optionalUser = userService.findById(id);
        if (!optionalUser.isPresent()) {
            throw new UserException("用户不存在");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", optionalUser.get());

        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, 
                                                          @Valid @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "更新成功");
        response.put("data", updatedUser);

        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> users = userService.getAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        response.put("total", users.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 公共端点 - 不需要认证
     */
    @GetMapping("/public/profile/{id}")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable Long id) {
        Optional<User> optionalUser = userService.findById(id);
        if (!optionalUser.isPresent()) {
            throw new UserException("用户不存在");
        }

        User user = optionalUser.get();
        // 只返回公开信息
        Map<String, Object> publicProfile = new HashMap<>();
        publicProfile.put("id", user.getId());
        publicProfile.put("username", user.getUsername());
        publicProfile.put("realName", user.getRealName());
        publicProfile.put("avatar", user.getAvatar());
        publicProfile.put("college", user.getCollege());
        publicProfile.put("major", user.getMajor());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", publicProfile);

        return ResponseEntity.ok(response);
    }
}