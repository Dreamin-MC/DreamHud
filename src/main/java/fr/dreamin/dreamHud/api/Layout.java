package fr.dreamin.dreamHud.api;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.element.Element;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService;
import fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Describes the arrangement of {@link Element elements} inside a single HUD layer.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>The {@code elements} list is evaluated in order when building the Adventure component.</li>
 *   <li>{@code offsetX} shifts the layout to the right; negative values shift to the left.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * Layout layout = Layout.builder()
 *     .background("rounded")
 *     .offsetX(4)
 *     .element(TextElement.builder()
 *         .textSupplier(() -> "Objective")
 *         .build())
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
@Getter
@Builder
public final class Layout {
  private String background;
  @Singular private final List<Element> elements;
  private final int offsetX;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Builds the Adventure {@link Component} representation of this layout,
   * including its background, spacing offsets, and contained {@link Element}s.
   *
   * <p>This method orchestrates the rendering of a single HUD layer, combining
   * visual backgrounds, negative-space offsets, and element components into
   * one contiguous {@link TextComponent}.</p>
   *
   * <p><strong>Behavior:</strong></p>
   * <ul>
   *   <li>All {@link Element}s are rendered in sequence via {@link Element#toComponent()}.</li>
   *   <li>If a {@link #getBackground()} is defined, it is rendered first using
   *       {@link fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService}.</li>
   *   <li>Negative spacing and pixel alignment are handled through the
   *       {@link fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService} to ensure
   *       pixel-perfect horizontal alignment between background and content.</li>
   *   <li>Internal debug information (widths, offsets, etc.) can be displayed in-game
   *       if debug mode is enabled in {@link CodexService#getConfig()}.</li>
   * </ul>
   *
   * <p><strong>Rendering sequence:</strong></p>
   * <ol>
   *   <li>Compute the message width based on all {@link Element#getPixelWidth()} values.</li>
   *   <li>Render the background adjusted to match that width.</li>
   *   <li>Apply a negative space offset to align the text over the background.</li>
   *   <li>Append all element components.</li>
   *   <li>Apply a final positive offset to restore spacing after the layout.</li>
   * </ol>
   *
   * <p><strong>Example:</strong></p>
   * <pre>{@code
   * Component layoutComponent = layout.toComponent();
   * barHud.bossBar().name(layoutComponent);
   * }</pre>
   *
   * @return a fully rendered Adventure {@link Component} representing this layout,
   *         including background, offsets, and element components
   * @see Element#toComponent()
   * @see fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService
   * @see fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService
   *
   * @since 1.0.0
   */
  public @NotNull Component toComponent() {
    final var backgroundLoader = DreamHud.getService(BackgroundLoaderService.class);
    final var negSpaceFont = DreamHud.getService(NegSpaceFontService.class);
    final var codexService = DreamHud.getService(CodexService.class);
    final var msgWidth = this.elements.stream().mapToInt(Element::getPixelWidth).sum();
    final var bgWidth = backgroundLoader.getRealWidth(msgWidth, background);
    final var negOffset = backgroundLoader.getTotalNegativeOffset(msgWidth, background);
    final var remove = -msgWidth - 8;
    final var add = 4;

    if (codexService.getConfig().debug) {
      Bukkit.broadcast(Component.text("Hud Layout", NamedTextColor.GOLD)
        .appendNewline().append(Component.text("| msgWidth=" + msgWidth, NamedTextColor.GOLD))
        .appendNewline().append(Component.text("| bgWidth=" + bgWidth, NamedTextColor.GOLD))
        .appendNewline().append(Component.text("| negOffset=" + negOffset, NamedTextColor.GOLD))
        .appendNewline().append(Component.text("| remove=" + remove, NamedTextColor.GOLD))
        .appendNewline().append(Component.text("| add=" + add, NamedTextColor.GOLD))
      );
    }

    Component result = Component.empty();
    if (background != null)
      result = result.append(backgroundLoader.generateBackground(msgWidth, background))
        .append(negSpaceFont.fromOffset(remove));

    result = result.append(getElementComponent());
    if (background != null)
      result = result.append(negSpaceFont.fromOffset(add));

    return result;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private Component getElementComponent() {
    TextComponent.Builder elementComponent = Component.text();

    for (final var element : this.elements) {
      elementComponent.append(element.toComponent());
    }

    return elementComponent.build();
  }

}
