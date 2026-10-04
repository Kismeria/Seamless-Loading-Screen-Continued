plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "seamless-loading-screen"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

sourceSets {
    create("autotest") {
        compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
    }
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://maven.terraformersmc.com/releases", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://maven.isxander.dev/releases", "Xander Maven", "dev.isxander", "org.quiltmc.parsers")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")

    modImplementation("dev.isxander:yet-another-config-lib:${sc.properties.get<String>("deps.yacl")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modCompileOnly("com.terraformersmc:modmenu:${sc.properties.get<String>("deps.modmenu")}") {
        isTransitive = false
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    mods {
        register("seamless_loading_screen") { sourceSet(sourceSets.main.get()) }
        register("sls_autotest") { sourceSet(sourceSets["autotest"]) }
    }

    runs {
        // Automated smoke test: creates a world, leaves it, rejoins it and quits, see src/autotest
        register("autotest") {
            client()
            source(sourceSets["autotest"])
            runDir("run/autotest-${sc.current.version}")
            vmArg("-Dsls.autotest=true")
        }
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
    }
    runConfigs.named("client") {
        runDir("run/${sc.current.version}")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", project.version.toString())
            register("minecraft", sc.properties["mod.mc_compat"])
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    withType<Jar> {
        inputs.property("archives_name", base.archivesName)
        from(rootProject.file("LICENSE")) { rename { "LICENSE_seamless_loading_screen" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// Production smoke test: the release jar, a production copy of the autotest mod and the dependencies,
// in a plain Fabric environment. Run with `gradlew :<version>:prodAutotest`.
val prodMods: Configuration = configurations.create("prodMods") { isTransitive = false }
dependencies {
    prodMods("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    prodMods("dev.isxander:yet-another-config-lib:${sc.properties.get<String>("deps.yacl")}")
}

val autotestJar = tasks.register<Jar>("autotestJar") {
    from(sourceSets["autotest"].output)
    archiveClassifier = "autotest-dev"
    destinationDirectory = layout.buildDirectory.dir("autotest")
}

val autotestModJar: Provider<RegularFile> = if (sc.current.parsed < "26.1") {
    tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapAutotestJar") {
        inputFile = autotestJar.flatMap { it.archiveFile }
        archiveClassifier = "autotest"
        destinationDirectory = layout.buildDirectory.dir("autotest")
        addNestedDependencies = false
    }.flatMap { it.archiveFile }
} else {
    autotestJar.flatMap { it.archiveFile }
}

tasks.register<net.fabricmc.loom.task.prod.ClientProductionRunTask>("prodAutotest") {
    // -Psls.testJar=<path> tests another jar, e.g. the jar of a neighbouring version
    val testJar = providers.gradleProperty("sls.testJar")
    if (testJar.isPresent) mods.from(testJar.get()) else mods.from(loomx.modJar.flatMap { it.archiveFile })
    mods.from(autotestModJar, prodMods)
    jvmArgs.add("-Dsls.autotest=true")
    runDir = layout.projectDirectory.dir("run/prod-autotest-${sc.current.version}")
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion) }
}
