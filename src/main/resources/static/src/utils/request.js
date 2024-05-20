import axios from 'axios';
import { Message } from 'element-ui'

const service = axios.create({
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    },
    timeout: 5000
});

service.interceptors.request.use(
    config => {
        // if (store.getters.token) {
        //     config.headers['X-Token'] = getToken() // 让每个请求携带token--['X-Token']为自定义key 请根据实际情况自行修改
        // }
        return config;
    },
    error => {
        console.log(error);
        return Promise.reject();
    }
);

service.interceptors.response.use(
    response => {
        if (response.status === 200) {
            // if (response.data.code === 103) {
            //     router.push('/login');
            //     Message.error("登录会话已过期,请重新登录.")
            // }
            return response.data;
        } else {
            Promise.reject();
        }
    },
    error => {
        console.log(error);
        return Promise.reject();
    }
);

export default service;
