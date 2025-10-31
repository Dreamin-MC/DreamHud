package fr.dreamin.dreamHud.api.element;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.pack.font.FontLoaderService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Element that renders dynamic plain text using the configured HUD font.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>{@code textSupplier} is invoked every time the HUD is rendered.</li>
 *   <li>The returned component width corresponds to the configured font and decorations.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * TextElement timer = TextElement.builder()
 *     .textSupplier(() -> formatDuration(remainingTicks))
 *     .font("pixel")
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter @Setter
@SuperBuilder
public final class TextElement extends Element {

  private final @NotNull Supplier<String> textSupplier;

  // ###############################################################
  // ----------------------- ELEMENT METHODS -----------------------
  // ###############################################################

  @Override
  public @NotNull Component toComponent() {
    final var codexService = DreamHud.getService(CodexService.class);

    TextColor color = this.getColor();
    ShadowColor shadowColor = this.isShadow() ? this.getShadowColor() : ShadowColor.none();
    Key font = this.getFont() != null ? Key.key(codexService.getConfig().namespace, String.format("font_%s", this.getFont())) : null;

    return Component.text(this.textSupplier.get())
      .color(color == null ? NamedTextColor.WHITE : color)
      .decorations(getDecorations())
      .shadowColor(shadowColor)
      .font(font);
  }

  @Override
  public int getPixelWidth() {
    final var fontLoader = DreamHud.getService(FontLoaderService.class);
    return fontLoader.getStringWidth(this.textSupplier.get(), getFont());
  }
}
