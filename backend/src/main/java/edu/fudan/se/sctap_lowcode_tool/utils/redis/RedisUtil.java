package edu.fudan.se.sctap_lowcode_tool.utils.redis;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RedisUtil {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询单条
     * */
    public String getSingle(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 存入redis，同时设置1个小时的过期时间
     * */
    public void setSingle(String key, String value){
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofHours(1));
    }


    /**
     * 查询多条
     * */
    public List<String> getMulti(List<String> keys, String prefix){
        List<String> list = new ArrayList<>();
        keys = keys.stream().map(key -> prefix + key).toList();
        if (!keys.isEmpty()) {
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
            if (values != null) {
                for (String val : values) {
                    if (val != null) {
                        list.add(val);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 匹配查询全部
     * */
    public List<String> getAll(String key){
        // 构建扫描选项：匹配 key 的键
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(key+"*")
                .count(100)  // 每批最多拉取100个
                .build();
        // 使用 scan 非阻塞方式获取匹配的键
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> result = new HashSet<>();
            try(Cursor<byte[]> cursor = connection.keyCommands().scan(scanOptions)){
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });
        // 如果有匹配的 key，批量获取它们的值
        List<String> list = new ArrayList<>();
        if (keys != null && !keys.isEmpty()) {
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
            if (values != null) {
                for (String val : values) {
                    if (val != null) {
                        list.add(val);
                    }
                }
            }
        }
        return list;
    }
}
