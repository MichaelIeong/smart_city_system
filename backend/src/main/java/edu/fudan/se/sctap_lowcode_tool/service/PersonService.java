package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
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
    private PersonRepository personRepository;

    @Autowired
    private PersonNodeRepository personNodeRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceNodeRepository spaceNodeRepository;

    @Autowired
    private KafkaProducerUtil kafkaProducerUtil;

    public List<PersonInfo> getAllPersons() {
        return personRepository.findAll();
    }

    public List<PersonNode> getAllPersonNodes() {
        return personNodeRepository.findAll();
    }

    public Optional<PersonInfo> getPersonById(Integer id) {
        return personRepository.findById(id);
    }

    public Optional<PersonNode> getPersonNodeById(Long id) {
        return personNodeRepository.findById(id);
    }

    public List<PersonInfo> getPersonsBySpaceId(Integer spaceId) {
        Optional<SpaceInfo> spaceOpt = spaceRepository.findById(spaceId);
        return spaceOpt.map(personRepository::findByCurrentSpace)
                       .orElse(Collections.emptyList());
    }

    public List<PersonNode> getPersonNodesBySpaceId(Long spaceNodeId) {
        Optional<SpaceNode> spaceNodeOpt = spaceNodeRepository.findById(spaceNodeId);
        return spaceNodeOpt.map(personNodeRepository::findByCurrentSpace)
                           .orElse(Collections.emptyList());
    }

    public PersonInfo createPerson(PersonCreateRequest request) {
        PersonInfo person = new PersonInfo();
        person.setPersonName(request.getPersonName());

        if (request.getSpaceId() != null) {
            spaceRepository.findBySpaceId(request.getSpaceId())
                           .ifPresent(person::setCurrentSpace);
        }

        PersonInfo saved = personRepository.save(person);

        PersonNode node = new PersonNode();
        node.setId(saved.getId().longValue());
        node.setPersonName(saved.getPersonName());

        if (request.getSpaceId() != null) {
            spaceNodeRepository.findBySpaceId(request.getSpaceId().toString())
                               .ifPresent(node::setCurrentSpace);
        }

        personNodeRepository.save(node);
        return saved;
    }

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
            spaceRepository.findBySpaceId(request.getSpaceId())
                           .ifPresent(person::setCurrentSpace);
        } else {
            person.setCurrentSpace(null);
        }

        PersonInfo updated = personRepository.save(person);

        personNodeRepository.findById(id.longValue()).ifPresent(node -> {
            node.setPersonName(updated.getPersonName());

            if (request.getSpaceId() != null) {
                node.setCurrentSpace(
                    spaceNodeRepository.findBySpaceId(request.getSpaceId().toString())
                                       .orElse(null)
                );
            } else {
                node.setCurrentSpace(null);
            }

            personNodeRepository.save(node);
        });

        Map<String, Object> message = new HashMap<>();
        message.put("currentSpaceId",
            person.getCurrentSpace() != null ? person.getCurrentSpace().getSpaceId() : null
        );
        message.put("name", person.getPersonName());
        kafkaProducerUtil.sendMessage("person_info", message);

        return Optional.of(updated);
    }

    public Optional<PersonInfo> setPersonSpace(Integer personId, Integer spaceId) {
        Optional<PersonInfo> personOpt = personRepository.findById(personId);
        if (personOpt.isEmpty()) {
            return Optional.empty();
        }

        PersonInfo person = personOpt.get();

        if (spaceId != null) {
            spaceRepository.findById(spaceId)
                           .ifPresent(person::setCurrentSpace);
        } else {
            person.setCurrentSpace(null);
        }

        PersonInfo updated = personRepository.save(person);

        personNodeRepository.findById(personId.longValue()).ifPresent(node -> {
            if (spaceId != null) {
                node.setCurrentSpace(
                    spaceNodeRepository.findBySpaceId(spaceId.toString())
                                       .orElse(null)
                );
            } else {
                node.setCurrentSpace(null);
            }
            personNodeRepository.save(node);
        });

        return Optional.of(updated);
    }

    public void deletePerson(Integer id) {
        personRepository.deleteById(id);
        personNodeRepository.findById(id.longValue()).ifPresent(node -> {
            personNodeRepository.deleteById(node.getId());
        });
    }

    public PersonInfo saveOrUpdatePerson(PersonInfo personInfo) {
        PersonInfo saved = personRepository.save(personInfo);

        personNodeRepository.findById(saved.getId().longValue())
            .ifPresentOrElse(node -> {
                node.setPersonName(saved.getPersonName());
                if (saved.getCurrentSpace() != null) {
                    spaceNodeRepository.findBySpaceId(
                        saved.getCurrentSpace().getSpaceId().toString()
                    ).ifPresent(node::setCurrentSpace);
                } else {
                    node.setCurrentSpace(null);
                }
                personNodeRepository.save(node);
            }, () -> {
                PersonNode newNode = new PersonNode();
                newNode.setId(saved.getId().longValue());
                newNode.setPersonName(saved.getPersonName());
                if (saved.getCurrentSpace() != null) {
                    spaceNodeRepository.findBySpaceId(
                        saved.getCurrentSpace().getSpaceId().toString()
                    ).ifPresent(newNode::setCurrentSpace);
                }
                personNodeRepository.save(newNode);
            });

        return saved;
    }

    public PersonNode createPersonNode(PersonCreateRequest request) {
        PersonNode person = new PersonNode();
        person.setPersonName(request.getPersonName());

        if (request.getSpaceId() != null) {
            spaceNodeRepository.findBySpaceId(request.getSpaceId().toString())
                               .ifPresent(person::setCurrentSpace);
        }

        return personNodeRepository.save(person);
    }

    public void deletePersonNode(Long id) {
        personNodeRepository.deleteById(id);
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
            spaceNodeRepository.findBySpaceId(request.getSpaceId().toString())
                               .ifPresent(person::setCurrentSpace);
        } else {
            person.setCurrentSpace(null);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("currentSpaceId",
            person.getCurrentSpace() != null ? person.getCurrentSpace().getSpaceId() : null
        );
        message.put("name", person.getPersonName());
        kafkaProducerUtil.sendMessage("person_info", message);

        return Optional.of(personNodeRepository.save(person));
    }
}
