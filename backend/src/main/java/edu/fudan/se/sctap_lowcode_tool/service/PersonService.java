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
        person.setPersonId(request.getPersonId());
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

        return Optional.of(personRepository.save(person));
    }

    /**
     * 讓人員離開空間（將 currentSpace 設為 null）
     */
    public Optional<PersonInfo> removePersonFromSpace(Integer personId) {
        Optional<PersonInfo> personOpt = personRepository.findById(personId);
        if (personOpt.isPresent()) {
            PersonInfo person = personOpt.get();
            person.setCurrentSpace(null);
            return Optional.of(personRepository.save(person));
        }
        return Optional.empty();
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