package edu.fudan.se.sctap_lowcode_tool;

import edu.fudan.se.sctap_lowcode_tool.constant.Redis_Constant;
import edu.fudan.se.sctap_lowcode_tool.constant.Sys_Prompt;
import edu.fudan.se.sctap_lowcode_tool.utils.redis.RedisUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisUtil redisUtil;

    @Test
    public void test1() {
        List<String> eventList = redisUtil.getAll(Redis_Constant.Event_Prefix);
        List<String> propertyList = redisUtil.getAll(Redis_Constant.Property_Prefix);
        List<String> actionList = redisUtil.getAll(Redis_Constant.Action_Prefix);
        String eventOptions    = String.join("\n", eventList);
        String propertyOptions = String.join("\n", propertyList);
        String actionOptions   = String.join("\n", actionList);
        String prompt = String.format(Sys_Prompt.SYSTEM_PROMPT1, eventOptions, propertyOptions, actionOptions);
        // 存入redis
        redisUtil.setSingle(Redis_Constant.SYSTEM_PROMPT1, prompt);
    }


}




























