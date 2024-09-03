# 使用官方的 Node.js 镜像作为基础镜像
FROM node:18

# 设置工作目录
WORKDIR /usr/src/app

# 将 package.json 和 package-lock.json（如果有）复制到工作目录
COPY package*.json ./

# 安装 Node.js 依赖项
RUN npm install

# 将本地的 Node-RED 项目复制到工作目录
COPY . .

# 暴露 1880 端口
EXPOSE 1880

# 运行 Node-RED
CMD ["npm", "start"]
