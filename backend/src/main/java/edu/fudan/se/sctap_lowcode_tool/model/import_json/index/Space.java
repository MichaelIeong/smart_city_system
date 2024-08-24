package edu.fudan.se.sctap_lowcode_tool.model.import_json.index;

import java.util.List;

public record Space(
        String Id,
        String Name,
        String Description,
        int Type,
        String GraphicUri,
        String MetaUri,
        List<Space> SubSpaces
) {

}
