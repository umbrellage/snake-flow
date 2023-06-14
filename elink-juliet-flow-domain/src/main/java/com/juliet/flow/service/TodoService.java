package com.juliet.flow.service;

import com.juliet.flow.domain.model.Post;
import com.juliet.flow.domain.model.Todo;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface TodoService {

    /**
     * 给这些岗位发todo
     */
    void sendTodo(List<Post> posts, Todo todo);
}
