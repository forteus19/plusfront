plugins {
    `kotlin-dsl`
}

val tinyRemapperVersion = "0.13.1"
val vineflowerVersion = "1.12.0"

val mappingIoVersion = "0.8.0"

repositories {
    gradlePluginPortal()

    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net")
    }
}

dependencies {
    implementation("net.fabricmc:tiny-remapper:$tinyRemapperVersion")
    implementation("org.vineflower:vineflower:$vineflowerVersion")

    // hack to get around the wrong mapping io version being used
    implementation("net.fabricmc:mapping-io:$mappingIoVersion")
}
