# 🧠 DreamHud — API Usage Guide

> Complete documentation for the **DreamHud API**.  
> Designed for developers integrating or extending DreamHud within Minecraft Paper 1.21.8 servers.

---

## 📚 Table of Contents

- [Installation](#installation)
- [Basic Setup](#basic-setup)
- [Core Concepts](#core-concepts)
- [Creating a HUD](#creating-a-hud)
  - [Getting the Service](#getting-the-service)
  - [Defining a HUD](#defining-a-hud)
  - [Adding or Removing HUDs](#adding-or-removing-huds)
- [Advanced Elements](#advanced-elements)
  - [Translations & Locales](#translations--locales)
  - [Custom Fonts & Styles](#custom-fonts--styles)
  - [Z-Index & Multi-HUD Management](#z-index--multi-hud-management)
- [Project Structure](#project-structure)
- [Best Practices & Performance Tips](#best-practices--performance-tips)
- [Changelog & Compatibility](#changelog--compatibility)
- [License](#license)

---

## ⚙️ Installation

Add DreamHud to your project using **Maven** or **Gradle**.

### Maven
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.Dreamin-MC</groupId>
  <artifactId>DreamHud</artifactId>
  <version>v1.0.2</version>
</dependency>
```

### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.Dreamin-MC:DreamHud:1.0.2'
}
```

> ⚠️ Make sure you’re using Java 21+ and Paper 1.21.8.

---

## 🧩 Basic Setup

If DreamHud is used as a standalone plugin, simply place it in your `/plugins` folder.  
If you use it as a library, import the API service in your main class:

```java
@Override
public void onEnable() {
    HudService hudService = DreamHud.getService(HudService.class);
    // ...your setup logic
}
```

---

## 🧠 Core Concepts

| Concept | Description |
|----------|-------------|
| **HudService** | Main entry point of the API. Manages adding, updating, and removing HUDs for players. |
| **Hud** | Represents a full visual layer (HUD) rendered through a bossbar. |
| **Layout** | Defines element positioning, backgrounds, and spacing. |
| **Element** | A displayable unit, such as `TextElement`, `TranslatableElement`, or `ImageElement`. |
| **BarHud** | Internal wrapper using a bossbar to display multiple HUDs for a player. |
| **HPlayer** | Represents a player instance that manages active BarHUDs. |

---

## 🎨 Creating a HUD

### Getting the Service
```java
HudService service = DreamHud.getService(HudService.class);
```

### Defining a HUD
```java
Hud welcomeHud = Hud.builder()
    .id("welcome-message")
    .zIndex(10)
    .layout(
        Layout.builder()
            .background("default")
            .element(
                TextElement.builder()
                    .textSupplier(() -> "Welcome " + player.getName())
                    .color(NamedTextColor.AQUA)
                    .font("default")
                    .build()
            )
            .build()
    )
    .build();
```

### Adding or Removing HUDs
```java
// Add the HUD on the "mainBar" bossbar, updating every 20 ticks (1s)
service.addHud(player, "mainBar", 20L, welcomeHud);

// Remove it when not needed
service.removeHud(player, "mainBar", "welcome-message");
```

---

## 🧭 Advanced Elements

### Translations & Locales
```java
TranslatableElement scoreHud = TranslatableElement.builder()
    .key("hud.score.display") // key from resource pack
    .element(
        TextElement.builder()
            .textSupplier(() -> String.valueOf(playerScore))
            .build()
    )
    .build();
```

### Custom Fonts & Styles
```java
TextElement styledText = TextElement.builder()
    .textSupplier(() -> "❤  100")
    .font("futuristic")
    .color(NamedTextColor.RED)
    .decoration(TextDecoration.BOLD, true)
    .build();
```

### Z-Index & Multi-HUD Management
Multiple HUDs can be layered on the same bossbar using different z-index values:  
- Higher `zIndex` = displayed above others.  
- Lower `zIndex` = displayed behind.

---

## 🗂️ Project Structure

```
📦 DreamHud
 ┣ 📁 assets/             → Visual resources, fonts, and images
 ┣ 📁 examples/           → Example usages and demonstration plugins
 ┣ 📁 src/
 ┃ ┣ 📁 api/              → Public interfaces & builders (Hud, Layout, Element…)
 ┃ ┗ 📁 internal/         → Internal rendering logic and implementations
 ┣ 📄 API_USAGE_GUIDE.md  → This documentation file
 ┗ 📄 README.md           → Project presentation
```

---

## 🧠 Best Practices & Performance Tips

- Avoid refreshing HUDs too frequently (prefer ≥ 5 ticks intervals).  
- Reuse HUD and Layout instances instead of recreating them for every player.  
- Group similar HUDs under one bossbar when possible.  
- Use resource packs to manage custom fonts efficiently.  
- Always clear active HUDs on player quit or plugin disable to prevent ghost bars.

---

## 🕓 Changelog & Compatibility

| Version  | Status | Notes                                          |
|----------|---------|------------------------------------------------|
| `v1.0.2` | 🧪 Active Development | First public beta compatible with Paper 1.21.8 |
| `v2.x`   | 🔜 Planned | Major refactor & API stabilization             |

**Compatibility:**  
✅ Paper 1.21.8 
✅ Java 21+  
⚠️ Folia are not officially supported yet.

---

