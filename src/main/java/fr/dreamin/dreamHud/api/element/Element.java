package fr.dreamin.dreamHud.api.element;

import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Base type for every renderable HUD element.
 * Implementations expose Adventure components and know how wide they are.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>The element id is optional but, when set, should be unique inside a {@link fr.dreamin.dreamHud.api.Layout}.</li>
 *   <li>{@code offsetX} is applied before the element content is appended to the layout.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * TextElement health = TextElement.builder()
 *     .id("health")
 *     .textSupplier(() -> String.valueOf(player.getHealth()))
 *     .offsetX(2)
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter @Setter
@SuperBuilder
public abstract class Element {
  private final String id;
  private final String font;
  private final TextColor color;
  private final boolean shadow;
  private final ShadowColor shadowColor;
  @Singular private final Map<TextDecoration, TextDecoration.State> decorations;
  private final int offsetX;

  /**
   * Converts this element into an Adventure {@link Component} instance,
   * applying its font, color, shadow, and text decorations.
   *
   * <p>This method is responsible for transforming the element’s
   * internal state (text content, color, style, etc.) into a renderable
   * component that can be appended to a {@link fr.dreamin.dreamHud.api.Layout}.
   * It automatically resolves the font and styling configuration from
   * the {@link fr.dreamin.dreamHud.internal.config.CodexService}.</p>
   *
   * <p><strong>Behavior:</strong></p>
   * <ul>
   *   <li>If a {@link #getFont()} is defined, the method builds a namespaced key using the configured Codex namespace.</li>
   *   <li>If {@link #isShadow()} is true, the {@link ShadowColor} defined by {@link #getShadowColor()} is applied;
   *       otherwise, {@link ShadowColor#none()} is used.</li>
   *   <li>Text color defaults to {@link net.kyori.adventure.text.format.NamedTextColor#WHITE} when none is specified.</li>
   *   <li>All {@link TextDecoration}s from {@link #getDecorations()} are applied in order.</li>
   * </ul>
   *
   * <p><strong>Example:</strong></p>
   * <pre>{@code
   * Element name = TextElement.builder()
   *     .font("pixel")
   *     .color(NamedTextColor.AQUA)
   *     .shadow(true)
   *     .shadowColor(ShadowColor.color(0, 0, 0, 128))
   *     .build();
   *
   * Component component = name.toComponent();
   * }</pre>
   *
   * @return a fully styled, render-ready {@link Component} representing this element
   *
   * @since 1.0.0
   */
  public abstract @NotNull Component toComponent();

  /**
   * Calculates the rendered pixel width of this element based on its font metrics.
   *
   * <p>This method queries the {@link fr.dreamin.dreamHud.internal.pack.font.FontLoaderService}
   * to determine the exact horizontal width of the element’s text,
   * considering its assigned {@link #getFont()}.
   * It is used internally by {@link fr.dreamin.dreamHud.api.Layout}
   * to align and space elements accurately within a HUD.</p>
   *
   * <p><strong>Behavior:</strong></p>
   * <ul>
   *   <li>If no font is defined, the default font from the FontLoader configuration is used.</li>
   *   <li>The calculation is based on per-character width mappings loaded from the resource pack.</li>
   *   <li>Special characters (e.g., negative spaces, icons) are fully supported.</li>
   * </ul>
   *
   * <p><strong>Example:</strong></p>
   * <pre>{@code
   * int width = element.getPixelWidth();
   * layout.addOffset(width + 2); // add 2px spacing between elements
   * }</pre>
   *
   * @return the total pixel width of this element’s textual content, using its associated font metrics
   *
   * @since 1.0.0
   */

  public abstract int getPixelWidth();
}
