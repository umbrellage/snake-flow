package com.juliet.flow.domain.model.rule.access;

import com.alibaba.fastjson2.JSON;
import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SpecialProcessRule
 *
 * @author Geweilang
 * @date 2023/8/24
 */
@Component
@Slf4j
public class SpecialProcessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "special_process_access";
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        log.info("param:{}", JSON.toJSONString(params));
        return  (boolean) params.get("specialTec");
    }
}
