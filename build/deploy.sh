#!/bin/bash

# 应用部署脚本
# 功能：1. 解压部署包 2. 检查JDK环境 3. 运行应用

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

# 检查root权限警告
check_root_privilege() {
    if [[ $EUID -eq 0 ]]; then
        log_warning "检测到使用root权限运行，建议使用普通用户运行应用"
    fi
}

# 检查系统资源
check_system_resources() {
    local total_mem=$(free -m 2>/dev/null | awk '/^Mem:/{print $2}' || sysctl hw.memsize 2>/dev/null | awk '{print int($2/1024/1024)}')
    local available_mem=$(free -m 2>/dev/null | awk '/^Mem:/{print $7}' || vm_stat 2>/dev/null | grep "Pages free" | awk '{print int($3)*4/1024}')
    
    log_info "系统总内存: ${total_mem}MB"
    log_info "可用内存: ${available_mem}MB"
    
    if [[ $available_mem -lt 512 ]]; then
        log_warning "可用内存不足512MB，可能影响应用性能"
    fi
}

# 检查端口占用
check_port_availability() {
    local port=${1:-8080}
    if command -v netstat &> /dev/null; then
        if netstat -tlnp 2>/dev/null | grep -q ":$port "; then
            log_warning "端口 $port 已被占用"
            netstat -tlnp 2>/dev/null | grep ":$port " | log_info
        fi
    elif command -v lsof &> /dev/null; then
        if lsof -i :$port &> /dev/null; then
            log_warning "端口 $port 已被占用"
            lsof -i :$port | log_info
        fi
    fi
}

# 安装JDK 17函数
install_jdk17() {
    log_info "开始安装JDK 17..."
    
    # 检测操作系统类型
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Ubuntu/Debian
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y openjdk-17-jdk
        # CentOS/RHEL/Fedora
        elif command -v yum &> /dev/null; then
            sudo yum install -y java-17-openjdk-devel
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y java-17-openjdk-devel
        else
            log_error "不支持的Linux发行版，请手动安装JDK 17"
            return 1
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        if command -v brew &> /dev/null; then
            brew install openjdk@17
            sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
        else
            log_error "请先安装Homebrew，然后运行: brew install openjdk@17"
            return 1
        fi
    else
        log_error "不支持的操作系统: $OSTYPE"
        return 1
    fi
    
    log_success "JDK 17安装完成"
}

# 检查并安装JDK 17
check_and_install_jdk() {
    log_info "检查Java环境..."
    
    # 检查是否已安装Java
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        local major_version=$(echo $java_version | cut -d'.' -f1)
        
        log_info "检测到Java版本: $java_version"
        
        # 检查是否为JDK 17+
        if [[ $major_version -ge 17 ]]; then
            log_success "Java环境满足要求 (版本 >= 17)"
            return 0
        else
            log_warning "当前Java版本 ($java_version) 低于要求的版本 (17+)"
        fi
    else
        log_warning "未检测到Java环境"
    fi
    
    # 询问是否自动安装
    read -p "是否自动安装JDK 17? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        install_jdk17
        # 验证安装
        if command -v java &> /dev/null; then
            local new_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
            log_success "JDK安装验证成功，版本: $new_version"
        else
            log_error "JDK安装失败，请手动安装后重试"
            return 1
        fi
    else
        log_error "请手动安装JDK 17后重新运行此脚本"
        log_info "Ubuntu/Debian: sudo apt-get install openjdk-17-jdk"
        log_info "CentOS/RHEL: sudo yum install java-17-openjdk-devel"
        log_info "macOS: brew install openjdk@17"
        return 1
    fi
}

# 查找jar文件
find_jar_file() {
    local jar_file=$(find . -name "*.jar" -not -name "*sources*" -not -name "*javadoc*" | head -1)
    
    if [[ -z "$jar_file" ]]; then
        log_error "未找到jar包文件"
        return 1
    fi
    
    log_info "找到应用jar包: $jar_file"
    echo "$jar_file"
}

# 停止已运行的应用实例
stop_existing_instance() {
    local jar_name=$(basename "$1")
    local pid=$(ps aux | grep "$jar_name" | grep -v grep | awk '{print $2}')
    
    if [[ -n "$pid" ]]; then
        log_warning "发现正在运行的应用实例 (PID: $pid)，正在停止..."
        kill "$pid" 2>/dev/null || true
        sleep 3
        
        # 强制杀死如果还在运行
        if ps -p "$pid" > /dev/null 2>&1; then
            log_info "强制终止应用进程..."
            kill -9 "$pid" 2>/dev/null || true
        fi
        
        log_success "已停止现有应用实例"
    fi
}

# 启动应用
start_application() {
    local jar_file="$1"
    local app_name=$(basename "$jar_file" .jar)
    
    log_info "开始启动应用: $app_name"
    
    # 设置JVM参数
    local jvm_options="-Xms512m -Xmx1024m -server"
    local gc_options="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    local monitoring_options="-XX:+PrintGC -XX:+PrintGCDetails -Xloggc:logs/gc.log"
    
    # 创建日志目录
    mkdir -p logs
    
    # 启动应用
    nohup java $jvm_options $gc_options $monitoring_options -jar "$jar_file" > logs/application.log 2>&1 &
    local app_pid=$!
    
    # 等待应用启动
    log_info "等待应用启动..."
    sleep 10
    
    # 检查应用是否正常运行
    if ps -p "$app_pid" > /dev/null 2>&1; then
        log_success "应用启动成功 (PID: $app_pid)"
        echo "$app_pid" > "logs/app.pid"
        
        # 测试应用是否可访问
        sleep 5
        if command -v curl &> /dev/null; then
            if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
                log_success "应用健康检查通过"
            else
                log_warning "无法访问应用健康端点，应用可能仍在启动中"
            fi
        fi
        
        log_info "=================================================="
        log_info "应用部署完成！"
        log_info "应用名称: $app_name"
        log_info "进程ID: $app_pid"
        log_info "日志文件: logs/application.log"
        log_info "访问地址: http://localhost:8080"
        log_info "健康检查: http://localhost:8080/actuator/health"
        log_info "=================================================="
    else
        log_error "应用启动失败"
        if [[ -f "logs/application.log" ]]; then
            log_info "最后10行错误日志:"
            tail -10 "logs/application.log" | log_error
        fi
        return 1
    fi
}

# 主函数
main() {
    log_info "========== 开始应用部署 =========="
    
    check_root_privilege
    check_system_resources
    check_port_availability 8080
    
    if ! check_and_install_jdk; then
        exit 1
    fi
    
    local jar_file=$(find_jar_file)
    if [[ $? -ne 0 ]]; then
        exit 1
    fi
    
    stop_existing_instance "$jar_file"
    start_application "$jar_file"
    
    log_success "========== 应用部署完成 =========="
}

# 执行主函数
main "$@"