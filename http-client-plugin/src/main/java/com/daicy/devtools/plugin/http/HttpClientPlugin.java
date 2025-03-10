package com.daicy.devtools.plugin.http;

import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.daicy.devtools.plugin.Plugin;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class HttpClientPlugin implements Plugin {
    private VBox root;
    private ComboBox<String> methodComboBox;
    private TextField urlField;
    private TabPane requestTabPane;
    private TabPane responseTabPane;
    private TextArea requestBodyArea;
    private TextArea responseBodyArea;
    private WebView responsePreview;
    private TableView<Header> headersTable;
    private ResponseFormatter responseFormatter;

    @Override
    public void initialize() {
        responseFormatter = new ResponseFormatter();
        root = new VBox(10);
        root.setPadding(new Insets(10));

        // 请求方法和URL区域
        HBox urlBox = createUrlBox();

        // 请求区域
        requestTabPane = new TabPane();
        Tab headersTab = new Tab("Headers", createHeadersTable());
        Tab bodyTab = new Tab("Body", createRequestBody());
        requestTabPane.getTabs().addAll(headersTab, bodyTab);
        
        // 发送按钮
        Button sendButton = new Button("发送请求");
        sendButton.setOnAction(e -> sendRequest());
        sendButton.setId("sendButton");

        // 响应区域
        responseTabPane = new TabPane();
        responseBodyArea = new TextArea();
        responseBodyArea.setEditable(false);
        responsePreview = new WebView();

        // 添加响应信息区域
        TextArea responseInfoArea = new TextArea();
        responseInfoArea.setEditable(false);
        responseInfoArea.setId("responseInfoArea");
        responseInfoArea.setPrefRowCount(5);

        Tab responseInfoTab = new Tab("Response Info", responseInfoArea);
        Tab responseBodyTab = new Tab("Response Body", responseBodyArea);
        Tab previewTab = new Tab("Preview", responsePreview);
        responseTabPane.getTabs().addAll(responseInfoTab, responseBodyTab, previewTab);

        // 布局
        SplitPane splitPane = new SplitPane(requestTabPane, responseTabPane);
        splitPane.setDividerPositions(0.5);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        root.getChildren().addAll(urlBox, splitPane, sendButton);

        urlField.setId("urlField");
        methodComboBox.setId("methodComboBox");
        requestBodyArea.setId("requestBodyArea");
        responseBodyArea.setId("responseBodyArea");
        headersTable.setId("headersTable");
    }

    private HBox createUrlBox() {
        HBox urlBox = new HBox(10);
        methodComboBox = new ComboBox<>();
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodComboBox.setValue("GET");

        urlField = new TextField();
        urlField.setPromptText("Enter URL");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        urlBox.getChildren().addAll(methodComboBox, urlField);
        return urlBox;
    }

    private Node createHeadersTable() {
        headersTable = new TableView<>();
        headersTable.setEditable(true);  // 设置表格为可编辑
        
        // Key列
        TableColumn<Header, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        keyColumn.setCellFactory(col -> {
            EditableCell<Header> cell = new EditableCell<>();
            cell.setOnCommit(event -> {
                Header header = event.getRowValue();
                header.setKey(event.getNewValue());
            });
            return cell;
        });
        keyColumn.setPrefWidth(150);
        keyColumn.setEditable(true);  // 设置列为可编辑
        
        // Value列
        TableColumn<Header, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        valueColumn.setCellFactory(col -> {
            EditableCell<Header> cell = new EditableCell<>();
            cell.setOnCommit(event -> {
                Header header = event.getRowValue();
                header.setValue(event.getNewValue());
            });
            return cell;
        });
        valueColumn.setPrefWidth(250);
        valueColumn.setEditable(true);  // 设置列为可编辑
        
        headersTable.getColumns().addAll(keyColumn, valueColumn);
        
        // 按钮区域
        HBox buttonBox = new HBox(10);
        Button addHeaderButton = new Button("Add Header");
        Button removeHeaderButton = new Button("Remove Header");
        Button commonHeadersButton = new Button("Common Headers");
        
        addHeaderButton.setId("addHeaderButton");
        removeHeaderButton.setId("removeHeaderButton");
        commonHeadersButton.setId("commonHeadersButton");
        
        // 添加新的header
        addHeaderButton.setOnAction(e -> {
            Header newHeader = new Header();
            headersTable.getItems().add(newHeader);
            // 自动开始编辑新行的Key列
            headersTable.getSelectionModel().select(newHeader);
            headersTable.edit(headersTable.getItems().size() - 1, keyColumn);
        });
        
        // 删除选中的header
        removeHeaderButton.setOnAction(e -> {
            Header selectedHeader = headersTable.getSelectionModel().getSelectedItem();
            if (selectedHeader != null) {
                headersTable.getItems().remove(selectedHeader);
            }
        });
        
        // 添加常用headers的菜单
        ContextMenu commonHeadersMenu = new ContextMenu();
        MenuItem contentTypeJson = new MenuItem("Content-Type: application/json");
        MenuItem contentTypeForm = new MenuItem("Content-Type: application/x-www-form-urlencoded");
        MenuItem accept = new MenuItem("Accept: application/json");
        MenuItem authorization = new MenuItem("Authorization");
        
        contentTypeJson.setOnAction(e -> addCommonHeader("Content-Type", "application/json"));
        contentTypeForm.setOnAction(e -> addCommonHeader("Content-Type", "application/x-www-form-urlencoded"));
        accept.setOnAction(e -> addCommonHeader("Accept", "application/json"));
        authorization.setOnAction(e -> addCommonHeader("Authorization", "Bearer "));
        
        commonHeadersMenu.getItems().addAll(contentTypeJson, contentTypeForm, accept, authorization);
        commonHeadersButton.setOnAction(e -> commonHeadersMenu.show(commonHeadersButton, Side.BOTTOM, 0, 0));
        
        buttonBox.getChildren().addAll(addHeaderButton, removeHeaderButton, commonHeadersButton);
        
        VBox headerBox = new VBox(10, buttonBox, headersTable);
        VBox.setVgrow(headersTable, Priority.ALWAYS);
        
        return headerBox;
    }

    private void addCommonHeader(String key, String value) {
        // 检查是否已存在相同的key
        boolean exists = false;
        for (Header header : headersTable.getItems()) {
            if (key.equals(header.getKey())) {
                header.setValue(value);
                exists = true;
                break;
            }
        }
        
        // 如果不存在，添加新的header
        if (!exists) {
            headersTable.getItems().add(new Header(key, value));
        }
    }

    private Node createRequestBody() {
        requestBodyArea = new TextArea();
        requestBodyArea.setPromptText("Enter request body");
        return requestBodyArea;
    }

    private void sendRequest() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = urlField.getText();
            String method = methodComboBox.getValue();
            
            HttpRequestBase request = createRequest(method, url);
            
            // 添加headers
            for (Header header : headersTable.getItems()) {
                if (header.getKey() != null && !header.getKey().isEmpty()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
            
            // 添加body
            if (request instanceof HttpEntityEnclosingRequestBase && !requestBodyArea.getText().isEmpty()) {
                ((HttpEntityEnclosingRequestBase) request).setEntity(
                    new StringEntity(requestBodyArea.getText(), StandardCharsets.UTF_8)
                );
            }
            
            // 记录请求开始时间
            long startTime = System.currentTimeMillis();
            
            // 发送请求
            try (CloseableHttpResponse response = client.execute(request)) {
                // 记录请求结束时间
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                // 获取响应信息
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                String contentType = response.getFirstHeader("Content-Type") != null 
                    ? response.getFirstHeader("Content-Type").getValue().toLowerCase() 
                    : "";
                
                // 构建请求信息
                StringBuilder requestInfo = new StringBuilder();
                requestInfo.append("=== Request Details ===\n");
                requestInfo.append("URL: ").append(url).append("\n");
                requestInfo.append("Method: ").append(method).append("\n");
                requestInfo.append("Request Headers:\n");
                for (Header header : headersTable.getItems()) {
                    if (header.getKey() != null && !header.getKey().isEmpty()) {
                        requestInfo.append("  ").append(header.getKey()).append(": ").append(header.getValue()).append("\n");
                    }
                }
                if (request instanceof HttpEntityEnclosingRequestBase && !requestBodyArea.getText().isEmpty()) {
                    requestInfo.append("Request Body:\n").append(requestBodyArea.getText()).append("\n");
                }
                
                // 构建响应信息
                StringBuilder responseInfo = new StringBuilder();
                responseInfo.append("\n=== Response Details ===\n");
                responseInfo.append("Timestamp: ").append(java.time.LocalDateTime.now()).append("\n");
                responseInfo.append("Status: ").append(response.getStatusLine().toString()).append("\n");
                responseInfo.append("Time: ").append(duration).append("ms\n");
                responseInfo.append("Response Size: ").append(responseBody.length()).append(" bytes\n");
                responseInfo.append("Response Headers:\n");
                for (org.apache.http.Header header : response.getAllHeaders()) {
                    responseInfo.append("  ").append(header.getName()).append(": ").append(header.getValue()).append("\n");
                }
                
                // 更新响应信息区域
                TextArea responseInfoArea = (TextArea) ((Tab) responseTabPane.getTabs().get(0)).getContent();
                responseInfoArea.setStyle("-fx-font-family: 'monospace';");  // 使用等宽字体
                responseInfoArea.setText(requestInfo.toString() + responseInfo.toString());
                
                // 格式化响应体
                String formattedResponse = responseFormatter.format(responseBody, contentType);
                responseBodyArea.setStyle("-fx-font-family: 'monospace';");  // 使用等宽字体
                responseBodyArea.setText(formattedResponse);
                
                // 更新预览
                String previewContent = responseFormatter.getPreviewContent(formattedResponse, contentType);
                responsePreview.getEngine().loadContent(previewContent, "text/html");  // 移除charset参数
                
                // 自动切换到响应信息标签
                responseTabPane.getSelectionModel().select(0);
            }
            
        } catch (Exception e) {
            showError("请求失败", e.getMessage());
        }
    }

    private HttpRequestBase createRequest(String method, String url) {
        switch (method) {
            case "GET":
                return new HttpGet(url);
            case "POST":
                return new HttpPost(url);
            case "PUT":
                return new HttpPut(url);
            case "DELETE":
                return new HttpDelete(url);
            case "PATCH":
                return new HttpPatch(url);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public String getName() {
        return "HTTP Client";
    }

    @Override
    public String getDescription() {
        return "A Postman-like HTTP client for testing APIs";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getAuthor() {
        return "DevTools Team";
    }

    @Override
    public Node getPluginNode() {
        return root;
    }

    @Override
    public void setContent(String content) {
        requestBodyArea.setText(content);
    }

    @Override
    public String getContent() {
        return requestBodyArea.getText();
    }
} 