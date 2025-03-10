package com.daicy.devtools.plugin.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Base64PluginTest {
    private Base64Plugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new Base64Plugin();
    }

    @Test
    void testGetName() {
        assertEquals("Base64转换", plugin.getName());
    }

    @Test
    void testGetDescription() {
        assertEquals("文本内容的Base64编码和解码工具", plugin.getDescription());
    }

    @Test
    void testEncodeBase64() {
        String input = "Hello, World!";
        String expected = "SGVsbG8sIFdvcmxkIQ==";
        
        String encoded = plugin.encodeBase64(input);
        assertEquals(expected, encoded);
    }

    @Test
    void testDecodeBase64() {
        String input = "SGVsbG8sIFdvcmxkIQ==";
        String expected = "Hello, World!";
        
        String decoded = plugin.decodeBase64(input);
        assertEquals(expected, decoded);
    }

    @Test
    void testDecodeInvalidBase64() {
        String invalidInput = "Invalid Base64!@#";
        assertThrows(RuntimeException.class, () -> plugin.decodeBase64(invalidInput));
    }
}