/**
 * Jenkins Script Console — export installed plugins as a Gradle version catalog.
 * Run this in Manage Jenkins → Script Console and save output to `gradle/jenkins.versions.toml`.
 */
import groovy.transform.Immutable
import java.time.Instant
import java.util.stream.Collectors
import jenkins.model.Jenkins

@Immutable class Plugin {
    String groupId, shortName, version
    String getTomlKey() { shortName }
}

final List<Plugin> plugins = Jenkins.get().pluginManager.plugins.stream()
    .filter { it.active && !it.deleted }
    .map { new Plugin(it.manifest.mainAttributes.getValue('Group-Id') ?: 'unknown', it.shortName, it.version) }
    .peek { if (it.groupId == 'unknown') System.err.println "WARNING: no Group-Id for $it.shortName" }
    .filter { it.groupId != 'unknown' }
    .sorted(Comparator.comparing { it.shortName })
    .collect(Collectors.toList())

final String versions = plugins.stream()
    .map { "${it.tomlKey} = \"${it.version}\"" }
    .collect(Collectors.joining('\n'))

final String libraries = plugins.stream()
    .map { "${it.tomlKey} = { module = \"${it.groupId}:${it.shortName}\", version.ref = \"${it.tomlKey}\" }" }
    .collect(Collectors.joining('\n'))

final String bundles = plugins.stream()
    .map { "    \"${it.tomlKey}\"" }
    .collect(Collectors.joining(',\n', '[\n', '\n]'))

print """\
# Generated from: ${Jenkins.get().rootUrl ?: 'unknown'}
# Jenkins: ${Jenkins.VERSION}
# Date: ${Instant.now()}
#
# 1. Save this file as: gradle/jenkins.versions.toml
#
# 2. Wire it in settings.gradle.kts:
#
# dependencyResolutionManagement {
#   versionCatalogs {
#     create("jenkinsPlugins") {
#       from(files("gradle/jenkins.versions.toml"))
#     }
#   }
# }
#
# 3. Use it in build.gradle.kts:
#
# sharedLibrary {
#   plugins {
#     // Declare individual plugins (hyphens become camelCase)
#     plugin(jenkinsPlugins.workflowJob)
#
#     // OR declare the entire bundle of all plugins
#     plugins(jenkinsPlugins.bundles.allPlugins)
#   }
# }

[versions]
$versions

[libraries]
$libraries

[bundles]
allPlugins = $bundles
"""
