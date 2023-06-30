import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
  // Application Specific Plugins
  id(BuildPlugins.androidApplication)
  id(BuildPlugins.kotlinAndroid)
  id(BuildPlugins.kotlinKapt)
  id(BuildPlugins.kotlinAndroidExtensions)
  id(BuildPlugins.androidHilt)

  // Internal Script plugins
  id(ScriptPlugins.variants)
  id(ScriptPlugins.quality)
  id(ScriptPlugins.compilation)
  id(ScriptPlugins.gg_service)
    id("org.jetbrains.kotlin.android")
//    id("org.jetbrains.kotlin.android")
}

android {
  compileSdkVersion(AndroidSdk.compile)

  defaultConfig {
    minSdkVersion(AndroidSdk.min)
    targetSdkVersion(AndroidSdk.target)
    multiDexEnabled = true
    applicationId = AndroidClient.appId
    versionCode = AndroidClient.versionCode
    versionName = AndroidClient.versionName
    testInstrumentationRunner = AndroidClient.testRunner
  }

  compileOptions {
    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
    // Sets Java compatibility to Java 8
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
  }

  sourceSets {
    map { it.java.srcDir("src/${it.name}/kotlin") }

    //TODO: Remove this when migrating the DI framework
    getByName("main") {
      jniLibs.srcDir("libs")
      java.srcDir("$buildDir/generated/source/kapt/main")
    }

  }


  // Add the block below if you're using Kotlin
  kotlinOptions {
    jvmTarget = "1.8"
  }
//    buildFeatures {
//        viewBinding = true
//    }

}


dependencies {
  implementation("com.github.prolificinteractive:material-calendarview:2.0.0")
  implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
  implementation("androidx.appcompat:appcompat:1.3.1")
  implementation("com.google.android.material:material:1.5.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.0")
  implementation("androidx.databinding:databinding-runtime:4.2.2")
//    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
//    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    //Compile time dependencies
  kapt(Libraries.lifecycleCompiler)
  kapt(Libraries.hiltCompiler)

  // Application dependencies
  implementation(Libraries.kotlinStdLib)
  implementation(Libraries.kotlinCoroutines)
  implementation(Libraries.kotlinCoroutinesAndroid)
  implementation(Libraries.appCompat)
  implementation(Libraries.ktxCore)
  implementation(Libraries.constraintLayout)
  implementation(Libraries.viewModel)
  implementation(Libraries.liveData)
  implementation(Libraries.lifecycleExtensions)
  implementation(Libraries.cardView)
  implementation(Libraries.recyclerView)
  implementation(Libraries.circleImageView)
  implementation(Libraries.material)
  implementation(Libraries.androidAnnotations)
  implementation(Libraries.hilt)
  implementation(Libraries.retrofit)
  implementation(Libraries.okHttpLoggingInterceptor)
  implementation(Libraries.swipeRefreshLayout)
  //Glide
  implementation(Libraries.glide)
  annotationProcessor(Libraries.glideAnnotation)

  //TODO: change this
  implementation ("androidx.fragment:fragment-ktx:1.2.5")

  // Unit/Android tests dependencies
  testImplementation(TestLibraries.junit4)
  testImplementation(TestLibraries.mockk)
  testImplementation(TestLibraries.kluent)
  testImplementation(TestLibraries.robolectric)

  // Acceptance tests dependencies
  androidTestImplementation(TestLibraries.testRunner)
  androidTestImplementation(TestLibraries.espressoCore)
  androidTestImplementation(TestLibraries.testExtJunit)
  androidTestImplementation(TestLibraries.testRules)
  androidTestImplementation(TestLibraries.espressoIntents)
  androidTestImplementation(TestLibraries.hiltTesting)

  // App center
  implementation("com.microsoft.appcenter:appcenter-analytics:4.3.1")
  implementation("com.microsoft.appcenter:appcenter-crashes:4.3.1")

  // Other dependencies
  implementation ("com.github.tapadoo:alerter:7.2.4")
  implementation ("com.github.auduongvanhieu:loadingview:1.1")
  implementation ("com.super_rabbit.wheel_picker:NumberPicker:1.0.1")
  implementation ("com.github.dhaval2404:imagepicker:2.1")
  implementation ("com.amazonaws:aws-android-sdk-s3:2.18.0")
  implementation ("com.amazonaws:aws-android-sdk-cognito:2.18.0")
  implementation ("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.18.0")
  implementation (platform("com.google.firebase:firebase-bom:28.4.1"))
  implementation ("com.google.firebase:firebase-auth-ktx")
  implementation ("com.google.firebase:firebase-core:9.6.1")
  implementation ("com.google.firebase:firebase-messaging:23.0.0")
  implementation ("com.google.android.gms:play-services-auth:19.2.0")
  implementation ("com.facebook.android:facebook-android-sdk:[8,9)")
  implementation ("com.github.chivorns:smartmaterialspinner:1.5.0")
  implementation("com.mikhaellopez:circularprogressbar:3.1.0")
//  implementation("me.relex:circleindicator:2.1.6")
  implementation("com.github.thanhnhu99it:CircleIndicator:2.1.7")

  implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
  implementation ("me.biubiubiu.justifytext:library:1.1")
  implementation("com.google.android.gms:play-services-location:18.0.0")
  implementation("com.github.kizitonwose:CalendarView:1.0.4")
  implementation("androidx.multidex:multidex:2.0.1")
  implementation("com.mapbox.maps:android:10.2.0"){
    exclude(group = "group_name", module = "module_name")
  }
  implementation("com.airbnb.android:lottie:4.2.2")
  implementation ("com.android.support:appcompat-v7:28.0.0-alpha3")
  implementation ("com.android.support:design:28.0.0-alpha3")

}