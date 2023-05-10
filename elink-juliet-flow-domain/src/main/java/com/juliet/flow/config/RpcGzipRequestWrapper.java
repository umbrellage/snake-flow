package com.juliet.flow.config;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * @author xujianjie
 * @date 2023-05-04
 */
@Slf4j
public class RpcGzipRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest request;

    public RpcGzipRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        ServletInputStream inputStream = request.getInputStream();
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            ServletInputStream newStream = new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }

                @Override
                public int read() throws IOException {
                    return gzipInputStream.read();
                }
            };
            return newStream;
        } catch (Exception e) {
            log.error("ungzip fail, ", e);
        }
        return inputStream;
    }
}
