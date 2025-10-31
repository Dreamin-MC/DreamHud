package fr.dreamin.hud.examples;

import fr.dreamin.hud.api.Hud;
import fr.dreamin.hud.api.HudService;
import fr.dreamin.hud.api.Layout;
import fr.dreamin.hud.api.element.TextElement;
import fr.dreamin.hud.api.element.TranslatableElement;
import fr.dreamin.hud.DreamHud;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced example showcasing dynamic HUD updates and multi-layer composition.
 */
public final class AdvancedExample extends JavaPlugin {

  private HudService hudService;
  private final AtomicInteger seconds = new AtomicInteger(0);

  @Override
  public void onEnable() {
    this.hudService = DreamHud.getService(HudService.class);
    getLogger().info("AdvancedExample enabled. HUD will be shown automatically to online players.");

    Bukkit.getScheduler().runTaskTimer(this, () -> {
      for (Player player : Bukkit.getOnlinePlayers()) {
        showDynamicHud(player);
      }
      seconds.incrementAndGet();
    }, 0L, 20L);
  }

  private void showDynamicHud(Player player) {
    int elapsed = seconds.get();

    Hud mainHud = Hud.builder()
        .id("stats-hud")
        .zIndex(10)
        .layout(
            Layout.builder()
                .background("dark_overlay")
                .element(
                    TranslatableElement.builder()
                        .key("hud.time_elapsed")
                        .element(
                            TextElement.builder()
                                .textSupplier(() -> elapsed + "s")
                                .color(NamedTextColor.AQUA)
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    Hud healthHud = Hud.builder()
        .id("health-hud")
        .zIndex(15)
        .layout(
            Layout.builder()
                .element(
                    TextElement.builder()
                        .textSupplier(() -> "❤ " + (int) player.getHealth() + " HP")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD, true)
                        .font("pixelFont")
                        .build()
                )
                .build()
        )
        .build();

    hudService.addHud(player, "main", 20L, mainHud);
    hudService.addHud(player, "main", 20L, healthHud);
  }

  @Override
  public void onDisable() {
    Bukkit.getOnlinePlayers().forEach(p -> hudService.removeHud(p, "main", "stats-hud"));
  }
}
