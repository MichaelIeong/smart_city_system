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

                // 如果该路径已访问过，则跳过
                if (visitedPaths.contains(currentMetaPathRelative)) {
                    continue;
                }

                // 标记为已访问
                visitedPaths.add(currentMetaPathRelative);

                Path currentMetaPath = basePath.resolve(currentMetaPathRelative);
                // 1. 将 Path 转换为字符串
                String metaPathString = currentMetaPath.toString();

                // 2. 使用正则表达式去掉 "/Floor" 到下一个 "/" 的部分
                String modifiedPathString = metaPathString.replaceAll("/Floor[^/]*", "");

                // 3. 将修改后的字符串转换回 Path 对象
                Path modifiedRootMetaRelativePath = Path.of(modifiedPathString);

                // 解析 Meta 文件
                Meta currentMeta = ImportFileParser.parseMeta(modifiedRootMetaRelativePath);
                queue.add(currentMeta);

                // 获取子 Meta 文件路径
                List<Path> childrenMetaPaths = currentMeta.getChildrenSpaces().stream()
                        .map(Meta::MetaUri)
                        .map(Path::of)
                        .toList();

                // 将未访问过的子路径加入队列
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
