package com.github.rholder.gradle.acumen

import com.github.rholder.gradle.acumen.api.AcumenTreeModel
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

import javax.inject.Inject

class GradleAcumenPlugin implements Plugin<Project> {
    final ToolingModelBuilderRegistry registry;

    @Inject
    public GradleAcumenPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    void apply(Project project) {
        registry.register(new AcumenToolingModelBuilder())
    }

    private static class AcumenToolingModelBuilder implements ToolingModelBuilder {

        static DefaultGradleTreeNode generateProjectTree(Project project) {
            DefaultGradleTreeNode rootNode = new DefaultGradleTreeNode(
                    name: project.name,
                    group: project.group,
                    version: project.version,
                    nodeType: "project"
            )

            project.subprojects.each {
                rootNode.children.add(generateProjectTree(it))
            }

            //noinspection GroovyAssignabilityCheck
            project.configurations.each { Configuration conf ->
                DefaultGradleTreeNode configurationNode = new DefaultGradleTreeNode(
                        name: conf.name,
                        nodeType: "configuration"
                )

                // reprocessing existing deps can overflow the stack when there are cycles
                Set<DefaultGradleTreeNode> existingDeps = new LinkedHashSet<DefaultGradleTreeNode>()
                conf.incoming.resolutionResult.root.dependencies.each { DependencyResult dr ->
                    DefaultGradleTreeNode dependencyNode = resolveDependency(configurationNode, dr, existingDeps)
                    configurationNode.children.add(dependencyNode)
                }

                rootNode.children.add(configurationNode)
            }

            return rootNode
        }

        static DefaultGradleTreeNode resolveDependency(DefaultGradleTreeNode parentNode, DependencyResult result, Set<DefaultGradleTreeNode> existingDeps) {
            DefaultGradleTreeNode node = new DefaultGradleTreeNode()
            if (result instanceof ResolvedDependencyResult) {
                ResolvedDependencyResult r = result
                node.parent = parentNode
                node.group = r.selected.moduleVersion.getGroup()
                node.id = r.selected.moduleVersion.getName()
                node.version = r.selected.moduleVersion.getVersion()
                node.nodeType = "dependency"

                if (existingDeps.add(node)) {
                    // only process children if we haven't seen this dep before
                    r.selected.dependencies.each { DependencyResult subDep ->
                        DefaultGradleTreeNode childNode = resolveDependency(node, subDep, existingDeps)
                        node.children.add(childNode)
                    }
                } else {
                    node.seenBefore = true
                }
            } else {
                node.name = "Could not resolve " + result.requested.displayName
            }

            return node
        }

        public boolean canBuild(String modelName) {
            modelName.equals(AcumenTreeModel.class.getName())
        }

        public Object buildAll(String modelName, Project project) {
            new DefaultAcumenTreeModel(
                    nodeTree: generateProjectTree(project)
            )
        }
    }
}
