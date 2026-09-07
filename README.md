# Zenith Client

[English](README.md) | [Русский](README.ru.md)

A Fabric client-side mod for Minecraft 1.21.6.

## ⚠ WARNING!

**This mod may be unstable. If you find bugs or have suggestions/fixes, reach out via email — _solareclipse12@inbox.eu_ — or Discord — _zenith.client_support_**.

## Features

- **Biome entry notification** — a short notification appears when you enter a new biome, with unique text per biome.
- **Item search in chests** — a "filter/highlight" toggle: depending on the mode, non-matching items are either dimmed or matching items are highlighted.
- **Real-time clock** — a clock in the top-left corner that matches your device's system time.
- **In-game clock** — next to the real-time clock, shows how much time is left until night/dawn.
- **Damage counter** — displays how much damage was dealt and to which mob/player when you hit them.
- **Coordinates** — subtle player coordinates shown at the bottom-left of the inventory screen.
- **Biome particles** — faint ambient particles matching the current biome.
- **Glowing cursor** — a custom glowing arrow replaces the OS cursor, with a trail of flickering sparks and a soft ambient glow that smoothly follows the mouse.
- **Mod menu** — opens with a configurable key (default `Right Shift`), lets you toggle each feature individually.
- **Built-in resource pack (`Zenith GUI`)** — a dark UI theme, toggled directly from the mod menu (not manageable from Options → Resource Packs — only via the mod menu or the pack file).
- **A bunch of smaller extras** — text color adjustments, cursor particles, and more.

## Requirements

- Minecraft 1.21.6
- Fabric Loader
- Fabric API

## Installation

Download `zenith-client-1.0.0-beta.1.jar` from the "Releases" tab and place it in your Minecraft `mods` folder.

**Or, for development:**
```bash
git clone https://github.com/pxrfectxx14/Zenith-Client
cd zenith-client-template-1.21.6
./gradlew runClient
```

## Project structure

```
src/main/java/dev/zenith/client/
├── feature/          # Base feature classes
├── menu/             # Mod menu screen, feature list, resource pack registration
├── mixin/            # Mixins
└── ZenithClientClient.java   # Client entry point

src/main/resources/
├── assets/zenith-client/     # Textures, language files, cursor/spark sprites
└── resourcepacks/zenith-gui/ # Built-in dark resource pack
```

## Controls

| Action              | Default key |
|---------------------|-------------|
| Open mod menu        | `Right Shift` |

The key can be rebound in **Controls → Zenith Client**.

## Known limitations

- 1.21.6 uses Minecraft's new two-phase GUI render pipeline — parts of the internal API are still unstable between patch versions, so updating to a newer Minecraft version may require changes to the mod's rendering code.