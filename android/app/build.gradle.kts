plugins {
    id("com.android.application")
}

android {
    namespace = "se.larsves.portalskarm"
    compileSdk = 34

    defaultConfig {
        applicationId = "se.larsves.portalskarm"
        minSdk = 28          // Portal+ gen 1 kör Android 9
        targetSdk = 29       // Metas rekommendation för nya Portal-appar
        versionCode = 2
        versionName = "1.1"
    }

    signingConfigs {
        // Fast nyckel i repot, så att nya byggen kan installeras ovanpå gamla
        getByName("debug") {
            storeFile = rootProject.file("debug.keystore")
            storeType = "PKCS12"
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
