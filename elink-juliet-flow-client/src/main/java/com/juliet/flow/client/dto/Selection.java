package com.juliet.flow.client.dto;

import lombok.Data;

/**
 * Selection
 *
 * @author Geweilang
 * @date 2024/1/25
 */
@Data
public class Selection<T> {

    private T value;
    private String label;

}
