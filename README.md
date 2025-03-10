# DevTools 开发工具集

一个基于JavaFX的可扩展开发工具集，提供多种实用的开发工具插件。项目采用模块化设计，支持插件扩展。

## 项目结构

项目包含三个主要模块：

- **dev-toolkit-app**: 主应用程序模块
  - 核心框架实现
  - 内置插件集合
  - 插件管理系统
  - 用户界面实现

- **dev-toolkit-api**: 插件开发API模块
  - 插件接口定义
  - 通用工具类
  - 插件开发SDK

- **markdown-plugin**: Markdown编辑器插件示例
  - Markdown编辑和预览功能
  - 实时HTML预览
  - HTML导出功能

## 功能特性

- 插件化架构：支持动态加载和管理多个工具插件
- 统一的用户界面：所有插件共享相同的UI风格和操作方式
- 内置多个实用插件：
  - Base64编解码器
  - Hosts文件编辑器
  - JSON格式化工具
  - 文字图标生成器
  - Markdown编辑器
- 插件使用统计：记录和展示插件使用情况
- 主题切换：支持明暗主题切换
- 文件操作：支持打开、保存和另存为等基本文件操作

## 环境要求

- JDK 11或更高版本
- Maven 3.6或更高版本
- JavaFX运行环境

## 构建和运行

1. 克隆项目到本地：
```bash
git clone <repository-url>
```

2. 进入项目根目录：
```bash
cd dev-toolkit
```

3. 构建项目：
```bash
mvn clean package
```

4. 运行应用：
```bash
java -jar dev-toolkit-app/target/dev-toolkit-app-1.0-SNAPSHOT.jar
```

## 插件开发

1. 创建新的Maven模块
2. 添加dev-toolkit-api依赖
3. 实现Plugin接口
4. 打包并放置到plugins目录

示例插件实现：

```java
public class YourPlugin implements Plugin {
    @Override
    public String getName() {
        return "插件名称";
    }

    @Override
    public Node getPluginNode() {
        // 返回插件的主界面节点
        return new VBox();
    }

    // 实现其他必要的接口方法...
}
```

## 项目配置

- 插件配置：`config/plugin-config.json`
- 主题设置：`config/theme.json`
- 插件使用统计：`config/plugin-usage.json`

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交改动
4. 推送到分支
5. 创建Pull Request

## 许可证

本项目采用 MIT 许可证