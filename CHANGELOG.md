<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Wsl-macros Changelog

## [Unreleased]

## [0.1.0] - 2026-09-02
### Added
- Wsl-prefixed macros that always expand to the Windows-visible path, so a plain Windows tool consuming a file on a WSL mount always receives a path it can open:
  - `$WslFilePath$` — absolute Windows path of the current file
  - `$WslFileDir$` — absolute Windows path of the directory containing the current file
  - `$WslProjectFileDir$` — absolute Windows path of the current project directory
  - `$WslProjectpath$` — absolute Windows path of the current project source path
  - `$WslContentRoot$` — absolute Windows path of the content root containing the current file
- Compatibility with IntelliJ Platform 2025.3+.
