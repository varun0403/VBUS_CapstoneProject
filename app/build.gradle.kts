plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.vbus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.vbus"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.annotations)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.storage)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.com.google.firebase.firebase.messaging)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.scenecore)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.espresso.core)
    //implementation(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose.v284)
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(platform(libs.firebase.bom))
    implementation(libs.okhttp)
    implementation(libs.gson)
    //Google Maps SDK
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    //Camera Dependecies
    implementation(libs.androidx.camera.core.v130) {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.androidx.camera.camera2){
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.androidx.camera.lifecycle.v130){
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.androidx.camera.view.v130){
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.androidx.ui.v150)
    implementation(libs.guava)
    implementation(libs.face.detection)
    implementation(libs.tensorflow.lite.v290)
    implementation(kotlin("script-runtime"))
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

}