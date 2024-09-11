package edu.fudan.se.sctap_lowcode_tool.model.import_json.meta;

import java.util.List;

public record Meta(
        String Id,
        String Name,
        int Type,
        String GraphicId,
        String GraphicUri,
        String MetaUri,
        Coordinate GraphicPosition,
        Coordinate GraphicRotation,
        Coordinate GraphicScale,
        boolean IsDevice,
        String DeviceId,
        String DeviceName,
        String DeviceType,
        List<Meta> Children,
        List<String> Tags, // TODO: confirm
        List<CustomerThing> CustomerPropertys,
        List<CustomerThing> CustomerEvents,
        List<CustomerThing> CustomerServices,
        Coordinate GeoLocation
){

    public List<Meta> getChildrenSpaces(){
        return this.Children().stream()
                .filter(meta -> meta.MetaUri() != null)
                .toList();
    }

    public List<Meta> getChildrenDevices(){
        return this.Children().stream()
                .filter(Meta::IsDevice)
                .toList();
    }
}

