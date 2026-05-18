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
    String getTomlKey() {
        shortName
    }
}

final List<Plugin> plugins = Jenkins.get().pluginManager.plugins.stream()
        .filter { p -> p.active && !p.deleted }
        .map { p -> new Plugin(p.manifest.mainAttributes.getValue('Group-Id') ?: 'unknown', p.shortName, p.version) }
        .peek { p ->
            if (p.groupId == 'unknown') {
                System.err.println "WARNING: no Group-Id for $p.shortName"
            }
        }
        .filter { p -> p.groupId != 'unknown' }
        .sorted(Comparator.comparing { p -> p.shortName })
        .collect(Collectors.toList())

final int maxKeyLen = plugins.stream()
        .mapToInt { p -> p.tomlKey.length() }
        .max()
        .orElse(0)

final String versions = plugins.stream()
        .map { p -> "${p.tomlKey.padRight(maxKeyLen)} = \"${p.version}\"" }
        .collect(Collectors.joining('\n', '[versions]\n', ''))

final String libraries = plugins.stream()
        .map { p -> "${p.tomlKey.padRight(maxKeyLen)} = { module = \"${p.groupId}:${p.shortName}\", version.ref = \"${p.tomlKey}\" }" }
        .collect(Collectors.joining('\n', '[libraries]\n', ''))

final String allPlugins = plugins.stream()
        .map { p -> "    \"${p.tomlKey}\"" }
        .collect(Collectors.joining(',\n', '[bundles]\nallPlugins = [\n', '\n]'))

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
#     // Declare individual plugins (hyphens become dots)
#     plugin(jenkinsPlugins.lockable.resources)
#
#     // OR declare the entire bundle of all plugins
#     plugins(jenkinsPlugins.bundles.allPlugins)
#   }
# }

$versions

$libraries

$allPlugins
"""
