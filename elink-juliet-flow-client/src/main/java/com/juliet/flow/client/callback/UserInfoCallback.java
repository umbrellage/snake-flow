package com.juliet.flow.client.callback;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xujianjie
 * @date 2023-05-19
 */
public interface UserInfoCallback {

    Long getUserId(HttpServletRequest request);

    Long getTenantId(HttpServletRequest request);
}
