plugins {
    id("dev.architectury.loom-remap") version "1.17-SNAPSHOT"
}

group = "dev.vuis"
version = "0.1.7"

val modId = "pf"
val modName = "PlusFront"

val minecraftVersion = "1.21.1"

val parchmentVersion = "2024.11.17"

val loaderVersionRange = "[1,)"
val neoforgeVersion = "21.1.233"

val blockfrontVersion = "0.9.0.14b"
val blockfrontModrinthVersion = "yh1JlKob"

val geckolibVersion = "4.7.3"
val veilVersion = "4.3.0"
val sodiumVersion = "0.8.12+mc1.21.1"
val voicechatApiVersion = "2.6.20"

val mcdevAnnotationsVersion = "2.1.0"

val blockfrontOriginal = configurations.create("blockfrontOriginal")

repositories {
    mavenCentral()

    maven {
        name = "NeoForge"
        url = uri("https://maven.neoforged.net/releases")
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "ParchmentMC"
                url = uri("https://maven.parchmentmc.org")
            }
        }
        filter {
            includeGroup("org.parchmentmc.data")
        }
    }
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

dependencies {
    minecraft("net.minecraft:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    neoForge("net.neoforged:neoforge:$neoforgeVersion")

    blockfrontOriginal("maven.modrinth:blockfront:$blockfrontModrinthVersion")

    // declared manually for sources
    modCompileOnly("software.bernie.geckolib:geckolib-neoforge-$minecraftVersion:$geckolibVersion")
    modCompileOnly("foundry.veil:veil-neoforge-$minecraftVersion:$veilVersion") {
        exclude(group = "maven.modrinth")
        exclude(group = "me.fallenbreath")
    }

    // optional dependencies
    modCompileOnly("de.maxhenkel.voicechat:voicechat-api:$voicechatApiVersion")

    compileOnly("com.demonwav.mcdev:annotations:$mcdevAnnotationsVersion")
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
    dependsOn(remapBlockfrontTask, extractBlockfrontLibrariesTask)

    input.set(remapBlockfrontTask.get().outputs.files.first())
    libraries.from(configurations["compileClasspath"], blockfrontLibraries)
    output.set(layout.buildDirectory.file("generated/bf-sources.jar"))
}

dependencies {
    modCompileOnly(files(remapBlockfrontTask))
    compileOnly(blockfrontLibraries)
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

    dependsOn(tasks["remapJar"], remapBlockfrontTask)

    input = tasks["remapJar"].outputs.files.first()
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

loom {
    runs {
        remove(getByName("client"))
        remove(getByName("server"))
    }
}
