package com.daicy.devtools.plugin.http;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public class EditableCell<T> extends TableCell<T, String> {
    private TextField textField;
    private EventHandler<CellEditEvent<T>> onCommit;

    public EditableCell() {
        textField = new TextField();
        
        // 当文本字段失去焦点时提交更改
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEdit(textField.getText());
            }
        });
        
        // 当按下回车键时提交更改
        textField.setOnAction(event -> commitEdit(textField.getText()));
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        textField.setText(getString());
        setGraphic(textField);
        textField.selectAll();
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                setText(null);
                textField.setText(getString());
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }

    public void setOnCommit(EventHandler<CellEditEvent<T>> value) {
        onCommit = value;
    }

    @Override
    public void commitEdit(String newValue) {
        if (!isEditing()) return;
        
        super.commitEdit(newValue);
        setText(newValue);
        setGraphic(null);
        
        if (onCommit != null) {
            CellEditEvent<T> event = new CellEditEvent<>(
                EditableCell.this,
                getTableView(),
                getTableRow().getItem(),
                newValue
            );
            onCommit.handle(event);
        }
    }

    public static class CellEditEvent<T> extends Event {
        private static final EventType<CellEditEvent> EDIT_EVENT_TYPE = new EventType<>("EDIT");
        
        private final EditableCell<T> cell;
        private final T rowValue;
        private final String newValue;
        private final javafx.scene.control.TableView<T> tableView;

        public CellEditEvent(EditableCell<T> cell, javafx.scene.control.TableView<T> tableView, T rowValue, String newValue) {
            super(EDIT_EVENT_TYPE);
            this.cell = cell;
            this.tableView = tableView;
            this.rowValue = rowValue;
            this.newValue = newValue;
        }

        public T getRowValue() {
            return rowValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public javafx.scene.control.TableView<T> getTableView() {
            return tableView;
        }
    }
} 