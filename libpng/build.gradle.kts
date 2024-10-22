import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin

val portVersion = "1.6.44"

group = "io.github.cryptorg"
version = "$portVersion${rootProject.extra.get("snapshotSuffix")}"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
    distribution
}

dependencies {
    implementation(project(":zlib"))
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("libpng-$portVersion.tar.xz"))
    minSdkVersion.set(19)
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<CMakePortTask>("buildPort") {
    cmake {
        arg("-DPNG_SHARED=ON")
        arg("-DPNG_STATIC=OFF")
        arg("-DPNG_TESTS=OFF")
        arg("-DPNG_EXECUTABLES=OFF")
        arg("-DZLIB_ROOT=$sysroot")
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    @Suppress("UnstableApiUsage") dependencies.set(
        mapOf(
            "zlib" to "1.3.1"  // Make sure this matches your zlib version
        )
    )

    modules {
        create("png16") {
            dependencies.set(listOf("//zlib:z"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("libpng16")
                description.set("The ndkports AAR for libpng16.")
                url.set(
                    "https://github.com/BubbleTrouble14/ndkports"
                )
                licenses {
                    license {
                        name.set("libpng License")
                        url.set("http://www.libpng.org/pub/png/src/libpng-LICENSE.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("The Android Open Source Project")
                    }
                }
                scm {
                    url.set("https://github.com/BubbleTrouble14/ndkports")
                    connection.set("scm:git:https://github.com/BubbleTrouble14/ndkports")
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