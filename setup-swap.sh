#!/bin/bash

# ============================================
# Swap 空间配置脚本
# 为 2GB 内存服务器增加 2GB Swap 空间
# ============================================

echo "======================================"
echo "配置 Swap 空间"
echo "======================================"

# 检查是否已存在 swap
if [ -f /swapfile ]; then
    echo "Swap 文件已存在"
    read -p "是否删除并重新创建？(y/n): " confirm
    if [ "$confirm" = "y" ]; then
        echo "删除现有 swap..."
        sudo swapoff /swapfile
        sudo rm -f /swapfile
    else
        echo "取消操作"
        exit 0
    fi
fi

# 创建 2GB Swap 文件
echo "创建 2GB Swap 文件..."
sudo fallocate -l 2G /swapfile

if [ $? -ne 0 ]; then
    echo "fallocate 失败，尝试使用 dd 方法..."
    sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
fi

# 设置权限
echo "设置 Swap 文件权限..."
sudo chmod 600 /swapfile

# 创建 Swap
echo "创建 Swap 空间..."
sudo mkswap /swapfile

# 启用 Swap
echo "启用 Swap..."
sudo swapon /swapfile

# 验证
echo "验证 Swap 状态:"
sudo swapon --show

# 设置开机自动挂载
echo "配置开机自动启用 Swap..."
if ! grep -q "/swapfile" /etc/fstab; then
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
else
    echo "Swap 已配置在 fstab 中"
fi

# 调整 Swap 使用策略
echo "调整系统 Swap 使用策略..."
# vm.swappiness=10 表示只在内存使用率达到 90% 时才使用 Swap
echo "vm.swappiness=10" | sudo tee -a /etc/sysctl.conf

echo ""
echo "======================================"
echo "Swap 配置完成!"
echo "======================================"
echo ""
echo "Swap 信息:"
free -h
echo ""
echo "系统内存和 Swap 使用情况:"
sudo swapon --show
echo ""
