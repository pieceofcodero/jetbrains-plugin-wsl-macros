package ro.pieceofcode.jetbrains.plugin.wslmacros

import org.junit.Assert.assertEquals
import org.junit.Test

class WslPathMacroTest {

    @Test
    fun uncPathWithForwardSlashesIsNormalizedToBackslashes() {
        assertEquals(
            "\\\\wsl.localhost\\Ubuntu\\home\\user",
            toWindowsPath("//wsl.localhost/Ubuntu/home/user"),
        )
    }

    @Test
    fun linuxPathIsRebuiltAsUncPathWithWslDistro() {
        assertEquals(
            "\\\\wsl.localhost\\Ubuntu\\home\\user\\file.txt",
            toWindowsPath("/home/user/file.txt") { "Ubuntu" },
        )
    }

    @Test
    fun plainWindowsPathKeepsItsForm() {
        assertEquals(
            "C:\\foo\\bar",
            toWindowsPath("C:/foo/bar"),
        )
    }

    @Test
    fun linuxPathWithNoWslDistroIsReturnedUnchanged() {
        assertEquals(
            "/home/user/file.txt",
            toWindowsPath("/home/user/file.txt") { null },
        )
    }

    @Test
    fun windowsBackslashPathIsLeftAsIs() {
        assertEquals(
            "C:\\foo\\bar",
            toWindowsPath("C:\\foo\\bar"),
        )
    }
}
