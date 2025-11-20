package fr.dreamin.dreamHud.api.element;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * HUD element that displays a fixed icon glyph from the configured HUD font.
 *
 * <p>This element is typically used to render symbolic characters such as
 * hearts, bullets, status icons, or any custom glyph defined inside the
 * resource pack fonts. The icon is rendered as-is and does not use a
 * {@code textSupplier} unlike text-based elements.
 *
 * <p><strong>Rendering behavior</strong>
 * <ul>
 *   <li>The {@code icon} string is rendered using the chosen font (if any).</li>
 *   <li>The element always reports a fixed pixel width, defined by {@code width}.</li>
 *   <li>Decorations, color, and optional shadow are applied normally.</li>
 *   <li>If a custom font is set via {@link #getFontString()} or {@link #getFont()},
 *       the glyph resolution depends on your pack's font providers.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * IconElement heart = IconElement.builder()
 *     .icon("\uE001")        // custom glyph from RP font
 *     .width(8)              // pixel width of that glyph
 *     .color(NamedTextColor.RED)
 *     .font("hud")           // maps to: <namespace>:font_hud
 *     .shadow(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Notes</strong></p>
 * <ul>
 *   <li>The width must be provided manually, as DreamHud does not compute
 *       glyph metrics automatically for icons.</li>
 *   <li>The font lookup respects DreamHud's Codex configuration system.</li>
 * </ul>
 *
 * @author Dreamin
 * @since 1.0.2
 */
@Getter @Setter
@SuperBuilder
public final class IconElement extends Element {

  private final char icon;
  private final int width;

  // ###############################################################
  // ----------------------- ELEMENT METHODS -----------------------
  // ###############################################################

  @Override
  public @NotNull Component toComponent() {
    final var codexService = DreamHud.getService(CodexService.class);

    TextColor color = this.getColor();
    ShadowColor shadowColor = this.isShadow() ? this.getShadowColor() : ShadowColor.none();

    Key font = null;
    if (this.getFontString() != null)
      font = Key.key(codexService.getConfig().namespace, String.format("font_%s", this.getFontString()));
    else if (this.getFont() != null) font = this.getFont();

    return Component.text(this.icon)
      .color(color == null ? NamedTextColor.WHITE : color)
      .decorations(getDecorations())
      .shadowColor(shadowColor)
      .font(font);
  }

  @Override
  public int getPixelWidth() {
    return this.width;
  }
}
