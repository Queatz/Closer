import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
  id("com.github.triplet.play")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("io.objectbox")
    id("com.google.gms.google-services")
}

repositories {
    google()
    jcenter()
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "closer.vlllage.com.closer"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode = readVersionCode()
        versionName = "2.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    signingConfigs {
        create("release") {
            storeFile = file("../../../Secrets/android.jks")
            storePassword = "closertome"
            keyAlias = "closer"
            keyPassword = "closertome"
            enableV1Signing = true
            enableV2Signing = true
        }
        getByName("debug") {
            storeFile = file("../../../Secrets/android.jks")
            storePassword = "closertome"
            keyAlias = "closer"
            keyPassword = "closertome"
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            minifyEnabled(false)
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

play {
    serviceAccountCredentials.set(file("../../../Secrets/service-account-key.json"))
    track.set("internal")
}

dependencies {
    "implementation"("androidx.multidex:multidex:2.0.1")
    "implementation"("androidx.constraintlayout:constraintlayout:2.0.4") {
        exclude(group = "com.android.support")
    }
    "implementation"("androidx.appcompat:appcompat:1.3.0-beta01")
    "implementation"("androidx.core:core-ktx:1.3.2")
    "implementation"("com.google.android.material:material:1.3.0-rc01")
    "implementation"("com.google.android.gms:play-services-maps:17.0.0") {
        exclude(group = "com.android.support")
    }
    "implementation"("com.google.android.gms:play-services-location:17.0.0") {
        exclude(group = "com.android.support")
    }
    "implementation"(group = "com.google.maps.android", name = "android-maps-utils", version = "0.6.1")
    "implementation"("com.squareup.retrofit2:retrofit:2.9.0")
    "implementation"("com.squareup.retrofit2:converter-gson:2.9.0")
    "implementation"("com.squareup.retrofit2:adapter-rxjava2:2.3.0")
    "implementation"("com.google.code.gson:gson:2.8.6")
    "implementation"("io.reactivex.rxjava2:rxandroid:2.1.0")
    "implementation"("io.reactivex.rxjava2:rxjava:2.2.18")
    "implementation"("com.google.firebase:firebase-core:18.0.2")
    "implementation"("com.google.firebase:firebase-messaging:21.0.1")
    "implementation"("com.github.bumptech.glide:glide:4.11.0")
    "implementation"("jp.wasabeef:glide-transformations:4.1.0")
    "implementation"(group = "com.journeyapps", name = "zxing-android-embedded", version = "4.1.0")
    "implementation"("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.4")
    "implementation"("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    "implementation"("com.github.chrisbanes:PhotoView:2.3.0") {
        exclude(group = "com.android.support")
    }
    "implementation"("com.googlecode.libphonenumber:libphonenumber:8.9.2")
    "implementation"("com.vdurmont:emoji-java:5.1.1")
    "implementation"("com.luckycatlabs:SunriseSunsetCalculator:1.2")
    "implementation"("com.github.Queatz:Android-UX-Extras-Slide-Screen:0.2")
    "implementation"("com.github.Queatz:On:0.1.6.4")
    "implementation"(group = "com.github.haifengl", name = "smile-core", version = "1.5.2")
    "implementation"("io.objectbox:objectbox-kotlin:${rootProject.extra["objectboxVersion"]}")
    "implementation"("io.objectbox:objectbox-rxjava:${rootProject.extra["objectboxVersion"]}")
    "testImplementation"("junit:junit:4.12")
    "androidTestImplementation"("androidx.test:runner:1.3.0")
    "androidTestImplementation"("androidx.test.espresso:espresso-core:3.3.0")
    "implementation"(kotlin("stdlib-jdk8", rootProject.extra["kotlinVersion"] as String))

    "implementation"("org.webrtc:google-webrtc:${rootProject.extra["webrtcVersion"]}")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

fun readVersionCode(): Int {
    val versionPropsFile = file("../version.properties")
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()
        versionProps.load(versionPropsFile.inputStream())
        return (versionProps["versionCode"] as String).toInt()
    }

    throw GradleException("Could not read version.properties!")
}

fun incrementVersionCode(): Int {
    val versionPropsFile = file("../version.properties")
    if (versionPropsFile.canRead()) {
        val versionProps = Properties()
        versionProps.load(versionPropsFile.inputStream())
        val code = (versionProps["versionCode"] as String).toInt() + 1
        versionProps["versionCode"] = code.toString()
        versionProps.store(versionPropsFile.writer(), null)
        return code
    }

    throw GradleException("Could not read version.properties!")
}

task("prepareVersionCode").doLast {
    print("Version code updated to " + incrementVersionCode())
}

tasks.whenTaskAdded {
    if (name == "publishReleaseBundle") {
        dependsOn.add(tasks.findByName("prepareVersionCode"))
    }
}
