# 第一阶段：构建前端
FROM node:18 AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./frontend/
WORKDIR /app/frontend
RUN npm install
COPY frontend/ .
RUN npm run build

# 第二阶段：构建后端
FROM maven:3.8-openjdk-17 AS backend-builder
WORKDIR /app
COPY backend/pom.xml ./backend/
WORKDIR /app/backend
RUN mvn dependency:go-offline
COPY backend/src ./src
RUN mvn clean package -DskipTests

# 第三阶段：运行
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=backend-builder /app/backend/target/*.jar app.jar
COPY --from=frontend-builder /app/frontend/dist /app/static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]