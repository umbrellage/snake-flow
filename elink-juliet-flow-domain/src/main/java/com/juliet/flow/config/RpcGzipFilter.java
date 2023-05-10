package com.juliet.flow.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author xujianjie
 * @date 2023-05-04
 */
@Slf4j
public class RpcGzipFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String contentEncoding = request.getHeader("Content-Encoding");
        if(StringUtils.isNotBlank(contentEncoding) && contentEncoding.contains("gzip")){
            request = new RpcGzipRequestWrapper(request);
        }
        filterChain.doFilter(request, servletResponse);
    }
}
