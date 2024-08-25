plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mallto.mylibrary"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.android.beacon.library)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.appcompat)
}