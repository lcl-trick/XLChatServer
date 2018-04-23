# XLChatServer
XL聊天软件服务器端，实现消息收发和文件传输等功能 - 计算机网络课程设计

### 主要功能
1. 用户注册、登录、在线用户管理、用户的在线和隐身
2. 聊天功能：公聊和私聊
3. P2P文件传输功能
4. 服务器端下线用户
5. 查看配置文件、测试和初始化数据库

### 技术细节
1. 应用层使用TLS1.2，利用JSSE实现
2. 密码在数据库使用SHA-256加盐存储
3. 传输使用JSON数据格式
4. 使用基于PKCS12的密钥库
5. 使用Properties配置文件管理配置

### 许可证
- Copyright (c) 2017-2018, 小路.
- This program is free software; you can redistribute it and/or modify it under the terms and conditions of the GNU General Public License, version 3, as published by the Free Software Foundation.
- This program is distributed in the hope it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.