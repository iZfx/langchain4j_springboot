#!/bin/bash

# --- 配置区 ---
APP_NAME="xingji"
APP_DIR="/home/deploy/app"
PROJECT_DIR="$APP_DIR/langchain4j_springboot" # 你的项目根目录名
JAR_NAME="target/langchain4j_springboot-0.0.1-SNAPSHOT.jar"
APP_PORT=8080
LOG_FILE="$APP_DIR/app.log"
# ---------------

echo "======= 开始部署 $APP_NAME ======="
# 直接进入项目根目录
cd $PROJECT_DIR || { echo "错误：无法进入项目目录 $PROJECT_DIR"; exit 1; }

# 1. 拉取最新代码
echo "拉取最新代码..."
git pull origin main

# 2. 使用 Maven 打包 (请确保服务器已安装 Maven)
echo "开始 Maven 打包..."
mvn clean package -DskipTests

# 3. 查找并停止旧进程
echo "停止旧进程..."
PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    kill -9 $PID
    echo "进程 $PID 已停止。"
else
    echo "未找到运行中的进程。"
fi

# 4. 启动新应用
echo "启动新应用..."
nohup java -jar $JAR_NAME --server.port=$APP_PORT > $LOG_FILE 2>&1 &

# 5. 简单检查启动状态
sleep 5
if ps -p $! > /dev/null; then
    echo "新应用启动成功，PID: $!"
else
    echo "应用启动失败，请检查日志: $LOG_FILE"
    exit 1
fi

echo "======= 部署完成 ======="
