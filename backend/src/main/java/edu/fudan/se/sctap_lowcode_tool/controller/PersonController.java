package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.service.PersonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/person")
@Tag(name = "PersonController", description = "人员状态控制器")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * 根据 personId 获取人员（优先 Neo4j）
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPerson(@PathVariable Integer id) {
        // 优先查 Neo4j
        Optional<PersonNode> neo4jPerson = personService.getPersonNodeById(id);
        if (neo4jPerson.isPresent()) {
            return ResponseEntity.ok(neo4jPerson.get());
        }

        // fallback 到 MySQL
        Optional<PersonInfo> mysqlPerson = personService.getPersonById(id);
        return mysqlPerson.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有人员（优先返回 Neo4j 格式）
     */
    @GetMapping
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<PersonDTO> dtoList = personService.getAllPersonNodes()
                .stream()
                .map(person -> new PersonDTO(
                        person.getPersonId(),
                        person.getPersonName(),
                        person.getCurrentSpace() != null ? person.getCurrentSpace().getSpaceId() : null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    /**
     * 根据空间 ID 获取人员（Neo4j）
     */
    @GetMapping("/by-space/{spaceId}")
    public ResponseEntity<List<PersonNode>> getPersonsBySpace(@PathVariable Integer spaceId) {
        return ResponseEntity.ok(personService.getPersonNodesBySpaceId(spaceId));
    }

    /**
     * 创建新人员（MySQL + Neo4j 双写）
     */
    @PostMapping
    public ResponseEntity<PersonDTO> createPerson(@RequestBody PersonCreateRequest request) {
        PersonDTO newPerson = personService.createPerson(request);
        return ResponseEntity.ok(newPerson);
    }

    /**
     * 更新人员信息（MySQL + Neo4j 双写）
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Integer id,
                                                  @RequestBody PersonUpdateRequest request) {
        return personService.updatePerson(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 设置人员的空间
     */
    @PostMapping("/{id}/set-space")
    public ResponseEntity<PersonInfo> setPersonSpace(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer spaceId) {

        return personService.setPersonSpace(id, spaceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除人员（MySQL + Neo4j）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Integer id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}