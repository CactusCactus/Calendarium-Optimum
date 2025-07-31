plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

val commitCount by project.extra {
    execCommand("git rev-list --count HEAD")?.toInt()
        ?: throw GradleException("Unable to get number of commits. Make sure git is initialized.")
}

val latestTag by project.extra {
    execCommand("git describe")
        ?: throw GradleException(
            "Unable to get version name using git describe.\n" +
                    "Make sure you have at least one annotated tag and git is initialized.\n" +
                    "You can create an annotated tag with: git tag -a 1.0 -m \"1.0\""
        )
}

android {
    namespace = "com.kuba.calendarium"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kuba.calendarium"
        minSdk = 26
        targetSdk = 35
        versionCode = commitCount
        versionName = latestTag

        testInstrumentationRunner = "com.kuba.calendarium.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }

        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    applicationVariants.all {
        val variant = this

        variant.outputs.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach {
                it.outputFileName =
                    "Calendarium_Optimum_${variant.buildType.name}_${variant.versionName}.apk"
            }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material3
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.ui.text.google.fonts)
    testImplementation(libs.junit.jupiter)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Timber
    implementation(libs.timber)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Firebase
    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.analytics)

    // Calendar Kizitonwose
    implementation(libs.calendar.compose)

    // Reorderable
    implementation(libs.reorderable)

    // Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    androidTestUtil(libs.orchestrator)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Mocking
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent.jvm)

}

fun execCommand(command: String): String? {
    val cmd = command.split(" ").toTypedArray()
    val process = ProcessBuilder(*cmd)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    return process.inputStream.bufferedReader().readLine()?.trim()
}
