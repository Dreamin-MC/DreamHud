package fr.dreamin.dreamHud.internal.cmd.admin;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.HudService;
import fr.dreamin.dreamHud.api.Layout;
import fr.dreamin.dreamHud.api.element.TextElement;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class AdminCmd {

  private final DreamHud plugin;

  private BossBar bossBarKyori = null;

  @CommandDescription("Test")
  @CommandMethod("test1 <message>")
  @CommandPermission("test")
  private void test(
    CommandSender sender,
    @Argument("message") @Greedy String message
    ) {
    if (!(sender instanceof Player player)) return;



  }

  @CommandDescription("Test")
  @CommandMethod("test2 <message>")
  @CommandPermission("test")
  private void test2(
    CommandSender sender,
    @Argument("message") @Greedy String message
  ) {
    if (!(sender instanceof Player player)) return;

    AtomicInteger i = new AtomicInteger();

    final var element = TextElement.builder()
      .id("e1")
      .textSupplier(() -> String.valueOf(i.get()))
      .font("test")
      .color(NamedTextColor.RED)
      .build();

    final var layout = Layout.builder()
      .background("default")
      .element(element)
      .build();

    final var hud = Hud.builder()
      .id("test")
      .layout(layout)
      .build();

    final var hudService = DreamHud.getService(HudService.class);

    hudService.addHud(player, "main", 20, hud);
    Bukkit.getScheduler().runTaskTimer(DreamHud.getInstance(), i::getAndIncrement, 20, 20);

  }
}
