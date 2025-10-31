package fr.dreamin.dreamHud.internal.pack.background;

import fr.dreamin.api.config.Configurations;
import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.config.ICodexService;
import fr.dreamin.dreamHud.internal.pack.IPackLoaderService;
import fr.dreamin.dreamHud.internal.pack.PackLoaderService;
import fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Default runtime implementation of the {@link BackgroundLoaderService}.
 *
 * <p>This service is automatically registered through the {@link DreaminAutoService}
 * system and is responsible for loading, generating, and managing HUD background resources
 * within the DreamHud ecosystem.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Load background definitions from the {@code /background/} folder.</li>
 *   <li>Generate multiple pixel-width variants for scalable middle segments.</li>
 *   <li>Register backgrounds into the resource pack and generate associated font JSON files.</li>
 *   <li>Compute width, offset, and spacing for precise background alignment.</li>
 * </ul>
 *
 * <p>Backgrounds are made of start, middle, and end textures. These are dynamically
 * combined to create scalable, pixel-perfect background bars used in HUD layouts.</p>
 *
 * <p><strong>Lifecycle:</strong></p>
 * <ol>
 *   <li>Backgrounds are loaded during plugin initialization via {@link #onLoad(Plugin)}.</li>
 *   <li>Each background folder contains a {@code background.json} configuration and its textures.</li>
 *   <li>JSON font definitions are automatically generated for Adventure text rendering.</li>
 * </ol>
 *
 * @see BackgroundLoaderService
 * @see fr.dreamin.dreamHud.api.Layout
 * @see fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService
 *
 * @author Dreamin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@DreaminAutoService(value = BackgroundLoaderService.class, dependencies = {ICodexService.class, IPackLoaderService.class})
public final class IBackgroundLoaderService implements DreaminService, BackgroundLoaderService {

  private static final int UNICODE_START = 0xE000;
  private static final int NEGATIVE_SPACE_HEIGHT = -3;

  private final @NotNull DreamHud plugin;

  private final @NotNull CodexService codexService = DreamHud.getService(CodexService.class);
  private final @NotNull PackLoaderService packLoader = DreamHud.getService(PackLoaderService.class);

  private int unicodeOffset = 0;

  private final Map<String, Background> backgrounds = new HashMap<>();

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  /**
   * Called automatically when the service is loaded.
   * <p>This method scans and registers all backgrounds, zips the resource pack,
   * and logs its resulting SHA-1 checksum.</p>
   */
  @Override
  public void onLoad(@NotNull Plugin plugin) {
    loadAllBackgrounds();

    this.packLoader.zipResourcePack(this.packLoader.getBuildFolder());

    final var sha1 = this.packLoader.getPackSha1();
    if (sha1 == null) return;

    this.plugin.getLogger().info(String.format("SHA-1 du pack : %s", sha1));
  }

  @Override
  public Component generateBackground(int size, @NotNull String name) {
    final var bg = this.backgrounds.get(name);
    if (bg == null) {
      this.plugin.getLogger().warning(String.format("Background introuvable : %s", name));
      return Component.empty();
    }

    final var negativeSpace = resolveNegativeSpaceComponent(name);
    if (negativeSpace.isEmpty()) return Component.empty();

    final var space_neg3 = negativeSpace.get().component();

    final var font = Key.key(this.codexService.getConfig().namespace, String.format("background_%s", name));

    var result = Component.text(bg.getStart().unicode()).font(font)
      .append(space_neg3)
      .append(Component.text(bg.getMiddle(4).unicode()).font(font))
      .append(space_neg3);

    var remaining = size;
    for (final var segment : bg.orderedSegments()) {
      while (remaining >= segment.size()) {
        result = result.append(Component.text(segment.unicode()).font(font))
          .append(space_neg3);
        remaining -= segment.size();
      }
    }

    return result.append(Component.text(bg.getMiddle(4).unicode()).font(font))
      .append(space_neg3)
      .append(Component.text(bg.getEnd().unicode()).font(font))
      .shadowColor(ShadowColor.none());
  }

  @Override
  public int getTotalNegativeOffset(int size, @NotNull String name) {
    final var bg = this.backgrounds.get(name);
    if (bg == null) {
      this.plugin.getLogger().warning(String.format("Background introuvable : %s", name));
      return 0;
    }

    final var negativeSpace = resolveNegativeSpaceComponent(name);
    if (negativeSpace.isEmpty()) return 0;

    final var negValue = negativeSpace.get().height();
    var negCount = 0;
    negCount += 2;

    var remaining = size;
    for (final var segment : bg.orderedSegments()) {
      while (remaining >= segment.size()) {
        negCount += 1;
        remaining -= segment.size();
      }
    }

    negCount += 2;

    return negCount * negValue;
  }

  @Override
  public int getRealWidth(int size, @NotNull String name) {
    final var bg = this.backgrounds.get(name);
    if (bg == null) {
      this.plugin.getLogger().warning(String.format("Background introuvable : %s", name));
      return 0;
    }

    var totalWidth = 0;
    totalWidth += bg.getStart().size();
    totalWidth += bg.getMiddle(4).size();


    var remaining = size;
    for (final var segment : bg.orderedSegments()) {
      while (remaining >= segment.size()) {
        totalWidth += segment.size();
        remaining -= segment.size();
      }
    }

    totalWidth += bg.getMiddle(4).size();
    totalWidth += bg.getEnd().size();

    return totalWidth;
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Loads all available backgrounds from the plugin’s {@code /background/} directory.
   *
   * <p>Each subdirectory represents a background entry containing a
   * {@code background.json} definition file and associated image textures.</p>
   *
   * <p>Automatically generates middle variants and sends them to the resource pack.
   * If debug mode is enabled in {@link CodexService.PluginConfig#debug}, detailed logs
   * are printed for each loaded background.</p>
   */
  public void loadAllBackgrounds() {
    final var backgroundDir = new File(this.plugin.getDataFolder(), "background");
    if (!backgroundDir.exists()) {
      backgroundDir.mkdirs();
      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info("Dossier 'background' crée (aucun background à charger pour le moment)");
      return;
    }

    final var subDirs = backgroundDir.listFiles(File::isDirectory);
    if (subDirs == null || subDirs.length == 0) {
      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("Aucun background chargé : aucun dossier trouvé dans %s", backgroundDir.getAbsolutePath()));
      return;
    }

    for (final var bgFolder : subDirs) {
      loadBackgroundFromFolder(bgFolder);
    }

    if (this.codexService.getConfig().debug)
      this.plugin.getLogger().info(String.format("%s backgrounds détéctés", backgrounds.size()));

  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Loads a single background definition from a folder.
   *
   * <p>This includes reading its JSON configuration, validating texture presence,
   * generating scaled variants, and packaging everything into the resource pack.</p>
   *
   * @param folder the directory representing the background
   */
  private void loadBackgroundFromFolder(final @NotNull File folder) {
    final var jsonFile = new File(folder, "background.json");
    if (!jsonFile.exists()) {
      this.plugin.getLogger().warning(String.format("Aucun fichier background.json trouvé dans %s", folder.getAbsolutePath()));
      return;
    }

    try {
      final var config = Configurations.MAPPER.readValue(jsonFile, BackgroundConfig.class);

      final var name = folder.getName();
      final var startName = config.start;
      final var middleName = config.middle;
      final var endName = config.end;
      final var ascent = config.ascent;
      final var height = config.height;

      final var startFile = new File(folder, startName);
      final var middleFile = new File(folder, middleName);
      var endFile = (startName.equals(endName) ? startFile : new File(folder, endName));

      if (!startFile.exists() || !middleFile.exists() || !endFile.exists()) {
        this.plugin.getLogger().warning(String.format("Fichiers d’image manquants pour %s", name));
        return;
      }

      final var tempDir = new File(folder, "generated_temp");
      if (!tempDir.exists()) tempDir.mkdirs();
      generateMiddleVariants(name, middleFile, tempDir);

      sendBackgroundToPack(name, startFile, endFile, tempDir, ascent, height);

      deleteDirectory(tempDir);

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("Background '%s' envoyé au pack", name));

    } catch (Exception e) {
      this.plugin.getLogger().severe("Erreur lors du chargement de " + folder.getName() + ": " + e.getMessage());
    }

  }

  /**
   * Generates multiple pixel-width variants for the middle texture segment.
   * <p>These are created by repeating the first pixel column to simulate scalable widths
   * for use in dynamic backgrounds.</p>
   *
   * @param name the background name
   * @param middleFile the source middle texture
   * @param outputDir the directory where generated variants are written
   */
  private void generateMiddleVariants(final @NotNull String name, final @NotNull File middleFile, final @NotNull File outputDir) {
    try {
      final var img = ImageIO.read(middleFile);
      final int height = img.getHeight();

      int[] column = new int[height];
      for (int y = 0; y < height; y++) {
        column[y] = img.getRGB(0, y);
      }

      int[] sizes = {1, 2, 4, 8, 16, 32, 64, 128};

      for (int size : sizes) {
        var newImg = new BufferedImage(size, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < size; x++) {
          for (int y = 0; y < height; y++) {
            newImg.setRGB(x, y, column[y]);
          }
        }

        File output = new File(outputDir, "middle_" + size + ".png");
        ImageIO.write(newImg, "png", output);
      }

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("Variantes middle générées pour '%s'", name));
    } catch (Exception e) {
      this.plugin.getLogger().severe(String.format("Erreur lors de la génération des middle variants pour %s: %s", name, e.getMessage()));
    }
  }

  /**
   * Sends all background textures and generated font data to the resource pack.
   *
   * @param name the background identifier
   * @param startFile the left texture
   * @param endFile the right texture
   * @param middleDir the directory containing generated middle variants
   * @param ascent the vertical baseline alignment
   * @param height the pixel height of the background
   */
  private void sendBackgroundToPack(final @NotNull String name, final @NotNull File startFile, final @NotNull File endFile, final @NotNull File middleDir, int ascent, int height) {
    Map<String, Segment> segments = new LinkedHashMap<>();

    this.packLoader.sendToBackgroundTextures(startFile,  name);
    segments.put("start", new Segment(nextChar(), getImageWidth(startFile)));

    for (var size : new int[]{1, 2, 4, 8, 16, 32, 64, 128}) {
      final var fileName = String.format("middle_%s.png", size);

      final var variant = new File(middleDir, fileName);
      if (variant.exists()) {
        this.packLoader.sendToBackgroundTextures(variant, name);
        segments.put(String.format("middle_%s", size), new Segment(nextChar(), size));
      }
    }

    var finalEndFile = endFile;

    if (startFile.equals(endFile)) {
      try  {
        finalEndFile = new File(startFile.getParentFile(), "end.png");
        Files.copy(startFile.toPath(), finalEndFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if (this.codexService.getConfig().debug)
          this.plugin.getLogger().info(String.format("Copie du start.png → end.png pour %s", name));
      } catch (IOException e) {
        this.plugin.getLogger().warning(String.format("Impossible de copier start.png → end.png : %s", e.getMessage()));
      }
    }

    this.packLoader.sendToBackgroundTextures(finalEndFile, name);
    segments.put("end", new Segment(nextChar(), getImageWidth(endFile)));

    final var bg = new Background(name, ascent, height, segments);
    this.backgrounds.put(name, bg);

    try {
      generateFontJson(bg);
    } catch (IOException e) {
      plugin.getLogger().severe("Erreur lors de la génération du JSON pour " + name + ": " + e.getMessage());
    }

  }

  /**
   * Generates the JSON font definition file for a background, used by Minecraft
   * to render the background characters as textured glyphs.
   *
   * @param bg the background definition
   * @throws IOException if the file cannot be written
   */
  private void generateFontJson(final @NotNull Background bg) throws IOException {
    final var providers = new ArrayList<Map<String, Object>>();

    for (final var entry : bg.segments().entrySet()) {
      final var key = entry.getKey();
      final var segment = entry.getValue();

      var provider = new LinkedHashMap<String, Object>();
      provider.put("type", "bitmap");
      provider.put("file", String.format(
        "%s:background/%s/%s.png",
        this.codexService.getConfig().namespace,
        bg.name(),
        key
      ));
      provider.put("ascent", bg.ascent());
      provider.put("height", bg.height());
      provider.put("chars", List.of(segment.unicode()));

      providers.add(provider);
    }


    final var root = new HashMap<String, Object>();
    root.put("providers", providers);

    final var jsonFile = new File(this.packLoader.getFontFolder(), String.format("background_%s.json", bg.name()));
    Configurations.MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonFile, root);

    if (codexService.getConfig().debug)
      plugin.getLogger().info(String.format("JSON généré pour background: %s", bg.name()));

  }

  /** Deletes a temporary directory recursively. */
  private void deleteDirectory(final @NotNull File dir) {
    if (dir.exists()) {
      try {
        Files.walk(dir.toPath())
          .sorted((a, b) -> b.compareTo(a))
          .forEach(path -> path.toFile().delete());
      } catch (IOException e) {
        this.plugin.getLogger().warning(String.format("Impossible de supprimer le dossier temporaire : %s", dir.getPath()));
      }
    }
  }

  /** Generates the next unique Unicode character index for background registration. */
  private char nextChar() {
    return (char) (UNICODE_START + (unicodeOffset++));
  }

  /** Reads and returns the width (in pixels) of an image file. */
  private int getImageWidth(File file) {
    try {
      return ImageIO.read(file).getWidth();
    } catch (IOException e) {
      return 0;
    }
  }

  /**
   * Resolves the negative-space glyph used to align backgrounds with HUD text.
   * <p>Returns an empty optional if the glyph is missing from the pack.</p>
   *
   * @param backgroundName the name of the background being aligned
   * @return a {@link NegativeSpace} record containing the glyph and height
   */
  private Optional<NegativeSpace> resolveNegativeSpaceComponent(final @NotNull String backgroundName) {
    final var negSpaceFont = DreamHud.getService(NegSpaceFontService.class);
    final var negChar = negSpaceFont.getCharForHeight(NEGATIVE_SPACE_HEIGHT);

    if (negChar == null) {
      this.plugin.getLogger().severe(String.format(
        "Impossible de générer le background '%s' : glyph négative %d absente du pack.",
        backgroundName,
        NEGATIVE_SPACE_HEIGHT
      ));
      return Optional.empty();
    }

    final var component = Component.text(negChar)
      .font(Key.key(this.codexService.getConfig().namespace, "space_split"));

    return Optional.of(new NegativeSpace(component, NEGATIVE_SPACE_HEIGHT));
  }

  private record NegativeSpace(Component component, int height) {
  }
}
