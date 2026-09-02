package ro.pieceofcode.jetbrains.plugin.wslmacros

import com.intellij.execution.wsl.WslDistributionManager
import com.intellij.ide.macro.Macro
import com.intellij.openapi.actionSystem.DataContext

/**
 * Converts a VFS path into the Windows-visible form.
 *
 * Normally the VFS already reports WSL-mounted projects in UNC form with forward slashes
 * (e.g. `//wsl.localhost/Ubuntu/home/user/file.txt`), so we only normalize separators to
 * backslashes. If the path came in as a Linux-style absolute path (e.g. `/home/user/file.txt`),
 * we reconstruct the `\\wsl.localhost\<Distro>\...` form so a plain Windows tool always receives
 * a path it can resolve. Where a built-in macro expands to the Linux-side path for a file on a
 * WSL mount, this keeps the value Windows-visible.
 *
 * @param wslDistro resolves the WSL distribution id used when rebuilding a Linux-style path.
 *                  Injectable so the reconstruction branch is testable without the platform.
 */
fun toWindowsPath(path: String, wslDistro: () -> String? = ::defaultWslDistro): String {
    if (path.startsWith("/") && !path.startsWith("//")) {
        val distro = wslDistro() ?: return path
        return "\\\\wsl.localhost\\" + distro + path.replace('/', '\\')
    }
    return path.replace('/', '\\')
}

/** First installed WSL distribution id (e.g. "Ubuntu"), or null when WSL is unavailable. */
private fun defaultWslDistro(): String? =
    WslDistributionManager.getInstance().installedDistributions.firstOrNull()?.msId

/**
 * Base class for the Wsl macros: supplies the name and description, and runs every produced
 * value through [toWindowsPath].
 */
abstract class WslMacroBase(
    private val macroName: String,
    private val macroDescription: String,
) : Macro() {
    override fun getName(): String = macroName

    override fun getDescription(): String = macroDescription
}

/**
 * Base class for Wsl macros whose value is an absolute filesystem path that should be forced
 * into the Windows-visible form.
 */
abstract class WslPathMacro(
    name: String,
    description: String,
) : WslMacroBase(name, description) {

    override fun expand(dataContext: DataContext): String? =
        expandPath(dataContext)?.let(::toWindowsPath)

    protected abstract fun expandPath(dataContext: DataContext): String?
}
