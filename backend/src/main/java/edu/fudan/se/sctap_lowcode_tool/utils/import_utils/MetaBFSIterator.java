package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

import edu.fudan.se.sctap_lowcode_tool.model.import_json.index.Index;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MetaBFSIterator implements Iterator<Meta> {

    private final Queue<Meta> queue = new LinkedList<>();

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

    public MetaBFSIterator(Path basePath, Path relativePath) throws IOException, ParseException {
        try {
            Queue<Path> waitingQueue = new LinkedList<>();
            waitingQueue.add(relativePath);

            while (!waitingQueue.isEmpty()) {
                Path currentMetaPathRelative = waitingQueue.poll();
                Path currentMetaPath = basePath.resolve(currentMetaPathRelative);
                Meta currentMeta = ImportFileParser.parseMeta(currentMetaPath);
                queue.add(currentMeta);
                List<Path> childrenMetaPaths = currentMeta.getChildrenSpaces().stream()
                        .map(Meta::MetaUri)
                        .map(Path::of)
                        .toList();
                waitingQueue.addAll(childrenMetaPaths);
            }
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
