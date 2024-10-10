package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

import edu.fudan.se.sctap_lowcode_tool.model.import_json.index.Index;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * The {@code MetaBFSIterator} class implements a breadth-first search (BFS) iterator for traversing
 * meta files starting from a given index file.
 * It reads the index file to determine the root meta file
 * and then iterates through all meta files in a BFS manner.
 */
public class MetaBFSIterator implements Iterator<Meta> {

    private final Queue<Meta> queue = new LinkedList<>();

    /**
     * Constructs a new {@code MetaBFSIterator} instance starting from the specified base path and relative path.
     *
     * @param basePath the base path where the index file is located
     * @param relativePath the relative path to the root meta file
     * @throws IOException if an I/O error occurs while reading the index or meta files
     * @throws ParseException if a parsing error occurs while reading the index or meta files
     */
    public MetaBFSIterator(Path basePath, Path relativePath) throws IOException, ParseException {
        try {
//            Queue<Path> waitingQueue = new LinkedList<>();
//            waitingQueue.add(relativePath);
//
//            while (!waitingQueue.isEmpty()) {
//                Path currentMetaPathRelative = waitingQueue.poll();
//                Path currentMetaPath = basePath.resolve(currentMetaPathRelative);
//                Meta currentMeta = ImportFileParser.parseMeta(currentMetaPath);
//                queue.add(currentMeta);
//                List<Path> childrenMetaPaths = currentMeta.getChildrenSpaces().stream()
//                        .map(Meta::MetaUri)
//                        .map(Path::of)
//                        .toList();
//                waitingQueue.addAll(childrenMetaPaths);
//            }
            Queue<Path> waitingQueue = new LinkedList<>();
            Set<Path> visitedPaths = new HashSet<>(); // 记录已访问的路径
            waitingQueue.add(relativePath);

            while (!waitingQueue.isEmpty()) {
                Path currentMetaPathRelative = waitingQueue.poll();
                if (visitedPaths.contains(currentMetaPathRelative)) {
                    continue;
                }
                visitedPaths.add(currentMetaPathRelative);

                Path currentMetaPath = basePath.resolve(currentMetaPathRelative);
                String metaPathString = currentMetaPath.toString();
                String modifiedPathString = metaPathString.replaceAll("/Floor[^/]*", "");
                Path modifiedRootMetaRelativePath = Path.of(modifiedPathString);
                Meta currentMeta = ImportFileParser.parseMeta(modifiedRootMetaRelativePath);
                queue.add(currentMeta);

                List<Path> childrenMetaPaths = currentMeta.getChildrenSpaces().stream()
                        .map(Meta::MetaUri)
                        .map(Path::of)
                        .toList();

                for (Path childPath : childrenMetaPaths) {
                    if (!visitedPaths.contains(childPath)) {
                        waitingQueue.add(childPath);
                    }
                }
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Creates a new {@code MetaBFSIterator} using the `index.json` located at the specified base path.
     *
     * @param basePath the base path where the index file is located
     * @return a new {@code MetaBFSIterator} instance
     * @throws IOException    if an I/O error occurs while reading the index or meta files
     * @throws ParseException (final fallback) if a parsing error occurs while reading the index or meta files
     */
    public static MetaBFSIterator usingIndex(Path basePath) throws IOException, ParseException {
        try {
            Path indexPath = basePath.resolve("index.json");
            Index index = ImportFileParser.parseIndex(indexPath);
            Path rootMetaRelativePath = Path.of(index.RootSpace().MetaUri());
            return new MetaBFSIterator(basePath, rootMetaRelativePath);
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public Meta next() {
        return queue.poll();
    }
}
