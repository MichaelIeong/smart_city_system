package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.MetaTreeNode;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.index.Index;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.meta.Meta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    /**
     * Parses the meta tree starting from the given base path and the relative path of the root meta file.
     *
     * @param basePath               the base path where the meta files are located
     * @param relativePathOfMetaFile the relative path of the root meta file
     * @return the root node of the parsed meta tree
     * @throws IOException    if an I/O error occurs while reading the meta files
     * @throws ParseException if a parsing error occurs while reading the meta files
     */
    public static MetaTreeNode parseMetaTree(Path basePath, Path relativePathOfMetaFile) throws IOException, ParseException {

        Queue<MetaTreeNode> waitingQueue = new LinkedList<>();
        Set<Path> visitedPaths = new HashSet<>(); // 记录已访问的路径
        MetaTreeNode root = new MetaTreeNode(relativePathOfMetaFile, null);
        waitingQueue.add(root);

        try {
            while (!waitingQueue.isEmpty()) {
                MetaTreeNode currentNode = waitingQueue.poll();
                Path currentMetaPathRelative = currentNode.getMetaFilePath();
                if (visitedPaths.contains(currentMetaPathRelative)) {
                    throw new ParseException(
                            "Duplicated parents detected in meta files, " +
                                    "this file has at least two parents: " +
                                    currentMetaPathRelative + "."
                    );
                } else {
                    visitedPaths.add(currentMetaPathRelative);
                }

                // Parse the meta file and set the meta to the current node
                Path currentMetaPath = basePath.resolve(currentMetaPathRelative);
                Meta currentMeta = ImportFileParser.parseMeta(currentMetaPath);
                currentNode.setMeta(currentMeta);

                // Parse the children of the current meta and add them to the current node
                List<MetaTreeNode> children = currentMeta.getChildrenSpaces().stream()
                        .map(Meta::MetaUri)
                        .map(Path::of)
                        .map(path -> new MetaTreeNode(path, currentNode))
                        .toList();
                currentNode.setChildren(children);

                // Add the children to the waiting queue
                waitingQueue.addAll(children);
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
        return root;
    }

    /**
     * Parses the meta tree starting from the given base path and use the index.json in the base path.
     *
     * @param indexFileBasePath the base path where the index file is located
     * @return the root node of the parsed meta tree
     * @throws IOException    if an I/O error occurs while reading the index or meta files
     * @throws ParseException if a parsing error occurs while reading the index or meta files
     */
    public static MetaTreeNode parseMetaTree(Path indexFileBasePath) throws IOException, ParseException {
        try {
            Path indexPath = indexFileBasePath.resolve("index.json");
            Index index = ImportFileParser.parseIndex(indexPath);
            Path rootMetaRelativePath = Path.of(index.RootSpace().MetaUri());
            return parseMetaTree(indexFileBasePath, rootMetaRelativePath);
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
