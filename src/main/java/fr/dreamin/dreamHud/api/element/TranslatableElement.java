package fr.dreamin.dreamHud.api.element;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.TranslationMetaService;
import fr.dreamin.dreamHud.internal.config.CodexService;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Element that renders a client-side translation key with optional nested elements.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>The {@code key} must exist in the client resource pack, otherwise an exception is thrown.</li>
 *   <li>Child elements are appended in the order they are declared.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * TranslatableElement score = TranslatableElement.builder()
 *     .key("hud.score")
 *     .element(TextElement.builder()
 *         .textSupplier(() -> String.valueOf(points))
 *         .build())
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter @Setter
@SuperBuilder
public final class TranslatableElement extends Element {

  private final @NotNull String key;
  @Singular private final List<Element> elements;

  // ###############################################################
  // ----------------------- ELEMENT METHODS -----------------------
  // ###############################################################

  @Override
  public @NotNull Component toComponent() {
    final var codexService = DreamHud.getService(CodexService.class);

    TextColor color = this.getColor();
    ShadowColor shadowColor = this.isShadow() ? this.getShadowColor() : ShadowColor.none();
    Key font = Key.key(codexService.getConfig().namespace, String.format("font_%s", this.getFont()));

    return Component.translatable(this.key, getElementsComponent())
      .color(color == null ? NamedTextColor.WHITE : color)
      .font(font)
      .decorations(getDecorations())
      .shadowColor(shadowColor);
  }

  @Override
  public int getPixelWidth() {
    final var translationMetaService = DreamHud.getService(TranslationMetaService.class);
    final var baseWidth = translationMetaService
      .getWidthForKey(this.key)
      .orElseThrow(() -> new IllegalStateException("Largeur introuvable pour la clé de traduction '" + this.key + "'"));

    return baseWidth + this.elements.stream().mapToInt(Element::getPixelWidth).sum();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private List<Component> getElementsComponent() {
    return this.elements.stream()
      .map(Element::toComponent)
      .toList();
  }

}
