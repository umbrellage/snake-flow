package com.juliet.flow.client.callback;

import javax.servlet.http.HttpServletRequest;

/**
 * DevProcessConfigCallback
 *
 * @author Geweilang
 * @date 2024/1/26
 */
public interface DevProcessConfigCallback {

    String getProcessConfigId(HttpServletRequest request);

}
