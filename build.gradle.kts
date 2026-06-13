import java.net.URI

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "2.4.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.elysium.servlogger"

version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.51.0.0")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks {
    runServer {
        minecraftVersion("1.21.10")

        doFirst {
            val pluginsDir = file("run/plugins")
            pluginsDir.mkdirs()

            val viaVersionJar = pluginsDir.resolve("ViaVersion-5.5.1.jar")
            if (!viaVersionJar.exists()) {
                println("Скачиваем ViaVersion...")
                URI("https://hangarcdn.papermc.io/plugins/ViaVersion/ViaVersion/versions/5.6.0/PAPER/ViaVersion-5.6.0.jar").toURL()
                    .openStream().use { input ->
                        viaVersionJar.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                println("ViaVersion скачан в run/plugins")
            }

            val placeholderAPI = pluginsDir.resolve("PlaceholderAPI-2.11.7.jar")
            if (!placeholderAPI.exists()) {
                println("Скачиваем PlaceholderAPI...")
                URI("https://cdn.modrinth.com/data/lKEzGugV/versions/sn9LYZkM/PlaceholderAPI-2.11.7.jar").toURL()
                    .openStream().use { input ->
                        placeholderAPI.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                println("PlaceholderAPI скачан в run/plugins")
            }
        }
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }

    val sourcesJar by registering(Jar::class) {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()

            artifact(tasks.named("sourcesJar"))
        }
    }
    repositories {
        maven {
            url = rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
    }
}
