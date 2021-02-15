// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra["kotlinVersion"] = "1.4.30"
    extra["objectboxVersion"] = "3.0.0-alpha2"
    extra["webrtcVersion"] = "1.0.30039"

    repositories {
        google()
        jcenter()
        maven(url ="https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-beta04")
        classpath("com.github.triplet.gradle:play-publisher:3.2.0-agp4.2")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("io.objectbox:objectbox-gradle-plugin:${rootProject.extra["objectboxVersion"]}")
        classpath(kotlin("gradle-plugin", version = rootProject.extra["kotlinVersion"] as String))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url ="https://jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}