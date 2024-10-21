package edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.index;

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
