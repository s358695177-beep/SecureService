package com.sunjintong.secureservice.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunjintong.secureservice.common.ErrorCode;
import com.sunjintong.secureservice.common.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class SecurityResponseWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SecurityResponseWriter() {}

    public static void writeUnauthorized(HttpServletResponse response) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED,
                Result.fail(ErrorCode.UNAUTHORIZED));
    }

    public static void writeForbidden(HttpServletResponse response) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN,
                Result.fail(ErrorCode.FORBIDDEN));
    }

    private static void write(HttpServletResponse response, int status, Result<?> body)
            throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer();
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        MAPPER.writeValue(response.getWriter(), body);
    }
}