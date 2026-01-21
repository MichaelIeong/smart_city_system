package edu.fudan.se.sctap_lowcode_tool.utils.event_fusion_mock_jan_2026;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRunHistory;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.EventFusionRunHistoryService;
import org.beryx.textio.InputReader;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

import java.time.LocalDateTime;
import java.util.List;

public class PushMockEvent {

    // ====================================================
    // 【⚠️重要】
    //  1. 先运行 Spring Boot Application
    //  2. 再运行此 main 方法推送模拟特斯联事件
    //  3. 在【控制台】按照提示操作
    // ====================================================

    public static void main(String[] args) throws JsonProcessingException {
        var textIO = TextIoFactory.getTextIO();
        String baseUrl = textIO.newStringInputReader()
                            .withDefaultValue("localhost:8080")
                            .read("请输入低平台后端服务地址, 默认");
        while (true) {
            System.out.println();
            System.out.println("=".repeat(50));
            MenuOption option = textIO.newEnumInputReader(MenuOption.class).read("请选择操作");
            switch (option) {
                case PUSH -> push(textIO, baseUrl);
                case HISTORY -> showHistories(textIO, baseUrl);
                case EXIT -> System.exit(0);
            }
        }

    }

    public enum MenuOption {
        PUSH("推送模拟事件"),
        HISTORY("查看运行历史"),
        EXIT("退出");
        private final String desc;
        MenuOption(String desc) {this.desc = desc;}
        @Override public String toString() {return desc;}
    }

    public static final String EVENT_PUSH_API_PATH = "/api/fusion-rules/event";
    public static final String HISTORIES_API_PATH = "/api/fusion-rules/histories";

    private static void push(TextIO textIO, String baseUrl) {
        MockTslEvent.Grid grid = textIO.newEnumInputReader(MockTslEvent.Grid.class).read("请选择网格");
        MockTslEvent.Photo photo = textIO.newEnumInputReader(MockTslEvent.Photo.class).read("请选择照片");
        LocalDateTime eventTime = textIO
            .newGenericInputReader(input -> new InputReader.ParseResult<>(LocalDateTime.parse(input)))
            .withDefaultValue(LocalDateTime.now())
            .read("请输入事件时间, 默认为: 当前时间");
        MockTslEvent mockEvent = new MockTslEvent(eventTime, grid, photo);
        DataEvent dataEvent = mockEvent.toDataEvent();
        try (HttpResponse httpResponse = HttpRequest
            .post(baseUrl + EVENT_PUSH_API_PATH).body(JSONUtil.toJsonStr(dataEvent))
            .timeout(3000).execute()) {
            if (httpResponse.getStatus() == 200) System.out.println("✅ 模拟事件推送成功!");
            else System.out.println("❌ 模拟事件推送失败! " + httpResponse.getStatus() + " " + httpResponse.body());
        } catch (Exception e) {
            System.out.println("❌ 模拟事件推送异常! " + e.getMessage());
        }
    }

    private static void showHistories(TextIO textIO, String baseUrl) throws JsonProcessingException {
        List<String> ids = showHistoriesReturningIds(baseUrl);
        if (ids.isEmpty()) {
            System.out.println("没有运行历史记录可供查看.");
            return;
        }
        String id = textIO.newStringInputReader()
                          .withInlinePossibleValues(ids)
                          .withDefaultValue(ids.get(0))
                          .read("请输入要查看的运行历史记录ID, 默认");
        showHistoryDetail(baseUrl, id);
    }

    private static void showHistoryDetail(String baseUrl, String id) throws JsonProcessingException {
        String url = baseUrl + HISTORIES_API_PATH + "/" + id;
        String respBody = HttpUtil.get(url);
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        EventFusionRunHistory detail = objectMapper.readValue(respBody, EventFusionRunHistory.class);
        System.out.println("运行历史记录详情:");
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        pp.indentObjectsWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        String pretty = objectMapper.writer(pp).writeValueAsString(detail);
        System.out.println(pretty);
    }

    private static List<String> showHistoriesReturningIds(String baseUrl) throws JsonProcessingException {
        String url = baseUrl + HISTORIES_API_PATH + "?pageNum=0&pageSize=5";
        String respBody = HttpUtil.get(url);
        PageDTO <EventFusionRunHistoryService.BriefResponse> results = new ObjectMapper().readValue(respBody, new TypeReference<>() {});
        System.out.println("最近5条运行历史记录:");
        results.data().forEach(r -> System.out.printf("[%d] %s (%s, %s)%n", r.id(), r.ruleName(), r.success() ? "成功" : "失败", r.createTime()));
        return results.data().stream().map(r -> r.id().toString()).toList();
    }


}
