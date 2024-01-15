import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin

val portVersion = "6.3.0"

group = "com.android.ndk.thirdparty"
version = "$portVersion${rootProject.extra.get("snapshotSuffix")}"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
    distribution
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("src.tar.xz"))
    minSdkVersion.set(19)
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf {
        args(
            "--disable-static",
            "--enable-shared",
            "--enable-cxx",
        )
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("gmp")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("gmp")
                description.set("The ndkports AAR for gmp.")
                url.set(
                    "https://android.googlesource.com/platform/tools/ndkports"
                )
                licenses {
                    license {
                        name.set("The gmp License")
                        url.set("https://gmp.haxx.se/docs/copyright.html")
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
