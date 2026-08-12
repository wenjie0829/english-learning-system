# 快速启动指南

## 环境准备

确保已安装以下软件：
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

## 1. 数据库设置

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE english_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 导入数据库结构
mysql -u root -p english_learning < database-schema.sql
```

## 2. 后端启动

```bash
# 进入后端目录
cd backend

# 修改配置文件
# 编辑 src/main/resources/application.yml
# 修改数据库密码和JWT密钥

# 编译项目
mvn clean install

# 启动后端
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

## 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 http://localhost:8000 启动

## 4. 访问系统

1. 打开浏览器访问 http://localhost:8000
2. 点击"注册"创建新账户
3. 登录后开始使用

## 测试账户

系统初始化时会创建一个管理员账户：
- 用户名：admin
- 密码：admin123（需要在数据库中用BCrypt加密）

## 功能测试

### 基本功能测试
1. 注册新用户
2. 登录系统
3. 查看学习统计
4. 搜索单词功能

### 学习功能测试
1. 点击"开始学习"
2. 测试单词发音
3. 标记单词掌握程度
4. 查看错词本

### 复习功能测试
1. 等待复习时间到达
2. 点击"复习单词"
3. 完成复习测试

## 常见问题

### 端口冲突
如果8080或8000端口被占用，可以修改配置：
- 后端：修改 `application.yml` 中的 `server.port`
- 前端：修改 `vite.config.js` 中的 `server.port`

### 数据库连接失败
检查：
1. MySQL服务是否启动
2. 用户名密码是否正确
3. 数据库是否创建成功

### 前端无法连接后端
检查：
1. 后端是否正常启动
2. 浏览器控制台是否有CORS错误
3. API代理配置是否正确

## 下一步

1. 导入你的PDF单词书
2. 配置AI接口（可选）
3. 添加管理员后台
4. 优化音频发音功能

## 技术支持

如有问题，请查看：
- README.md - 详细文档
- 代码注释 - 实现细节
- 控制台日志 - 错误信息
