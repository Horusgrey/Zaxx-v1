plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val withLlama = providers.gradleProperty("withLlama").orNull == "true"

android {
    namespace = "com.horusgrey.aurion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.horusgrey.aurion"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "WITH_LLAMA", withLlama.toString())
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "WITH_LLAMA", withLlama.toString())
        }
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/java")
        if (withLlama) {
            getByName("main").java.srcDir("src/withLlama/java")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        // Robolectric tests read the protected Aurion kernel assets.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)

    if (withLlama) {
        implementation(project(":llama"))
    }

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
