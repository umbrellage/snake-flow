package com.juliet.flow.common.utils;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.common.StatusCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public class BusinessAssert {

    /**
     * @param obj
     * @param statusCode
     */
    public static void assertNotNull(Object obj, StatusCode statusCode) {
        assertNotNull(obj, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param obj
     * @param statusCode
     */
    public static void assertNotNull(Object obj, StatusCode statusCode, String... format) {
        assertNotNull(obj, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param obj
     * @param status
     * @param code
     * @param msg
     */
    public static void assertNotNull(Object obj, int status, String code, String msg) {
        if (Objects.isNull(obj)) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }

    }


    /**
     * @param str
     * @param statusCode
     */
    public static void assertNotBlank(String str, StatusCode statusCode) {
        assertNotBlank(str, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param str
     * @param statusCode
     */
    public static void assertNotBlank(String str, StatusCode statusCode, String... format) {
        assertNotBlank(str, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param str
     * @param status
     * @param code
     * @param msg
     */
    public static void assertNotBlank(String str, int status, String code, String msg) {
        if (StringUtils.isBlank(str)) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }
    }

    /**
     * @param collection
     * @param statusCode
     */
    public static void assertNotEmpty(Collection collection, StatusCode statusCode) {
        assertNotEmpty(collection, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param collection
     * @param statusCode
     */
    public static void assertEmpty(Collection collection, StatusCode statusCode) {
        assertEmpty(collection, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param collection
     * @param statusCode
     */
    public static void assertNotEmpty(Collection collection, StatusCode statusCode, String... format) {
        assertNotEmpty(collection, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param collection
     * @param statusCode
     * @param format
     */
    public static void assertEmpty(Collection collection, StatusCode statusCode, String... format) {
        assertEmpty(collection, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param collection
     * @param status
     * @param code
     * @param msg
     */
    public static void assertNotEmpty(Collection collection, int status, String code, String msg) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }
    }

    /**
     * @param collection
     * @param status
     * @param code
     * @param msg
     */
    public static void assertEmpty(Collection collection, int status, String code, String msg) {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }
    }

    /**
     * @param map
     * @param statusCode
     */
    public static void assertNotEmpty(Map map, StatusCode statusCode) {
        assertNotEmpty(map, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param map
     * @param statusCode
     */
    public static void assertNotEmpty(Map map, StatusCode statusCode, String... format) {
        assertNotEmpty(map, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param map
     * @param status
     * @param code
     * @param msg
     */
    public static void assertNotEmpty(Map map, int status, String code, String msg) {
        if (MapUtils.isEmpty(map)) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }
    }


    /**
     * @param a
     * @param b
     * @param statusCode
     */
    public static void assertEq(Object a, Object b, StatusCode statusCode) {
        assertEq(a, b, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param a
     * @param b
     * @param statusCode
     */
    public static void assertEq(Object a, Object b, StatusCode statusCode, String... format) {
        assertEq(a, b, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param a
     * @param b
     * @param status
     * @param code
     * @param msg
     */
    public static void assertEq(Object a, Object b, int status, String code, String msg) {
        if (!Objects.equals(a, b)) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }
    }

    /**
     * @param bool
     * @param statusCode
     */
    public static void assertTrue(Boolean bool, StatusCode statusCode) {
        assertTrue(bool, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * @param bool
     * @param statusCode
     */
    public static void assertTrue(Boolean bool, StatusCode statusCode, String... format) {
        assertTrue(bool, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * @param bool
     * @param status
     * @param code
     * @param msg
     */
    public static void assertTrue(Boolean bool, int status, String code, String msg) {
        if (Objects.isNull(bool) || bool == false) {
            throw new ServiceException("[" + code + "]" + msg, status);
        }
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGt(Integer origin, Integer target, StatusCode statusCode) {
        assertGt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGt(Integer origin, Integer target, StatusCode statusCode, String... format) {
        assertGt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断大于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertGt(Integer origin, Integer target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin > target) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGte(Integer origin, Integer target, StatusCode statusCode) {
        assertGte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGte(Integer origin, Integer target, StatusCode statusCode, String... format) {
        assertGte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断大于等于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertGte(Integer origin, Integer target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin > target) {
            return;
        }
        if (origin.equals(target)) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }


    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLt(Integer origin, Integer target, StatusCode statusCode) {
        assertLt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLt(Integer origin, Integer target, StatusCode statusCode, String... format) {
        assertLt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断小于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertLt(Integer origin, Integer target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin < target) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLte(Integer origin, Integer target, StatusCode statusCode) {
        assertLte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLte(Integer origin, Integer target, StatusCode statusCode, String... format) {
        assertLte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断小于等于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertLte(Integer origin, Integer target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin < target) {
            return;
        }
        if (origin.equals(target)) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }


    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGt(Long origin, Long target, StatusCode statusCode) {
        assertGt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGt(Long origin, Long target, StatusCode statusCode, String... format) {
        assertGt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断大于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertGt(Long origin, Long target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin > target) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGte(Long origin, Long target, StatusCode statusCode) {
        assertGte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断大于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertGte(Long origin, Long target, StatusCode statusCode, String... format) {
        assertGte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断大于等于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertGte(Long origin, Long target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin > target) {
            return;
        }
        if (origin.equals(target)) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLt(Long origin, Long target, StatusCode statusCode) {
        assertLt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLt(Long origin, Long target, StatusCode statusCode, String... format) {
        assertLt(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断小于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertLt(Long origin, Long target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin < target) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLte(Long origin, Long target, StatusCode statusCode) {
        assertLte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg());
    }

    /**
     * 判断小于
     *
     * @param origin
     * @param target
     * @param statusCode
     */
    public static void assertLte(Long origin, Long target, StatusCode statusCode, String... format) {
        assertLte(origin, target, statusCode.getStatus(), statusCode.getCode(), statusCode.getMsg(format));
    }

    /**
     * 判断小于等于
     *
     * @param status
     * @param code
     * @param msg
     */
    public static void assertLte(Long origin, Long target, int status, String code, String msg) {
        assertNotNull(origin, status, code, msg);
        assertNotNull(target, status, code, msg);
        if (origin < target) {
            return;
        }
        if (origin.equals(target)) {
            return;
        }
        throw new ServiceException("[" + code + "]" + msg, status);
    }

}
