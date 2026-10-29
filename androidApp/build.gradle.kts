import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val debugApiBaseUrl = providers.gradleProperty("API_BASE_URL_DEBUG")
    .orElse(providers.environmentVariable("API_BASE_URL_DEBUG"))
    .orElse("http://10.0.2.2:8080/api/v1")
val releaseApiBaseUrl = providers.gradleProperty("API_BASE_URL_RELEASE")
    .orElse(providers.environmentVariable("API_BASE_URL_RELEASE"))

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.al4apps.chatshop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.al4apps.chatshop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${debugApiBaseUrl.get()}\"",
            )
        }
        release {
            isMinifyEnabled = false
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${releaseApiBaseUrl.orNull.orEmpty()}\"",
            )
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

if (gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }) {
    val releaseUrl = releaseApiBaseUrl.orNull
    check(!releaseUrl.isNullOrBlank()) {
        "API_BASE_URL_RELEASE is required for release builds. " +
            "Pass -PAPI_BASE_URL_RELEASE=https://<host>/api/v1 or set the environment variable."
    }
    check(releaseUrl.startsWith("https://")) {
        "API_BASE_URL_RELEASE must use https://"
    }
    check(!releaseUrl.contains("localhost", ignoreCase = true) && !releaseUrl.contains("10.0.2.2")) {
        "API_BASE_URL_RELEASE must not point to a development host"
    }
}