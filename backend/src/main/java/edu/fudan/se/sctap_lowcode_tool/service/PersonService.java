package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jmodel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jmodel.SpaceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jrepository.PersonNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jrepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.PersonRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.KafkaProducerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonNodeRepository personNodeRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceNodeRepository spaceNodeRepository;

    @Autowired
    private KafkaProducerUtil kafkaProducerUtil;

    /**
     * 獲取所有人員
     */
    public List<PersonInfo> getAllPersons() {
        return personRepository.findAll();
    }

    public List<PersonNode> getAllPersonNodes() {
        return personNodeRepository.findAll();
    }

    /**
     * 根據 ID 查詢人員
     */
    public Optional<PersonInfo> getPersonById(Integer id) {
        return personRepository.findById(id);
    }

    public Optional<PersonNode> getPersonNodeById(Long id) {
        return personNodeRepository.findById(id);
    }

    /**
     * 根據空間 ID 查詢人員
     */
    public List<PersonInfo> getPersonsBySpaceId(Integer spaceId) {
        Optional<SpaceInfo> spaceOpt = spaceRepository.findById(spaceId);
        return spaceOpt.map(personRepository::findByCurrentSpace).orElse(Collections.emptyList());
    }

    public List<PersonNode> getPersonNodesBySpaceId(Long spaceNodeId) {
        Optional<SpaceNode> spaceNodeOpt = spaceNodeRepository.findById(spaceNodeId);
        return spaceNodeOpt.map(personNodeRepository::findByCurrentSpace).orElse(Collections.emptyList());
    }

    /**
     * 建立新的人員
     */
    public PersonInfo createPerson(PersonCreateRequest request) {
        PersonInfo person = new PersonInfo();
        person.setPersonName(request.getPersonName());

        if (request.getSpaceId() != null) {
            spaceRepository.findBySpaceId(request.getSpaceId()).ifPresent(person::setCurrentSpace);
        }

        return personRepository.save(person);
    }

    /**
     * 更新人員資料（可更新姓名與空間）
     */
    public Optional<PersonInfo> updatePerson(Integer id, PersonUpdateRequest request) {
        Optional<PersonInfo> personOpt = personRepository.findById(id);
        if (personOpt.isEmpty()) {
            return Optional.empty();
        }

        PersonInfo person = personOpt.get();

        if (request.getPersonName() != null) {
            person.setPersonName(request.getPersonName());
        }

        if (request.getSpaceId() != null) {
            Optional<SpaceInfo> spaceOpt = spaceRepository.findBySpaceId(request.getSpaceId());
            person.setCurrentSpace(spaceOpt.orElse(null));
        } else {
            person.setCurrentSpace(null); // 明確傳 null，則離開空間
        }

        // 消息队列
        Map<String, Object> message = new HashMap<>();
        message.put("currentSpaceId", person.getCurrentSpace().getSpaceId());
        message.put("name", person.getPersonName());

        kafkaProducerUtil.sendMessage("person_info", message);
        return Optional.of(personRepository.save(person));
    }

    public Optional<PersonNode> updatePersonNode(Long id, PersonUpdateRequest request) {
        Optional<PersonNode> personOpt = personNodeRepository.findById(id);
        if (personOpt.isEmpty()) {
            return Optional.empty();
        }

        PersonNode person = personOpt.get();

        if (request.getPersonName() != null) {
            person.setPersonName(request.getPersonName());
        }

        if (request.getSpaceId() != null) {
            // 你需要确保 spaceId 是 SpaceNode 中的某个唯一字段，比如 spaceId（不是数据库ID）
            Optional<SpaceNode> spaceOpt = spaceNodeRepository.findBySpaceId(request.getSpaceId());
            person.setCurrentSpace(spaceOpt.orElse(null));
        } else {
            person.setCurrentSpace(null); // 明确设为 null 表示移除关系
        }

        // 消息队列（同原逻辑）
        Map<String, Object> message = new HashMap<>();
        message.put("currentSpaceId", person.getCurrentSpace() != null ? person.getCurrentSpace().getSpaceId() : null);
        message.put("name", person.getPersonName());

        kafkaProducerUtil.sendMessage("person_info", message);
        return Optional.of(personNodeRepository.save(person));
    }
    /**
     * 設定人員所屬空間（可傳 null 表示移除）
     */
    public Optional<PersonInfo> setPersonSpace(Integer personId, Integer spaceId) {
        Optional<PersonInfo> personOpt = personRepository.findById(personId);
        if (personOpt.isEmpty()) {
            return Optional.empty();
        }

        PersonInfo person = personOpt.get();

        if (spaceId != null) {
            Optional<SpaceInfo> spaceOpt = spaceRepository.findById(spaceId);
            person.setCurrentSpace(spaceOpt.orElse(null)); // 無效 ID 時設 null
        } else {
            person.setCurrentSpace(null); // 明確移除空間
        }

        return Optional.of(personRepository.save(person));
    }

    /**
     * 刪除人員
     */
    public void deletePerson(Integer id) {
        personRepository.deleteById(id);
    }

    /**
     * 儲存或更新人員（底層方法）
     */
    public PersonInfo saveOrUpdatePerson(PersonInfo personInfo) {
        return personRepository.save(personInfo);
    }
}