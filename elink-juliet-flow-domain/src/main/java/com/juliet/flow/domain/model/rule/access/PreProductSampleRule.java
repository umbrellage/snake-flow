package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PreProductSampleRule
 *
 * @author Geweilang
 * @date 2023/8/9
 */
@Slf4j
@Component
public class PreProductSampleRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "pre_production_sample";
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        Boolean value = (Boolean) params.get("isEnd");
        log.info("pre_production_sample value:{}", value);
        return value == null || !value;
    }
}
