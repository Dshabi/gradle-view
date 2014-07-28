package com.github.rholder.gradle.acumen

interface GradleTreeNode {

    String getName()

    GradleTreeNode getParent()
    String getGroup()
    String getId()
    String getVersion()
    List<GradleTreeNode> getChildren()

    boolean getSeenBefore()
}
