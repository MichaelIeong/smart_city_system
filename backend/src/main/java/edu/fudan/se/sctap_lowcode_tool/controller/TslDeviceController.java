//package edu.fudan.se.sctap_lowcode_tool.controller;
//
//import edu.fudan.se.sctap_lowcode_tool.service.TslDeviceService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.HttpStatus;
//
//import java.util.Map;
//
///**
// * TSL 设备接口控制器
// * 负责响应前端点击设备类型后的查询请求
// */
//@RestController
//@RequestMapping("/api/devices")
//public class TslDeviceController {
//
//    @Autowired
//    private TslDeviceService tslDeviceService;
//
//    /**
//     * 根据设备类型(prodId)查询设备实例列表（包含区域信息）
//     * 示例：
//     *   GET /api/devices/instances?prodId=p_ai_camera_tst
//     * 前端 Vue 调用示例：
//     *   axios.get('/api/devices/instances', { params: { prodId: 'p_vrv' } })
//     */
//    @GetMapping("/instances")
//    public ResponseEntity<?> getDeviceInstances(@RequestParam String prodId) {
//        try {
//            Map<String, Object> result = tslDeviceService.queryDeviceInstances(prodId);
//
//            if (result.containsKey("error")) {
//                // 外部接口异常时
//                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
//                        .body(Map.of("success", false, "message", result.get("error")));
//            }
//
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("success", false, "message", "服务异常：" + e.getMessage()));
//        }
//    }
//}