plugins {
    id("net.neoforged.moddev") version "2.0.141"
    id("idea")
}

group = "dev.vuis"
version = "0.1.4"

val modId = "pf"
val modName = "PlusFront"

val loaderVersionRange = "[1,)"
val neoforgeVersion = "21.1.233"

val minecraftVersion = "1.21.1"

val parchmentMappingsVersion = "2024.11.17"
val parchmentMinecraftVersion = "1.21.1"

val blockfrontVersion = "0.9.0.13b"
val blockfrontModrinthVersion = "HTLp5q91"

val geckolibVersion = "4.7.3"
val veilVersion = "4.3.0"
val voicechatApiVersion = "2.6.20"

val blockfrontOriginal by configurations.creating

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "GeckoLib"
                url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven")
            }
        }
        filter {
            includeGroup("software.bernie.geckolib")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "BlameJared"
                url = uri("https://maven.blamejared.com")
            }
        }
        filter {
            includeGroup("foundry.veil")
            includeGroup("gg.moonflower")
            includeGroup("io.github.ocelot")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "maxhenkel"
                url = uri("https://maven.maxhenkel.de/releases")
            }
        }
        filter {
            includeGroup("de.maxhenkel.voicechat")
        }
    }
}

neoForge {
    version = neoforgeVersion

    validateAccessTransformers = true

    parchment {
        mappingsVersion = parchmentMappingsVersion
        minecraftVersion = parchmentMinecraftVersion
    }

    mods {
        create(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    blockfrontOriginal("maven.modrinth:blockfront:$blockfrontModrinthVersion")

    // declared manually for sources
    compileOnly("software.bernie.geckolib:geckolib-neoforge-$minecraftVersion:$geckolibVersion")
    compileOnly("foundry.veil:veil-neoforge-$minecraftVersion:$veilVersion") {
        exclude(group = "maven.modrinth")
        exclude(group = "me.fallenbreath")
    }

    // optional dependencies
    compileOnly("de.maxhenkel.voicechat:voicechat-api:$voicechatApiVersion")
}

// https://github.com/ThatCuteOne/bfapi/blob/docker/build.gradle.kts
val extractBlockfrontLibrariesTask = tasks.register<Copy>("extractBlockfrontLibraries") {
    dependsOn(blockfrontOriginal)

    from(zipTree(blockfrontOriginal.resolve().first()))
    include("META-INF/jarjar/*.jar")
    exclude(
        "META-INF/jarjar/geckolib*.jar",
        "META-INF/jarjar/veil*.jar"
    )
    into("build/extracted/bf-jarjar")
    eachFile {
        path = name
    }
}

val blockfrontLibraries = files(
    extractBlockfrontLibrariesTask.map {
        fileTree(it.destinationDir) {
            include("*.jar")
        }
    }
)

val remapBlockfrontTask = tasks.register<RemapTask>("remapBlockfront") {
    dependsOn(blockfrontOriginal, extractBlockfrontLibrariesTask)

    input = blockfrontOriginal.resolve().first()
    output = layout.buildDirectory.file("generated/bf-remapped.jar")
    mappings = file("bf-mappings.tiny")
    classpath.from(extractBlockfrontLibrariesTask)
    from = "official"
    to = "named"
    nonClassFiles = false
    mixinExtension = false
}

val decompileBlockfrontTask = tasks.register<DecompileTask>("decompileBlockfront") {
    dependsOn(remapBlockfrontTask)

    input.set(remapBlockfrontTask.get().outputs.files.first())
    output.set(layout.buildDirectory.file("generated/bf-sources.jar"))
}

dependencies {
    compileOnly(files(remapBlockfrontTask))
    compileOnly(blockfrontLibraries)
}

neoForge {
    ideSyncTask(extractBlockfrontLibrariesTask)
    ideSyncTask(remapBlockfrontTask)
    ideSyncTask(decompileBlockfrontTask)
}

val generateModMetadataTask = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_version" to project.version,
        "mod_name" to modName,
        "neoforge_version" to neoforgeVersion,
        "minecraft_version" to minecraftVersion,
        "blockfront_version" to blockfrontVersion,
        "voicechat_api_version" to voicechatApiVersion
    )

    inputs.properties(replaceProperties)
    expand(replaceProperties)

    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main {
    resources {
        srcDir(generateModMetadataTask)
    }
}

val remapModTask = tasks.register<RemapTask>("remapMod") {
    group = "build"

    dependsOn(tasks["jar"], remapBlockfrontTask)

    input = tasks["jar"].outputs.files.first()
    output = layout.buildDirectory.file("libs/${base.archivesName.get()}-${project.version}-bfobf.jar")
    mappings = file("bf-mappings.tiny")
    classpath.from(remapBlockfrontTask.get().outputs.files.first())
    from = "named"
    to = "official"
    nonClassFiles = true
    mixinExtension = true
}

val createLatestJarSymlinkTask = tasks.register<SymlinkTask>("createLatestJarSymlink") {
    dependsOn(remapModTask)

    target.set(remapModTask.get().outputs.files.first().absolutePath)
    link.set(layout.buildDirectory.file("libs/${base.archivesName.get()}-latest-bfobf.jar"))
}

tasks.build {
    dependsOn(remapModTask, createLatestJarSymlinkTask)
}

idea {
    module {
        isDownloadSources = true
    }
}
