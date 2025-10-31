package fr.dreamin.dreamHud.internal.pack.neg;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service responsible for managing negative-space characters used for pixel-perfect
 * positioning in DreamHud layouts.
 *
 * <p>The {@code NegSpaceFontService} provides a mapping between pixel offset values
 * (positive or negative) and corresponding Unicode glyphs. These glyphs are part of
 * a special “negative space” font registered in the DreamHud resource pack, and are
 * used to precisely align text, backgrounds, and elements along the X-axis.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Provide the correct negative-space glyph character for a given pixel offset.</li>
 *   <li>Generate Adventure {@link Component}s representing pixel offsets via
 *       {@link #fromOffset(int)} for layout composition.</li>
 * </ul>
 *
 * <p>Offsets are typically used by background and layout systems such as
 * {@link fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService}
 * to fine-tune horizontal alignment between elements and decorative components.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * NegSpaceFontService negSpace = DreamHud.getService(NegSpaceFontService.class);
 *
 * // Retrieve the character used for -3px alignment
 * Character glyph = negSpace.getCharForHeight(-3);
 *
 * // Generate a component that moves the next element 4px to the right
 * Component offsetComponent = negSpace.fromOffset(4);
 * }</pre>
 *
 * @see fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService
 * @see fr.dreamin.dreamHud.api.Layout
 * @see net.kyori.adventure.text.Component
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface NegSpaceFontService {

  /**
   * Retrieves the negative-space glyph corresponding to a specific vertical height.
   *
   * <p>This method is used internally to find the correct Unicode character mapped
   * to a given pixel offset (positive or negative). If the glyph is not available
   * in the resource pack, {@code null} is returned.</p>
   *
   * @param height the offset value in pixels (e.g. -3, -4, +8)
   * @return the matching Unicode character, or {@code null} if none exists
   */
  @Nullable
  Character getCharForHeight(int height);

  /**
   * Builds an Adventure {@link Component} representing a horizontal pixel offset.
   *
   * <p>The resulting component uses a glyph from the negative-space font to move
   * subsequent text or elements horizontally by the specified offset. Positive values
   * shift content to the right, while negative values move it to the left.</p>
   *
   * <p>Example usage:</p>
   * <pre>{@code
   * Component offset = negSpace.fromOffset(-6);
   * Component text = offset.append(Component.text("Aligned text"));
   * }</pre>
   *
   * @param offset the desired pixel offset
   * @return a {@link Component} representing the negative-space adjustment
   */
  @NotNull
  Component fromOffset(int offset);


}
