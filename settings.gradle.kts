rootProject.name = "ndkports"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

// include("curl")
// include("googletest")
// include("jsoncpp")
// include("openssl")
// include("blst")
// include("gmp")
// include("utf8proc")
include("zlib")
include("libpng")
