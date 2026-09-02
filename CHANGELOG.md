<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Wsl-macros Changelog

## [Unreleased]

## [0.1.0] - 2026-09-02
### Added
- Wsl-prefixed macros that always expand to the Windows-visible path, bypassing IntelliJ's WSL path translation when a plain Windows tool consumes a file on a WSL mount:
  - `$WslFilePath$` — absolute Windows path of the current file
  - `$WslFileDir$` — absolute Windows path of the directory containing the current file
  - `$WslProjectFileDir$` — absolute Windows path of the current project directory
  - `$WslProjectpath$` — absolute Windows path of the current project source path
  - `$WslContentRoot$` — absolute Windows path of the content root containing the current file
- Compatibility with IntelliJ IDEA 2026.1+.
