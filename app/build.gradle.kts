plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val releaseStorePath = providers.gradleProperty("FLUID_RELEASE_KEYSTORE_PATH").orNull
    ?: System.getenv("FLUID_RELEASE_KEYSTORE_PATH")
val releaseStorePassword = providers.gradleProperty("FLUID_RELEASE_STORE_PASSWORD").orNull
    ?: System.getenv("FLUID_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = providers.gradleProperty("FLUID_RELEASE_KEY_ALIAS").orNull
    ?: System.getenv("FLUID_RELEASE_KEY_ALIAS")
val releaseKeyPassword = providers.gradleProperty("FLUID_RELEASE_KEY_PASSWORD").orNull
    ?: System.getenv("FLUID_RELEASE_KEY_PASSWORD")
val releaseStoreFile = releaseStorePath?.let { file(it) } ?: rootProject.file("release.jks")
val missingReleaseSigningInputs = listOf(
    "FLUID_RELEASE_KEYSTORE_PATH" to releaseStorePath,
    "FLUID_RELEASE_STORE_PASSWORD" to releaseStorePassword,
    "FLUID_RELEASE_KEY_ALIAS" to releaseKeyAlias,
    "FLUID_RELEASE_KEY_PASSWORD" to releaseKeyPassword
).filter { it.second.isNullOrBlank() }.map { it.first }

gradle.taskGraph.whenReady {
    if (allTasks.any { it.name == "assembleRelease" || it.name == "bundleRelease" }) {
        check(missingReleaseSigningInputs.isEmpty() && releaseStoreFile.isFile) {
            "Release signing requires a valid keystore and protected values for: ${missingReleaseSigningInputs.joinToString()}. " +
                "Set FLUID_RELEASE_KEYSTORE_PATH, FLUID_RELEASE_STORE_PASSWORD, FLUID_RELEASE_KEY_ALIAS, and FLUID_RELEASE_KEY_PASSWORD."
        }
    }
}

android {
    namespace = "com.smmiqbal.fluidtracker"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.smmiqbal.fluidtracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storePassword = releaseStorePassword.orEmpty()
            keyAlias = releaseKeyAlias.orEmpty()
            keyPassword = releaseKeyPassword.orEmpty()
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
