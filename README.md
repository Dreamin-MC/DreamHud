# ✨ DreamHud

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/dreamin/dreamintablist)
[![](https://jitpack.io/v/Dreamin-MC/DreamHud.svg)](https://jitpack.io/#Dreamin-MC/DreamHud)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.8-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/license-GPL-lightgrey.svg)](LICENSE)

---

### 🧠 Overview

**DreamHud** is a modern and high-performance library for **displaying dynamic HUDs** on **Minecraft Paper 1.21+** servers using the **bossbar** system.
Designed for **customization**, **smoothness**, and **creativity**, it allows developers to create immersive and completely **custom-made** interfaces.

---

### ⚡ Key Features

- 🎨 **Dynamic Components** — Build your HUDs from `Layout`, `Hud`, and `Element`.
- 🧩 **Seamless Integration** — Compatible with all plugins and APIs of the Dreamin Ecosystem.
- 🧠 **Intelligent Rendering** — Automatically manages offsets, spacing, and display priorities.
- 🔤 **Full support for custom fonts** (via resource pack).
- 🪶 **Ultra-lightweight and optimized** — Zero lag, instant rendering via bossbars.
- 🛠️ **Intuitive API** — Uses the latest features of Adventure & Kyori Components.

---

### 🧭 Project Goal

The idea behind DreamHud is to **replace the basic Minecraft HUDs** with a **flexible and modular** system capable of:
- displaying **contextual information** (health, score, objectives, timers, etc.),
- animating **visual scenes** or event transitions,
- allowing servers to have a **unique visual identity**.

DreamHud is **used in production** on projects within the **Dreamin'** ecosystem, particularly for dynamic HUDs, cinematics, and immersive interfaces. ---

### 🧱 Project Structure

```
📦 HudLib
┣ 📁 changelog/
┃ ┗ … (file changelog.)
┣ 📁 assets/
┃ ┗ … (visual resources, images, etc.)
┣ 📁 examples/
┃ ┗ … (usage examples, practical cases)
┣ 📁 src/
┃ ┣ 📁 api/
┃ ┃ ┗ … (public interfaces, builders, etc.)
┃ ┣ 📁 internal/
┃ ┃ ┗ … (internal implementations, rendering logic, etc.)
┣ 📄 API_USAGE_GUIDE.md
┗ 📄 README.md
```

---

### 🧩 Developer Integration

#### 💻 Adding as a Dependency

**Maven**
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
<version>Tag</version>
</dependency>
```

**Gradle**
```groovy
repositories {
maven { url "https://jitpack.io" }
}
dependencies {
compileOnly 'com.github.Dreamin-MC:DreamHud:Tag'
}
```

---

### 🚀 Quick Overview

![Banner](/assets/image.png)

➡️ Each block is generated **dynamically** via the player's bossbar.
Texts, colors, fonts, and positions are **100% configurable** via code or a JSON file.

---

### 🧃 Dreamin’ Ecosystem

DreamHud is part of the **Dreamin Ecosystem**, a set of modular projects designed to enrich the server-side Minecraft experience:

| Project | Description |
|--------|--------------|
| 🧠 **DreaminAPI** | Central and abstract API common to all modules. |
| 💬 **DreaminTabList** | Advanced TabList management, fully customizable. |
| 🌈 **DreamHud** | Dynamic display of HUDs and overlays. |

---

### 🛠️ Project Status

| État | Version        | Compatibilité             |
|------|----------------|---------------------------|
| 🧪 Under active development | `v1.0.0`       | Paper 1.21.8              |
| 🔜 Final migration | `DreamHud` (v2) | Stable and public API planned |

> ⚠️ *The current version (DreamHud v1) serves as the technical foundation for DreamHud v2.
Major API changes are to be expected.*

---

### 📚 Documentation

📘 **[→ API Usage Guide](API_USAGE_GUIDE.md)**
Contains:
- detailed code examples;
- complete documentation of public classes;
- integration examples with other modules of the Dreamin Ecosystem.

---

### 🤝 Contributing

Contributions are welcome!
Before any PR, please check:
- that the code follows the general project style;
- that the added features are tested and documented;
- that Minecraft/Paper 1.20+ compatibility is maintained.

---

### 📄 License

This project is distributed under the **GPL-3.0** license.
➡️ [See the LICENSE file](LICENSE)

---

## 📞 Support

- **Documentation**: [API Usage Guide](API_USAGE_GUIDE.md)
- **Examples**: [examples/](examples/)
- **Issues**: [GitHub Issues](https://github.com/dreamin/dreamhud/issues)
- **Discord**: [Join our Discord](https://discord.gg/dreamin)

---

![Banner](/assets/banner.png)

**Made with ❤️ by the Dreamin Studio**
