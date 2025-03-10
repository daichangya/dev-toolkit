#!/bin/bash

# 设置脚本使用UTF-8编码
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

# 设置错误时退出
set -e

# 显示执行的命令
set -x

# 设置应用信息
APP_NAME="DevTools"
APP_VERSION="1.0.0"

# 保存当前目录
INITIAL_DIR=$(pwd)

# 检查并创建插件目录
PLUGINS_DIR="dev-toolkit-app/plugins"
if [ ! -d "$PLUGINS_DIR" ]; then
    echo "创建插件目录..."
    mkdir -p "$PLUGINS_DIR"
fi

# 清理之前的构建
echo "清理之前的构建..."
mvn clean -DskipTests

# 编译并打包
echo "编译并打包项目..."
mvn package -DskipTests

# 创建安装包
echo "生成安装包..."
cd app || exit

# 检测操作系统类型
OS_TYPE=$(uname)

if [ "$OS_TYPE" = "Darwin" ]; then
    # macOS
    echo "正在为 macOS 创建安装包..."
    mvn jpackage:jpackage -DskipTests \
        -Djpackage.mac.sign=false \
        -Djpackage.mac.bundle-name="$APP_NAME" \
        -Djpackage.mac.package-name="$APP_NAME" \
        -Djpackage.app-version="$APP_VERSION"
else
    # Linux 或其他系统
    echo "正在为 $OS_TYPE 创建安装包..."
    mvn jpackage:jpackage -DskipTests \
        -Djpackage.app-version="$APP_VERSION" \
        -Djpackage.name="$APP_NAME"
fi

# 返回初始目录
cd "$INITIAL_DIR" || exit

echo "构建完成！"
echo "安装包位置: app/target/dist/"