package fr.dreamin.dreamHud.api;

import fr.dreamin.dreamHud.api.player.BarHud;
import lombok.Builder;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable description of a HUD that can be rendered on top of a boss bar.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>The {@code id} must be unique inside the same {@link BarHud}.</li>
 *   <li>{@code layouts} are rendered in the order they are declared when
 *   building the HUD.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * Hud hud = Hud.builder()
 *     .id("combat-status")
 *     .zIndex(50)
 *     .layout(Layout.builder()
 *         .background("default")
 *         .element(TextElement.builder()
 *             .textSupplier(() -> "HP: " + player.getHealth())
 *             .build()))
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
@Builder
public record Hud(@NotNull String id, @Singular List<Layout> layouts, int zIndex) {

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Builds the final {@link Component} representation of this HUD by combining
   * all registered {@link Layout}s in their declared order.
   *
   * <p>This method merges each layout’s rendered content into a single
   * {@link TextComponent}, ready to be displayed within a {@link BarHud}.
   * Layouts are appended sequentially to preserve the intended composition order.</p>
   *
   * <p><strong>Behavior:</strong></p>
   * <ul>
   *   <li>Each {@link Layout} is converted into an Adventure {@link Component}
   *       via {@link Layout#toComponent()}.</li>
   *   <li>Layouts are rendered in the order they were declared when building
   *       this HUD (first declared → first rendered).</li>
   *   <li>The resulting {@link Component} is immutable and safe to reuse
   *       across multiple players or render cycles.</li>
   * </ul>
   *
   * <p><strong>Example:</strong></p>
   * <pre>{@code
   * Component hudComponent = hud.toComponent();
   * player.showBossBar(BarHud.of(player).bossBar().name(hudComponent));
   * }</pre>
   *
   * @return a fully composed Adventure {@link Component} containing
   *         all rendered layouts of this HUD
   * @see Layout#toComponent()
   * @see BarHud#update()
   *
   * @since 1.0.0
   */
  public @NotNull Component toComponent() {
    TextComponent.Builder hudComponent = Component.text();
    for (final var layout : layouts) {
      hudComponent.append(layout.toComponent());
    }
    return hudComponent.build();
  }
}
