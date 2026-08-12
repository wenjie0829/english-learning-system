# 智能英语学习系统

基于Vue3 + Spring Boot的智能英语学习系统，集单词学习、复习和口语学习于一体。

## 功能特性

### 用户功能
- 用户注册登录
- 单词学习与查询
- 单词发音和例句查看
- 单词收藏
- 错词记录
- 个性化复习计划（艾宾浩斯遗忘曲线）
- 学习数据统计
- AI辅助单词解释
- PDF单词书导入

### 管理员功能
- 用户管理
- 单词库管理
- 单词书管理
- 学习数据统计分析
- 系统统计概览

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- MySQL
- Maven
- Apache PDFBox（PDF处理）
- Spring WebFlux（AI接口调用）

### 前端
- Vue 3
- Vue Router
- Pinia
- Element Plus
- Axios
- Vite

## 项目结构

```
english-learning-system/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/englishlearning/app/
│   │   │   │       ├── config/        # 配置类
│   │   │   │       │   └── SecurityConfig.java
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       │   ├── AuthController.java
│   │   │   │       │   ├── WordController.java
│   │   │   │       │   ├── LearningController.java
│   │   │   │       │   ├── WordBookController.java
│   │   │   │       │   ├── ImportController.java
│   │   │   │       │   ├── AIController.java
│   │   │   │       │   └── AdminController.java
│   │   │   │       ├── dto/          # 数据传输对象
│   │   │   │       │   ├── LoginRequest.java
│   │   │   │       │   ├── RegisterRequest.java
│   │   │   │       │   └── JwtResponse.java
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       │   ├── User.java
│   │   │   │       │   ├── Word.java
│   │   │   │       │   ├── ExampleSentence.java
│   │   │   │       │   ├── LearningRecord.java
│   │   │   │       │   ├── Favorite.java
│   │   │   │       │   ├── WrongWord.java
│   │   │   │       │   ├── WordBook.java
│   │   │   │       │   ├── WordBookWord.java
│   │   │   │       │   └── LearningDailyStatistics.java
│   │   │   │       ├── repository/   # 数据访问层
│   │   │   │       │   ├── UserRepository.java
│   │   │   │       │   ├── WordRepository.java
│   │   │   │       │   ├── LearningRecordRepository.java
│   │   │   │       │   ├── FavoriteRepository.java
│   │   │   │       │   ├── WrongWordRepository.java
│   │   │   │       │   ├── WordBookRepository.java
│   │   │   │       │   ├── WordBookWordRepository.java
│   │   │   │       │   └── ExampleSentenceRepository.java
│   │   │   │       ├── security/     # 安全配置
│   │   │   │       │   ├── JwtTokenProvider.java
│   │   │   │       │   ├── UserPrincipal.java
│   │   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │   │       │   ├── CustomUserDetailsService.java
│   │   │   │       │   └── SecurityConfig.java
│   │   │   │       ├── service/      # 业务逻辑层
│   │   │   │       │   ├── AuthService.java
│   │   │   │       │   ├── WordService.java
│   │   │   │       │   ├── LearningService.java
│   │   │   │       │   ├── WordBookService.java
│   │   │   │       │   ├── PDFImportService.java
│   │   │   │       │   └── AIService.java
│   │   │   │       ├── util/         # 工具类
│   │   │   │       │   └── EbbinghausUtil.java
│   │   │   │       └── EnglishLearningApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml   # 配置文件
│   │   └── test/
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API接口
│   │   │   ├── auth.js
│   │   │   ├── word.js
│   │   │   ├── learning.js
│   │   │   ├── import.js
│   │   │   ├── ai.js
│   │   │   └── admin.js
│   │   ├── components/        # 组件
│   │   ├── router/            # 路由配置
│   │   │   └── index.js
│   │   ├── store/             # 状态管理
│   │   │   └── user.js
│   │   ├── utils/             # 工具函数
│   │   │   └── request.js
│   │   ├── views/             # 页面组件
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Home.vue
│   │   │   ├── Learn.vue
│   │   │   ├── Review.vue
│   │   │   ├── Search.vue
│   │   │   ├── Favorites.vue
│   │   │   ├── WrongWords.vue
│   │   │   ├── Statistics.vue
│   │   │   ├── Import.vue
│   │   │   └── Admin.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── database-schema.sql        # 数据库脚本
├── README.md                 # 项目文档
├── QUICKSTART.md             # 快速启动指南
└── PROJECT_SUMMARY.md        # 项目总结
```

## 安装说明

### 前置要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE english_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本：
```bash
mysql -u root -p english_learning < database-schema.sql
```

### 后端配置

1. 修改数据库连接配置：
编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/english_learning?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的MySQL密码
```

2. 修改JWT密钥：
```yaml
jwt:
  secret: your-secret-key-change-this-in-production-environment-at-least-256-bits
```

3. （可选）配置AI接口：
```yaml
ai:
  api-key: your-openai-api-key
  api-url: https://api.openai.com/v1/chat/completions
  model: gpt-3.5-turbo
```

4. 启动后端：
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

### 前端配置

1. 安装依赖：
```bash
cd frontend
npm install
```

2. 启动前端：
```bash
npm run dev
```

前端将在 `http://localhost:8000` 启动

## 使用说明

### 首次使用

1. 访问 `http://localhost:8000`
2. 点击"注册"创建账户
3. 登录后进入主页

### 学习流程

1. **开始学习**：点击"开始学习"按钮，系统会显示单词卡片
2. **学习单词**：查看单词、音标、释义和例句
3. **标记掌握程度**：
   - 点击"认识"：标记为已掌握，进入下一阶段
   - 点击"不认识"：添加到错词本，重新学习
4. **复习单词**：根据艾宾浩斯遗忘曲线进行复习
5. **查看统计**：在统计页面查看学习进度

### PDF单词书导入

1. 点击主页的"导入单词书"
2. 填写单词书名称和分类
3. 上传PDF文件
4. 系统自动解析并创建单词书
5. 导入完成后可以开始学习

### AI功能使用

1. 在单词学习页面，点击"生成AI释义"按钮
2. 系统会调用AI接口生成详细的单词释义
3. 需要在配置文件中设置有效的AI API密钥

### 管理员功能

1. 在数据库中将用户角色修改为ADMIN
2. 登录后主页会显示"管理后台"按钮
3. 进入管理后台可以进行：
   - 用户管理（修改角色、删除用户）
   - 单词管理（查看、删除单词）
   - 单词书管理（查看、删除单词书）
   - 查看系统统计数据

## 艾宾浩斯复习算法

系统根据艾宾浩斯遗忘曲线自动安排复习时间：
- 5分钟后
- 30分钟后
- 12小时后
- 1天后
- 2天后
- 4天后
- 7天后
- 15天后

复习算法特性：
- 答对：进入下一阶段，延长复习间隔
- 答错：重置到第一阶段，添加到错词本
- 达到最高阶段：标记为已掌握

## API接口文档

### 认证接口
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/register` - 用户注册
- GET `/api/auth/me` - 获取当前用户信息

### 单词接口
- GET `/api/words` - 获取所有单词
- GET `/api/words/{id}` - 获取单词详情
- GET `/api/words/search` - 搜索单词
- GET `/api/words/{id}/examples` - 获取例句
- POST `/api/words` - 创建单词
- PUT `/api/words/{id}` - 更新单词
- DELETE `/api/words/{id}` - 删除单词

### 学习接口
- GET `/api/learning/records/{userId}` - 获取学习记录
- GET `/api/learning/due/{userId}` - 获取待复习单词
- POST `/api/learning/start` - 开始学习
- POST `/api/learning/review` - 复习单词
- POST `/api/learning/favorites` - 添加收藏
- DELETE `/api/learning/favorites` - 取消收藏
- GET `/api/learning/favorites/{userId}` - 获取收藏列表
- GET `/api/learning/wrong-words/{userId}` - 获取错词列表
- POST `/api/learning/wrong-words/resolve` - 标记错词已掌握
- GET `/api/learning/statistics/{userId}` - 获取学习统计

### 单词书接口
- GET `/api/wordbooks` - 获取所有单词书
- GET `/api/wordbooks/{id}` - 获取单词书详情
- POST `/api/wordbooks` - 创建单词书
- PUT `/api/wordbooks/{id}` - 更新单词书
- DELETE `/api/wordbooks/{id}` - 删除单词书
- GET `/api/wordbooks/{id}/words` - 获取单词书中的单词
- POST `/api/wordbooks/{id}/words` - 添加单词到单词书
- DELETE `/api/wordbooks/{id}/words/{wordId}` - 从单词书移除单词

### 导入接口
- POST `/api/import/pdf` - 导入PDF单词书

### AI接口
- GET `/api/ai/definition` - 生成单词释义
- GET `/api/ai/examples` - 生成单词例句
- GET `/api/ai/pronunciation` - 生成发音指导

### 管理员接口
- GET `/api/admin/users` - 获取所有用户
- GET `/api/admin/users/{id}` - 获取用户详情
- PUT `/api/admin/users/{id}/role` - 修改用户角色
- DELETE `/api/admin/users/{id}` - 删除用户
- GET `/api/admin/words` - 获取所有单词（管理员）
- DELETE `/api/admin/words/{id}` - 删除单词（管理员）
- GET `/api/admin/wordbooks` - 获取所有单词书（管理员）
- DELETE `/api/admin/wordbooks/{id}` - 删除单词书（管理员）
- GET `/api/admin/statistics` - 获取系统统计数据

## 待实现功能

- [ ] 口语学习功能
- [ ] 学习数据导出功能
- [ ] 移动端适配优化

## 已完成功能

- [x] PDF单词书导入功能
- [x] 管理员后台界面
- [x] AI接口集成（单词释义、例句生成）
- [x] 单词书管理功能

## 注意事项

1. 首次运行前请确保MySQL服务已启动
2. 修改配置文件中的数据库密码和JWT密钥
3. 建议在生产环境中使用更强的JWT密钥
4. AI功能需要配置有效的API密钥，否则会返回降级响应
5. PDF导入功能要求PDF是文本格式，不是扫描图片
6. 音频功能默认使用浏览器TTS，可配置外部音频服务
7. 管理员权限需要在数据库中手动设置用户角色为ADMIN

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

### PDF导入失败
检查：
1. PDF文件是否为文本格式（非扫描图片）
2. PDF格式是否符合要求
3. 文件大小是否超过限制（10MB）

### AI功能不可用
检查：
1. 是否配置了有效的API密钥
2. 网络连接是否正常
3. API配额是否充足

## 开发指南

### 添加新的API接口
1. 在后端创建对应的Controller方法
2. 在前端创建对应的API调用函数
3. 在页面中调用API并处理响应

### 添加新的页面
1. 在 `frontend/src/views` 创建Vue组件
2. 在 `frontend/src/router/index.js` 添加路由配置
3. 在主页或其他页面添加导航链接

### 数据库迁移
1. 修改 `database-schema.sql` 或创建新的迁移脚本
2. 在本地测试SQL语句
3. 更新相关的Entity类

## 性能优化建议

1. 前端：
   - 使用路由懒加载
   - 图片懒加载
   - 组件按需引入

2. 后端：
   - 添加数据库索引
   - 使用缓存
   - 异步处理耗时操作

3. 数据库：
   - 定期清理无用数据
   - 优化查询语句
   - 考虑读写分离

## 安全建议

1. 生产环境必须：
   - 使用HTTPS
   - 修改默认JWT密钥
   - 修改默认数据库密码
   - 启用CSRF保护
   - 配置防火墙规则

2. 用户数据保护：
   - 密码加密存储
   - 敏感信息脱敏
   - 定期备份数据

## 许可证

MIT License

## 联系方式

如有问题或建议，请联系开发者。

## 致谢

感谢以下开源项目：
- Spring Boot
- Vue.js
- Element Plus
- Apache PDFBox
- OpenAI API
