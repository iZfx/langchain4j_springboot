# LangChain4J SpringBoot 示例项目

## 项目结构说明

此项目采用了前后端分离的架构设计：

### 后端 (Spring Boot)
- 核心应用: [Langchain4jSpringbootApplication.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/Langchain4jSpringbootApplication.java)
- 配置: [config/AiConfig.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/config/AiConfig.java)
- 控制器: 
  - [controller/SpringAiController.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/controller/SpringAiController.java)
  - [controller/FunctionCallController.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/controller/FunctionCallController.java)
  - [controller/ChartController.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/controller/ChartController.java)
  - 新增: [controller/FrontendController.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/controller/FrontendController.java) - 前端页面路由
  - 新增: [controller/TracesApiController.java](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/java/com/example/langchain4j_springboot/controller/TracesApiController.java) - 运动轨迹相关API

### 前端 (苹果风格的"行跡 - 运动热力图"App官网)
- 静态资源目录: `src/main/resources/static/`
- 主页面: [src/main/resources/static/index.html](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/resources/static/index.html)
- App相关资源: `src/main/resources/static/traces-app/`
  - 样式: [src/main/resources/static/traces-app/styles.css](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/resources/static/traces-app/styles.css)
  - 脚本: [src/main/resources/static/traces-app/script.js](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/resources/static/traces-app/script.js)
  - 工具页面: [src/main/resources/static/traces-app/generate-images.html](file:///Users/izfx/IdeaProjects/langchain4j_springboot/src/main/resources/static/traces-app/generate-images.html)

## 设计特点

1. 苹果风格的简约设计
2. 响应式布局，适配各种设备
3. 平滑滚动和交互动画
4. 渐变色彩和毛玻璃效果
5. 直观的功能展示

## 如何使用

1. 将所有文件放在同一目录下
2. 在浏览器中打开 [index.html](./index.html) 查看网站
3. 如果需要真实的App截图，可以用 [generate-images.html](./generate-images.html) 生成模拟截图，然后替换网站中的占位图

## 功能模块

- 自动轨迹记录
- 热力图展示
- 数据分析
- 足迹地图

## 技术栈

- HTML5
- CSS3 (包含Flexbox和Grid布局)
- JavaScript (ES6+)

## 响应式特性

网站完全响应式，在桌面端、平板和手机上都有良好的显示效果。

## 浏览器兼容性

支持现代浏览器（Chrome、Firefox、Safari、Edge等）。