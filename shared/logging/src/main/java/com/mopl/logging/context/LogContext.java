package com.mopl.logging.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * 구조화 로깅을 위한 컨텍스트 헬퍼.
 *
 * <pre>{@code
 * // 단순 사용 (일회성)
 * LogContext.with("contentId", contentId)
 *     .and("contentType", type)
 *     .info("Content created");
 *
 * // 예외 포함
 * LogContext.with("orderId", orderId)
 *     .error("Payment failed", exception);
 *
 * // 블록 스코프 (자동 정리)
 * try (var scope = LogContext.scoped("jobName", "syncJob")) {
 *     log.info("Job started");  // jobName이 MDC에 포함됨
 *     // ... 작업 수행
 *     log.info("Job completed");
 * }  // 자동으로 MDC에서 제거
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class LogContext {

    private final Map<String, String> fields = new HashMap<>();

    public static LogContext with(String key, Object value) {
        LogContext context = new LogContext();
        return context.and(key, value);
    }

    @SuppressWarnings("resource") // 호출자가 try-with-resources로 닫아야 함
    public static Scope scoped(String key, Object value) {
        return new Scope().add(key, value);
    }

    public LogContext and(String key, Object value) {
        if (key != null && value != null) {
            fields.put(key, String.valueOf(value));
        }
        return this;
    }

    public void debug(String message) {
        executeWithContext(() -> log.debug(formatMessage(message)));
    }

    public void info(String message) {
        executeWithContext(() -> log.info(formatMessage(message)));
    }

    public void warn(String message) {
        executeWithContext(() -> log.warn(formatMessage(message)));
    }

    public void warn(String message, Throwable t) {
        executeWithContext(() -> log.warn(formatMessage(message), t));
    }

    public void error(String message) {
        executeWithContext(() -> log.error(formatMessage(message)));
    }

    public void error(String message, Throwable t) {
        executeWithContext(() -> log.error(formatMessage(message), t));
    }

    private void executeWithContext(Runnable logAction) {
        Map<String, String> previousValues = new HashMap<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            previousValues.put(entry.getKey(), MDC.get(entry.getKey()));
            MDC.put(entry.getKey(), entry.getValue());
        }
        try {
            logAction.run();
        } finally {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String previous = previousValues.get(entry.getKey());
                if (previous != null) {
                    MDC.put(entry.getKey(), previous);
                } else {
                    MDC.remove(entry.getKey());
                }
            }
        }
    }

    private String formatMessage(String message) {
        if (fields.isEmpty()) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        sb.append(" |");
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 블록 스코프 MDC 컨텍스트. try-with-resources와 함께 사용.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Scope implements AutoCloseable {

        private final Map<String, String> fields = new HashMap<>();
        private final Map<String, String> previousValues = new HashMap<>();

        public Scope add(String key, Object value) {
            if (key != null && value != null) {
                String stringValue = String.valueOf(value);
                fields.put(key, stringValue);
                previousValues.put(key, MDC.get(key));
                MDC.put(key, stringValue);
            }
            return this;
        }

        @Override
        public void close() {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String previous = previousValues.get(entry.getKey());
                if (previous != null) {
                    MDC.put(entry.getKey(), previous);
                } else {
                    MDC.remove(entry.getKey());
                }
            }
        }
    }
}
