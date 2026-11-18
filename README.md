# yggraHeads

![Release (latest by date)](https://img.shields.io/github/v/release/TavstalDev/yggraHeads?style=plastic-square)
![Workflow Status](https://img.shields.io/github/actions/workflow/status/TavstalDev/yggraHeads/release.yml?branch=stable&label=build&style=plastic-square)
![License](https://img.shields.io/github/license/TavstalDev/yggraHeads?style=plastic-square)
![Downloads](https://img.shields.io/github/downloads/TavstalDev/yggraHeads/total?style=plastic-square)
![Issues](https://img.shields.io/github/issues/TavstalDev/yggraHeads?style=plastic-square)

**YggraHeads** is a **free and open-source** decorative heads plugin for Minecraft servers. It provides a **graphical user interface (GUI)** for browsing and obtaining custom player heads, with category-based navigation, search, favorites, and optional economy support.

## Features

- **Category-based head browser:** Heads are organized into configurable categories (e.g. alphabet, animals, blocks, decoration, food, humanoids, humans, misc, mobs, plants).
- **GUI navigation:** Paginated menus for categories and heads, including search and favorites views.
- **Search & favorites:** Players can search for heads by name and save/remove favorites for quick access.
- **Optional economy support:** Integrates with **Vault** to charge for categories/heads when economy usage is enabled.
- **Configurable storage:** Supports **SQLite**, **MySQL/MariaDB**, and **PostgreSQL** for persistent data.
- **Optional Redis caching:** Can use Redis as a cache backend (falls back to in-memory caching when disabled).
- **Localization:** Configurable locale settings with bundled language resources (English included).
- **Update checks:** Optional startup update checking.
- **Permissions:** Per-command and category permission support, including aggregate permission nodes.

## Requirements

- **Minecraft server:** PaperMC (and other Spigot/Paper-compatible servers), versions **1.8.8 – 26.x** (compiled against the 1.8.8 API with compatibility handling for newer versions).
- **Java:** Java 8 runtime/toolchain compatible build target.
- **Optional dependency:**
  - **[Vault](https://www.spigotmc.org/resources/vault.34315/)** + a Vault-compatible economy plugin (e.g. EssentialsX, CMI) if `general.use-economy` is enabled.

> **Note:** If economy is enabled in the config but Vault/economy is not available, the plugin will disable itself on startup.

## Installation

1. Download the latest release `.jar` from the [GitHub Releases page](https://github.com/TavstalDev/yggraHeads/releases).
2. Place the `.jar` file into your server's `plugins/` directory.
3. (Optional) Install **Vault** and an economy plugin if you want to use economy features.
4. Start or restart the server to generate configuration and data files.
5. Adjust configuration as needed and run `/heads reload`.

## Commands

All commands are under `/heads`.

| Command | Description | Permission |
| :------ | :---------- | :--------- |
| `/heads` | Opens the heads GUI. | `yggraHeads.commands.heads` |
| `/heads help [page]` | Shows command help. | `yggraHeads.commands.help` |
| `/heads version` | Shows the installed plugin version. | `yggraHeads.commands.version` |
| `/heads reload` | Reloads configuration and localizations. | `yggraHeads.commands.reload` |

## Permissions

| Permission Node | Description | Default |
| :-------------- | :---------- | :------ |
| `yggraHeads.commands.heads` | Allows using `/heads` to open the GUI. | OP |
| `yggraHeads.commands.help` | Allows viewing help. | OP |
| `yggraHeads.commands.version` | Allows viewing plugin version. | OP |
| `yggraHeads.commands.reload` | Allows reloading the plugin. | OP |
| `yggraHeads.player` | Meta-permission for player commands (`heads`, `help`, `version`). | OP |
| `yggraHeads.admin` | Meta-permission for admin commands (`reload`). | OP |
| `yggraHeads.*` | Grants all YggraHeads permissions. | OP |

Category permissions are also supported via `yggraHeads.category.<name>` (as configured in `categories.yml`) when a category requires permission.

## Configuration Overview

YggraHeads generates and uses configuration/resources such as:

- `config.yml` (via plugin configuration)
  - `general.*`: locale, player locale usage, update checks, debug mode, prefix, economy toggle
  - `storage.db.*`: database type (`sqlite`, `mysql`/`mariadb`, `postgres`/`postgresql`), connection settings, table prefix, auto-clean interval, pool settings
  - `storage.redis.*`: optional Redis cache settings
- `categories.yml`: category definitions, display/localization keys, permission settings, prices, and head data source files
- `heads/*.json`: head data files referenced by categories
- `lang/*.yml`: localization messages

## Screenshots

![Preview](docs/images/img0.png)
![Preview](docs/images/img1.png)
![Preview](docs/images/img2.png)
![Preview](docs/images/img4.png)
![Preview](docs/images/img5.png)

## Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a branch for your change (e.g. `feature/...` or `fix/...`).
3. Commit your changes with clear messages.
4. Open a Pull Request describing what changed and why.

## License

This project is licensed under the **GNU General Public License v3.0**. See the [`LICENSE`](LICENSE) file for details.

## Contact

For bug reports, questions, or feature requests, please use the [GitHub issue tracker](https://github.com/TavstalDev/yggraHeads/issues).

## Credits & Disclaimer

> **Important Disclaimer:**
> This plugin utilizes and displays head data sourced from [minecraft-heads.com](https://minecraft-heads.com). We do not own, control, or claim intellectual property rights over any content originating from `minecraft-heads.com`. All credit, rights, and ownership for the custom heads displayed within YggraHeads belong to their original creators on [minecraft-heads.com](https://minecraft-heads.com).

![Minecraft Heads Banner](docs/images/minecraft-heads-large.png)