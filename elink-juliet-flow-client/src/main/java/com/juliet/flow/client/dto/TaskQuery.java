package com.juliet.flow.client.dto;


import java.util.List;
import org.apache.poi.ss.formula.functions.T;

/**
 * TaskQuery
 *
 * @author Geweilang
 * @date 2024/7/12
 */
public interface TaskQuery extends Query<TaskQuery>{


    TaskQuery taskAssignee(String assignee);

}
