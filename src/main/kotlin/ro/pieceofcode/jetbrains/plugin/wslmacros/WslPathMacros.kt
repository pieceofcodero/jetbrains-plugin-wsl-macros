package ro.pieceofcode.jetbrains.plugin.wslmacros

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.roots.ProjectFileIndex

/** `$WslFilePath$` — absolute Windows path of the current file (replaces `$FilePath$`). */
class WslFilePathMacro : WslPathMacro(
    name = "WslFilePath",
    description = "Absolute Windows path of the current file (bypasses WSL path translation)",
) {
    override fun expandPath(dataContext: DataContext): String? =
        dataContext.getData(CommonDataKeys.VIRTUAL_FILE)?.path
}

/** `$WslFileDir$` — absolute Windows path of the directory containing the current file. */
class WslFileDirMacro : WslPathMacro(
    name = "WslFileDir",
    description = "Absolute Windows path of the directory containing the current file (bypasses WSL path translation)",
) {
    override fun expandPath(dataContext: DataContext): String? =
        dataContext.getData(CommonDataKeys.VIRTUAL_FILE)?.parent?.path
}

/** `$WslProjectFileDir$` — absolute Windows path of the current project directory. */
class WslProjectFileDirMacro : WslPathMacro(
    name = "WslProjectFileDir",
    description = "Absolute Windows path of the current project directory (bypasses WSL path translation)",
) {
    override fun expandPath(dataContext: DataContext): String? =
        dataContext.getData(CommonDataKeys.PROJECT)?.basePath
}

/** `$WslProjectpath$` — absolute Windows path of the current project source path. */
class WslProjectpathMacro : WslPathMacro(
    name = "WslProjectpath",
    description = "Absolute Windows path of the current project source path (bypasses WSL path translation)",
) {
    override fun expandPath(dataContext: DataContext): String? =
        dataContext.getData(CommonDataKeys.PROJECT)?.basePath
}

/** `$WslContentRoot$` — absolute Windows path of the content root containing the current file. */
class WslContentRootMacro : WslPathMacro(
    name = "WslContentRoot",
    description = "Absolute Windows path of the content root containing the current file (bypasses WSL path translation)",
) {
    override fun expandPath(dataContext: DataContext): String? {
        val file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return null
        return ProjectFileIndex.getInstance(project).getContentRootForFile(file)?.path
    }
}
