package com.daicy.devtools.plugin.http;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class HttpClientPluginTest extends FxRobot {
    
    private HttpClientPlugin plugin;
    private MockWebServer mockWebServer;
    private Stage stage;

    @Start
    public void start(Stage stage) {
        this.stage = stage;
        plugin = new HttpClientPlugin();
        plugin.initialize();
        stage.setScene(new javafx.scene.Scene((Parent) plugin.getPluginNode()));
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void waitForFxEvents() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testPluginInitialization() {
        assertNotNull(plugin);
        assertEquals("HTTP Client", plugin.getName());
        assertNotNull(plugin.getPluginNode());
    }

    @Test
    public void testHeaderManagement() {
        // 等待JavaFX线程
        WaitForAsyncUtils.waitForFxEvents();
        
        // 获取Headers表格
        TableView<Header> headersTable = lookup("#headersTable").query();
        assertNotNull(headersTable);
        
        // 测试添加header
        clickOn("#addHeaderButton");
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(headersTable.getItems().isEmpty());
        Header header = headersTable.getItems().get(0);
        assertNotNull(header);
        
        // 设置header值
        header.setKey("Content-Type");
        header.setValue("application/json");
        
        assertEquals("Content-Type", header.getKey());
        assertEquals("application/json", header.getValue());
    }

    @Test
    public void testGetRequest() throws InterruptedException {
        // 设置mock响应
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"message\":\"success\"}")
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // 设置请求URL
        interact(() -> {
            String url = mockWebServer.url("/test").toString();
            TextField urlField = lookup("#urlField").query();
            urlField.setText(url);

            // 选择GET方法
            ComboBox<String> methodComboBox = lookup("#methodComboBox").query();
            methodComboBox.setValue("GET");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // 发送请求
        clickOn("#sendButton");
        
        // 等待响应
        WaitForAsyncUtils.sleep(5, TimeUnit.SECONDS);
        
        // 验证响应
        TextArea responseArea = lookup("#responseBodyArea").query();
        String responseText = responseArea.getText().replaceAll("\\s+", "");
        String expectedText = "{\"message\":\"success\"}".replaceAll("\\s+", "");
        assertEquals(expectedText, responseText, "Response text should match");

        // 验证发送的请求
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/test", recordedRequest.getPath());
    }

    @Test
    public void testPostRequest() throws InterruptedException {
        // 设置mock响应
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"id\":1}")
            .setResponseCode(201));

        // 设置请求URL和方法
        interact(() -> {
            String url = mockWebServer.url("/api/create").toString();
            TextField urlField = lookup("#urlField").query();
            urlField.setText(url);

            ComboBox<String> methodComboBox = lookup("#methodComboBox").query();
            methodComboBox.setValue("POST");

            // 设置请求体
            TextArea requestBody = lookup("#requestBodyArea").query();
            requestBody.setText("{\"name\":\"test\"}");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // 添加header
        clickOn("#addHeaderButton");
        WaitForAsyncUtils.waitForFxEvents();
        
        // 设置header
        interact(() -> {
            TableView<Header> headersTable = lookup("#headersTable").query();
            assertFalse(headersTable.getItems().isEmpty(), "Headers table should not be empty");
            
            Header header = headersTable.getItems().get(0);
            header.setKey("Content-Type");
            header.setValue("application/json");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // 发送请求
        clickOn("#sendButton");
        
        // 等待响应
        WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);
        
        // 验证请求
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/api/create", recordedRequest.getPath());
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
        assertEquals("{\"name\":\"test\"}", recordedRequest.getBody().readUtf8());
    }

    @Test
    public void testErrorHandling() {
        // 设置无效URL
        interact(() -> {
            TextField urlField = lookup("#urlField").query();
            urlField.setText("invalid-url");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // 发送请求
        clickOn("#sendButton");
        
        // 等待错误提示显示
        WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);
        
        // 验证错误提示
        Node alertNode = lookup(".dialog-pane").query();
        assertNotNull(alertNode, "Alert dialog should be present");
        assertTrue(alertNode.isVisible(), "Alert dialog should be visible");
        
        // 关闭错误提示
        press(KeyCode.ENTER).release(KeyCode.ENTER);
    }
}