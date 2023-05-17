package com.juliet.flow.client.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * UserDTO
 * 用户信息
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class UserDTO {

    private Long userId;
    private Long tenantId;
    private List<Long> postId;
}
