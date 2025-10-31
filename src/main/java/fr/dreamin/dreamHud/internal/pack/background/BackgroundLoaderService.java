package fr.dreamin.dreamHud.internal.pack.background;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for generating and managing background components
 * used in DreamHud layouts.
 *
 * <p>The {@code BackgroundLoaderService} provides pixel-perfect visual backgrounds
 * rendered via Unicode-based font segments. These backgrounds are composed of
 * start, middle, and end segments dynamically repeated or resized to match the
 * content width of a HUD layout.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Generate visual background components for {@link fr.dreamin.dreamHud.api.Layout}.</li>
 *   <li>Compute background pixel widths and negative offsets for precise alignment.</li>
 *   <li>Provide access to background metadata (segment definitions, ascent, height, etc.).</li>
 * </ul>
 *
 * <p>Internally, each background is defined through a {@link Background}
 * record containing {@link Segment} data, which are Unicode characters
 * representing parts of the background texture in the resource pack font.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * BackgroundLoaderService bgService = DreamHud.getService(BackgroundLoaderService.class);
 * Component bg = bgService.generateBackground(64, "rounded");
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface BackgroundLoaderService {

  /**
   * Generates a fully rendered background {@link Component} matching a given width.
   *
   * <p>The generated background is composed by concatenating the start, middle, and end
   * segments defined in the {@link Background} configuration. The middle section is
   * repeated or adjusted as necessary to achieve the requested width.</p>
   *
   * @param size the target width (in pixels) of the background to generate
   * @param name the background name, as defined in the configuration
   * @return a {@link Component} representing the generated background
   */
  Component generateBackground(final int size, final @NotNull String name);

  /**
   * Computes the total negative offset needed to align the background with the text.
   *
   * <p>Negative offsets are used to “shift” subsequent components horizontally so that
   * text aligns visually with the rendered background instead of being displaced by padding.</p>
   *
   * @param size the width of the text component being aligned
   * @param name the background name
   * @return the total pixel offset to apply
   */
  int getTotalNegativeOffset(int size, final @NotNull String name);

  /**
   * Calculates the real rendered width (in pixels) of a background based on its segment sizes.
   *
   * @param size the width of the foreground content
   * @param name the background name
   * @return the computed pixel width of the full background
   */
  int getRealWidth(int size, final @NotNull String name);

  // ###############################################################
  // ------------------ INTERNAL DATA STRUCTURES -------------------
  // ###############################################################

  /**
   * Represents the configuration structure of a background entry.
   *
   * <p>This configuration is typically deserialized from JSON or YAML files
   * located in the resource pack definition. Each background is made up of
   * three parts: {@code start}, {@code middle}, and {@code end}, which
   * correspond to Unicode characters mapped to textured glyphs.</p>
   */
  class BackgroundConfig {
    /** Unicode character used as the leftmost part of the background. */
    public String start;

    /** Unicode character(s) forming the middle part of the background, repeatable. */
    public String middle;

    /** Unicode character used as the rightmost part of the background. */
    public String end;

    /** Vertical ascent (baseline alignment) of the background glyphs. */
    public int ascent = 7;

    /** Total pixel height of the background glyphs. */
    public int height = 14;
  }

  /**
   * Immutable representation of a loaded background resource.
   *
   * <p>Each {@link Background} defines its visual parameters and the collection
   * of {@link Segment}s that make up the complete background rendering logic.</p>
   *
   * @param name the background identifier
   * @param ascent the ascent value (baseline)
   * @param height the height in pixels
   * @param segments the mapping of segment names to {@link Segment} definitions
   */
  record Background (
    String name,
    int ascent,
    int height,
    Map<String, Segment> segments
  ) {

    /**
     * Retrieves all “middle” segments ordered from largest to smallest.
     *
     * <p>This method filters out invalid or zero-sized segments, ensuring that
     * only usable middle parts are included for dynamic background stretching.</p>
     *
     * @return an ordered list of middle {@link Segment}s, sorted by descending size
     */
    public List<Segment> orderedSegments() {
      final var start = segments.get("start");
      final var end = segments.get("end");

      final var middles = segments.entrySet().stream()
        .filter(e -> e.getKey().startsWith("middle_"))
        .map(Map.Entry::getValue)
        .filter(s -> s.size > 0)
        .sorted(Comparator.comparingInt(Segment::size).reversed())
        .toList();

      return middles;
    }

    /**
     * Gets the leftmost “start” segment of this background.
     *
     * @return the start {@link Segment}, or {@code null} if undefined
     */
    @Nullable
    public Segment getStart() {
      return segments.get("start");
    }

    /**
     * Gets the rightmost “end” segment of this background.
     *
     * @return the end {@link Segment}, or {@code null} if undefined
     */
    @Nullable
    public Segment getEnd() {
      return segments.get("end");
    }

    /**
     * Retrieves the middle {@link Segment} matching the given size, if available.
     *
     * @param size the middle width size to find
     * @return the matching {@link Segment}, or {@code null} if not found
     */
    @Nullable
    public Segment getMiddle(int size) {
      return segments.get(String.format("middle_%s", size));
    }

  }

  /**
   * Represents a single background glyph segment.
   *
   * <p>A {@code Segment} maps a specific Unicode character to a defined pixel size.
   * It is used to build structured backgrounds from resource pack fonts, ensuring
   * alignment and scalability across various widths.</p>
   *
   * @param unicode the Unicode character associated with this segment
   * @param size the pixel width of this segment
   */
  record Segment(
    char unicode,
    int size
  ) {}

}
