package com.github.codexjb.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager
import com.intellij.terminal.frontend.view.TerminalView
import com.intellij.ui.content.Content
import java.lang.ref.WeakReference
import kotlinx.coroutines.runBlocking

/**
 * Owns the per-project "Codex" terminal widget.
 *
 * - [launchOrFocus] creates the tab and runs the configured Codex command if no live tab exists,
 *   focuses the existing Codex tab if it is still running, or restarts Codex in that tab when
 *   the terminal is still open but the foreground command has exited.
 * - [insertText] writes raw text into the live tab's PTY without pressing Enter, so the user can
 *   append their own prompt before submitting.
 */
@Service(Service.Level.PROJECT)
class CodexTerminalService(private val project: Project) {

    private val log = Logger.getInstance(CodexTerminalService::class.java)
    private var tabRef: WeakReference<TerminalToolWindowTab>? = null
    private var lastCommandSentAtMillis: Long = 0L

    fun launchOrFocus(): TerminalView {
        val existing = findCodexTab()
        if (existing != null) {
            tabRef = WeakReference(existing)
            focusTerminalContent(existing.content)
            // hasChildProcesses() must not run on the EDT. Check on a pooled thread and
            // restart Codex on the EDT if the shell is idle.
            ApplicationManager.getApplication().executeOnPooledThread {
                val start = shouldStartCodex(existing.view)
                if (start) {
                    ApplicationManager.getApplication().invokeLater {
                        if (existing.content.isValid) {
                            runCodexCommand(existing.view)
                        }
                    }
                }
            }
            return existing.view
        }

        val tab = createNew()
        tabRef = WeakReference(tab)
        return tab.view
    }

    fun insertText(text: String) {
        val selectedTerminal = findSelectedTerminalTab() ?: run {
            log.warn("No selected terminal tab is available; cannot insert text.")
            return
        }
        val widget = selectedTerminal.widget
        val connector = widget.ttyConnector
        if (connector == null) {
            log.warn(
                "Selected terminal TTY connector is not ready; cannot insert text yet. " +
                    "Try again once the terminal has finished starting."
            )
            return
        }

        try {
            tab.view.sendText(text)
        } catch (t: Throwable) {
            log.warn("Failed to write to Codex terminal", t)
            return
        }
    }

        focusTerminalContent(tab.content)
    }

    private fun findSelectedTerminalTab(): TerminalTab? {
        val selectedContent: Content = ToolWindowManager.getInstance(project)
            .getToolWindow(TERMINAL_TOOL_WINDOW_ID)
            ?.contentManager
            ?.selectedContent
            ?: return null
        return terminalTabForContent(selectedContent)
    }

    private fun terminalTabForContent(content: Content): TerminalTab? {
        if (!content.isValid) return null
        val widget = TerminalToolWindowManager.findWidgetByContent(content) ?: return null
        return widget.takeIf(::isWidgetAlive)?.let { TerminalTab(content, it) }
    }

    private fun findCodexTab(): TerminalToolWindowTab? {
        val cached = cachedAlive()
        if (cached != null) {
            markCodexContent(cached.content)
            return cached
        }

        return TerminalToolWindowTabsManager.getInstance(project).tabs
            .asSequence()
            .filter { it.content.isValid && isCodexContent(it.content) }
            .onEach { markCodexContent(it.content) }
            .firstOrNull()
    }

    private fun createNew(): TerminalToolWindowTab {
        val settings = CodexSettings.getInstance(project).state
        val workDir = settings.workingDirectory.ifBlank { project.basePath.orEmpty() }
        val tab = TerminalToolWindowTabsManager.getInstance(project)
            .createTabBuilder()
            .workingDirectory(workDir)
            .tabName(TAB_NAME)
            .requestFocus(true)
            .deferSessionStartUntilUiShown(true)
            .createTab()
        markCodexContent(tab.content)
        runCodexCommand(tab.view)
        return tab
    }

    private fun focusTerminalContent(content: Content) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TERMINAL_TOOL_WINDOW_ID)
            ?: return
        val selectContent = Runnable {
            val manager = content.manager ?: toolWindow.contentManager
            if (content.isValid && manager.getIndexOfContent(content) >= 0) {
                manager.setSelectedContent(content, true)
                manager.requestFocus(content, true)
            }
        }
        if (toolWindow.isActive) {
            selectContent.run()
        } else {
            toolWindow.activate(selectContent, true)
        }
    }

    private fun focusTerminalToolWindow() {
        ToolWindowManager.getInstance(project)
            .getToolWindow(TERMINAL_TOOL_WINDOW_ID)
            ?.activate(null, true)
    }

    private fun shouldStartCodex(view: TerminalView): Boolean {
        if (System.currentTimeMillis() - lastCommandSentAtMillis < COMMAND_START_GRACE_MS) return false
        val hasRunningCommand = hasRunningCommand(view)
        return hasRunningCommand == false
    }

    /**
     * Returns true only when it's safe to inject @path text into the terminal.
     *
     * Behavior for the insert shortcut:
     * - If Codex is running: insert immediately.
     * - If shell is idle: start Codex and wait for the next shortcut press.
     * - If command just started: avoid injecting text too early.
     * - If command state is unknown: allow insertion after the grace window as a fallback.
     */
    private fun isInsertSafe(view: TerminalView): Boolean {
        val inGrace = System.currentTimeMillis() - lastCommandSentAtMillis < COMMAND_START_GRACE_MS
        if (inGrace) {
            return false
        }
        val hasRunningCommand = hasRunningCommand(view)
        return when (hasRunningCommand) {
            true -> true
            false -> {
                runCodexCommand(view)
                false
            }
            null -> true
        }
    }

    private fun runCodexCommand(view: TerminalView) {
        val command = CodexSettings.getInstance(project).state.codexCommand
            .ifBlank { CodexSettings.DEFAULT_COMMAND }
        try {
            view.createSendTextBuilder()
                .shouldExecute()
                .send(command)
            lastCommandSentAtMillis = System.currentTimeMillis()
        } catch (t: Throwable) {
            log.warn("Failed to start Codex command in terminal", t)
        }
    }

    private fun hasRunningCommand(view: TerminalView): Boolean? {
        return try {
            runBlocking { view.hasChildProcesses() }
        } catch (t: Throwable) {
            log.debug("Cannot determine terminal command state; leaving existing tab untouched", t)
            null
        }
    }

    private fun cachedAlive(): TerminalToolWindowTab? {
        val tab = tabRef?.get() ?: return null
        return if (tab.content.isValid) tab else null
    }

    private fun isCodexContent(content: Content): Boolean {
        return content.getUserData(CODEX_CONTENT_KEY) == true ||
            content.displayName == TAB_NAME ||
            content.tabName == TAB_NAME ||
            content.toolwindowTitle == TAB_NAME
    }

    private fun markCodexContent(content: Content) {
        content.putUserData(CODEX_CONTENT_KEY, true)
    }

    companion object {
        const val TAB_NAME: String = "Codex"
        private const val TERMINAL_TOOL_WINDOW_ID = "Terminal"
        private const val COMMAND_START_GRACE_MS: Long = 2_000L
        private val CODEX_CONTENT_KEY: Key<Boolean> = Key.create("com.github.codexjb.codexTerminal")

        fun getInstance(project: Project): CodexTerminalService = project.service()
    }
}
