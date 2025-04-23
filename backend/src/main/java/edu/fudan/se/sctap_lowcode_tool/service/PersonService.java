package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.PersonRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    /**
     * 獲取所有人員
     */
    public List<PersonInfo> getAllPersons() {
        return personRepository.findAll();
    }

    /**
     * 根據 ID 查詢人員
     */
    public Optional<PersonInfo> getPersonById(Integer id) {
        return personRepository.findById(id);
    }

    /**
     * 根據空間 ID 查詢人員
     */
    public List<PersonInfo> getPersonsBySpaceId(Integer spaceId) {
        Optional<SpaceInfo> spaceOpt = spaceRepository.findById(spaceId);
        return spaceOpt.map(personRepository::findByCurrentSpace).orElse(Collections.emptyList());
    }

    /**
     * 建立新的人員
     */
    public PersonInfo createPerson(PersonCreateRequest request) {
        PersonInfo person = new PersonInfo();
        person.setPersonName(request.getPersonName());

        if (request.getSpaceId() != null) {
            spaceRepository.findById(request.getSpaceId()).ifPresent(person::setCurrentSpace);
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
            Optional<SpaceInfo> spaceOpt = spaceRepository.findById(request.getSpaceId());
            person.setCurrentSpace(spaceOpt.orElse(null));
        } else {
            person.setCurrentSpace(null); // 明確傳 null，則離開空間
        }

        // 消息队列
        return Optional.of(personRepository.save(person));
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