# GitHub文档自动更新功能说明

## 功能概述

本项目现已支持从GitHub自动获取文本内容并进行向量化处理，同时提供了定时任务功能，在每天的0点、4点、8点、12点、16点、20点自动更新文档内容。

## 主要特性

### 1. 自动文档获取
- 支持从GitHub原始文件URL获取文本内容
- 使用标准HTTP客户端实现，无需额外依赖
- 自动处理网络异常和超时情况

### 2. 智能向量化处理
- 继承原有的文档分割和向量化逻辑
- 支持多种来源标识（启动时、定时任务、手动触发）
- 详细的日志记录便于监控

### 3. 定时任务调度
- 每天6个时间点自动执行：0点、4点、8点、12点、16点、20点
- 使用Spring的@Scheduled注解实现
- 可通过API手动触发更新

## 配置说明

### application.properties 配置项

```properties
# GitHub文档配置
# 请替换为实际的GitHub原始文件URL
github.document.url=https://github.com/iZfx/KnowledgeBase/blob/main/SportHeatMap.txt

# 定时任务日志级别配置
logging.level.com.example.langchain4j_springboot.service.DocumentService=INFO
```

### 如何获取GitHub原始文件URL

1. 在GitHub上找到目标文件
2. 点击"Raw"按钮
3. 复制浏览器地址栏中的URL
4. 将URL配置到`github.document.url`属性中

## API接口

### 1. 手动触发文档更新
```
POST /api/document/update
```

### 2. 获取当前配置
```
GET /api/document/config
```

### 3. 更新GitHub文档URL
```
PUT /api/document/config/url?url={新的GitHub文件URL}
```

### 4. 测试连接（待完善）
```
POST /api/document/test-connection?url={要测试的URL}
```

## 使用示例

### 启动时自动处理
应用启动时会自动执行一次文档获取和向量化处理。

### 定时任务执行
系统会在以下时间点自动执行文档更新：
- 00:00 (午夜)
- 04:00 
- 08:00
- 12:00 (中午)
- 16:00
- 20:00 (晚上)

### 手动触发更新
```bash
curl -X POST http://localhost:8080/api/document/update
```

### 修改GitHub文档源
```bash
curl -X PUT "http://localhost:8080/api/document/config/url?url=https://raw.githubusercontent.com/new-user/new-repo/main/new-file.txt"
```

## 日志输出示例

```
[INFO] [startup] 开始从GitHub获取文档内容: https://raw.githubusercontent.com/user/repo/main/doc.txt
[INFO] [startup] 开始时间: 2026-03-01 14:30:25
[INFO] [startup] 成功获取文档，内容长度: 1250 字符
[INFO] [startup] 文档分割完成，共 15 个片段
[INFO] [startup] 向量化处理完成！结束时间: 2026-03-01 14:30:28, 总耗时: 3150ms
```

## 注意事项

1. **网络连接**：确保服务器能够访问GitHub
2. **URL格式**：必须使用GitHub原始文件URL（以`raw.githubusercontent.com`开头）
3. **文件大小**：建议文档大小适中，避免内存溢出
4. **权限控制**：如果GitHub仓库是私有的，需要相应的认证机制
5. **错误处理**：网络异常时会记录错误日志但不会中断应用运行

## 故障排除

### 常见问题

1. **文档获取失败**
   - 检查GitHub URL是否正确
   - 确认网络连接正常
   - 查看日志中的具体错误信息

2. **向量化处理慢**
   - 检查文档大小是否过大
   - 确认AI服务配置正确
   - 调整文档分割参数

3. **定时任务未执行**
   - 确认`SchedulingConfig`类存在且正确配置
   - 检查系统时间是否正确
   - 查看应用日志确认调度器是否启动

## 扩展建议

1. **增加认证支持**：为私有仓库添加token认证
2. **多源支持**：支持同时从多个GitHub仓库获取文档
3. **增量更新**：只更新发生变化的部分内容
4. **缓存机制**：添加本地缓存减少重复下载
5. **监控告警**：添加失败通知机制

## 相关文件

- `GithubDocumentService.java` - 核心服务类
- `SchedulingConfig.java` - 定时任务配置
- `DocumentController.java` - API控制器
- `Langchain4jSpringbootApplication.java` - 主应用类（包含定时任务）

这个功能使得RAG系统的知识库能够保持最新状态，特别适合需要定期更新文档内容的应用场景。