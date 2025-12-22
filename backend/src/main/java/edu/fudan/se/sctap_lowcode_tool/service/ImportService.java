package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.SceneImportRequest;
import edu.fudan.se.sctap_lowcode_tool.model.MeshInfo;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.InvalidJsonValueException;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.MetaTreeNode;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.meta.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertySpaceRepository propertySpaceRepository;

    // 新增：MeshService，用于保存 mesh 数据
    @Autowired
    private MeshService meshService;

    // 新增：ObjectMapper，用于序列化 meshGridList
    @Autowired
    private ObjectMapper objectMapper;



    /* ----------------------------------------------------------------------
     *                            JSON 导入方法
     * ---------------------------------------------------------------------- */

    /**
     * 从 JSON 导入新的项目（mesh 信息）
     *
     * @param dto JSON 结构包含 projectName 和 meshes
     * @return 新项目的 projectId
     */
    // ImportService.java (在 importJsonProject 方法中，检查 MeshInfo 是否被正确设置)

    @Transactional("jpaTransactionManager")
    public Integer importJsonProject(SceneImportRequest dto) {

        // ... (保留 projectName 校验) ...

        // 检查 meshes 列表是否被 Controller 正确设置
        if (dto.getMeshes() == null || dto.getMeshes().isEmpty()) {
            throw new BadRequestException(
                    "400",
                    "mesh 列表不能为空",
                    "/meshes",
                    "meshes",
                    "至少需要一个 mesh 数据"
            );
        }

        // 1. 创建项目
        ProjectInfo project = new ProjectInfo();
        project.setProjectName(dto.getProjectName());
        project = projectRepository.save(project);

        Integer projectId = project.getProjectId();

        // 2. 保存 mesh 列表
        for (MeshInfo mesh : dto.getMeshes()) {

            // meshGridList 现在是 List<MeshGridPoint> 对象，必须序列化为数据库所需的 String/Text 类型
            try {
                if (mesh.getMeshGridList() != null) {

                    // 序列化 List<MeshGridPoint> 对象
                    String serializedList = objectMapper.writeValueAsString(mesh.getMeshGridList());

                    // 【关键修改】：将序列化后的字符串存入新的 JPA 映射字段
                    mesh.setMeshGridListJson(serializedList);

                }
            } catch (Exception e){
                // ... (保留异常处理) ...
            }

            mesh.setProjectId(projectId);
            meshService.save(mesh);
        }

        return projectId;
    }

    /* ----------------------------------------------------------------------
     *                            ZIP 导入方法（你原来的）
     * ---------------------------------------------------------------------- */

    public void requireNotNull(Meta object, String fieldName) throws InvalidJsonValueException {
        if (object == null) {
            throw new InvalidJsonValueException("Unknown", "object cannot be null", "/", "null");
        }

        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(object);
            if (value == null) {
                throw new InvalidJsonValueException(object.Name(), "field cannot be null", fieldName, "null");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error accessing field: " + fieldName, e);
        }
    }

    private boolean isSensor(Meta meta) {
        return false;
    }

    private void addDevice(Meta deviceMeta, Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    private void addEvent(Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    private void addProperty(Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    private void addService(Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    /**
     * Add SpaceInfo
     */
    private void addSpace(Meta meta, ProjectInfo projectInfo) throws InvalidJsonValueException {
        if (meta == null) return;

        requireNotNull(meta, "Id");
        requireNotNull(meta, "Name");

        Integer spaceId;
        try {
            spaceId = Integer.valueOf(meta.Id());
        } catch (NumberFormatException e) {
            throw new InvalidJsonValueException(meta.Name(), "Id must be an integer", "Id", meta.Id());
        }

        SpaceInfo spaceInfo = new SpaceInfo();
        spaceInfo.setSpaceId(spaceId);
        spaceInfo.setSpaceName(meta.Name());
        spaceInfo.setProjectInfo(projectInfo);

        spaceRepository.save(spaceInfo);
    }


    /**
     * 原 ZIP 导入入口方法
     */
    @Transactional
    public void importRecursively(Iterable<MetaTreeNode> metaTree, String projectName) {

        try {
            // 创建项目
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setProjectName(projectName);
            projectInfo = projectRepository.save(projectInfo);

            for (var node : metaTree) {
                Meta spaceMeta = node.getMeta();
                this.addSpace(spaceMeta, projectInfo);
                // TODO: event/property/service/device 解析
            }

        } catch (InvalidJsonValueException e) {
            throw new BadRequestException(
                    "400", "Invalid value found when importing the json file.",
                    e.location + "." + e.key, e.value, e.prompt
            );
        } catch (Exception e) {
            log.error("Error importing meta tree", e);
            throw new RuntimeException("Error importing meta tree", e);
        }
    }

}