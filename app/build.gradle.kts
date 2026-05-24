import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val renderBackendUrl = "https://hostel-management-system-vh4u.onrender.com"
val debugApiBaseUrl = localProperties.getProperty("debugApiBaseUrl")
    ?.takeIf { it.isNotBlank() }
    ?: "$renderBackendUrl/api/"
val debugSocketUrl = localProperties.getProperty("debugSocketUrl")
    ?.takeIf { it.isNotBlank() }
    ?: renderBackendUrl
val productionApiBaseUrl = localProperties.getProperty("productionApiBaseUrl")
    ?.takeIf { it.isNotBlank() }
    ?: "$renderBackendUrl/api/"
val productionSocketUrl = localProperties.getProperty("productionSocketUrl")
    ?.takeIf { it.isNotBlank() }
    ?: renderBackendUrl

android {
    namespace = "com.example.hostelmanagementsystem"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.hostelmanagementsystem"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            buildConfigField("String", "SOCKET_URL", "\"$debugSocketUrl\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"$productionApiBaseUrl\"")
            buildConfigField("String", "SOCKET_URL", "\"$productionSocketUrl\"")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

configurations.all {
    resolutionStrategy.force(
        "com.razorpay:standard-core:1.7.10",
        "com.razorpay:core:1.0.10"
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("io.socket:socket.io-client:2.1.1")
    implementation("com.razorpay:checkout:1.6.41")
}
