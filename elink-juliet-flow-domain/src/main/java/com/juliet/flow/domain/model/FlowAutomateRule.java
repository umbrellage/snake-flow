package com.juliet.flow.domain.model;

import java.util.Map;

/**
 * FlowAutomateRule
 *
 * @author Geweilang
 * @date 2024/3/11
 */
public abstract class FlowAutomateRule {

    public abstract String flowAutomateRuleName();


    public abstract boolean flowAutomate(Node node, Map<String, Object> automateParam);

}
