package com.juliet.flow.domain.model;

import com.juliet.flow.client.vo.FieldVO;
import com.juliet.flow.client.vo.FormVO;
import java.util.stream.Collectors;
import lombok.Data;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Form extends BaseModel {

    private Long id;

    private String name;

    private String code;

    private String path;

    private List<Field> fields;


    public FormVO toForm() {
        FormVO data = new FormVO();
        data.setId(id);
        data.setName(name);
        data.setPath(path);
        data.setCode(code);
        if (CollectionUtils.isNotEmpty(fields)) {
            List<FieldVO> fieldVOList = fields.stream()
                .map(Field::toField)
                .collect(Collectors.toList());
            data.setFields(fieldVOList);
        }
        return data;
    }
}
