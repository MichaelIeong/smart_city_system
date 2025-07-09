# 使用 Node.js 作为构建镜像
FROM node:20 as builder

WORKDIR /app

# 複製 env.server 為 .env.production（或視你的框架調整）
COPY .env.server .env.production

# 複製依賴相關檔案
COPY package.json yarn.lock ./

RUN yarn install

# 複製專案其餘檔案
COPY . .

# 構建
RUN yarn build

# 使用 nginx 作為伺服器
FROM nginx:alpine

# 複製構建好的檔案
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]