dependencyResolutionManagement {
    repositories {
        mavenCentral {
            content {
                includeGroup("net.kyori")
                includeGroup("org.apache.logging.log4j")
            }
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            content { includeGroup("net.kyori") }
        }
        maven("https://jitpack.io") {
            content { includeGroup("com.github.milkbowl") }
        }
        maven("https://repo.codemc.org/repository/maven-public") {
            content { includeGroup("org.bstats") }
        }
        maven("https://m2.dv8tion.net/releases/") {
            content { includeGroup("net.dv8tion") }
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            content { includeGroup("me.clip") }
        }
        maven("https://libraries.minecraft.net/") {
            content { includeGroup("com.mojang") }
        }
        maven("https://hub.spigotmc.org/nexus/content/groups/public/") {
            content {
                includeGroup("org.spigotmc")
                includeGroup("net.md_5")
            }
        }
        maven("https://papermc.io/repo/repository/maven-public/")
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "EssentialsXParent"

// Modules
sequenceOf(
    "",
    "AntiBuild",
    "Chat",
    "Discord",
    "DiscordLink",
    "GeoIP",
    "Protect",
    "Spawn",
    "XMPP",
).forEach {
    include(":EssentialsX$it")
    project(":EssentialsX$it").projectDir = file("Essentials$it")
}

// Providers
include(":providers:BaseProviders")
include(":providers:NMSReflectionProvider")
include(":providers:PaperProvider")
include(":providers:1_8Provider")
include(":providers:1_12Provider")
