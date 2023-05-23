package com.juliet.flow.client.callback;


import com.juliet.flow.client.dto.NotifyDTO;

/**
 * MsgNotifyCallback
 * 消息通知回调接口
 * @author Geweilang
 * @date 2023/5/23
 */
public interface MsgNotifyCallback {

    /**
     *
     * @param dto
     */
    void notify(NotifyDTO dto);

}
