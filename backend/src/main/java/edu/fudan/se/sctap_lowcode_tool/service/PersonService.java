package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.PersonNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.PersonRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.KafkaProducerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository; // MySQL

    @Autowired
    private PersonNodeRepository personNodeRepository; // Neo4j

    @Autowired
    private SpaceRepository spaceRepository; // MySQL

    @Autowired
    private SpaceNodeRepository spaceNodeRepository; // Neo4j

    private final Object personIdLock = new Object();

    public List<PersonInfo> getAllPersons() {
        return personRepository.findAll();
    }

    public List<PersonNode> getAllPersonNodes() {
        return personNodeRepository.findAll();
    }

    public Optional<PersonInfo> getPersonById(Integer personId) {
        return personRepository.findById(personId);
    }

    public Optional<PersonNode> getPersonNodeById(Integer personId) {
        return personNodeRepository.findById(personId);
    }

    public List<PersonInfo> getPersonsBySpaceId(Integer spaceId) {
        return spaceRepository.findBySpaceId(spaceId)
                .map(personRepository::findByCurrentSpace)
                .orElse(Collections.emptyList());
    }

    public List<PersonNode> getPersonNodesBySpaceId(Integer spaceId) {
        return spaceNodeRepository.findBySpaceId(spaceId)
                .map(personNodeRepository::findByCurrentSpace)
                .orElse(Collections.emptyList());
    }

    public PersonDTO createPerson(PersonCreateRequest request) {
        synchronized (personIdLock) {
            Integer maxId = personNodeRepository.findMaxPersonId();
            int newId = (maxId != null ? maxId + 1 : 1);

            PersonInfo mysqlPerson = new PersonInfo();
            mysqlPerson.setPersonId(newId);
            mysqlPerson.setPersonName(request.getPersonName());

            if (request.getSpaceId() != null) {
                spaceRepository.findBySpaceId(request.getSpaceId())
                        .ifPresent(mysqlPerson::setCurrentSpace);
            }

            PersonInfo savedMySQL = personRepository.save(mysqlPerson);

            PersonNode node = new PersonNode();
            node.setPersonId(newId);
            node.setPersonName(request.getPersonName());

            if (request.getSpaceId() != null) {
                spaceNodeRepository.findBySpaceId(request.getSpaceId())
                        .ifPresent(node::setCurrentSpace);
            }

            personNodeRepository.save(node);

            return new PersonDTO(
                    savedMySQL.getPersonId(),
                    savedMySQL.getPersonName(),
                    savedMySQL.getCurrentSpace() != null ? savedMySQL.getCurrentSpace().getSpaceId() : null
            );
        }
    }

    public Optional<PersonDTO> updatePerson(Integer personId, PersonUpdateRequest request) {
        Optional<PersonInfo> mysqlOpt = personRepository.findById(personId);
        if (mysqlOpt.isEmpty()) return Optional.empty();

        PersonInfo person = mysqlOpt.get();
        if (request.getPersonName() != null) person.setPersonName(request.getPersonName());

        if (request.getSpaceId() != null) {
            spaceRepository.findBySpaceId(request.getSpaceId())
                    .ifPresent(person::setCurrentSpace);
        } else {
            person.setCurrentSpace(null);
        }

        PersonInfo updated = personRepository.save(person);

        personNodeRepository.deletePersonSpaceRelation(personId);

        personNodeRepository.findById(personId).ifPresentOrElse(node -> {
            node.setPersonName(person.getPersonName());

            if (person.getCurrentSpace() != null) {
                spaceNodeRepository.findBySpaceId(person.getCurrentSpace().getSpaceId())
                        .ifPresent(node::setCurrentSpace);
            } else {
                node.setCurrentSpace(null);
            }

            personNodeRepository.save(node);
        }, () -> {
            PersonNode node = new PersonNode();
            node.setPersonId(personId);
            node.setPersonName(person.getPersonName());

            if (person.getCurrentSpace() != null) {
                spaceNodeRepository.findBySpaceId(person.getCurrentSpace().getSpaceId())
                        .ifPresent(node::setCurrentSpace);
            }

            personNodeRepository.save(node);
        });

        return Optional.of(new PersonDTO(
                updated.getPersonId(),
                updated.getPersonName(),
                updated.getCurrentSpace() != null ? updated.getCurrentSpace().getSpaceId() : null
        ));
    }

    public Optional<PersonInfo> setPersonSpace(Integer personId, Integer spaceId) {
        Optional<PersonInfo> personOpt = personRepository.findById(personId);
        if (personOpt.isEmpty()) return Optional.empty();

        PersonInfo person = personOpt.get();
        if (spaceId != null) {
            spaceRepository.findBySpaceId(spaceId)
                    .ifPresent(person::setCurrentSpace);
        } else {
            person.setCurrentSpace(null);
        }

        return Optional.of(personRepository.save(person));
    }

    public void deletePerson(Integer personId) {
        personRepository.deleteById(personId);
        personNodeRepository.deleteById(personId);
    }
}