package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.PersonCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.PersonInfo;
import edu.fudan.se.sctap_lowcode_tool.service.PersonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/person")
@Tag(name = "PersonController", description = "人員状态控制器")
public class PersonController {

    @Autowired
    private PersonService personService;

    /**
     * 根據 ID 獲取單個人員
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonInfo> getPerson(@PathVariable Integer id) {
        return personService.getPersonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查詢所有人員
     */
    @GetMapping
    public ResponseEntity<List<PersonInfo>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
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
    public ResponseEntity<PersonInfo> createPerson(@RequestBody PersonCreateRequest request) {
        return ResponseEntity.ok(personService.createPerson(request));
    }

    /**
     * 更新人員（使用 DTO）
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PersonInfo> updatePerson(@PathVariable Integer id,
                                                   @RequestBody PersonUpdateRequest request) {
        return personService.updatePerson(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 將某人設為離開空間（currentSpace 設為 null）
     */
    @PostMapping("/{id}/leave-space")
    public ResponseEntity<PersonInfo> leaveSpace(@PathVariable Integer id) {
        Optional<PersonInfo> result = personService.removePersonFromSpace(id);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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