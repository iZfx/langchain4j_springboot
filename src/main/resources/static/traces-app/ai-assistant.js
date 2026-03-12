// AI助手功能
document.addEventListener('DOMContentLoaded', function() {
    const aiFloat = document.getElementById('aiAssistantFloat');
    const aiPanel = document.getElementById('aiAssistantPanel');
    const closeBtn = document.getElementById('closeBtn');
    const chatMessages = document.getElementById('chatMessages');
    const userInput = document.getElementById('userInput');
    const sendBtn = document.getElementById('sendBtn');
    
    // 显示聊天面板
    aiFloat.addEventListener('click', function() {
        aiPanel.classList.add('show');
        // 隐藏悬浮按钮
        aiFloat.style.display = 'none';
        // 滚动到底部
        scrollToBottom();
    });
    
    // 关闭聊天面板
    closeBtn.addEventListener('click', function() {
        aiPanel.classList.remove('show');
        // 显示悬浮按钮
        aiFloat.style.display = 'flex';
    });
    
    // 发送消息
    function sendMessage() {
        const message = userInput.value.trim();
        if (!message) return;
        
        // 显示用户消息
        addMessage(message, 'user');
        userInput.value = '';
        
        // 显示加载指示器
        const loadingMsg = addMessage('思考中...', 'bot', true);
        
        // 确定协议，避免HTTPS/HTTP混淆
        const protocol = window.location.protocol;
        const baseUrl = `${protocol}//${window.location.host}`;
        
        // 调用后端API
        fetch(`${baseUrl}/ai/memory_stream_chat?message=${encodeURIComponent(message)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.status);
                }
                
                // 处理流式响应
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let botResponse = '';
                
                // 移除加载指示器
                chatMessages.removeChild(loadingMsg);
                
                // 创建机器人回复消息容器
                const botMessageElement = addMessage('', 'bot', true);
                
                // 逐字显示回复
                function readStream() {
                    reader.read().then(({done, value}) => {
                        if (done) {
                            return;
                        }
                        
                        const chunk = decoder.decode(value, {stream: true});
                        botResponse += chunk;
                        // 格式化显示回复
                        botMessageElement.querySelector('p').innerHTML = formatMessage(botResponse);
                        
                        scrollToBottom();
                        
                        readStream();
                    }).catch(error => {
                        console.error('Error reading stream:', error);
                        botMessageElement.querySelector('p').textContent = '抱歉，出现了错误：' + error.message;
                    });
                }
                
                readStream();
            })
            .catch(error => {
                console.error('Error:', error);
                // 移除加载指示器
                chatMessages.removeChild(loadingMsg);
                addMessage('抱歉，连接出现问题：' + error.message, 'bot');
            });
    }
    
    // 添加消息到聊天窗口
    function addMessage(text, sender, isLoading = false) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', `${sender}-message`);
        
        const msgContent = document.createElement('p');
        if (isLoading) {
            msgContent.textContent = text;
        } else {
            // 格式化显示消息
            msgContent.innerHTML = formatMessage(text);
        }
        messageDiv.appendChild(msgContent);
        
        chatMessages.appendChild(messageDiv);
        scrollToBottom();
        
        return messageDiv;
    }
    
    // 滚动到底部
    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // 发送按钮事件
    sendBtn.addEventListener('click', sendMessage);
    
    // 回车发送消息
    userInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    // 格式化消息：使用 Markdown 解析
    function formatMessage(text) {
        if (!text) return '';
        
        // 预处理文本，确保 Markdown 格式正确
        let processedText = text
            // 确保粗体前后有适当的空格
            .replace(/([\u4e00-\u9fa5])\*\*/g, '$1 **')
            .replace(/\*\*([\u4e00-\u9fa5])/g, '** $1')
            // 处理列表项（中文破折号转 Markdown 列表）
            .replace(/^\s*[·•●◆■](.*)$/gm, '-$1');
        
        // 使用 marked.js 进行 Markdown 解析
        // 如果 marked 未加载，则降级处理
        if (typeof marked !== 'undefined') {
            try {
                // 配置 marked 选项
                const html = marked.parse(processedText, {
                    breaks: true, // 将换行符转换为<br>
                    gfm: true, // 启用 GitHub 风格 Markdown
                    headerIds: false, // 不生成标题 ID
                    mangle: false, // 不转义 HTML
                    pedantic: false // 严格遵循 Markdown 规范
                });
                return html;
            } catch (e) {
                console.error('Markdown 解析失败:', e);
                // 降级到简单格式化
                return simpleFormat(text);
            }
        } else {
            // 降级到简单格式化
            return simpleFormat(text);
        }
    }
    
    // 简单格式化（降级方案）
    function simpleFormat(text) {
        let formatted = text
            // 转义 HTML 特殊字符，防止 XSS 攻击
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            // 处理链接 [文本](URL)
            .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
            // 处理粗体 **文本**
            .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
            // 处理斜体 *文本*
            .replace(/\*([^*]+)\*/g, '<em>$1</em>')
            // 处理换行符
            .replace(/\n/g, '<br>');
        
        return formatted;
    }
});