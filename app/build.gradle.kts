plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.solicare.monitor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.solicare.monitor"
        minSdk = 26
        targetSdk = 35
        versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = project.findProperty("versionName") as String? ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                project.findProperty("keystoreFile") ?: System.getenv("SOLICARE_KEYSTORE_FILE")
                ?: "solicare-keystore.jks"
            )
            storePassword = project.findProperty("keystorePassword") as String?
                ?: System.getenv("SOLICARE_KEYSTORE_PASSWORD")
            keyAlias =
                project.findProperty("keyAlias") as String? ?: System.getenv("SOLICARE_KEY_ALIAS")
            keyPassword = project.findProperty("keyPassword") as String?
                ?: System.getenv("SOLICARE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://www.solicare.kro.kr\"")
            buildConfigField("String", "BASE_API_URL", "\"https://api.solicare.kro.kr\"")
        }
        debug {
            buildConfigField("String", "BASE_URL", "\"https://dev-www.solicare.kro.kr\"")
            buildConfigField("String", "BASE_API_URL", "\"https://dev-api.solicare.kro.kr\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "36.0.0"

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "app-${buildType.name}-v${versionName}-${versionCode}.apk"
        }
    }
}

dependencies {
    // 1. 플랫폼(bom) 선언
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.androidx.compose.bom))

    // 2. AndroidX 및 Jetpack
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.webkit)

    // 3. Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 4. Google/Material
    implementation(libs.material)

    // 5. 외부 라이브러리
    implementation(libs.okhttp)

    // 6. Firebase
    implementation(libs.google.firebase.messaging)

    // 7. Debug 환경 의존성
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kotlin {
    jvmToolchain(17)
}
