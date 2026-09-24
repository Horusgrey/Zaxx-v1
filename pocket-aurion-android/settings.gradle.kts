pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PocketAurion"
include(":app")

val withLlama = providers.gradleProperty("withLlama").orNull == "true"
if (withLlama) {
    val llamaLib = file("vendor/llama.cpp/examples/llama.android/lib")
    require(llamaLib.exists()) {
        "withLlama=true but vendor/llama.cpp is missing. Run scripts/bootstrap_llama.sh or bootstrap_llama.ps1 first."
    }
    include(":llama")
    project(":llama").projectDir = llamaLib
}
