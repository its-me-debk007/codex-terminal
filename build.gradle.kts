import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = "com.github.codexjb"
version = "1.0.5-beta"

val pluginId = "com.github.codexjb"
val pluginName = "Codex CLI"
val pluginVersion = project.version.toString()
val platformVersion = providers.gradleProperty("platformVersion").orElse("2025.3.3")
val pluginSinceBuild = providers.gradleProperty("pluginSinceBuild").orElse("253")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(platformVersion.get(), useInstaller = false)
        bundledPlugin("org.jetbrains.plugins.terminal")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = pluginId
        name = pluginName
        version = pluginVersion
        description = """
            <p><b>Codex CLI keeps the OpenAI Codex command-line agent one shortcut away inside your IDE.</b></p>
            <p>Open a dedicated Codex terminal tab, send the current editor file or selected line range as an <code>@path</code> reference, and keep the conversation grounded in the exact code you are looking at.</p>
            <p>This extension is intentionally terminal-first: it launches the Codex CLI you install or configure, then focuses on fast editor-to-terminal handoff.</p>
            <br/>
            <p><b>Features</b></p>
            <ul>
              <li><b>Dedicated Codex terminal tab</b>: open or focus Codex from the tool window strip, Tools menu, or keyboard shortcut.</li>
              <li><b>Editor file references</b>: insert <code>@path</code> with no selection, or <code>@path#L12-L34</code> when code is selected.</li>
              <li><b>Working-directory-aware paths</b>: references are relative to the configured Codex working directory, with absolute paths for files outside it.</li>
              <li><b>Smart terminal recovery</b>: if the Codex tab is still open but the process stopped, the launch action reruns your configured command in that same tab.</li>
              <li><b>Configurable launch command</b>: use <code>codex</code>, <code>npx @openai/codex</code>, a custom binary, or a WSL wrapper.</li>
            </ul>
            <br/>
            <p><b>Usage</b></p>
            <ul>
              <li>Click the Codex icon in the right-hand tool window strip, or press <code>Ctrl+Alt+Shift+C</code> / <code>Cmd+Alt+Shift+C</code>.</li>
              <li>The integrated terminal opens a dedicated Codex tab and runs the configured command automatically.</li>
              <li>In the editor, select code if needed and press <code>Ctrl+Alt+K</code> / <code>Cmd+Alt+K</code> to insert the reference into Codex, then type your question and submit.</li>
            </ul>
            <br/>
            <p><b>Configuration</b></p>
            <p>Open Settings → Tools → Codex CLI to set the launch command and working directory.</p>
            <br/>
            <p><b>Requirements</b></p>
            <ul>
              <li>IDE based on platform build 253 (2025.3) or later</li>
              <li>OpenAI Codex CLI installed and available in your system PATH, or launched through <code>npx</code> / a custom command</li>
            </ul>
            <p><b>Note:</b> Codex also has an official IDE integration. This extension remains a lightweight terminal-first alternative for developers who prefer the CLI workflow.</p>
        """.trimIndent()
        changeNotes = """
            <p><b>1.0.4 &mdash; Marketplace name update</b></p>
            <ul>
              <li><b>Renamed the extension</b>: the Marketplace-visible name is now <code>Codex CLI</code>, avoiding restricted platform/vendor terms.</li>
              <li><b>Focused the extension on the CLI workflow</b>: removed the experimental Codex notification feature and its background terminal-output monitor.</li>
              <li><b>Simplified settings</b>: Settings &rarr; Tools &rarr; Codex CLI now contains only the launch command and working directory.</li>
              <li><b>Fixed first shortcut exception</b>: checking whether the Codex process is still running now happens outside the IDE read action, so the first file-reference shortcut no longer trips terminal threading assertions.</li>
              <li><b>Kept terminal workflow intact</b>: launch/focus behavior and editor file reference insertion are unchanged.</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = pluginSinceBuild.get()
        }
        vendor {
            name = "Oleh Khomyk"
            email = "oleh.khomyk@gmail.com"
        }
    }
    instrumentCode = false
    buildSearchableOptions = false
    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2025.3.5", useInstaller = false)
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2026.1.2", useInstaller = false)
        }
    }
}

kotlin {
    jvmToolchain(21)
}
