package org.zith.expr.ctxwl.webapi.base;

import jakarta.ws.rs.core.Response;
import org.zith.expr.ctxwl.webapi.common.ErrorCode;
import org.zith.expr.ctxwl.webapi.common.WebApiErrorCode;
import org.zith.expr.ctxwl.webapi.mapper.SimpleExceptionMapper;

import java.util.Objects;

public class SimpleErrorCodeAdapter<C extends ErrorCode> implements SimpleExceptionMapper.Code {
    protected final C code;

    public SimpleErrorCodeAdapter(C code) {
        Objects.requireNonNull(code);
        this.code = code;
    }

    @Override
    public String component() {
        return code.component().identifier();
    }

    @Override
    public String name() {
        return code.representation();
    }

    @Override
    public Response.StatusType status() {
        return Response.Status.BAD_REQUEST;
    }
}
