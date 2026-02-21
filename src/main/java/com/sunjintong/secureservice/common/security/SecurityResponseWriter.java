package com.sunjintong.secureservice.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.common.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class SecurityResponseWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SecurityResponseWriter() {}

    public static void writeUnauthorized(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer(); // 关键：清空可能已有的sendError缓冲
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 先写一个你100%能识别的固定内容，排除序列化/Result问题
        String body = "{\"code\":\"UNAUTHORIZED\",\"message\":\"from SecurityResponseWriter\"}";
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}