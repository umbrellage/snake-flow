
-- 新增节点激活时间，认领时间，完成时间
alter table jbpm_flow_node
add column active_time DATETIME null comment '记录节点激活时间',
add column claim_time DATETIME null comment '记录节点认领时间',
add column finish_time DATETIME null comment '记录节点完成时间';
