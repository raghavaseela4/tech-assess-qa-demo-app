package com.example.demo.adapter.out.http;

import brave.Tracing;
import brave.propagation.TraceContext;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OkHttp interceptor that propagates Brave/Zipkin trace context headers
 * to downstream services, enabling distributed tracing in Zipkin.
 */
@Component
public class TracingInterceptor implements Interceptor {

    private final Tracing tracing;

    public TracingInterceptor(Tracing tracing) {
        this.tracing = tracing;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        TraceContext context = tracing.currentTraceContext().get();
        if (context == null) {
            return chain.proceed(originalRequest);
        }

        Request.Builder builder = originalRequest.newBuilder();

        // Inject B3 propagation headers
        tracing.propagation().injector(Request.Builder::header).inject(context, builder);

        return chain.proceed(builder.build());
    }
}
