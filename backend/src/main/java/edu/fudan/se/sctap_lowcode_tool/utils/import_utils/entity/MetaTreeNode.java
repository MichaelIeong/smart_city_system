package edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity;

import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.entity.meta.Meta;
import jakarta.annotation.Nonnull;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Data
public class MetaTreeNode implements Iterable<MetaTreeNode> {
    private Path metaFilePath;
    private Meta meta;
    private MetaTreeNode parent;
    private List<MetaTreeNode> children;

    public MetaTreeNode(
            Path metaFilePath,
            Meta meta,
            MetaTreeNode parent,
            List<MetaTreeNode> children
    ) {
        this.metaFilePath = metaFilePath;
        this.meta = meta;
        this.parent = parent;
        this.children = children;
    }

    public MetaTreeNode(Path metaFilePath, MetaTreeNode parent) {
        this(metaFilePath, null, parent, new ArrayList<>());
    }

    public Optional<MetaTreeNode> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public @Nonnull Iterator<MetaTreeNode> iterator() {
        return new BFSIterator();
    }

    private class BFSIterator implements Iterator<MetaTreeNode> {
        private final List<MetaTreeNode> queue = new ArrayList<>();

        public BFSIterator() {
            queue.add(MetaTreeNode.this);
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public MetaTreeNode next() {
            MetaTreeNode node = queue.removeFirst();
            if (node.children != null) {
                queue.addAll(node.children);
            }
            return node;
        }
    }
}
