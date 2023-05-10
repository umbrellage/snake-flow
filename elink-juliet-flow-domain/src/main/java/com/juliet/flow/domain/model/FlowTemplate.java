package com.juliet.flow.domain.model;

import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.domain.entity.FlowTemplateEntity;
import java.util.Date;
import java.util.Optional;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowTemplate extends BaseModel {

    private Long id;

    private String name;

    private String code;

    private Node node;

    private FlowTemplateStatusEnum status;

    public Flow initTouYangFlow() {
        Flow flow = new Flow();
        Node start = buildStartNode();
        Node designer = buildDesignerNode();
        Node sampleDesigner = buildSampleDesignerNode();
        Node end = buildEndNode();
        start.setNext(Arrays.asList(designer));
        designer.setNext(Arrays.asList(sampleDesigner));
        sampleDesigner.setNext(Arrays.asList(end));
        this.node = start;
        flow.setNode(node);
        return flow;
    }

    private Node buildStartNode() {
        // start节点
        Node nodeStart = new Node();
        nodeStart.setType(NodeTypeEnum.START);
        return nodeStart;
    }

    private Node buildEndNode() {
        // start节点
        Node nodeStart = new Node();
        nodeStart.setType(NodeTypeEnum.END);
        return nodeStart;
    }

    private Node buildSampleDesignerNode() {
        Node node = new Node();
        node.setType(NodeTypeEnum.HANDLE);

        List<Post> posts = Lists.newArrayList();
        Post post = new Post();
        post.setPostId("sampleDesigner");
        post.setPostName("版师");
        posts.add(post);
        node.setBindPosts(posts);

        List<Field> fields = new ArrayList<>();
        fields.add(Field.builder()
                .code("name")
                .name("类型")
                .build());
        Form form = new Form();
        form.setFields(fields);
        form.setPath("/abc/efg/aa");
        form.setCode("12345");
        node.setForm(form);
        return node;
    }


    private Node buildDesignerNode() {
        Node designer = new Node();
        designer.setType(NodeTypeEnum.HANDLE);

        List<Post> posts = Lists.newArrayList();
        Post post = new Post();
        post.setPostId("designer");
        post.setPostName("设计师");
        posts.add(post);
        designer.setBindPosts(posts);

        List<Field> fields = new ArrayList<>();
        fields.add(Field.builder()
                .code("type")
                .name("类型")
                .build());
        Form formDesigner = new Form();
        formDesigner.setFields(fields);
        formDesigner.setPath("/abc/efg");
        formDesigner.setCode("123");
        designer.setForm(formDesigner);
        return designer;
    }
}
