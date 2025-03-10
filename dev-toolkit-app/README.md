# 开发工具集

一个基于JavaFX的可扩展开发工具集，提供多种实用的开发工具插件。

## 功能特性

- 插件化架构：支持动态加载和管理多个工具插件
- 统一的用户界面：所有插件共享相同的UI风格和操作方式
- 文件操作支持：支持打开、保存和另存为等基本文件操作
- 快捷键支持：提供常用操作的快捷键

## 内置插件

- Base64编解码器：支持文本的Base64编码和解码
- Hosts文件编辑器：提供hosts文件的编辑功能
- Markdown编辑器：支持Markdown文件的编辑和预览（插件）

## 构建和运行

### 环境要求

- JDK 11或更高版本
- Maven 3.6或更高版本

### 构建步骤

1. 克隆项目到本地
2. 在项目根目录执行Maven命令进行构建：
   ```bash
   mvn clean package
   ```
3. 构建完成后，可以在`app/target`目录下找到可执行的jar文件

### 运行应用

```bash
 java -jar app/target/app-1.0-SNAPSHOT.jar
```

## 插件开发指南

### 创建新插件

1. 创建一个新的Maven模块
2. 在`pom.xml`中添加必要的依赖
3. 实现`Plugin`接口：
   ```java
   public class YourPlugin implements Plugin {
       @Override
       public String getName() {
           return "插件名称";
       }

       @Override
       public Image getIcon() {
           return new Image(getClass().getResourceAsStream("/icons/your-icon.png"));
       }

       @Override
       public Node getPluginNode() {
           // 返回插件的主界面节点
       }

       @Override
       public String getContent() {
           // 返回当前编辑内容
       }

       @Override
       public void setContent(String content) {
           // 设置编辑内容
       }
   }
   ```

### 注册插件

在`MainApp`类的`initializePlugins`方法中注册新插件：

```java
pluginManager.registerPlugin(new YourPlugin());
```

## 项目结构

- `app/`: 主应用模块
  - `src/main/java/com/devtools/`: 核心代码
  - `src/main/resources/`: 资源文件
- `markdown-plugin/`: Markdown编辑器插件模块

## 贡献

欢迎提交Issue和Pull Request来帮助改进项目。

## 许可证

本项目采用MIT许可证。