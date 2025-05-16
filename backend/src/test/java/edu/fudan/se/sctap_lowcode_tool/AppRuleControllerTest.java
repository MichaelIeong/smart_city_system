package edu.fudan.se.sctap_lowcode_tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    public void login() throws Exception {
        // 构造登录请求的JSON数据
        String loginRequest = objectMapper.writeValueAsString(new LoginRequest("admin", "123"));

        // 模拟POST请求到/auth/login并获取响应的token
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 获取返回的JSON字符串
        String jsonResponse = result.getResponse().getContentAsString();
        // 从响应中解析出token（假设返回的JSON中有一个名为"token"的字段）
        this.token = objectMapper.readTree(jsonResponse).get("token").asText();
    }

    private void printJsonResponse(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        var result = resultActions
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        System.out.println(resultActions.andReturn().getResponse().getStatus());

        if (!result.isEmpty()) {
            Object json = objectMapper.readValue(result, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println(prettyJson);
        } else {
            System.out.println("[Empty Response]");
        }
    }

    @Test
    public void queryAll() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/taps?project=1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void queryById() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/taps/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void create() throws Exception {
        AppRuleRequest ruleRequest = new AppRuleRequest(
                1, "description", "{}", "uuid"
        );
        printJsonResponse(mockMvc.perform(
                MockMvcRequestBuilders.post("/api/taps")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest))
        ));
    }

    @Test
    public void update() throws Exception {
        AppRuleRequest ruleRequest = new AppRuleRequest(
                1, "description", "{}", "uuid"
        );
        printJsonResponse(mockMvc.perform(
                MockMvcRequestBuilders.put("/api/taps/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest))
        ));
    }

    @Test
    public void delete() throws Exception {
        printJsonResponse(mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/taps/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void batchDelete() throws Exception {
        printJsonResponse(mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/taps?id=1,2,3")
                        .header("Authorization", "Bearer " + token)
        ));
    }

}
