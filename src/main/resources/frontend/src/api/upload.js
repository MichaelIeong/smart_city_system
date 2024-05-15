import request from '../utils/request';


export const uploadDSL = query => {
    return request({
        url: 'http://localhost:8080/api/upload/dsl',
        method: 'post',
        data: query
    });
};