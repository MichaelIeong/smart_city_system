#!/bin/bash

# 发布到github仓库中

Folder="SCTAP.github.io"


cp -r dist/* ../$Folder && \
cd ../$Folder && \
git pull && \
mv index.html development-tool/index.html && \
git add . && \
git commit -m "update" && \
git push