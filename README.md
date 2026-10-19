# Codex CLI

**Share the current editor file or selection with the OpenAI [`codex`](https://www.npmjs.com/package/@openai/codex) CLI in one keystroke.**

Codex CLI is a lightweight, terminal-first extension for developers who already use the Codex command-line agent and want it close to their editor. Press a shortcut in the editor, and a precise file reference is typed straight into your running Codex session. If text is selected, the reference includes the selected line range. No copy/paste, no re-typing paths, no re-explaining where the file lives.

Inspired by the official Claude Code IDE workflow — same UX, different backend.

Codex also has an official IDE integration. This extension stays intentionally lightweight for developers who prefer the CLI running in the IDE terminal.

## How it works

1. Open any project. Click the **Codex** icon in the right-hand sidebar (or press `Ctrl/Cmd+Alt+Shift+C`). A terminal tab named `Codex` opens and runs the CLI.
2. Switch to any editor, optionally selecting the code you want to point at.
3. Press `Ctrl+Alt+K` (Windows/Linux) or `Cmd+Alt+K` (macOS).
4. The Codex terminal receives, at the cursor — without submitting:

   ```
   @src/main/kotlin/com/github/codexjb/service/CodexTerminalService.kt
   ```

5. Type your question after the reference and press Enter. Codex resolves the file, or the selected line range when a selection was present.

### Reference format

| Editor state | What gets inserted |
|---|---|
| No selection | `@src/main/.../File.kt` |
| Single-line selection | `@src/main/.../File.kt#L42` |
| Multi-line selection | `@src/main/.../File.kt#L25-L40` |

Paths are relative to the configured Codex working directory. If a file is outside that directory, the plugin inserts its absolute path so Codex does not confuse it with a same-named file elsewhere.

## Features

- **Insert file references from the editor** — the headline feature. `Ctrl+Alt+K` / `Cmd+Alt+K`, editor right-click menu, or bind your own shortcut.
- **Dedicated Codex terminal tab** — click the Codex sidebar icon or press `Ctrl+Alt+Shift+C` / `Cmd+Alt+Shift+C` to open or focus the CLI.
- **Smart terminal recovery** — if the Codex tab is still open but the process has stopped, the launch action reruns your configured command in that same tab.
- **Working-directory-aware paths** — references are relative to the configured Codex working directory, with absolute paths used for files outside it.
- **Configurable command** — `Settings → Tools → Codex CLI`. Works with `codex`, `npx @openai/codex`, custom paths, or WSL wrappers like `wsl -d Ubuntu -- bash -lic "codex"`.

## Supported IDEs

Supported IDEs based on platform build `253` (2025.3) or later.

## Installation

The extension does not bundle the Codex CLI. Install `codex` separately or configure it to launch through `npx`.

### From source

```sh
./gradlew buildPlugin
```

Output: `build/distributions/codex-cli-1.0.4.zip`.

In your IDE: `Settings → Plugins → ⚙ → Install Plugin from Disk…` and select the zip. Restart the IDE.

### Prerequisites

Install the Codex CLI on your system:

```sh
npm install -g @openai/codex
```

Or configure the plugin to use `npx @openai/codex` (no global install required).

## Keybindings

Both shortcuts are rebindable under `Settings → Keymap → Plugins → Codex CLI`.

| Action | Windows/Linux | macOS |
|---|---|---|
| Insert File Reference into Codex | `Ctrl+Alt+K` | `Cmd+Alt+K` |
| Launch Codex | `Ctrl+Alt+Shift+C` | `Cmd+Alt+Shift+C` |

## Settings

`Settings → Tools → Codex CLI`

| Setting | Default | Notes |
|---|---|---|
| Codex command | `codex` | Anything runnable in the integrated terminal shell. |
| Working directory | *(project root)* | Where the shell starts. |

## Known limitations

- **Paths with spaces** — the unquoted `@path` / `@path#Lx-Ly` format may misparse on some shells. File an issue if you hit this.

## Building

Requires JDK 17+. First build downloads ~1 GB of IDEA Community SDK.

```sh
./gradlew buildPlugin     # produces the installable zip
./gradlew runIde          # launches a sandbox IDE with the plugin installed
```

## License

MIT. See [LICENSE](LICENSE).
