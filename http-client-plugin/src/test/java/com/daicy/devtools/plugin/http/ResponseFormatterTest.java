package com.daicy.devtools.plugin.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseFormatterTest {
    private ResponseFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new ResponseFormatter();
    }

    @Test
    void testFormatJson() {
        String input = "{\"name\":\"John\",\"age\":30}";
        String contentType = "application/json";
        String formatted = formatter.format(input, contentType);
        
        // 验证JSON格式化
        assertTrue(formatted.contains("\"name\""));
        assertTrue(formatted.contains("\"John\""));
        assertTrue(formatted.contains("\"age\""));
        assertTrue(formatted.contains("30"));
        assertTrue(formatted.contains("\n"));  // 应该包含换行符
    }

    @Test
    void testFormatXml() {
        String input = "<root><name>John</name><age>30</age></root>";
        String contentType = "application/xml";
        String formatted = formatter.format(input, contentType);
        
        // 验证XML格式化
        assertTrue(formatted.contains("<root>"));
        assertTrue(formatted.contains("  <name>"));  // 应该有缩进
        assertTrue(formatted.contains("John"));
        assertTrue(formatted.contains("  <age>"));   // 应该有缩进
        assertTrue(formatted.contains("30"));
        assertTrue(formatted.contains("</root>"));
    }

    @Test
    void testFormatHtml() {
        String input = "<html><body><div>Hello</div></body></html>";
        String contentType = "text/html";
        String formatted = formatter.format(input, contentType);
        
        // 验证HTML格式化
        assertTrue(formatted.contains("<html>"));
        assertTrue(formatted.contains("  <body>"));  // 应该有缩进
        assertTrue(formatted.contains("    <div>"));  // 应该有更多缩进
        assertTrue(formatted.contains("Hello"));
        assertTrue(formatted.contains("</html>"));
    }

    @Test
    void testFormatInvalidJson() {
        String input = "{invalid json}";
        String contentType = "application/json";
        String formatted = formatter.format(input, contentType);
        
        // 当格式化失败时，应该返回原始内容
        assertEquals(input, formatted);
    }

    @Test
    void testFormatInvalidXml() {
        String input = "<invalid>xml<";
        String contentType = "application/xml";
        String formatted = formatter.format(input, contentType);
        
        // 当格式化失败时，应该返回原始内容
        assertEquals(input, formatted);
    }

    @Test
    void testGetPreviewContent() {
        // 测试JSON预览
        String jsonInput = "{\"name\":\"John\"}";
        String jsonPreview = formatter.getPreviewContent(jsonInput, "application/json");
        assertTrue(jsonPreview.contains("<!DOCTYPE html>"));
        assertTrue(jsonPreview.contains("<pre class='json'>"));
        assertTrue(jsonPreview.contains("John"));

        // 测试XML预览
        String xmlInput = "<root><name>John</name></root>";
        String xmlPreview = formatter.getPreviewContent(xmlInput, "application/xml");
        assertTrue(xmlPreview.contains("<!DOCTYPE html>"));
        assertTrue(xmlPreview.contains("<pre class='xml'>"));
        assertTrue(xmlPreview.contains("John"));

        // 测试HTML预览
        String htmlInput = "<div>Hello</div>";
        String htmlPreview = formatter.getPreviewContent(htmlInput, "text/html");
        assertTrue(htmlPreview.contains("<!DOCTYPE html>"));
        assertTrue(htmlPreview.contains("Hello"));

        // 测试纯文本预览
        String textInput = "Hello World";
        String textPreview = formatter.getPreviewContent(textInput, "text/plain");
        assertTrue(textPreview.contains("<!DOCTYPE html>"));
        assertTrue(textPreview.contains("<pre>"));
        assertTrue(textPreview.contains("Hello World"));
    }

    @Test
    void testEscapeHtml() {
        String input = "<div class=\"test\">Hello & World</div>";
        String preview = formatter.getPreviewContent(input, "text/plain");
        
        // 验证HTML转义
        assertTrue(preview.contains("&lt;div"));
        assertTrue(preview.contains("&quot;test&quot;"));
        assertTrue(preview.contains("Hello &amp; World"));
        assertTrue(preview.contains("&lt;/div&gt;"));
    }
} 