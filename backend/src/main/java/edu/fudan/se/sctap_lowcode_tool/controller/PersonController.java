package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.service.PersonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/person")
@Tag(name = "PersonController", description = "人員状态控制器")
public class PersonController {

    @Autowired
    private PersonService personService;


    /**
     * 根據 ID 獲取單個人員
     */
//    @GetMapping("/{id}")
//    public ResponseEntity<PersonInfo> getPerson(@PathVariable Integer id) {
//        return personService.getPersonById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//    @GetMapping("/{id}")
    public ResponseEntity<PersonNode> getPerson(@PathVariable Integer id) {
        return personService.getPersonNodeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查詢所有人員
     */
//    @GetMapping
//    public ResponseEntity<List<PersonDTO>> getAllPersons() {
//        List<PersonDTO> dtoList = personService.getAllPersons()
//                .stream()
//                .map(person -> new PersonDTO(
//                        person.getId(),
//                        person.getPersonName(),
//                        person.getCurrentSpace() != null ? person.getCurrentSpace().getId() : null
//                ))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(dtoList);
//    }
    @GetMapping
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<PersonDTO> dtoList = personService.getAllPersonNodes()
                .stream()
                .map(person -> new PersonDTO(
                        person.getPersonId(), // 改为 person.getPersonId()
                        person.getPersonName(),
                        person.getCurrentSpace() != null ? person.getCurrentSpace().getSpaceId() : null // 假设 Space 也用自定义 Long 类型主键
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    /**
     * 根據空間 ID 查詢人員
     */
    @GetMapping("/by-space/{spaceId}")
    public ResponseEntity<List<PersonInfo>> getPersonsBySpace(@PathVariable Integer spaceId) {
        return ResponseEntity.ok(personService.getPersonsBySpaceId(spaceId));
    }

    /**
     * 新增人員（使用 DTO）
     */
    @PostMapping
    public ResponseEntity<PersonDTO> createPerson(@RequestBody PersonCreateRequest request) {
        PersonDTO newPerson = personService.createPerson(request);
        return ResponseEntity.ok(newPerson);
    }

    /**
     * 更新人員（使用 DTO）
     */
//    @PatchMapping("/{id}")
//    public ResponseEntity<PersonInfo> updatePerson(@PathVariable Integer id,
//                                                   @RequestBody PersonUpdateRequest request) {
//        return personService.updatePerson(id, request)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

    @PatchMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Integer id,
                                                  @RequestBody PersonUpdateRequest request) {
        return personService.updatePersonNode(id, request)
                .map(person -> new PersonDTO(
                        person.getPersonId(),
                        person.getPersonName(),
                        person.getCurrentSpace() != null ? person.getCurrentSpace().getSpaceId() : null
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    /**
     * 設定人員的空間（可傳 null 表示離開空間）
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
     * 刪除人員
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Integer id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}