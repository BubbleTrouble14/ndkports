import com.android.ndkports.AdHocPortTask
import com.android.ndkports.AndroidExecutableTestTask
import com.android.ndkports.CMakeCompatibleVersion
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

fun blstVersionToCMakeVersion(blstVersion: String): CMakeCompatibleVersion {
    val parts = blstVersion.split(".")
    if (parts.size != 3) {
        throw IllegalArgumentException("Expected BLST version to be in 'major.minor.patch' format, but got: $blstVersion")
    }

    val major = parts[0].toInt()
    val minor = parts[1].toInt()
    val patch = parts[2].toInt()

    return CMakeCompatibleVersion(major, minor, patch, 0)
}

val portVersion = "0.3.11"
val prefabVersion = blstVersionToCMakeVersion(portVersion)

group = "com.bubble.blst"
version = "$portVersion${rootProject.extra.get("snapshotSuffix")}"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
    distribution
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("src.tar.gz"))
    minSdkVersion.set(16)
}

val buildTask = tasks.register<AdHocPortTask>("buildPort") {

    builder {
        // Define the commands for the toolchain
        val cc = toolchain.clang.absolutePath
        println("......${installDirectory.absolutePath}......")
        // Compile the source files

        val commonCFlags = arrayOf(
            "-O2", // Optimization flag
            "-fno-builtin", // Disable intrinsic functions
            "-fPIC", // Position-independent code
            "-Wall", // Enable all warnings
            "-Wextra", // Enable extra warnings
            "-Werror", // Treat warnings as errors
            "-frtti", // Enable RTTI
            "-fexceptions", // Enable exceptions
            "-fstack-protector-all", // Stack protection
            "-DON_ANDROID", // Define ON_ANDROID
            "-DANDROID", // Define ANDROID
        )

        val linkerFlags = arrayOf(
            "-lc++_shared" // Link against the shared C++ standard library
        )

        // Compile the source files
        run {
            args(*(arrayOf(cc, "-c", sourceDirectory.resolve("src/server.c").absolutePath) + commonCFlags))
        }

        // println(sourceDirectory.resolve("build/assembly.S").absolutePath)

        run {
            args(*(arrayOf(cc, "-c", sourceDirectory.resolve("build/assembly.S").absolutePath) + commonCFlags))
        }

        // Create the library archive
        run {
            args(*(arrayOf(cc, "-shared", "-o", buildDirectory.resolve("libblst.so").absolutePath) +
                    arrayOf("${buildDirectory.absolutePath}/assembly.o", "${buildDirectory.absolutePath}/server.o") +
                    commonCFlags + linkerFlags))
        }

        run {
            val includeDir = installDirectory.resolve("include/blst")
            args(
                "mkdir", "-p", includeDir.absolutePath  // Create the include directory, '-p' ensures no error if it already exists
            )
        }

        run {
            val destDir = installDirectory.resolve("lib")
            args(
                "mkdir", "-p", destDir.absolutePath  // Correctly using '-p' option with mkdir
            )
        }

        run {
            val bindingsSrcDir = sourceDirectory.resolve("bindings")
            // val srcSrcDir = sourceDirectory.resolve("src")
            val destDir = installDirectory.resolve("include/blst")
            args(
                "bash", "-c",
                // "cp -v $bindingsSrcDir/*.{h,hpp} $destDir && cp -v $srcSrcDir/*.{h,hpp} $destDir"
                "cp -v $bindingsSrcDir/*.{h,hpp} $destDir"
            )
        }


        run {
            val soFile = buildDirectory.resolve("libblst.so").absolutePath
            val destDir = installDirectory.resolve("lib")

            args(
                "bash", "-c",  // Invoke bash shell
                "cp -v $soFile $destDir"  // Pass the entire command as a single string
            )
        }
    }
}

tasks.prefabPackage {
    version.set(prefabVersion)

    modules {
        create("blst")
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("BLST")
                description.set("The ndkports AAR for BLST.")
                url.set("https://github.com/supranational/blst")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/supranational/blst/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("The BLST Developers")
                    }
                }
                scm {
                    url.set("https://github.com/supranational/blst")
                    connection.set("scm:git:https://github.com/supranational/blst.git")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("${project.buildDir}/repository")
        }
    }
}

distributions {
    main {
        contents {
            from("${project.buildDir}/repository")
            include("**/*.aar")
            include("**/*.pom")
        }
    }
}

tasks {
    distZip {
        dependsOn("publish")
        destinationDirectory.set(File(rootProject.buildDir, "distributions"))
    }
}
