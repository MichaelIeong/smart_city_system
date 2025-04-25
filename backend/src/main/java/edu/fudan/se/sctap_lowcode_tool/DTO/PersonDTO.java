package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {

    private Integer id;
    private String personName;
    private Integer spaceId;
}
