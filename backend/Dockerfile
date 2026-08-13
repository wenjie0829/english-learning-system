# 第一阶段：构建前端
FROM node:18 AS frontend-builder
WORKDIR /app/frontend

# 复制前端依赖文件并安装
COPY frontend/package*.json ./
RUN npm install

# 复制前端源码并构建
COPY frontend/ .
RUN npm run build

# 第二阶段：构建后端
FROM maven:3.8-openjdk-17 AS backend-builder
WORKDIR /app

# 复制后端依赖并下载
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制后端源码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 第三阶段：最终运行镜像
FROM amazoncorretto:17-alpine
WORKDIR /app

# 从后端构建阶段复制 jar 包
COPY --from=backend-builder /app/target/*.jar app.jar

# 从前端构建阶段复制静态文件
COPY --from=frontend-builder /app/frontend/dist /app/static

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]