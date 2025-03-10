# Markdown编辑器插件

这是一个用于DevTools的Markdown编辑器插件，提供了Markdown文本编辑和实时预览功能。

## 功能特性

- Markdown文本编辑
- 实时HTML预览
- 支持导出HTML

## 构建说明

1. 确保已安装Maven和JDK 11或更高版本
2. 在项目根目录执行：`mvn clean package`
3. 构建完成后，插件jar包将自动复制到主项目的plugins目录下

## 使用方法

1. 将生成的jar包放置在DevTools的plugins目录下
2. 启动DevTools主程序
3. 插件将自动加载并显示在工具列表中

## 依赖说明

- JavaFX 17.0.1
- Flexmark 0.64.0
- DevTools Plugin API

## 开发说明

本插件使用了以下技术：

- Java 11
- JavaFX用于GUI开发
- Flexmark用于Markdown解析
- Maven用于项目管理和构建