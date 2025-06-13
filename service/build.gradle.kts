plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
//    alias(libs.plugins.org.jetbrains.kotlin.kapt)
    id("kotlin-kapt")
}
android {
    namespace = "com.example.service"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Force latest supported kotlinx-metadata-jvm
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //Room version
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}