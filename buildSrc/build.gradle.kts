plugins {
    `kotlin-dsl`
}

val tinyRemapperVersion = "0.13.1"

repositories {
    gradlePluginPortal()

    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net")
    }
}

dependencies {
    implementation("net.fabricmc:tiny-remapper:$tinyRemapperVersion")
}
