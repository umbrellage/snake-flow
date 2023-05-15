package com.juliet.flow.client.utils;

import com.juliet.common.core.exception.ServiceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RestUtils
 *
 * @author geweilang
 * @date 2020/10/17
 */
public final class ServletUtils {

    public static String readBody(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        try {
            BufferedReader br = request.getReader();

            String str;
            StringBuilder wholeStr = new StringBuilder();
            while ((str = br.readLine()) != null) {
                wholeStr.append(str);
            }

            return wholeStr.toString();
        } catch (Exception e) {
            logger.debug("Failed to read body.", e);
        }

        return "";
    }

    final static String UNKNOWN_IP = "unknown";

    public static String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getRemoteAddr();

        return ip;
    }


    /**
     * 先获取cookie里的，cookie里没有，再尝试从url参数内获取
     *
     * @param request request
     * @return sessionId
     */
    static public String getSessionId(HttpServletRequest request, String paramName) {
        String sessionId = getCookieValueByName(request, paramName);
        if (StringUtils.isNotBlank(sessionId)) {
            return sessionId;
        }

        return request.getParameter(paramName);
    }


    /**
     * 获取cookie 的值
     *
     * @param request http request
     * @param name    cookie name
     * @return cookie 的值
     */
    static public String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookies = getCookieByName(request, name);
        if (cookies == null) {
            return null;
        }

        return cookies.getValue();
    }

    /**
     * 获取cookie 的值
     *
     * @param request http request
     * @param name    cookie name
     * @return cookie 对象
     */
    static public Cookie getCookieByName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies).filter(c -> StringUtils.equals(c.getName(), name))
            .findFirst()
            .orElse(null);
    }

    public static void downloadFile(HttpServletResponse response, File file) {
        OutputStream out = null;
        if (!file.exists()) {
            logger.info("Download local file not exist. File:{}", file);
            return;
        }

        String fileName = file.getName();
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (Exception e) {
            logger.error("Url encode error.", e);
        }
        fileName = fileName.replaceAll("\\+", "%20");
        try {
            logger.debug("Download local file:{}", file);
            response.reset();
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            out = response.getOutputStream();
            byte[] data = FileUtils.readFileToByteArray(file);
            if (ArrayUtils.isEmpty(data)) {
                // 空文件
                out.write("Empty File.".getBytes());
            } else {
                out.write(FileUtils.readFileToByteArray(file));
            }
            out.flush();

        } catch (IOException e) {
            logger.info("Error to download file. File:" + file, e);
            throw new ServiceException("下载文件失败");
        } finally {
            file.deleteOnExit();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("Delete file error.file:" + file, e);
                }
            }
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(ServletUtils.class);

    private ServletUtils() {
    }
}
