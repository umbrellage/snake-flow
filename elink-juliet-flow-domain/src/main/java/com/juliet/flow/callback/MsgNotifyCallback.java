package com.juliet.flow.callback;


import com.juliet.flow.client.dto.NotifyDTO;
import java.util.List;

/**
 * MsgNotifyCallback
 * 消息通知回调接口
 * @author Geweilang
 * @date 2023/5/23
 */
public interface MsgNotifyCallback {

    /**
     *
     * @param list
     */
    void notify(List<NotifyDTO> list);


    void message(List<NotifyDTO> list);

}
