# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) where applicable.

## [1.2.0] - 2026-11-17

### Added
- Optional Vault economy integration for purchasable heads/categories (configurable).
- Configurable storage backends: SQLite, MySQL/MariaDB, and PostgreSQL.
- Optional Redis cache support (with in-memory cache fallback).

### Changed
- Renamed the project/plugin from **OpenHeads** to **YggraHeads**.
- Migrated core functionality to **yggraLib** (plugin bootstrap, services, localization, database abstractions, caching, GUI framework integration).
- Refactored GUI implementation to use the yggraLib/triumph-gui-based menu system.
- Restructured configuration and category/head data handling with typed models.
- Updated build configuration for Spigot API/yggraLib-based workflow.

## [1.1.1] - 2025-10-29

### Added
- Plugin usage metrics/analytics support.

### Changed
- Improved command handling and help message formatting/functionality.
- Refactored player data management and configuration handling.
- Made GUI icon materials configurable.
- Updated dependencies/libraries (including MineCoreLib and sign GUI library updates during the 1.1.x line).
- Improved update status checking/logging.
- Refactored logging for consistency.

### Removed
- Removed ProtocolLib dependency.

### Fixed
- Fixed help navigation commands to use the correct help command.

## [1.0.0] - 2025-7-18

### Added
- Initial release of the heads menu plugin with GUI support for browsing and obtaining decorative/custom heads.

[1.2.0]: https://github.com/TavstalDev/yggraHeads/compare/v1.1.1...HEAD
[1.1.1]: https://github.com/TavstalDev/yggraHeads/compare/v1.0.0...v1.1.1
[1.0.0]: https://github.com/TavstalDev/yggraHeads/releases/tag/v1.0.0
