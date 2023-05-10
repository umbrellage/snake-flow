package com.juliet.flow.client.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xujianjie
 * @date 2023-05-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemFieldQuery {

    private String value;

    private boolean like;

}
