# 使用 Node.js 作为基础镜像
FROM node:20

# 设置工作目录
WORKDIR /app

# 复制 yarn.lock 和 package.json
COPY package.json yarn.lock ./

# 安装依赖
RUN yarn install

# 复制项目文件
COPY . .

# 构建项目
RUN yarn build

# 使用 nginx 作为 web 服务器
FROM nginx:alpine

COPY deploy/nginx.conf /etc/nginx/nginx.conf

# 复制构建输出到 nginx 的默认静态文件目录
COPY --from=0 /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
