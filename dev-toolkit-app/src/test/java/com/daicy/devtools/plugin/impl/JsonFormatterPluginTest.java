//package com.daicy.devtools.plugin.impl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class JsonFormatterPluginTest {
//    private JsonFormatterPlugin plugin;
//
//    @BeforeEach
//    void setUp() {
//        plugin = new JsonFormatterPlugin();
//    }
//
//    @Test
//    void testGetName() {
//        assertEquals("JSON格式化", plugin.getName());
//    }
//
//    @Test
//    void testGetDescription() {
//        assertEquals("JSON字符串的格式化和压缩工具", plugin.getDescription());
//    }
//
//    @Test
//    void testFormatJson() {
//        String unformattedJson = "{\"name\":\"test\",\"age\":25}";
//        String expectedJson = "{\n  \"name\" : \"test\",\n  \"age\" : 25\n}";
//
//        String formattedJson = plugin.formatJson(unformattedJson);
//        assertEquals(expectedJson, formattedJson);
//    }
//
//    @Test
//    void testFormatInvalidJson() {
//        String invalidJson = "{\"name\":\"test\"";
//        assertThrows(RuntimeException.class, () -> plugin.formatJson(invalidJson));
//    }
//}