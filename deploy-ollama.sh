#!/bin/bash

# ============================================
# Ollama 本地大模型部署脚本
# 适用于阿里云 2vCPU/2GB 服务器
# 本来推荐nomic-embed-text 和 qwen2:0.5b，mac上实测问答效果不好，实际使用看情况修改对应模型名称
# ============================================

echo "======================================"
echo "开始安装 Ollama 和本地大模型"
echo "======================================"

# 1. 检查系统
echo "[1/5] 检查系统..."
if [ ! -f /etc/os-release ]; then
    echo "错误：无法识别操作系统"
    exit 1
fi

source /etc/os-release
echo "检测到操作系统：$PRETTY_NAME"

# 2. 安装 Ollama
echo "[2/5] 安装 Ollama..."
if command -v ollama &> /dev/null; then
    echo "Ollama 已安装，跳过安装步骤"
else
    echo "正在安装 Ollama..."
#    curl -fsSL https://ollama.com/install.sh | sh
# 这是一个专门为国内用户优化的加速镜像站，提供了包装好的安装脚本。
    curl -fsSL https://cnb.cool/hex/ollama/-/git/raw/main/install.sh | sh

    
    if [ $? -ne 0 ]; then
        echo "错误：Ollama 安装失败"
        exit 1
    fi
fi

# 3. 配置 Ollama 服务
echo "[3/5] 配置 Ollama 服务..."

# 创建 systemd 服务配置
sudo mkdir -p /etc/systemd/system/ollama.service.d/
sudo tee /etc/systemd/system/ollama.service.d/override.conf > /dev/null <<EOF
[Service]
# 限制内存使用 (2GB 服务器)
Environment="OLLAMA_MAX_LOADED_MODELS=1"
# 模型保持时间 (60 秒)
Environment="OLLAMA_KEEP_ALIVE=60"
# 限制 VRAM 使用
Environment="OLLAMA_NUM_PARALLEL=1"
EOF

# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动 Ollama 服务
sudo systemctl enable ollama
sudo systemctl start ollama

# 检查服务状态
sleep 3
sudo systemctl status ollama --no-pager

# 4. 下载模型
echo "[4/5] 下载 AI 模型..."

# 下载 Embedding 模型
echo "正在下载 Embedding 模型：qwen3-embedding:0.6b..."
ollama pull qwen3-embedding:0.6b

if [ $? -ne 0 ]; then
    echo "错误：Embedding 模型下载失败"
    exit 1
fi

# 下载 Chat 模型 (选择最小的适合 2GB 内存的模型)
echo "正在下载 Chat 模型：deepseek-r1:1.5b..."
ollama pull deepseek-r1:1.5b

if [ $? -ne 0 ]; then
    echo "错误：Chat 模型下载失败"
    exit 1
fi

# 5. 验证安装
echo "[5/5] 验证安装..."
echo "已安装的模型列表:"
ollama list

# 测试 Embedding 模型
echo "测试 Embedding 模型..."
echo "你好" | ollama run qwen3-embedding:0.6b

# 测试 Chat 模型
echo "测试 Chat 模型..."
echo "你好，请简单介绍一下你自己" | ollama run deepseek-r1:1.5b

echo ""
echo "======================================"
echo "安装完成!"
echo "======================================"
echo ""
echo "模型信息:"
echo "  - Embedding: qwen3-embedding:0.6b (~639MB)"
echo "  - Chat: deepseek-r1:1.5b (~1.1GB)"
echo ""
echo "Ollama 服务地址：http://localhost:11434"
echo ""
echo "常用命令:"
echo "  - 查看模型列表：ollama list"
echo "  - 查看服务状态：sudo systemctl status ollama"
echo "  - 重启服务：sudo systemctl restart ollama"
echo "  - 停止服务：sudo systemctl stop ollama"
echo ""
echo "下一步:"
echo "  1. 修改项目的 application.properties 配置"
echo "  2. 运行 Spring Boot 应用"
echo "  3. 测试 AI 助手功能"
echo ""
