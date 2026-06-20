plugins {
    id("net.neoforged.moddev") version "2.0.141"
}

group = "dev.vuis"
version = "0.1.0"

val modId = "pf"
val modName = "PlusFront"

val loaderVersionRange = "[1,)"
val neoforgeVersion = "21.1.233"
val minecraftVersionRange = "[1.21.1]"
val blockfrontVersion = "0.9.0.0b"

val tinyRemapperVersion = "0.13.1"

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
}

neoForge {
    version = neoforgeVersion

    validateAccessTransformers = true

    mods {
        create(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    blockfrontOriginal("maven.modrinth:blockfront:$blockfrontVersion")
}

// https://github.com/ThatCuteOne/bfapi/blob/docker/build.gradle.kts
val extractBlockfrontLibrariesTask = tasks.register<Copy>("extractBlockfrontLibraries") {
    dependsOn(blockfrontOriginal)

    from(zipTree(blockfrontOriginal.resolve().first()))
    include("META-INF/jarjar/*.jar")
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

val remapBlockfrontTask = tasks.register<Remap>("remapBlockfront") {
    dependsOn(blockfrontOriginal, extractBlockfrontLibrariesTask)

    input = blockfrontOriginal.resolve().first()
    output = file("build/generated/bf-remapped.jar")
    mappings = file("bf-mappings.tiny")
    classpath.from(extractBlockfrontLibrariesTask)
    from = "official"
    to = "named"
    nonClassFiles = false
    mixinExtension = false
}

neoForge {
    ideSyncTask(extractBlockfrontLibrariesTask)
    ideSyncTask(remapBlockfrontTask)
}

dependencies {
    compileOnly(files(remapBlockfrontTask))
    compileOnly(blockfrontLibraries)
}

val generateModMetadataTask = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_version" to project.version,
        "mod_name" to modName,
        "neoforge_version" to neoforgeVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "blockfront_version" to blockfrontVersion
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

val remapModTask = tasks.register<Remap>("remapMod") {
    group = "build"

    dependsOn(tasks["jar"], remapBlockfrontTask)

    input = tasks["jar"].outputs.files.first()
    output = file("build/libs/${base.archivesName.get()}-${project.version}-bfobf.jar")
    mappings = file("bf-mappings.tiny")
    classpath.from(remapBlockfrontTask.get().outputs.files.first())
    from = "named"
    to = "official"
    nonClassFiles = true
    mixinExtension = true
}

tasks.build {
    dependsOn(remapModTask)
}
