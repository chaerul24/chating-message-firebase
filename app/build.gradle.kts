import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.modern.chating"
    compileSdk = 35

    val localProps = Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localProps.load(FileInputStream(localFile))
    }
    defaultConfig {
        applicationId = "com.modern.chating"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TWILIO_ACCESS_TOKEN", "\"${localProps.getProperty("TWILIO_ACCESS_TOKEN", "")}\"")
        buildConfigField("String", "TWILIO_SID", "\"${localProps.getProperty("TWILIO_SID", "")}\"")
        buildConfigField("String", "TWILIO_PHONE_NUMBER", "\"${localProps.getProperty("TWILIO_PHONE_NUMBER", "")}\"")
        buildConfigField("String", "_U", "\"${localProps.getProperty("BASE_URL", "")}\"")
        buildConfigField("String", "_EMAIL_SENDER", "\"${localProps.getProperty("BASE_URL_EMAIL_SENDER", "")}\"")
        buildConfigField("String", "_VERIFY_OTP", "\"${localProps.getProperty("BASE_URL_EMAIL_VERIFY", "")}\"")
        buildConfigField("String", "_CALL_STATUS", "\"${localProps.getProperty("BASE_URL_CALL_STATUS", "")}\"")
        buildConfigField("String", "_CALL_END", "\"${localProps.getProperty("BASE_URL_CALL_END", "")}\"")
        buildConfigField("String", "_INDEX", "\"${localProps.getProperty("BASE_URL_INDEX", "")}\"")
        buildConfigField("String", "_TOKEN", "\"${localProps.getProperty("BASE_URL_TOKEN", "")}\"")
        buildConfigField("String", "IMAGE", "\"${localProps.getProperty("BASE_URL_IAMGE", "")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "TWILIO_ACCESS_TOKEN", "\"${localProps.getProperty("TWILIO_ACCESS_TOKEN", "")}\"")
            buildConfigField("String", "TWILIO_SID", "\"${localProps.getProperty("TWILIO_SID", "")}\"")
            buildConfigField("String", "TWILIO_PHONE_NUMBER", "\"${localProps.getProperty("TWILIO_PHONE_NUMBER", "")}\"")
            buildConfigField("String", "_U", "\"${localProps.getProperty("BASE_URL", "")}\"")
            buildConfigField("String", "_EMAIL_SENDER", "\"${localProps.getProperty("BASE_URL_EMAIL_SENDER", "")}\"")
            buildConfigField("String", "IMAGE", "\"${localProps.getProperty("BASE_URL_IAMGE", "")}\"")

        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.google.firebase:firebase-messaging:23.0.0")
    implementation(libs.osmdroid.android)
    implementation(libs.preference)
    implementation(libs.play.services.location)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("org.apache.httpcomponents:httpclient-android:4.3.5")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.15.1")
    implementation(libs.video.android)
    implementation(libs.play.services.vision.common)
    implementation(libs.ccp)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}