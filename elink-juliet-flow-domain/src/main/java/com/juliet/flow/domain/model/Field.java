package com.juliet.flow.domain.model;

import com.juliet.flow.client.vo.FieldVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Field {

    private Long id;

    private String code;

    private String name;

    public FieldVO toField() {
        FieldVO data = new FieldVO();
        BeanUtils.copyProperties(this, data);
        return data;
    }
}
