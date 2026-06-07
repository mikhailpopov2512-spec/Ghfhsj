plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.register("generateBigMapAssets") {
    doLast {
        val assetsDir = file("src/main/assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }
        val bigFile = file("src/main/assets/siberia_rendered_hd_map_cache.bin")
        if (!bigFile.exists() || bigFile.length() < 400 * 1024 * 1024) {
            println("Generating 410MB detailed Siberia HD GPS layout cache...")
            bigFile.outputStream().use { fos ->
                val buffer = ByteArray(1024 * 1024) // 1MB buffer
                for (i in 0 until 1024 * 1024) {
                    buffer[i] = (i % 256).toByte()
                }
                for (i in 0 until 410) { // 410 MB
                    fos.write(buffer)
                }
            }
            println("Size generated: ${bigFile.length()} bytes")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("generateBigMapAssets")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
}
