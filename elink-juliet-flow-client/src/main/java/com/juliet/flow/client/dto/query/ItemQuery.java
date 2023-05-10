package com.juliet.flow.client.dto.query;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-04-24
 */
@Data
public class ItemQuery {

    private final static Integer MAX_ES_QUERY_PAGE_SIZE = 1000;

    private List<ItemFieldQuery> idList;

    private List<ItemFieldQuery> itemNoList;

    private List<ItemFieldQuery> categoryIdList;

    private List<ItemFieldQuery> categoryNameList;

    private List<ItemFieldQuery> brandIdList;

    private List<ItemFieldQuery> brandNameList;

    private List<ItemFieldQuery> itemNameList;

    /**
     * 1: 面料
     * 2: 辅料
     */
    private List<ItemFieldQuery> itemTypeList;

    /**
     * 1: 上架
     */
    private List<ItemFieldQuery> statusList;

    private List<ItemFieldQuery> tenantIdList;

    private List<ItemFieldQuery> createByList;

    private List<ItemFieldQuery> updateByList;

    private Date createTimeStart;

    private Date createTimeEnd;

    private Date updateTimeStart;

    private Date updateTimeEnd;

    private Map<String, AttrFilter> filter;

    private Integer pageNo;

    private Integer pageSize;

    public Integer getFrom() {
        return (getPageNo() - 1) * getSize();
    }

    public Integer getSize() {
        return getPageSize();
    }

    public Integer getPageNo() {
        if (pageNo == null) {
            pageNo = 1;
        }
        return pageNo;
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            return 10;
        }
        if (pageSize > MAX_ES_QUERY_PAGE_SIZE) {
            pageSize = MAX_ES_QUERY_PAGE_SIZE;
        }
        return pageSize;
    }
}
