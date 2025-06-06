INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (1, '湾谷办公室', null, '1', '湾谷办公室', 1);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (2, 'Ai Park', null, '2', 'Ai Park', 1);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (3, '湾谷会议室', null, '3', '湾谷会议室', 1);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (4, '客厅', null, '4', 'LivingRoom', 2);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (5, '阳台', null, '5', 'Balcony', 2);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (6, '厨房', null, '6', 'Kitchen', 2);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (7, '浴室', null, '7', 'Bathroom', 2);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (8, '衣帽间', null, '8', 'Cloakroom', 2);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (9, '卧室一', null, '9', 'BedroomOne', 2);
INSERT INTO smart_city.spaces (id, description, fixed_properties, space_id, space_name, project_id) VALUES (10, '卧室二', null, '10', 'BedroomTwo', 2);

INSERT INTO smart_city.person (id, person_name, space_id) VALUES (1, '越神', 3);
INSERT INTO smart_city.person (id, person_name, space_id) VALUES (2, '土子哥', 1);
INSERT INTO smart_city.person (id, person_name, space_id) VALUES (3, '丰哥', 3);
INSERT INTO smart_city.person (id, person_name, space_id) VALUES (4, 'mmhu', 3);
INSERT INTO smart_city.person (id, person_name, space_id) VALUES (5, '章伯雄猫培育基地', 2);
INSERT INTO smart_city.person (id, person_name, space_id) VALUES (6, '邓贝多芬', 2);

INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (1, 55, 55, 55, '1', '摄像头1', '55', null, 1, 1);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (2, 2, 4, 5, '2', '摄像头2', '55', null, 1, 2);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (3, 5, 11, 11, '3', '温度传感器1', '55', null, 2, 1);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (4, 55, 2, 33, '4', '温度传感器2', '55', null, 2, 2);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (5, 11, 55, 11, '5', 'CO/CO2传感器', '55', null, 3, 2);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (6, 55, 11, 44, '6', '湿度传感器', '55', null, 4, 2);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (7, 545, 44, 3, '7', '广播喇叭', '55', null, 5, 2);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (8, 22, 33, 55, '8', '机器人1', null, null, 6, 1);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (9, 22, 33, 55, '9', '智能门1', null, null, 7, 1);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (10, null, null, null, '10', 'Microphone', '位于会议室', null, 8, 3);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (11, null, null, null, '11', 'Microphone', '位于办公室', null, 8, 1);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (12, null, null, null, '12', 'TV', '位于会议室', null, 9, 3);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (13, null, null, null, '13', 'TV', '位于办公室', null, 9, 1);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (14, null, null, null, '14', '会议室顶灯', '位于会议室', null, 10, 3);
INSERT INTO smart_city.devices (id, coordinatex, coordinatey, coordinatez, device_id, device_name, fixed_properties, last_update_time, device_type_id, space_id) VALUES (15, null, null, null, '15', '会议室空调', '位于会议室', null, 11, 3);

INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (1, '1', '摄像头', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (2, '2', '温度传感器', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (3, '3', 'CO/CO2传感器', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (4, '4', '湿度传感器', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (5, '5', '广播喇叭', false, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (6, '6', '机器人', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (7, '7', '门', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (8, '8', 'Microphone', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (9, '9', 'TV', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (10, '10', '灯', true, 1);
INSERT INTO smart_city.device_types (id, device_type_id, device_type_name, is_sensor, project_id) VALUES (11, '11', '空调', true, 1);

INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (1, 'stream', 'stream', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (2, 'temperature detection', 'temperature', '2');
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (3, 'CO2 detection', 'CO2 detection', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (4, 'CO detection', 'CO detection', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (5, 'humidity', 'humidity', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (6, '行动', '行动', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (7, '开门', '开门', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (8, '音量采集', 'Volume Caption', null);
INSERT INTO smart_city.actuating_functions (id, description, name, params) VALUES (9, '开电视', 'Turn on TV', null);

INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (1, null, 1, 1, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (2, null, 1, 2, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (3, null, 2, 3, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (4, null, 2, 4, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (5, null, 3, 5, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (6, null, 4, 5, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (7, null, 5, 6, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (8, 'api/robot/move', 6, 8, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (9, 'api/door/open', 7, 9, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (10, null, 8, 10, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (11, null, 8, 11, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (12, 'api/tv/show', 9, 12, null);
INSERT INTO smart_city.actuating_function_device (id, url, actuating_function_id, device_id, description) VALUES (13, 'api/tv/show', 9, 13, null);
