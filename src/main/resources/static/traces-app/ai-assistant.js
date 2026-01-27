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
        // 滚动到底部
        scrollToBottom();
    });
    
    // 关闭聊天面板
    closeBtn.addEventListener('click', function() {
        aiPanel.classList.remove('show');
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
                        botMessageElement.querySelector('p').textContent = botResponse;
                        
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
        msgContent.textContent = text;
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
});