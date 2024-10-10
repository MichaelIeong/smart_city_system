package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.LoginRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.LoginResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.RegisterRequest;
import edu.fudan.se.sctap_lowcode_tool.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/auth")
@Tag(name = "UserController", description = "用户控制器")
public class UserController {

    @Autowired
    private UserService userService;

    // 登录接口，接收用户名和密码
    @Operation(summary = "用户登录", description = "登录，验证后返回token和projectList")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 调用 UserService 进行用户验证
            String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            // 返回生成的 JWT token以及用户的项目list
            LoginResponse loginResponse = new LoginResponse(token);
            return ResponseEntity.ok(loginResponse);  // 返回生成的 JWT token 或者其他凭证
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        }
    }

    // 注册接口，接收用户名和密码
    @Operation(summary = "用户注册", description = "注册用户，成功返回注册成功，失败返回注册失败")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            userService.register(registerRequest.getUsername(), registerRequest.getPassword());
            return ResponseEntity.ok("Register success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Register failed: " + e.getMessage());
        }
    }
}
