package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;

import java.util.Iterator;

// TODO: remove this file, move the method into ProjectService

public interface ImportService {

    void importMetaRecursively(Iterator<Meta> metaIterator, String projectName);

}
