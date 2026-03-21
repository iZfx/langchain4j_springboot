# Ollama 本地大模型部署指南

## 📋 项目说明

本项目已将原有的通义千问 API 切换为本地 Ollama 大模型，适用于阿里云 2vCPU/2GB 配置的服务器。

## 🎯 使用的模型

- **Embedding 模型**: `nomic-embed-text` (~500MB 内存)
- **Chat 模型**: `qwen2:0.5b` (~400MB 内存)

## 🚀 快速部署步骤

### 步骤 1: 配置 Swap 空间 (可选但推荐)

对于 2GB 内存的服务器，建议先配置 Swap 空间以防止内存不足:

```bash
# 上传 setup-swap.sh 到服务器
chmod +x setup-swap.sh
sudo ./setup-swap.sh
```

### 步骤 2: 安装 Ollama 和模型

```bash
# 上传 deploy-ollama.sh 到服务器
chmod +x deploy-ollama.sh
sudo ./deploy-ollama.sh
```

脚本会自动完成:
1. 安装 Ollama
2. 配置服务参数 (内存限制、超时等)
3. 下载 `nomic-embed-text` 和 `qwen2:0.5b` 模型
4. 验证安装

### 步骤 3: 验证安装

```bash
# 上传 test-ollama.sh 到服务器
chmod +x test-ollama.sh
./test-ollama.sh
```

### 步骤 4: 部署 Spring Boot 应用

```bash
# 打包项目
mvn clean package

# 上传 jar 包到服务器
# 运行应用
java -jar langchain4j_springboot-0.0.1-SNAPSHOT.jar
```

## 📝 已修改的配置文件

### 1. pom.xml
- ✅ 启用了 `spring-ai-starter-model-ollama` 依赖
- ✅ 移除了 `qianfan-core` 依赖

### 2. application.properties
- ✅ 注释了通义千问 API 配置
- ✅ 添加了 Ollama 本地模型配置:
  ```properties
  spring.ai.ollama.server-url=http://localhost:11434
  spring.ai.ollama.chat.options.model=qwen2:0.5b
  spring.ai.ollama.embedding.options.model=nomic-embed-text
  spring.ai.ollama.chat.options.keep-alive=60
  ```

### 3. AiConfig.java
- ✅ 修改 `assistant` Bean 使用 Ollama 模型
- ✅ 修改 `assistantUnique` Bean 使用 Ollama 模型
- ✅ 添加了 Embedding 模型的适配器

## 🔧 常用运维命令

### Ollama 服务管理

```bash
# 查看服务状态
sudo systemctl status ollama

# 启动服务
sudo systemctl start ollama

# 重启服务
sudo systemctl restart ollama

# 停止服务
sudo systemctl stop ollama

# 查看日志
sudo journalctl -u ollama -f
```

### 模型管理

```bash
# 查看已安装的模型
ollama list

# 查看模型详细信息
ollama show qwen2:0.5b

# 删除模型
ollama rm qwen2:0.5b

# 重新下载模型
ollama pull qwen2:0.5b
```

### 内存管理

```bash
# 查看内存使用
free -h

# 查看 Swap 使用
sudo swapon --show

# 手动卸载模型 (释放内存)
ollama stop qwen2:0.5b
```

## ⚙️ 性能优化配置

### Ollama 服务优化

已配置在 `/etc/systemd/system/ollama.service.d/override.conf`:

```ini
[Service]
# 限制同时加载的模型数量
Environment="OLLAMA_MAX_LOADED_MODELS=1"

# 模型空闲保持时间 (秒)
Environment="OLLAMA_KEEP_ALIVE=60"

# 限制并行请求数
Environment="OLLAMA_NUM_PARALLEL=1"
```

### 系统优化

已在 `/etc/sysctl.conf` 中添加:
```
vm.swappiness=10
```
这表示只在内存使用率达到 90% 时才使用 Swap，提高性能。

## 📊 预期性能指标

基于 2vCPU/2GB 配置:

| 指标 | 预期值 |
|------|--------|
| 内存占用 | 800MB - 1.2GB |
| 响应时间 | 2-5 秒/回答 |
| 并发能力 | 1-2 个请求 |
| 启动时间 | 30-60 秒 |

## 🔍 故障排查

### 问题 1: Ollama 服务无法启动

```bash
# 检查端口占用
sudo lsof -i :11434

# 查看错误日志
sudo journalctl -u ollama -n 50
```

### 问题 2: 内存不足

```bash
# 查看内存使用
free -h

# 如果内存不足，可以:
# 1. 增加 Swap 空间
# 2. 停止其他服务
# 3. 使用更小的模型
```

### 问题 3: Spring Boot 应用启动失败

检查日志:
```bash
tail -f nohup.out
```

常见错误:
- Ollama 服务未启动
- 模型未下载完成
- 端口冲突

## 🆙 模型升级建议

如果后续需要更好的效果，可以考虑:

1. **升级到 4GB 内存服务器**
   - 可以使用更大的模型如 `qwen2:1.5b` 或 `tinyllama:1.1b`

2. **使用 GPU 加速**
   - 如果有 GPU，可以显著提升推理速度

3. **混合方案**
   - Embedding 本地化
   - Chat 使用免费 API (如百度、智谱等)

## 📞 技术支持

如有问题，请检查:
1. Ollama 服务状态
2. 模型是否正确下载
3. Spring Boot 配置文件
4. 服务器内存和网络状况

## 📚 参考资源

- [Ollama 官方文档](https://ollama.com/)
- [Qwen2 模型介绍](https://qwenlm.github.io/)
- [LangChain4j 文档](https://docs.langchain4j.dev/)

---

**最后更新**: 2026-03-21
**适用版本**: Spring Boot 4.0.1 + Ollama + LangChain4j
