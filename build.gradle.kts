buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("multiplatform") version "1.3.31"
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "http://dl.bintray.com/kotlin/ktor")
}

val ktorVersion = "1.0.1"
val logbackVersion = "1.2.3"
val gsonVersion = "2.8.5"
val exposedVersion = "0.13.7"
val sqliteJdbcVersion = "3.27.2.1"
val fuelVersion = "2.1.0"

kotlin {
    jvm()
    js()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("com.google.code.gson:gson:$gsonVersion")
                implementation("org.jetbrains.exposed:exposed:$exposedVersion")
                implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")
                implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
                implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")
                implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion")
            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

val webFolder = File(project.buildDir, "../src/jsMain/web")
val jsCompilations = kotlin.targets["js"].compilations
val jvmCompilations = kotlin.targets["jvm"].compilations

task("populateWebFolder") {
    dependsOn += "jsMainClasses"
    doLast {
        copy {
            from(jsCompilations["main"].output)
            from(kotlin.sourceSets["jsMain"].resources.srcDirs)
            jsCompilations["test"].compileDependencyFiles.forEach {
                if (it.exists() && !it.isDirectory) {
                    from(zipTree(it.absolutePath).matching {
                        include("*.js")
                    })
                }
            }
            into(webFolder)
        }
    }
}

tasks["jsJar"].dependsOn += "populateWebFolder"

tasks.create<JavaExec>("run") {
    dependsOn += "jvmMainClasses"
    dependsOn += "jsJar"
    main = "de.athox.planning.MainKt"
    classpath = jvmCompilations["main"].output.allOutputs + configurations["jvmRuntimeClasspath"]
    args = mutableListOf()
}
