package fr.dreamin.dreamHud.internal.pack.font;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Service responsible for managing and measuring custom fonts used in DreamHud.
 *
 * <p>The {@code FontLoaderService} provides utilities to determine text dimensions
 * (width and height in pixels) for strings and Adventure {@link Component}s rendered
 * with fonts registered in the DreamHud resource pack.</p>
 *
 * <p>These calculations are used internally for pixel-perfect HUD alignment,
 * ensuring consistent placement of text and backgrounds across multiple
 * resolutions and font styles.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Compute the pixel width of text rendered in a specific font.</li>
 *   <li>Determine the maximum height of any glyph contained in a component tree.</li>
 *   <li>Handle fallback logic for mixed-font components or missing metadata.</li>
 * </ul>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * FontLoaderService fontService = DreamHud.getService(FontLoaderService.class);
 *
 * int width = fontService.getStringWidth("DreamHud", "default");
 * int height = fontService.getTotalHeightOf(Component.text("DreamHud"));
 *
 * plugin.getLogger().info("Font width: " + width + ", height: " + height);
 * }</pre>
 *
 * @see net.kyori.adventure.text.Component
 * @see fr.dreamin.dreamHud.api.element.Element
 * @see fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface FontLoaderService {

  /**
   * Calculates the pixel width of a given text string when rendered with
   * the specified font.
   *
   * <p>This method takes into account the individual glyph widths defined
   * in the resource-pack’s font JSON files, ensuring precise text measurement
   * even for custom or variable-width fonts.</p>
   *
   * <p>If the specified font is missing or its metrics are unavailable,
   * a default font width calculation is used as fallback.</p>
   *
   * @param text the string to measure
   * @param fontName the font identifier (without namespace)
   * @return the total pixel width of the rendered text
   */
  int getStringWidth(final @NotNull String text, final @NotNull String fontName);

  /**
   * Calculates the vertical space required to render the supplied component.
   * <p>
   * The returned value corresponds to the maximum glyph height found while
   * traversing the component tree, taking every referenced font into account.
   * Components mixing several fonts are therefore supported—the tallest
   * glyph wins—and a sensible default height is used when no metadata is
   * available for a font.
   *
   * @param component the component whose rendered height should be computed
   * @return the tallest glyph height, expressed in pixels
   */
  int getTotalHeightOf(final @NotNull Component component);

  /**
   * Configuration model for defining a font’s metadata inside the resource pack.
   *
   * <p>This structure typically represents the deserialized content of a
   * font JSON definition file. Each font entry defines its texture path,
   * baseline ascent, and glyph height used for rendering alignment.</p>
   *
   * <p><strong>Example JSON:</strong></p>
   * <pre>{@code
   * {
   *   "file": "dreamhud:font/default.png",
   *   "ascent": 8,
   *   "height": 8
   * }
   * }</pre>
   *
   * @author Dreamin
   * @since 1.0.0
   */
  class FontConfig {
    /** Path to the font texture file (e.g. {@code dreamhud:font/default.png}). */
    public String file;

    /** Vertical ascent (distance from baseline to top of glyphs). */
    public int ascent = 8;

    /** Total pixel height of the glyphs within this font. */
    public int height = 8;
  }

}
