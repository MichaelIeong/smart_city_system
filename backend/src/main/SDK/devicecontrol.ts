import Tacos, { Root } from "@tslfe/tacos-sdk";
Tacos.connect({
    appCode: "<应用id>",
    request: {
        baseURL: "<应用接口根路径域名>"
    },
    socket:{
        url: "<空间与设备通讯连接地址>" // 指定websoket地址
    }
}).then(core => {
    // RootSDK实例
    const root = core.createInstance(Root);
})