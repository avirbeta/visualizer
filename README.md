# Visualizer

**Pure-client cosmetic HUD mod for Minecraft 1.16.5 (Fabric).** Visuals only — no gameplay, no server, no network changes.

- Customizable in-game **HUD overlay** with movable, individually-scalable elements
- **7 themes** + **11 accent colors** (including a live rainbow `chroma`)
- **HUD editor** — drag elements around, snap to grid, resize
- **Animations, UI sounds, toast notifications, ambient particles**
- **Adaptive quality** — auto-hides the heaviest elements when FPS drops, restores when it recovers
- Never touches game logic: safe against cheat detection, purely informational display

> Visualizer is cosmetic software — like shaders or resource packs. It does not automate gameplay, modify packets, or read memory.

## Features

### HUD elements
| Element | Shows |
|---|---|
| FPS | Frames per second |
| FPS graph | Live 28-bar FPS sparkline |
| Ping | Latency to server |
| Coordinates | Current XYZ |
| Clock | Real time (HH:mm:ss) |
| Day / Time | In-game day + time of day |
| Direction | Facing direction |
| Biome | Current biome name |
| Light | Sky + block light at player |
| Server | Server name / address |
| Keystrokes | WASD + space input indicator |
| Armor | Armor pieces equipped |
| Status | HP + food level |
| Watermark | Mod version badge |

Every element can be shown/hidden, moved, and scaled in the **HUD Editor**.

### Themes
`dark`, `light`, `midnight`, `forest`, `sunrise`, `graphite`, `candy`

### Accents
`emerald`, `amber`, `sky`, `rose`, `violet`, `mint`, `orange`, `pink`, `gold`, `aqua`, `chroma` (rainbow cycle)

## Installation
1. Install **Fabric Loader** for Minecraft **1.16.5** and **Fabric API** (`fabric` runtime dependency).
2. Copy `visualizer-1.0.0.jar` into the `mods/` folder.
3. Launch. Open the menu from the main menu button or a keybind.

## Usage
- **Main menu**: small **Visualizer** button in the corner opens the settings menu.
- **Sections**: Appearance · HUD · Animation · Sound · Keybinds · Performance · About.
- **HUD Editor**: set a keybind (Keybinds section) or open from the HUD section.

Default keybindings (configurable):

| Action | Default |
|---|---|
| Open Visualizer menu | `V` |
| Toggle HUD | `F7` |
| HUD Editor | `F8` |
| Compact mode | `F9` |
| Quick settings | `F6` |

## Configuration
Settings are stored in `config/visualizer.json` (auto-created on first launch). Everything is editable in-game; the file is there for backups or manual tweaks.

## Building from source
Requires JDK 8+.

```bash
./gradlew build
```

The built mod appears at `build/libs/visualizer-1.0.0.jar`.

## Mod list (dependencies)
- `fabric-api` (runtime dependency declared as `fabric`)

## Disclaimer
This project's source code contains **no cheat functionality** — it only renders UI overlays. Server rules about allowed clients vary; check your server's rules.