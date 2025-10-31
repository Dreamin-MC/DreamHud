package fr.dreamin.dreamHud.internal.pack.font;

import fr.dreamin.api.config.Configurations;
import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.config.ICodexService;
import fr.dreamin.dreamHud.internal.pack.IPackLoaderService;
import fr.dreamin.dreamHud.internal.pack.PackLoaderService;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Default runtime implementation of the {@link FontLoaderService}.
 *
 * <p>This service is responsible for loading, analyzing, and registering fonts
 * used by DreamHud. It supports both PNG bitmap fonts and TTF TrueType fonts,
 * automatically converting and packaging them into the Minecraft resource pack.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Scan the {@code /fonts/} directory and load font metadata.</li>
 *   <li>Analyze bitmap font glyphs to determine pixel widths and heights.</li>
 *   <li>Convert TrueType fonts (TTF) into bitmap PNGs for Minecraft usage.</li>
 *   <li>Generate and register the corresponding Minecraft font JSON definitions.</li>
 *   <li>Expose APIs to measure string width and total rendered component height.</li>
 * </ul>
 *
 * <p>This class integrates deeply with the {@link PackLoaderService} to include
 * generated font assets in the final resource pack zip during build.</p>
 *
 * @see FontLoaderService
 * @see CodexService
 * @see PackLoaderService
 * @see fr.dreamin.dreamHud.api.element.Element
 *
 * @author Dreamin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@DreaminAutoService(value = FontLoaderService.class, dependencies = {IPackLoaderService.class, ICodexService.class})
public final class IFontLoaderService implements DreaminService, FontLoaderService {

  private static final int CHAR_WIDTH = 8;
  private static final int CHAR_HEIGHT = 8;

  private final @NotNull DreamHud plugin;
  private final @NotNull Map<String, Map<Character, Integer>> fontCharWidths = new HashMap<>();
  private final @NotNull Map<String, Integer> fontHeights = new HashMap<>();

  private final @NotNull CodexService codexService = DreamHud.getService(CodexService.class);
  private final @NotNull PackLoaderService packLoader = DreamHud.getService(PackLoaderService.class);

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  /**
   * Called automatically when the service is registered.
   * <p>Scans and loads all available fonts under {@code /fonts/}.</p>
   */
  @Override
  public void onLoad(@NotNull Plugin plugin) {
    loadAllFonts();
  }

  @Override
  public int getStringWidth(@NotNull String text, @NotNull String fontName) {
    final var fontMap = fontCharWidths.get(fontName);
    if (fontMap == null) {
      this.plugin.getLogger().warning(String.format("Font introuvable : %s", fontName));
      return 0;
    }

    final var chars = text.toCharArray();
    if (chars.length == 0) return 0;

    int totalWidth = 0;
    for (int i = 0; i < chars.length; i++) {
      final var c = chars[i];
      totalWidth += fontMap.getOrDefault(c, 6);

      if (i < chars.length - 1) totalWidth += 1;
    }
    return totalWidth;
  }

  @Override
  public int getTotalHeightOf(@NotNull Component component) {
    final Set<String> fonts = new HashSet<>();
    collectFonts(component, fonts);

    if (fonts.isEmpty()) {
      final var config = this.codexService.getConfig();
      final var defaultFont = config.default_font_name;
      if (defaultFont != null && !defaultFont.isBlank()) {
        fonts.add(defaultFont);
        fonts.add(String.format("font_%s", defaultFont));
        if (config.namespace != null && !config.namespace.isBlank()) {
          fonts.add(String.format("%s:%s", config.namespace, defaultFont));
          fonts.add(String.format("%s:font_%s", config.namespace, defaultFont));
        }
      }
    }

    int maxHeight = 0;
    for (final var fontId : fonts) {
      maxHeight = Math.max(maxHeight, resolveFontHeight(fontId));
    }

    return maxHeight > 0 ? maxHeight : CHAR_HEIGHT;
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Loads all fonts from the plugin’s {@code /fonts/} directory.
   *
   * <p>Each font must contain a {@code font.json} definition and either
   * a PNG or TTF file. TrueType fonts are automatically converted into
   * bitmap PNGs for Minecraft font compatibility.</p>
   *
   * <p>When debug mode is active in {@link CodexService.PluginConfig#debug},
   * detailed information about each font is logged during loading.</p>
   */
  public void loadAllFonts() {
    final var fontsDir = new File(this.plugin.getDataFolder(), "fonts");
    if (!fontsDir.exists()) {
      fontsDir.mkdirs();
      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info("Dossier 'fonts' créé (aucune font à charger pour l’instant).");
      return;
    }

    final var subDirs = fontsDir.listFiles(File::isDirectory);
    if (subDirs == null || subDirs.length == 0) {
      this.plugin.getLogger().warning(String.format("Aucune font trouvée dans %s", fontsDir.getAbsolutePath()));
      return;
    }

    for (final var fontFolder : subDirs) {
      loadFontFromFolder(fontFolder);
    }

    if (this.codexService.getConfig().debug)
      this.plugin.getLogger().info(String.format("%s fonts chargée%s", fontCharWidths.size(), fontCharWidths.size() > 1 ? "s" : ""));
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Loads and registers a single font from a folder.
   *
   * <p>This method supports both bitmap fonts ({@code .png}) and TrueType fonts ({@code .ttf}).
   * For TTF fonts, a conversion pipeline generates a temporary PNG that is later analyzed
   * and registered in the resource pack.</p>
   *
   * @param folder the folder containing the font definition and assets
   */
  private void loadFontFromFolder(final @NotNull File folder) {
    final var jsonFile = new File(folder, "font.json");
    if (!jsonFile.exists()) {
      this.plugin.getLogger().warning(String.format("Aucun fichier font.json dans %s", folder.getAbsolutePath()));
      return;
    }

    try {
      final var config = Configurations.MAPPER.readValue(jsonFile, FontConfig.class);

      final var name = folder.getName();
      final var fileName = config.file;
      final var ascent = config.ascent;
      final var height = config.height;

      if (fileName == null || fileName.isBlank()) {
        this.plugin.getLogger().warning(String.format("Fichier non définie dans %s", jsonFile.getAbsolutePath()));
        return;
      }

      final var file = new File(folder, fileName);
      if (!file.exists()) {
        this.plugin.getLogger().warning(String.format("Fichier introuvable : %s", file.getAbsolutePath()));
        return;
      }

      Map<Character, Integer> charWidths = new HashMap<>();

      if (fileName.endsWith(".ttf")) {
        if (this.codexService.getConfig().debug)
          this.plugin.getLogger().info(String.format("Conversion TTF -> PNG pour %s", name));
        final var genertedPng = convertTtfToPng(file, folder, name, height);
        charWidths = analyzeFontImage(genertedPng);

        generateMinecraftFontJson(name, genertedPng.getName(), ascent, height, charWidths);
        this.packLoader.sendToFontTextures(genertedPng);
      }
      else if (fileName.endsWith(".png")) {
        charWidths.putAll(analyzeFontImage(file));
        generateMinecraftFontJson(name, fileName, ascent, height, charWidths);
        this.packLoader.sendToFontTextures(file);
      }
      else {
        this.plugin.getLogger().warning(String.format("Format de fichier non supporté : %s", fileName));
        return;
      }

      registerFontHeight(name, height);
      fontCharWidths.put(name, charWidths);

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("→ Font '%s' chargée (%s caractères, ascent=%s)", name, charWidths.size(), ascent ));
    } catch (Exception e) {
      this.plugin.getLogger().severe("Erreur lors du chargement de " + folder.getName() + ": " + e.getMessage());
    }

  }

  /**
   * Extracts and records all font keys used in a {@link Component} tree.
   * <p>This is used to compute the overall rendered height of complex components
   * mixing multiple fonts (e.g., for HUD layout adjustments).</p>
   */
  private void collectFonts(final @NotNull Component component, final @NotNull Set<String> fonts) {
    final var fontKey = component.style().font();
    if (fontKey != null) {
      fonts.add(fontKey.asString());
      fonts.add(fontKey.value());
    }

    for (final var child : component.children()) {
      collectFonts(child, fonts);
    }

    if (component instanceof TranslatableComponent translatable) {
      for (final ComponentLike argument : translatable.arguments()) {
        collectFonts(argument.asComponent(), fonts);
      }
    }
  }

  /**
   * Resolves the actual pixel height of a font by checking multiple naming variants.
   * <p>This handles flexible identifiers such as {@code "default"}, {@code "font_default"},
   * or fully qualified keys like {@code "dreamhud:font_default"}.</p>
   */
  private int resolveFontHeight(final @NotNull String identifier) {
    final var candidates = new LinkedHashSet<String>();
    final var trimmed = identifier.trim();
    if (trimmed.isEmpty()) return CHAR_HEIGHT;

    candidates.add(trimmed);
    if (trimmed.contains(":")) {
      final var withoutNamespace = trimmed.substring(trimmed.indexOf(':') + 1);
      if (!withoutNamespace.isBlank()) {
        candidates.add(withoutNamespace);
        if (withoutNamespace.startsWith("font_")) {
          candidates.add(withoutNamespace.substring("font_".length()));
        }
      }
    }

    if (trimmed.startsWith("font_")) {
      candidates.add(trimmed.substring("font_".length()));
    }

    for (final var candidate : candidates) {
      final var height = this.fontHeights.get(candidate);
      if (height != null) {
        final int normalized = Math.abs(height);
        if (normalized > 0) return normalized;
      }
    }

    return CHAR_HEIGHT;
  }

  /**
   * Registers the height of a newly loaded font and all its naming aliases
   * (e.g., {@code "default"}, {@code "font_default"}, and {@code "namespace:font_default"}).
   */
  private void registerFontHeight(final @NotNull String fontName, int height) {
    final int normalized = height > 0 ? Math.abs(height) : CHAR_HEIGHT;
    final var resolvedNames = new LinkedHashSet<String>();

    final var baseName = fontName.startsWith("font_") && fontName.length() > 5
      ? fontName.substring("font_".length())
      : fontName;

    if (!fontName.isBlank()) resolvedNames.add(fontName);
    if (!baseName.isBlank()) {
      resolvedNames.add(baseName);
      resolvedNames.add("font_" + baseName);
    }

    final var config = this.codexService.getConfig();
    final var namespace = config != null ? config.namespace : null;

    for (final var name : resolvedNames) {
      this.fontHeights.put(name, normalized);
      if (namespace != null && !namespace.isBlank()) {
        this.fontHeights.put(String.format("%s:%s", namespace, name), normalized);
      }
    }
  }

  /**
   * Analyzes a PNG bitmap font to determine the width of each glyph (in pixels).
   *
   * @param imageFile the image file containing glyphs
   * @return a mapping between characters and their measured pixel widths
   */
  private Map<Character, Integer> analyzeFontImage(final @NotNull File imageFile) {
    Map<Character, Integer> widths = new HashMap<>();

    try {
      final var img = ImageIO.read(imageFile);
      final var cols = img.getWidth() / CHAR_WIDTH;
      final var rows = img.getHeight() / CHAR_HEIGHT;

      for (var row = 0; row < rows; row ++) {
        for (var col = 0; col < cols; col++) {
          final var startX = col * CHAR_WIDTH;
          final var startY = row * CHAR_HEIGHT;

          var minX = CHAR_WIDTH;
          var maxX = -1;

          for (var x = 0; x < CHAR_WIDTH; x++) {
            for (var y = 0; y < CHAR_HEIGHT; y++) {
              final var alpha = (img.getRGB(startX + x, startY + y) >> 24) & 0xFF;
              if (alpha > 0) {
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
              }
            }
          }

          final var width = (maxX == -1) ? 0 : (maxX - minX + 1);
          char c = (char) (row * cols + col);
          widths.put(c, width);
        }
      }

    } catch (IOException e) {
      plugin.getLogger().severe("Erreur de lecture PNG: " + imageFile.getName());
    }

    return widths;

  }

  /**
   * Generates the Minecraft-compatible JSON definition for a font.
   * <p>The resulting file is written in the resource-pack’s {@code /font/} folder
   * and references either a PNG or a TTF-converted texture.</p>
   */
  private void generateMinecraftFontJson(
    final @NotNull String fontName,
    final @NotNull String textureFile,
    int ascent,
    int height,
    final @Nullable Map<Character, Integer> charWidths
  ) throws IOException {
    final var outputDir = this.packLoader.getFontFolder();
    if (!outputDir.exists()) outputDir.mkdirs();

    List<String> lines = new ArrayList<>();
    var currentLine = new StringBuilder();
    var counter = 0;

    for (char c : charWidths.keySet()) {
      currentLine.append(c);
      counter++;
      if (counter % 16 == 0) {
        lines.add(currentLine.toString());
        currentLine = new StringBuilder();
      }
    }
    if (!currentLine.isEmpty()) lines.add(currentLine.toString());

    Map<String, Object> bitmapProvider = new LinkedHashMap<>();
    bitmapProvider.put("type", "bitmap");
    bitmapProvider.put("file", String.format("%s:font/%s", this.codexService.getConfig().namespace, textureFile));
    bitmapProvider.put("ascent", ascent);
    bitmapProvider.put("height", height);
    bitmapProvider.put("chars", lines);

    Map<String, Object> spaceAdvances = new LinkedHashMap<>();
    spaceAdvances.put(" ", 4);
    spaceAdvances.put("\u200C", 0);

    Map<String, Object> spaceProvider = new LinkedHashMap<>();
    spaceProvider.put("type", "space");
    spaceProvider.put("advances", spaceAdvances);

    Map<String, Object> root = new HashMap<>();
    root.put("providers", List.of(bitmapProvider, spaceProvider));

    final var jsonFile = new File(
      this.packLoader.getFontFolder(),
      String.format("font_%s.json", fontName)
    );
    Configurations.MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonFile, root);

    if (this.codexService.getConfig().debug)
      this.plugin.getLogger().info("Généré : " + jsonFile.getPath());
  }

  /**
   * Converts a TrueType Font (TTF) file into a Minecraft-compatible PNG bitmap.
   * <p>This process draws each ASCII character into a fixed-size grid, disabling
   * antialiasing for pixel consistency. The output image is later analyzed and
   * converted into a font JSON definition.</p>
   *
   * @param ttfFile the source TTF file
   * @param outputFolder the folder where the generated PNG is written
   * @param fontName the name used for the generated font
   * @param fontSize the pixel height of each glyph
   * @return the generated PNG file
   * @throws Exception if the conversion fails
   */
  private @NotNull File convertTtfToPng(final @NotNull File ttfFile, final @NotNull File outputFolder, final @NotNull String fontName, int fontSize) throws Exception {
    Font awtFont = Font.createFont(Font.TRUETYPE_FONT, ttfFile).deriveFont(Font.PLAIN, (float) fontSize);

    String chars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    int cols = 16;
    int rows = (int) Math.ceil((double) chars.length() / cols);
    int cellSize = fontSize;

    // Crée l'image finale
    BufferedImage img = new BufferedImage(cols * cellSize, rows * cellSize, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setFont(awtFont);
    g.setColor(Color.WHITE);

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

    FontMetrics metrics = g.getFontMetrics();

    int baseline = (cellSize + metrics.getAscent() - metrics.getDescent()) / 2;

    int i = 0;
    for (char c : chars.toCharArray()) {
      int col = i % cols;
      int row = i / cols;
      int x = col * cellSize + (cellSize - metrics.charWidth(c)) / 2;
      int y = row * cellSize + baseline;
      g.drawString(String.valueOf(c), x, y);
      i++;
    }
    g.dispose();

    // Sauvegarde le fichier
    File outFile = new File(outputFolder, fontName + ".png");
    ImageIO.write(img, "png", outFile);

    return outFile;
  }

}
