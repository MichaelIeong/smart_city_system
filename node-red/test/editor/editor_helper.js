/**
 * Copyright JS Foundation and other contributors, http://js.foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

var when = require("when");
const express = require('express');
var http = require('http');
var express = require("express");
var fs = require('fs-extra');
var path = require('path');
var app = express();

var RED = require("nr-test-utils").require("node-red/lib/red.js");

var utilPage = require("./pageobjects/util/util_page");

var server = require('http').createServer(app);;
var homeDir = './test/resources/home';
var address = '127.0.0.1';
var listenPort = 0; // use ephemeral port
var url;
/*
 * Set false when you need a flow to reproduce the failed test case.
 * The flow file is under "homeDir" defined above.
 */
var isDeleteFlow = true;

function getFlowFilename() {
    var orig = Error.prepareStackTrace;
    var err = new Error();
    Error.prepareStackTrace = function (err, stack) {
        return stack;
    };
    // Two level higher caller is the actual caller (e.g. a caller of startServer).
    var filepath = err.stack[2].getFileName();
    var filename = path.basename(filepath, ".js");
    Error.prepareStackTrace = orig;

    var flowFile = 'flows_' + filename + '.json';
    return flowFile;
}

function cleanup(flowFile) {
    var credentialFile = flowFile.replace(/\.json$/, '') + '_cred.json';
    deleteFile(homeDir + "/" + flowFile);
    deleteFile(homeDir + "/." + flowFile + ".backup");
    deleteFile(homeDir + "/" + credentialFile);
    deleteFile(homeDir + "/." + credentialFile + ".backup");
    deleteFile(homeDir + "/package.json");
    deleteFile(homeDir + "/lib/flows");
    deleteFile(homeDir + "/lib");
}

function deleteFile(flowFile) {
    try {
        fs.statSync(flowFile);
        if (isDeleteFlow) {
            fs.unlinkSync(flowFile);
        }
    } catch (e) {}
}

function setupCustomRoutes(app) {
    // 添加中间件来解析 JSON 请求体
    app.use(express.json());

    // 自定义路由处理节点导入
    app.post('/import-nodes', (req, res) => {
        const nodesJson = req.body; // 获取请求体中的节点 JSON

        if (!nodesJson || !Array.isArray(nodesJson.flows)) {
            return res.status(400).send('Invalid data format');
        }

        // 清空当前画布上的节点
        RED.nodes.removeFlows();

        // 将新的节点导入到 Node-RED
        try {
            RED.nodes.addFlows(nodesJson.flows);
            res.status(200).send('Nodes imported successfully');
        } catch (error) {
            console.error('Error importing nodes:', error);
            res.status(500).send('Failed to import nodes');
        }
    });
}


module.exports = {
    startServer: function() {
        try{
            utilPage.init();

            // Name a flow file including caller filename so that multiple Node-RED servers can run simultaneously.
            // Call this method here because retrieving the caller filename by call stack.
            var flowFilename = getFlowFilename();
            browser.windowHandleMaximize();
            browser.call(function () {
                return new Promise(function(resolve, reject) {
                    cleanup(flowFilename);
                    server = http.createServer(app);
                    var settings = {
                        httpAdminRoot: "/",
                        httpNodeRoot: "/api",
                        userDir: homeDir,
                        flowFile: flowFilename,
                        functionGlobalContext: { },    // enables global context
                        SKIP_BUILD_CHECK: true,
                        logging: {console: {level:'off'}}
                    };
                    console.log(44332211);
                    RED.init(server, settings);
                    app.use(settings.httpAdminRoot,RED.httpAdmin);
                    app.use(settings.httpNodeRoot,RED.httpNode);
                    //server.listen(listenPort, address);
                    server.listen(1880, () => {
                        console.log('Node-RED server is running on port 1880');
                        setupCustomRoutes(app); // 在服务器启动后设置自定义路由
                    });
                    server.on('listening', function() {
                        var port = server.address().port;
                        url = 'http://' + address + ':' + port;
                    });
                    RED.start().then(function() {
                        resolve();
                    });
                });
            });
            browser.url(url);
            browser.waitForExist(".red-ui-palette-node[data-palette-type='inject']");
        } catch (err) {
            console.log(err);
            throw err;
        }
    },

    stopServer: function(done) {
        try {
            // Call this method here because retrieving the caller filename by call stack.
            var flowFilename = getFlowFilename();
            browser.call(function () {
                browser.close();  // need to call this inside browser.call().
                return when.promise(function(resolve, reject) {
                    if (server) {
                        RED.stop().then(function() {
                            server.close(function() {
                                cleanup(flowFilename);
                                resolve();
                            });
                        });
                    } else {
                        cleanup(flowFilename);
                        resolve();
                    }
                });
            });
        } catch (err) {
            console.log(err);
            throw err;
        }
    },

    url: function() {
        return url;
    },

    red: function() {
        return RED;
    },
};
