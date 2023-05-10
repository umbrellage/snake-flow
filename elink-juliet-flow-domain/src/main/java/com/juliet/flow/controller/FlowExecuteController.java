package com.juliet.flow.controller;

import com.juliet.api.development.domain.entity.SysUser;
import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.common.security.utils.SecurityUtils;
import com.juliet.flow.client.dto.FlowIdDTO;
import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.domain.dto.FieldDTO;
import com.juliet.flow.domain.dto.FlowOpenResultDTO;
import com.juliet.flow.domain.model.Field;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.service.FlowExecuteService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Api(tags = "流程管理")
@RequestMapping("/juliet/flow/execute")
@RestController
public class FlowExecuteController {

    @Autowired
    private FlowExecuteService flowExecuteService;

    /**
     * 流程还没创建，做预创建时的查询
     */
    @PostMapping("/open")
    public AjaxResult open(FlowOpenDTO dto) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long tenantId = sysUser.getTenantId();
        Long userId = sysUser.getUserId();
        Node node = flowExecuteService.queryStartNodeByCode(tenantId, dto.getCode());
        BusinessAssert.assertNotNull(node, StatusCode.SERVICE_ERROR, "can not find node by code:" + dto.getCode());
        BusinessAssert.assertTrue(node.isOperator(sysUser.getPostIds()),
                StatusCode.SERVICE_ERROR, "user:" + userId +" can not handle current node!");
        return AjaxResult.success(toFlowOpenResultDTO(node));
    }

    @PostMapping("/start")
    public AjaxResult start(FlowOpenDTO dto) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Long tenantId = sysUser.getTenantId();
        Long userId = sysUser.getUserId();





        return AjaxResult.success();
    }



    @PostMapping("/forward")
    public AjaxResult forward(@RequestParam("julietProcessId") String processId,
                              @RequestBody String body) {
        System.out.println(processId);
        System.out.println(body);
        return AjaxResult.success();
    }

    /**
     * 获取某个流程是否已经结束
     * @param dto
     * @return
     */
    @PostMapping("/is/end")
    public AjaxResult<Boolean> flowIsEnd(@RequestBody FlowIdDTO dto) {
        return AjaxResult.success(new Flow().isEnd());
    }

    private static FlowOpenResultDTO toFlowOpenResultDTO(Node node) {
        FlowOpenResultDTO dto = new FlowOpenResultDTO();
        if (node.getForm() != null && !CollectionUtils.isEmpty(node.getForm().getFields())) {
            dto.setAllowFields(node.getForm().getFields().stream().map(FlowExecuteController::toFieldDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private static FieldDTO toFieldDTO(Field field) {
        FieldDTO fieldDTO = new FieldDTO();
        fieldDTO.setName(field.getName());
        fieldDTO.setCode(field.getCode());
        return fieldDTO;
    }





}
