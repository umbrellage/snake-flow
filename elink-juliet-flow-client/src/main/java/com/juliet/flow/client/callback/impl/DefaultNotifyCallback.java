package com.juliet.flow.client.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.flow.client.callback.MsgNotifyCallback;
import com.juliet.flow.client.dto.NotifyDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * DefaultNotifyCallback
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Service
@Slf4j
public class DefaultNotifyCallback implements MsgNotifyCallback {

    @Override
    public void notify(List<NotifyDTO> list) {
        log.info("notify param:{}", JSON.toJSONString(list));
    }
}
