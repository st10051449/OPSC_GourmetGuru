

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    signingConfigs {
        create("release") {
            storeFile =
                file("release-key-2.jks")
            storePassword = "snowqueen46*"
            keyAlias = "releasekey"
            keyPassword = "bloonstd6isawesomelol4321*"
        }
    }
    namespace = "com.opsc7311poe.gourmetguru_opscpoe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.opsc7311poe.gourmetguru_opscpoe"
        minSdk = 27
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
        jvmTarget = "17" //used to be 1.8 for future ref, also i edited the grade properties
    }

    // Data Binding Configuration
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}
dependencies {
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    implementation("com.google.android.gms:play-services-auth:19.0.0")
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.androidx.fragment.testing)
    implementation(libs.firebase.storage.ktx)
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.3.1")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.mockito:mockito-inline:4.3.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // For AndroidJUnit4
    androidTestImplementation("androidx.test:core-ktx:1.5.0") // Core KTX
    androidTestImplementation("androidx.test:rules:1.5.0") // For UI rules
    androidTestImplementation("androidx.test.ext:truth:1.4.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("androidx.biometric:biometric:1.2.0-alpha03")
    implementation(libs.picasso)
    implementation(libs.retrofit)
    implementation(libs.gsonConverter)
    implementation(libs.gson)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
}

