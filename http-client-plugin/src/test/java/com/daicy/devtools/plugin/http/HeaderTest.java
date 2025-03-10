package com.daicy.devtools.plugin.http;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HeaderTest {

    @Test
    public void testHeaderConstructor() {
        Header header = new Header("Content-Type", "application/json");
        assertEquals("Content-Type", header.getKey());
        assertEquals("application/json", header.getValue());
    }

    @Test
    public void testEmptyConstructor() {
        Header header = new Header();
        assertEquals("", header.getKey());
        assertEquals("", header.getValue());
    }

    @Test
    public void testSettersAndGetters() {
        Header header = new Header();
        
        header.setKey("Accept");
        assertEquals("Accept", header.getKey());
        
        header.setValue("text/html");
        assertEquals("text/html", header.getValue());
    }

    @Test
    public void testProperties() {
        Header header = new Header();
        
        // 测试属性绑定
        header.keyProperty().set("Authorization");
        assertEquals("Authorization", header.getKey());
        
        header.valueProperty().set("Bearer token123");
        assertEquals("Bearer token123", header.getValue());
    }
} 