// File: app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")        // <— apply KSP here (no version needed)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.github.bbqribs.pushupstracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.bbqribs.pushupstracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Import the Compose BOM for 'implementation' configuration - THIS MANAGES VERSIONS
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    // ALSO Import the Compose BOM for 'androidTestImplementation' configuration
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))

    // Core Android & Jetpack Libraries
    implementation("androidx.core:core-ktx:1.13.1") // Or latest stable
    implementation("androidx.activity:activity-compose:1.9.0") // Or latest stable

    // Lifecycle (for ViewModel, viewModelScope, collectAsState, etc.) - Let BOM manage these if possible,
    // but if not, use their latest stable versions.
    // The Compose BOM doesn't typically manage all lifecycle artifacts directly.
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Or latest stable
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")      // Or latest stable
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // Or latest stable

    // Compose UI - Versions managed by the BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room (Data Persistence) - Consider updating to their latest stable versions.
    // The Compose BOM does not manage Room versions.
    implementation("androidx.room:room-runtime:2.6.1") // Example: Latest stable as of writing
    implementation("androidx.room:room-ktx:2.6.1")     // Example: Latest stable as of writing
    ksp("androidx.room:room-compiler:2.6.1")        // Example: Latest stable as of writing

    // Hilt (Dependency Injection) - Consider updating to their latest stable versions.Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //  XML Material Components
    implementation("com.google.android.material:material:1.12.0") // Example: Latest stable as of writing
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")

    // For the charting library
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
    implementation("com.patrykandpatrick.vico:core:1.14.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Or latest stable
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Or latest stable
    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // Version managed by BOM
}

