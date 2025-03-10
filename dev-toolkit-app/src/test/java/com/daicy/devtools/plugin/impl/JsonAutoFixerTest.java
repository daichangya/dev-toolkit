//package com.daicy.devtools.plugin.impl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class JsonAutoFixerTest {
//    private JsonAutoFixer jsonAutoFixer;
//
//    @BeforeEach
//    void setUp() {
//        jsonAutoFixer = new JsonAutoFixer();
//    }
//
//    @Test
//    void testNullOrEmptyInput() {
//        assertNull(jsonAutoFixer.fix(null));
//        assertEquals("", jsonAutoFixer.fix(""));
//        assertEquals("  ", jsonAutoFixer.fix("  "));
//    }
//
//    @Test
//    void testFixBasicFormat() {
//        String input = "{\"name\":\"John\"}";  // 修复字符串格式
//        String expected = "{\"name\":\"John\"}";  // 保持一致
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixEmptyValues() {
//        String input = "{\"name\":\"\",\"age\":,\"skills\":[]}";
//        String expected = "{\"name\":\"\",\"age\":null,\"skills\":[]}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixUnclosedQuotes() {
//        String input = "{\"name\":\"John}";
//        String expected = "{\"name\":\"John\"}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixCommas() {
//        String input = "{\"names\":[\"John\",\"Jane\",],\"age\":30,}";
//        String expected = "{\"names\":[\"John\",\"Jane\"],\"age\":30}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixUnclosedBrackets() {
//        String input = "{\"data\":{\"array\":[1,2,3";
//        String expected = "{\"data\":{\"array\":[1,2,3]}}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixBooleanAndNull() {
//        String input = "{\"active\":TRUE,\"deleted\":False,\"data\":NULL,\"status\":undefined}";
//        String expected = "{\"active\":true,\"deleted\":false,\"data\":null,\"status\":null}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixNonStandardFormat() {
//        String input = "{'name':'John',age:30,status:'active'}";
//        String expected = "{\"name\":\"John\",\"age\":\"30\",\"status\":\"active\"}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testComplexJson() {
//        String input = "{'user':{name:'John',age:30,hobbies:['reading','gaming',],address:{city:'New York',}}}";
//        String expected = "{\"user\":{\"name\":\"John\",\"age\":\"30\",\"hobbies\":[\"reading\",\"gaming\"],\"address\":{\"city\":\"New York\"}}}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertTrue(jsonAutoFixer.hasFixed());
//    }
//
//    @Test
//    void testFixEscapedJson() {
//        String input = "{\"insured_num\":\"\",\"policy_code\":\"370162998794008\",\"policy_type\":\"\"}";
//        String expected = "{\"insured_num\":\"\",\"policy_code\":\"370162998794008\",\"policy_type\":\"\"}";
//        assertEquals(expected, jsonAutoFixer.fix(input));
//        assertFalse(jsonAutoFixer.hasFixed());
//    }
//}