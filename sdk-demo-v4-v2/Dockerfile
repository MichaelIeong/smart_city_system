# 使用Node.js的官方镜像
FROM node:18-alpine

# 设置工作目录
WORKDIR /app

# 复制package.json和package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制应用程序的其余代码
COPY . .

## 构建应用程序
#RUN npm run build

# 安装serve包来提供静态文件服务
RUN npm install -g serve

# 暴露端口
EXPOSE 5173

# 启动应用
CMD ["serve", "-s", "dist","-l", "5173"]