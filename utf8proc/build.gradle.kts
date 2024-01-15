import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin

val portVersion = "2.9.0"

group = "com.android.ndk.thirdparty"
version = "$portVersion${rootProject.extra.get("snapshotSuffix")}"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
    distribution
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("src.tar.gz"))
    minSdkVersion.set(19)
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<CMakePortTask>("buildPort") {
    cmake {
        arg("-DBUILD_SHARED_LIBS=ON")
        // arg("-Dgmock_build_tests=ON")
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE.md")

    modules {
        create("utf8proc")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("utf8proc")
                description.set("The ndkports AAR for utf8proc.")
                url.set(
                    "https://android.googlesource.com/platform/tools/ndkports"
                )
                licenses {
                    license {
                        name.set("The utf8proc License")
                        url.set("https://github.com/JuliaStrings/utf8proc/blob/master/LICENSE.md")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("The Android Open Source Project")
                    }
                }
                scm {
                    url.set("https://android.googlesource.com/platform/tools/ndkports")
                    connection.set("scm:git:https://android.googlesource.com/platform/tools/ndkports")
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
