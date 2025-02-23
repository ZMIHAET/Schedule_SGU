plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.shedule"
    compileSdk = 34
    buildFeatures{
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.shedule"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("jsoup-1.17.2.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("junit:junit:4.12")
    androidTestImplementation("junit:junit:4.12")
    implementation("com.google.android.material:material:1.12.0")
    implementation ("org.postgresql:postgresql:42.2.23")

}
