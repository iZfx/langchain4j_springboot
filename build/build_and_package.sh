#!/bin/bash

# 项目构建和打包脚本
# 功能：1. Maven打包成jar包 2. 将脚本与jar包打成zip压缩包

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

# 检查是否在项目根目录
check_project_root() {
    if [[ ! -f "pom.xml" ]]; then
        log_error "未找到pom.xml文件，请确保在项目根目录下执行此脚本"
        exit 1
    fi
    log_info "检测到项目根目录: $(pwd)"
}

# 检查Maven是否安装
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "未检测到Maven，请先安装Maven"
        exit 1
    fi
    MAVEN_VERSION=$(mvn -v | grep "Apache Maven" | awk '{print $3}')
    log_info "检测到Maven版本: $MAVEN_VERSION"
}

# 清理和构建项目
build_project() {
    log_info "开始清理项目..."
    mvn clean
    
    log_info "开始编译和打包项目..."
    mvn package -DskipTests
    
    # 查找生成的jar包
    JAR_FILE=$(find target -name "*.jar" -not -name "*sources*" -not -name "*javadoc*" | head -1)
    
    if [[ -z "$JAR_FILE" ]]; then
        log_error "未找到生成的jar包文件"
        exit 1
    fi
    
    log_success "成功生成jar包: $JAR_FILE"
    echo "$JAR_FILE" > build/jar_file.txt
}

# 创建部署包
create_deployment_package() {
    local jar_file=$(cat build/jar_file.txt)
    local jar_name=$(basename "$jar_file")
    local package_name="langchain4j-springboot-deployment-$(date +%Y%m%d-%H%M%S).zip"
    
    log_info "创建部署包: $package_name"
    
    # 创建临时目录
    local temp_dir="build/temp_deploy"
    mkdir -p "$temp_dir"
    
    # 复制jar包
    cp "$jar_file" "$temp_dir/"
    
    # 复制部署脚本
    cp build/deploy.sh "$temp_dir/"
    
    # 复制应用配置文件（如果存在）
    if [[ -f "src/main/resources/application.properties" ]]; then
        cp src/main/resources/application.properties "$temp_dir/"
    fi
    
    # 复制README或部署说明（如果存在）
    if [[ -f "README.md" ]]; then
        cp README.md "$temp_dir/"
    fi
    
    # 创建部署说明文件
    cat > "$temp_dir/DEPLOYMENT_INSTRUCTIONS.txt" << EOF
部署说明文档
=============

1. 解压此压缩包到目标部署目录
2. 确保系统已安装JDK 17或更高版本
3. 执行 deploy.sh 脚本进行部署
4. 应用默认端口: 8080
5. 访问地址: http://localhost:8080

注意事项:
- 部署前请检查端口占用情况
- 建议在生产环境中配置JVM参数
- 日志文件将输出到控制台和nohup.out文件
EOF
    
    # 创建zip包
    cd "$temp_dir"
    zip -r "../$package_name" ./*
    cd ../..
    
    # 清理临时目录
    rm -rf "$temp_dir"
    
    log_success "部署包创建完成: build/$package_name"
    echo "$package_name" > build/package_name.txt
}

# 主函数
main() {
    log_info "========== 开始执行项目构建和打包 =========="
    
    check_project_root
    check_maven
    build_project
    create_deployment_package
    
    log_success "========== 构建和打包完成 =========="
    log_info "生成的部署包位置: build/$(cat build/package_name.txt)"
    log_info "可以将此部署包分发到目标服务器进行部署"
}

# 执行主函数
main "$@"