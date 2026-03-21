#!/bin/bash

# ============================================
# Ollama 模型测试脚本
# ============================================

echo "======================================"
echo "Ollama 模型测试"
echo "======================================"

# 检查 Ollama 服务
echo "[1/3] 检查 Ollama 服务状态..."
if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "✓ Ollama 服务运行正常"
else
    echo "✗ Ollama 服务未运行"
    echo "请执行：sudo systemctl start ollama"
    exit 1
fi

# 显示已安装的模型
echo ""
echo "[2/3] 已安装的模型:"
ollama list

# 测试 Embedding 模型
echo ""
echo "[3/3] 测试 Embedding 模型..."
echo "发送测试文本到 nomic-embed-text..."
result=$(echo "测试文本向量化" | ollama run nomic-embed-text 2>&1)

if [ $? -eq 0 ]; then
    echo "✓ Embedding 模型测试通过"
else
    echo "✗ Embedding 模型测试失败"
    echo "错误信息：$result"
fi

# 测试 Chat 模型
echo ""
echo "测试 Chat 模型..."
echo "发送测试问题到 qwen2:0.5b..."
result=$(echo "你好，请用一句话介绍你自己" | ollama run qwen2:0.5b 2>&1)

if [ $? -eq 0 ]; then
    echo "✓ Chat 模型测试通过"
    echo "回答示例：${result:0:100}..."
else
    echo "✗ Chat 模型测试失败"
    echo "错误信息：$result"
fi

echo ""
echo "======================================"
echo "测试完成!"
echo "======================================"
