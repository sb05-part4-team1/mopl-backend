package com.mopl.logging.mdc;

public final class MdcKeys {

    private MdcKeys() {
    }

    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";
    public static final String REQUEST_METHOD = "requestMethod";
    public static final String REQUEST_URI = "requestUri";
    public static final String REQUEST_START_TIME = "requestStartTime";
    public static final String USER_ID = "userId";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String USER_AGENT = "userAgent";

    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_TRACE_ID = "X-Trace-ID";
}
