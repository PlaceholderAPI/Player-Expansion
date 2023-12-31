plugins {
    java
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

group = "at.helpch.placeholderapi.expansion"
version = "3.0.0"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")
}

tasks {
    shadowJar {
        archiveFileName.set("PAPI-Expansion-Player-${project.version}.jar")
    }
}