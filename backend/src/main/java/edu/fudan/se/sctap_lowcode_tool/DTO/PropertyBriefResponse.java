package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.PropertyInfo;

public record PropertyBriefResponse(
        Integer id,
        String propertyId,
        String propertyKey
) {
    public PropertyBriefResponse(PropertyInfo propertyInfo) {
        this(propertyInfo.getId(), propertyInfo.getPropertyId(), propertyInfo.getPropertyKey());
    }
}
