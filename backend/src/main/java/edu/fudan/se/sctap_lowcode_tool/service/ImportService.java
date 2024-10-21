package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
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

    /**
     * Checks if the given Meta object represents a sensor.
     *
     * @param meta the Meta object to check
     * @return true if the Meta object represents a sensor, false otherwise
     */
    private boolean isSensor(Meta meta) {
        // TODO: implementation pending
        return false;
    }

    /**
     * Add DeviceInfo and DeviceTypeInfo to the database. <br>
     * details: <br>
     * - Add Device,
     * - Add DeviceType,
     * - Add Device-Space Reference
     * - Add States of devices <br>
     *
     * @param deviceMeta the Meta object representing the device
     * @param spaceMeta  the Meta object representing the space where the device is located
     */
    private void addDevice(Meta deviceMeta, Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    /**
     * Add EventInfo to the database. <br>
     *
     * @param spaceMeta   the Meta object representing the space where the event is located
     * @param projectInfo the ProjectInfo object where the event is located
     */
    private void addEvent(Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    /**
     * Add PropertyInfo and PropertySpace to the database. <br>
     * details: <br>
     * - Add Property,
     * - Add PropertySpace Reference <br>
     *
     * @param spaceMeta   the Meta object representing the space where the property is located
     * @param projectInfo the ProjectInfo object where the property is located
     */
    private void addProperty(Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }

    /**
     * Add ServiceInfo to the database. <br>
     *
     * @param spaceMeta   the Meta object representing the space where the service is located
     * @param projectInfo the ProjectInfo object where the service is located
     */
    private void addService(Meta spaceMeta, ProjectInfo projectInfo) {
        // TODO: implementation pending
    }


    /**
     * Add SpaceInfo and the relation between it and the Project to the database.
     *
     * @param meta        the Meta object representing the space
     * @param projectInfo the ProjectInfo object where the space is located
     */
    private void addSpace(Meta meta, ProjectInfo projectInfo) throws InvalidJsonValueException {
        if (meta == null) return;
        requireNotNull(meta, "Id");
        requireNotNull(meta, "Name");
        SpaceInfo spaceInfo = new SpaceInfo();
        spaceInfo.setSpaceId(meta.Id());
        spaceInfo.setSpaceName(meta.Name());
        spaceInfo.setProjectInfo(projectInfo);
        // TODO: maintain the relations of adjacent spaces
        spaceRepository.save(spaceInfo);
    }


    /**
     * **Entry Method of this class** <br>
     * Import the Meta objects recursively. <br>
     * <p>
     * - Make a new ProjectInfo and
     * - for each space in the meta tree, <br>
     * - Add SpaceInfo and their Events, Properties, Services <br>
     * - Add All Devices and DeviceTypes in the space <br>
     *
     * @param metaTree    the meta tree to be imported
     * @param projectName the project name to be saved
     */
    @Transactional
    public void importRecursively(Iterable<MetaTreeNode> metaTree, String projectName) {

        try {
            // Make a new Project and save
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setProjectName(projectName);
            projectInfo = projectRepository.save(projectInfo);

            // Import the meta tree
            for (var node : metaTree) {
                Meta spaceMeta = node.getMeta();
                this.addSpace(spaceMeta, projectInfo);
                // TODO: add events, properties, services of this space
                // TODO: add devices and device types in this space
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
