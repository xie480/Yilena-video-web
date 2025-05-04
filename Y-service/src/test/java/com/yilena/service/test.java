package com.yilena.service;

import com.alibaba.fastjson.JSON;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
@RequiredArgsConstructor
public class test {

    private final SnowFlake snowFlake;

    @Test
    public void test() {
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        System.out.println(tags);
        String tagsJson = JSON.toJSONString(tags);
        System.out.println(tagsJson);
    }

    @Test
    public void getID(){
        //这里直接调用该方法 获取雪花算法生成ID
        long id = snowFlake.getID();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("ID",id);
        map.put("Binary",Long.toBinaryString(id));
        map.put("BinaryLength",Long.toBinaryString(id).length());
        System.out.println(map);
    }
}
