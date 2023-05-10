package com.juliet.flow.client.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.callback.ControllerResponseCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * juliet的默认实现
 *
 * @author xujianjie
 * @date 2023-05-06
 */
@Service
@Slf4j
public class DefaultControllerResponseCallbackImpl implements ControllerResponseCallback {
    @Override
    public boolean responseIsSuccess(Object object) {
        try {
            AjaxResult result = JSON.to(AjaxResult.class, object);
            return result.getCode() != null && result.getCode().intValue() == 200;
        } catch (Exception e) {
            log.error("response fail, data:{}", object, e);
        }
        return false;
    }
}
