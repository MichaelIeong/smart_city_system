package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.index.Index;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for parsing index and meta files.
 * Read the JSON string or file content and parse it into an {@code Index} or {@code Meta} object.
 */
public class ImportFileParser {

    /**
     * Parses the given JSON string into an {@code Index} object.
     *
     * @param json the JSON string to parse
     * @return the parsed {@code Index} object
     * @throws ParseException if a parsing error occurs
     */
    public static Index parseIndex(String json) throws ParseException {
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX").create();
            return gson.fromJson(json, Index.class);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    /**
     * Parses the given JSON string into a {@code Meta} object.
     *
     * @param json the JSON string to parse
     * @return the parsed {@code Meta} object
     * @throws ParseException if a parsing error occurs
     */
    public static Meta parseMeta(String json) throws ParseException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, Meta.class);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    /**
     * Reads the JSON content from the given file path and parses it into an {@code Index} object.
     *
     * @param path the file path to read the JSON content from
     * @return the parsed {@code Index} object
     * @throws IOException    if an I/O error occurs while reading the file
     * @throws ParseException if a parsing error occurs
     */
    public static Index parseIndex(Path path) throws IOException, ParseException {
        String json = Files.readString(path);
        return parseIndex(json);
    }

    /**
     * Reads the JSON content from the given file path and parses it into a {@code Meta} object.
     *
     * @param path the file path to read the JSON content from
     * @return the parsed {@code Meta} object
     * @throws IOException if an I/O error occurs while reading the file
     * @throws ParseException if a parsing error occurs
     */
    public static Meta parseMeta(Path path) throws IOException, ParseException {
        String json = Files.readString(path);
        return parseMeta(json);
    }
}
