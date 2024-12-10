package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.UserInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.UserRepository;
import edu.fudan.se.sctap_lowcode_tool.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;  // 负责生成 JWT Token

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;  // 密码加密/验证

    /**
     * 用户登录方法，验证用户身份并生成 JWT Token。
     *
     * @param username 用户名
     * @param password 密码
     * @return 生成的 JWT Token
     * @throws Exception 如果用户名不存在或密码错误
     */
    public String login(String username, String password) throws Exception {
        // 根据用户名查询用户
        UserInfo user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("密码错误");
        }

        // 如果验证成功，生成 JWT Token
        return jwtTokenProvider.createToken(username, user.getProjects());
    }

    /**
     * 用户注册方法，创建新用户。
     *
     * @param username 用户名
     * @param password 密码
     * @throws Exception 如果用户名已存在
     */
    public void register(String username, String password) throws Exception {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("用户名已存在");
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);

        // 创建并保存用户
        UserInfo newUser = new UserInfo();
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);  // 保存加密后的密码
        userRepository.save(newUser);
    }
}