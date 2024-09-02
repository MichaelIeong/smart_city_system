const http = require('http'); // 引入http模块


module.exports = function (RED) {
    "use strict";

    function EstablishNode(config) {
        RED.nodes.createNode(this, config);
        var node = this;
        node.rulename = config.rulename;

        this.on("input", function (msg, send, done) {


            // 将rulename存储在msg中

            msg.rulename = node.rulename;
            //console.log(msg);
            var flowNodes = [];
            RED.nodes.eachNode(function(n) {
                flowNodes.push(n);
            });

            // 将节点数据转换为 JSON 字符串
            var flowJson = JSON.stringify(flowNodes, null, 2);

            // 输出到控制台
            //console.log(flowJson);
            // 不能只发送msg，msg是为了方便后端运行才设计的，还应该把export里的json也传给后端，这样点击配置才能恢复出来

            const postData = JSON.stringify(msg);
            fetch('http://127.0.0.1:8080/api/fusion/uploadrule', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(msg)
            })
                // .then(response => {
                //     if (!response.ok) {
                //         throw new Error('Network response was not ok');
                //     }
                //     return response.json();
                // })
                // .then(data => {
                //     console.log('Success:', data);
                // })
                // .catch(error => {
                //     console.error('Errorrr:', error);
                // });
        });
    }

    RED.nodes.registerType("establish", EstablishNode);
}
