package com.juliet.flow.client.callback.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.flow.client.callback.MsgNotifyCallback;
import com.juliet.flow.client.dto.NotifyDTO;

import java.io.IOException;
import java.util.List;

import com.juliet.flow.client.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${flow.callback.url:http://127.0.0.1:9400/todo/callback}")
//    @Value("${flow.callback.url:http://172.16.1.157:9400/todo/callback}")
    private String url;

    @Override
    public void notify(List<NotifyDTO> list) {
        log.info("notify param:{}, url{}", JSON.toJSONString(list), url);
        try {
            String param = JSON.toJSONString(list, "yyyy-MM-dd HH:mm:ss");
            String resp =  HttpUtil.postJson(url, param);
            log.info("notify callback response:{}", resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        HttpUtil.postJson("http://172.16.1.157:9400/todo/callback", "[]");
    }
}
