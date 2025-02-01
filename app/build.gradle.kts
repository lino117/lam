plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.progettolam"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.progettolam"
        minSdk = 23
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.work.runtime)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.mpandroidchart)
    implementation (libs.play.services.location.v2101)
//    implementation (libs.ucs.credential.developers)
//    implementation (libs.location)
//    implementation (libs.base)
    // Sostituisci con la versione disponibile


//    implementation(libs.play.services.activity.recognition)

}