#!/bin/bash

# 将前端应用部署到122服务器上的脚本

host="fdse@10.177.29.122"
read -s -p "请输入122服务器密码: "  pw


/usr/bin/expect << EOF
set timeout 15
spawn ssh $host 
expect "*password*"                 
send "$pw\r"
expect "*login*"   # 期待ssh提示 Last login:
send "rm -rf /home/fdse/sctap-frontend/dist\r" 
expect ""
send "logout\r"
expect eof

spawn scp -r ./dist $host:/home/fdse/sctap-frontend/dist
expect "*password*" 
send "$pw\r" 
expect eof

spawn ssh $host 
expect "*password*"                 
send "$pw\r" 
expect "*login*"   # 期待ssh提示 Last login:
send "/home/fdse/sctap-frontend/deploy.sh\r" 
expect "*success*"      
send "logout\r" 
expect eof
EOF
