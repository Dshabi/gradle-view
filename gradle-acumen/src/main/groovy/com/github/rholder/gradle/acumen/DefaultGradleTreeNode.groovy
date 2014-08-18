package com.github.rholder.gradle.acumen

import com.github.rholder.gradle.acumen.api.GradleTreeNode

class DefaultGradleTreeNode implements GradleTreeNode, Serializable {

    String name

    GradleTreeNode parent
    String group
    String id
    String version
    String nodeType
    String reason
    String requestedVersion
    List<GradleTreeNode> children = new ArrayList<GradleTreeNode>()

    boolean seenBefore = false

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof DefaultGradleTreeNode)) return false

        DefaultGradleTreeNode that = (DefaultGradleTreeNode) o

        if (group != that.group) return false
        if (id != that.id) return false
        if (name != that.name) return false
        if (nodeType != that.nodeType) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (group != null ? group.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + nodeType.hashCode()
        return result
    }
}
