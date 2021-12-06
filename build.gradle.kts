buildscript {
    extra["kotlinVersion"] = "1.6.0"
    extra["objectboxVersion"] = "3.0.1"

    repositories {
        maven(url = "https://developer.huawei.com/repo/")
        mavenCentral()
        google()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        classpath("com.huawei.agconnect:agcp:1.6.1.300")
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("com.github.triplet.gradle:play-publisher:3.7.0")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("io.objectbox:objectbox-gradle-plugin:${rootProject.extra["objectboxVersion"]}")
        classpath(kotlin("gradle-plugin", version = rootProject.extra["kotlinVersion"] as String))
    }
}

allprojects {
    repositories {
        maven(url = "https://developer.huawei.com/repo/")
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
