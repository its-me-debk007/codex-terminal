# Changelog

## 1.0.4 - Marketplace name update

### Changed

- Renamed the Marketplace-visible extension to `Codex CLI`, avoiding restricted platform/vendor terms in `plugin.xml`.
- Streamlined the extension back to its core terminal-first workflow by removing the experimental Codex notification feature and its background terminal-output monitor.
- Simplified `Settings → Tools → Codex CLI` so it now contains only the launch command and working directory.

### Kept

- Codex launch/focus behavior is unchanged: the toolbar icon still opens the dedicated `Codex` terminal tab, focuses the existing tab, and reruns the configured command if the Codex process has stopped.
- Editor reference insertion is unchanged: no selection inserts `@path`; any selected text inserts `@path#Lline` or `@path#Lstart-Lend`.

### Fixed

- Fixed the first file-reference shortcut crash in 2025.3 IDEs. The plugin now checks whether Codex is still running on a pooled thread, outside the IDE read action, before deciding whether to rerun the command.

## 1.0.3 - Selection-aware references

### Fixed

- Reference insertion now uses the expected mixed behavior:
  - no selection inserts only `@path`;
  - selected text inserts `@path#Lline` or `@path#Lstart-Lend`.
- Selecting even one character now links the file plus the selected line number/range.
- The settings page label now matches the plugin name.

## 1.0.1 - Terminal sync and safer file links

### Fixed

- File references now use the actual open editor document. This fixes the shortcut linking the wrong file when two files have the same name in different folders.
- File links are now resolved relative to the configured Codex working directory, with absolute paths used for files outside it. This keeps references aligned with what Codex can actually resolve.
- Clicking the Codex sidebar icon now selects the existing `Codex` terminal tab instead of just opening the Terminal tool window on whichever tab was last active.
- If the `Codex` terminal tab is still open but the agent process has stopped, clicking the Codex icon now reruns the configured Codex command in that same tab.

## 1.0.0 - Initial release of Codex CLI

### Headline feature

- **Insert file references into Codex from the editor** (`Ctrl+Alt+K` / `Cmd+Alt+K`). Sends `@path#Lstart-Lend` for the current selection, or `@path` when nothing is selected, straight into the running Codex terminal.

### Also included

- One-click launcher: click the Codex sidebar icon or press `Ctrl+Alt+Shift+C` / `Cmd+Alt+Shift+C` to open `codex` in a dedicated terminal tab.
- Paths are resolved relative to the project root, so references match Codex's own working directory.
- `Settings → Tools → Codex CLI` page with configurable command and working directory.
- Works on supported IDEs based on platform build 253+.
