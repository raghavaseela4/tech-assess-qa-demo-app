package com.example.demo.adapter.out.http;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OkHttp interceptor that propagates correlation IDs to downstream services.
 *
 * <p>This interceptor reads the correlation ID from MDC (set by CorrelationIdFilter)
 * and adds it as an X-Correlation-ID header to all outgoing HTTP requests.
 *
 * <p>This ensures end-to-end tracing across service boundaries:
 * <pre>
 * Client → BFF (CorrelationIdFilter sets MDC) → Claims Service (receives X-Correlation-ID)
 * </pre>
 *
 * @see com.example.demo.adapter.in.web.filters.CorrelationIdFilter
 */
@Component
public class CorrelationIdInterceptor implements Interceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Read correlation ID from MDC (set by CorrelationIdFilter)
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);

        // If correlation ID exists in MDC, add it to outgoing request
        if (correlationId != null && !correlationId.isBlank()) {
            Request requestWithCorrelationId = originalRequest.newBuilder()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build();

            return chain.proceed(requestWithCorrelationId);
        }

        // No correlation ID in MDC, proceed with original request
        return chain.proceed(originalRequest);
    }
}
