package edu.fudan.se.sctap_lowcode_tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class APIControllerTest {

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

        Object json = objectMapper.readValue(result, Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        System.out.println(prettyJson);
    }

    @Test
    public void getCyberResourcesByProjectId() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/cyberResources/project/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void getSocialResourcesByProjectId() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/socialResources/project/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void getDeviceTypesByProjectId() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/deviceTypes?project=1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void getDeviceDetailById() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/devices/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void getDevicesByProjectId() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/devices?project=1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }

    @Test
    public void getAllSpacesByProjectId() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/spaces?project=1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }
    @Test
    public void getSpaceDetail() throws Exception {
        printJsonResponse(mockMvc.perform(
                get("/api/spaces/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ));
    }
}
