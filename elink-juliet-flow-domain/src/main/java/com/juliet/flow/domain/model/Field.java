package com.juliet.flow.domain.model;

import com.juliet.flow.client.vo.FieldVO;
import com.juliet.flow.common.utils.IdGenerator;
import java.util.Date;
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
public class Field extends BaseModel {

    private Long id;

    private String code;

    private String name;

    public FieldVO toField() {
        FieldVO data = new FieldVO();
        BeanUtils.copyProperties(this, data);
        return data;
    }

    public Field deepCopy() {
        Field ret = new Field();
        ret.setId(IdGenerator.getId());
        ret.setCode(code);
        ret.setName(name);
        ret.setTenantId(getTenantId());
        ret.setCreateBy(getCreateBy());
        ret.setUpdateBy(getUpdateBy());
        ret.setCreateTime(new Date());
        ret.setUpdateTime(new Date());
        return ret;
    }
}
