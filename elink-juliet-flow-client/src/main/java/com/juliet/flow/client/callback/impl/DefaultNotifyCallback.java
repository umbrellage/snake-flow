package com.juliet.flow.client.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.flow.client.callback.MsgNotifyCallback;
import com.juliet.flow.client.dto.NotifyDTO;

import java.io.IOException;
import java.util.List;

import com.juliet.flow.client.utils.HttpUtil;
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
        try {
            String resp =  HttpUtil.postJson("http://127.0.0.1:9400/todo/callback", JSON.toJSONString(list));
            log.info("notify callback response:{}", resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        HttpUtil.postJson("http://172.16.1.152:9400/todo/callback", "[]");
    }
}
