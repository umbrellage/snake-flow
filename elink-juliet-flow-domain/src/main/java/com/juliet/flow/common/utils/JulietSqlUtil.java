package com.juliet.flow.common.utils;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.common.core.exception.base.BaseException;
import java.util.function.Function;
import lombok.NoArgsConstructor;

/**
 * JulietSqlUtil
 *
 * @author Geweilang
 * @date 2024/4/8
 */
@NoArgsConstructor
public final class JulietSqlUtil {


    public static <T> T findById(Long id, Function<Long, T> function, String msg) {
        T t = function.apply(id);
        if (t == null) {
            throw new ServiceException(msg);
        }
        return t;
    }
}
