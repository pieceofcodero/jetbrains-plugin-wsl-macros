# WSL Macros

WSL-safe replacements for IntelliJ's built-in path macros. `Wsl`-prefixed macros —
`$WslFilePath$`, `$WslFileDir$`, `$WslProjectFileDir$`, `$WslProjectpath$`,
`$WslContentRoot$` — always expand to the **Windows-visible** path
(`\\wsl.localhost\…`) for files on a WSL mount, matching what the built-in macro
previews show.

> Compatible with IntelliJ Platform **2026.1+** (IntelliJ IDEA and all JetBrains IDEs).

## Why

When an IDE opens a project that lives on a WSL mount, built-in path macros behave
inconsistently. `$FilePath$` *previews* as the Windows UNC path
(`\\wsl.localhost\Ubuntu\home\user\project\Main.kt`), but *expands* at runtime to
the Linux path (`/home/user/project/Main.kt`). External Tools receive the wrong
value — a plain Windows tool gets a Linux path it cannot open.

This is a platform regression first reported in September 2025, predating the 2026.x
line: [IJPL-207641](https://youtrack.jetbrains.com/issue/IJPL-207641) —
"(External Tools) Macro values are not correct when using WSL" (IntelliJ Platform,
subsystem "Core. WSL") — still open. The same symptom was filed separately for
CLion ([CPP-49454](https://youtrack.jetbrains.com/issue/CPP-49454), resolved as a
duplicate).

Built-in macros are registered by name through the `com.intellij.macro` extension
point, so a plugin cannot override them. This plugin registers **new** `Wsl`-prefixed
macros instead, which always expand to the Windows-visible UNC form — matching what
the macro preview shows.

## Macros

| Macro | Replaces | Expands to |
|---|---|---|
| `$WslFilePath$` | `$FilePath$` | Windows path of the current file |
| `$WslFileDir$` | `$FileDir$` | Windows path of the directory containing the current file |
| `$WslProjectFileDir$` | `$ProjectFileDir$` | Windows path of the current project directory |
| `$WslProjectpath$` | `$Projectpath$` | Windows path of the current project source path |
| `$WslContentRoot$` | `$ContentRoot$` | Windows path of the content root containing the current file |

**Example** — current file `/home/user/project/Main.kt` on a WSL distro named `Ubuntu`:

```
$FilePath$       → /home/user/project/Main.kt                                    (Linux — unusable by Windows tools)
$WslFilePath$    → \\wsl.localhost\Ubuntu\home\user\project\Main.kt
```

## Install

<!-- TODO: add the Marketplace listing URL once the plugin is published -->
- From the IDE: **Settings → Plugins → Marketplace**, search for **WSL Macros**.
- From [JetBrains Marketplace](https://plugins.jetbrains.com/) once the listing is live.

Requires an IntelliJ Platform IDE **2026.1 or newer**.

> Note: the regression this plugin works around is older than 2026.1 — it also
> affects, e.g., 2025.3 (see [CPP-49454](https://youtrack.jetbrains.com/issue/CPP-49454)).
> Because the plugin targets 2026.1+, those affected versions cannot use this
> workaround yet.

## Usage

The `Wsl` macros are available anywhere IntelliJ expands built-in macros — such as
**Settings → Tools → External Tools**, file templates, and run configurations.
Swap `$FilePath$` for `$WslFilePath$` (and so on) wherever the macro's value is
consumed by a tool that is **not** WSL-aware.

## Development

Building, testing, and publishing are documented in [DEVELOPMENT.md](DEVELOPMENT.md).
Changes are tracked in [CHANGELOG.md](CHANGELOG.md).
