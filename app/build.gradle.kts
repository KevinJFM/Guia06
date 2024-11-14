plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.kevin.loginproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kevin.loginproject"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures{
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    // Nuevas dependencias de Firebase y Google Sign-In
    implementation(libs.firebase.auth.v2310)
    implementation(libs.play.services.auth.v2120)
    implementation(libs.firebase.core)

    // para la autenticación con google
    implementation("com.google.firebase:firebase-auth")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Otras dependencias
    implementation(libs.circleimageview)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)

    // Dependencias de Glide
    implementation(libs.glide) // Glide
    annotationProcessor(libs.glide.compiler) // Glide Compiler

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
