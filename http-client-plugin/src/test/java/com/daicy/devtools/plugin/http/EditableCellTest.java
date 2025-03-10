package com.daicy.devtools.plugin.http;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class EditableCellTest {

    private TableView<Header> table;
    private EditableCell<Header> cell;
    private TableColumn<Header, String> column;

    @Start
    public void start(Stage stage) {
        table = new TableView<>();
        column = new TableColumn<>("Test");
        cell = new EditableCell<>();
        
        // 设置单元格工厂
        column.setCellFactory(col -> cell);
        table.getColumns().add(column);
        
        // 添加测试数据
        table.getItems().add(new Header("test", "value"));
        
        // 显示表格
        stage.setScene(new javafx.scene.Scene(table));
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testStartEdit() {
        Platform.runLater(() -> {
            cell.updateItem("test", false);
            cell.startEdit();
            
            assertNotNull(cell.getGraphic());
            assertNull(cell.getText());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testCancelEdit() {
        Platform.runLater(() -> {
            cell.updateItem("test", false);
            cell.startEdit();
            cell.cancelEdit();
            
            assertNull(cell.getGraphic());
            assertEquals("test", cell.getText());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testUpdateItem() {
        Platform.runLater(() -> {
            cell.updateItem("test", false);
            assertEquals("test", cell.getText());
            assertNull(cell.getGraphic());

            cell.updateItem(null, true);
            assertNull(cell.getText());
            assertNull(cell.getGraphic());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
} 