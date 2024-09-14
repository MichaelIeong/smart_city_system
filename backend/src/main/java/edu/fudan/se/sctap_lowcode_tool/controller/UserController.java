package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        System.out.println(loginRequest.getUsername());
        System.out.println(loginRequest.getPassword());
        try {
            // 调用 UserService 进行用户验证
            String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            // 返回生成的 JWT token以及用户的项目list
            LoginResponse loginResponse = new LoginResponse(token);
            System.out.println("Login Response: " + new ObjectMapper().writeValueAsString(loginResponse));  // 打印 JSON 格式的响应
            return ResponseEntity.ok(loginResponse);  // 返回生成的 JWT token 或者其他凭证
        } catch (Exception e) {
            System.out.println("gagagaga");
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        }
    }

    // 注册接口，接收用户名和密码
    @Operation(summary = "用户登录", description = "登录，验证后返回token和projectList")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest) {
        System.out.println(registerRequest.getUserName());
        System.out.println(registerRequest.getPassWord());
        try {
            userService.register(registerRequest.getUserName(), registerRequest.getPassWord());
            return ResponseEntity.ok("用户注册成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("注册失败: " + e.getMessage());
        }
    }
}
