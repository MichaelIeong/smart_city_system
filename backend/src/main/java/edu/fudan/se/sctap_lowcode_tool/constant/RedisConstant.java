package edu.fudan.se.sctap_lowcode_tool.constant;

public class RedisConstant {
    public static final String TIME_WAIT_PREFIX = "timeWait:";
    public static final String ACTION_WAIT_PREFIX = "actionWait:";
    public static final String WAIT_SET_PREFIX = "appRule:wait:";
    public static final String LOG_LIST_PREFIX = "appRule:log:";
    public static final String PUSH_LIST_PREFIX = "appRule:push:";
    public static final long DEFAULT_EXPIRE_DAYS = 1L; // 兜底过期时间
}
