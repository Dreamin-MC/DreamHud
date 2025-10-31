package fr.dreamin.hud.examples;

import fr.dreamin.hud.api.Hud;
import fr.dreamin.hud.api.HudService;
import fr.dreamin.hud.api.Layout;
import fr.dreamin.hud.api.element.TextElement;
import fr.dreamin.hud.DreamHud;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Basic example showing how to display a simple HUD using DreamHud.
 */
public final class BasicExample extends JavaPlugin {

  private HudService hudService;

  @Override
  public void onEnable() {
    this.hudService = DreamHud.getService(HudService.class);
    getLogger().info("BasicExample enabled. Use /welcomehud to test.");
    
    getCommand("welcomehud").setExecutor((sender, command, label, args) -> {
      if (sender instanceof Player player) {
        showWelcomeHud(player);
        return true;
      }
      sender.sendMessage("This command can only be used by players.");
      return false;
    });
  }

  private void showWelcomeHud(Player player) {
    Hud welcomeHud = Hud.builder()
        .id("welcome")
        .zIndex(5)
        .layout(
            Layout.builder()
                .background("default_bg")
                .element(
                    TextElement.builder()
                        .textSupplier(() -> "Welcome, " + player.getName() + "!")
                        .color(NamedTextColor.GOLD)
                        .font("pixelFont")
                        .build()
                )
                .build()
        )
        .build();

    // Displays the HUD every 20 ticks (1 second)
    hudService.addHud(player, "main", 20L, welcomeHud);
  }

  @Override
  public void onDisable() {
    getServer().getOnlinePlayers().forEach(p ->
        hudService.removeHud(p, "main", "welcome")
    );
  }
}
