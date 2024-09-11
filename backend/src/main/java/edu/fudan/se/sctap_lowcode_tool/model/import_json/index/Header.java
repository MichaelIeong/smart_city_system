package edu.fudan.se.sctap_lowcode_tool.model.import_json.index;

import java.util.Date;

public record Header(
        String Publisher,
        String Copyright,
        Date PublishTime,
        String EditorVersion
) {
}
