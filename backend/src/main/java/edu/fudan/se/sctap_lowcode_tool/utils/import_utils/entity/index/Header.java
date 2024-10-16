package edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.index;

import java.util.Date;

public record Header(
        String Publisher,
        String Copyright,
        Date PublishTime,
        String EditorVersion
) {
}
