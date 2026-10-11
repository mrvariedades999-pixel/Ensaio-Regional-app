plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "br.com.ismaelcell.ensaio"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.ismaelcell.ensaio"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
