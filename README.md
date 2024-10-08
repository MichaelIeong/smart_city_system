# SCTAP低代码融合感知工具

##  项目概述

### 目标
设计并开发一款智慧城市低代码平台，用户能够通过简单的操作创建和编辑3D模型。该平台允许用户通过界面将物联网设备与模型进行绑定，并实现对物联网设备的控制和交互。用户还可以进行低代码编程，以便针对不同环境和传感器触发特定动作。此外，用户可将设备与3D建模的绑定信息导出为JSON格式，便于查看设备的具体信息以及设备之间的关联。平台还将整合感知功能，实现对环境数据的感知和处理。

## 核心功能

### 低代码物联网编程环境

- 直观的编程接口：采用图形化编程界面，使用户可以通过拖拽组件来设计逻辑，无需编写复杂的代码。
- 设备快速集成：简化了物联网设备的集成过程，用户只需几步操作即可将设备接入系统。
- 逻辑可视化：逻辑编写过程可视化，帮助用户直观理解程序结构和流程，降低逻辑错误。

![image.png](https://cdn.nlark.com/yuque/0/2024/png/35903307/1713346568573-bae88595-8871-4f84-8a8f-af20133f8718.png#averageHue=%23ccd5c7&clientId=u3c1a3e2d-b4e9-4&from=paste&height=413&id=u969daad5&originHeight=413&originWidth=603&originalType=binary&ratio=1&rotation=0&showTitle=false&size=226401&status=done&style=none&taskId=u4493fa7b-73af-4f04-b391-acd40cdef59&title=&width=603)

### 物联网设备接入

- 界面化操作：通过友好的界面操作，简化设备绑定和控制过程。
- 实时交互：支持与物联网设备的实时数据交互，实现即时控制和反馈。
- 多设备管理：允许同时管理和控制多个设备，提高效率。
- 设备状态监控：实时监控设备状态，包括在线状态、工作参数等，确保系统稳定运行。

![image.png](https://cdn.nlark.com/yuque/0/2024/png/35903307/1713346620033-9f633980-3e5b-419b-9a37-d3ebd055c5bb.png#averageHue=%238d8e8c&clientId=u3c1a3e2d-b4e9-4&from=paste&height=365&id=ufbea8a7a&originHeight=365&originWidth=563&originalType=binary&ratio=1&rotation=0&showTitle=false&size=228618&status=done&style=none&taskId=udcaf6ff6-bac1-4aae-8589-8e835781479&title=&width=563)


### 设备绑定信息导入导出

- JSON格式化：使用JSON格式导出和导入设备绑定信息，确保导出和导入过程中数据的完整性和一致性。
- 快速配置：通过导入预设的设备绑定信息，快速配置新项目。

![image.png](https://cdn.nlark.com/yuque/0/2024/png/35903307/1713346643925-62446f35-573d-4ccd-9554-abdffd6a3eb9.png#averageHue=%238a8a87&clientId=u3c1a3e2d-b4e9-4&from=paste&height=467&id=u8e730dd6&originHeight=467&originWidth=659&originalType=binary&ratio=1&rotation=0&showTitle=false&size=257212&status=done&style=none&taskId=u027e5b8d-5760-45c3-a4e5-ee88d09a11b&title=&width=659)

### 设备抽象与应用

- 设备抽象化：将物理设备的功能和特性抽象化，简化设备管理和应用开发。
- 场景适应性：快速将设备配置应用于不同的场景和项目，提高复用性。
- 配置共享：支持设备配置信息的共享和重用，加速团队协作。
- 跨平台应用：确保设备配置和控制逻辑在不同平台和环境中的一致性和可移植性。

![image.png](https://cdn.nlark.com/yuque/0/2024/png/35903307/1713346654676-bef79a34-6db1-4af6-bdea-5d59ffc445b7.png#averageHue=%238e8e8b&clientId=u3c1a3e2d-b4e9-4&from=paste&height=385&id=ufb020d22&originHeight=385&originWidth=559&originalType=binary&ratio=1&rotation=0&showTitle=false&size=184560&status=done&style=none&taskId=u372fa540-ff56-46b6-9256-adaa238a7d0&title=&width=559)

### 融合感知功能

- 环境监测：集成多种传感器数据，实时监测环境变化。
- 数据分析：应用先进的数据分析技术，提炼有价值的信息和洞见。
- 智能响应：根据分析结果自动调整设备行为或通知用户，提升智能化水平。
- 个性化服务：提供个性化服务和功能，根据用户需求和环境数据定制响应策略。

![image.png](https://cdn.nlark.com/yuque/0/2024/png/35903307/1713346675804-8524282d-0d41-4265-b7d7-7f56b921aa10.png#averageHue=%238b938a&clientId=u3c1a3e2d-b4e9-4&from=paste&height=397&id=u7bed5540&originHeight=529&originWidth=838&originalType=binary&ratio=1&rotation=0&showTitle=false&size=457805&status=done&style=none&taskId=ucd8d7e5c-808b-470f-a723-d72c94107ac&title=&width=629)

### 与 Model Studio 的联动

- **模块结构：**插件可以直接通过 Model Studio 提供的 API 进行联动，把 3d 建模与其他功能分离，变成 Model Studio 的插件。

- **环境感知信息（输入）：**Model Studio 需要传递环境感知所需要的信息，包括楼层内所有传感器的信息。

- **3D 模型高亮显示（输出）**：当用户点击插件上的事件时，Model Studio 需要根据事件触发的顺序高亮显示设备。

  

# 技术架构与平台

### 前端技术栈

- **基于 Three.js 的 Vis-Three 框架**：前端界面采用Vis-Three框架，这是一个基于Three.js的库，专为3D内容的展示和交互设计。Three.js作为一个低层次的3D图形API，为我们提供了创建和显示3D内容的能力。通过Vis-Three，我们能够为用户提供一个直观的3D模型编辑器，让用户通过简单的操作即可创建、编辑和查看3D模型。
- **数据库 IndexedDB**：为了在客户端存储大量3D模型数据，我们采用了Three.js内建的IndexedDB支持。IndexedDB是一种低级API，用于在用户的浏览器中存储大量结构化数据。这种方法让我们的应用能够即使在离线状态下也能工作，同时提高了数据处理的速度和效率。

### 后端技术栈

- **Java 与 Spring Boot**：后端采用Java语言，框架选择为Spring Boot。Spring Boot简化了基于Spring的应用开发，提供了快速搭建和运行Spring应用的能力。它内置了大量常用服务的自动配置，极大地提高了开发效率和简化了部署过程。
- **数据库 MySQL**MySQL是一个广泛使用的开源关系型数据库管理系统，与Spring Boot有着良好的兼容性。它提供了强大的数据存储、查询和事务处理能力，适合处理复杂的数据关系和高并发访问。