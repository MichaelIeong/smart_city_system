package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.index.Index;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImportFileParser {
    public static Index parseIndex(String json) throws ParseException{
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX").create();
            return gson.fromJson(json, Index.class);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public static Meta parseMeta(String json) throws ParseException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, Meta.class);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public static Index parseIndex(Path path) throws IOException, ParseException {
        String json = Files.readString(path);
        return parseIndex(json);
    }

    public static Meta parseMeta(Path path) throws IOException, ParseException {
        String json = Files.readString(path);
        return parseMeta(json);
    }
}
