package fr.dreamin.dreamHud.internal.pack;

import fr.dreamin.api.config.Configurations;
import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.config.ICodexService;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Default implementation of the {@link PackLoaderService}.
 *
 * <p>This service handles the creation, structure management, and packaging of the
 * DreamHud resource pack. It integrates the work of various subsystems such as
 * {@link fr.dreamin.dreamHud.internal.pack.font.FontLoaderService},
 * {@link fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService},
 * and {@link fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService}.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Generate and clean the build directory structure.</li>
 *   <li>Create default metadata (pack.mcmeta) and bossbar textures.</li>
 *   <li>Copy runtime assets (fonts, textures, JSONs) into the correct folders.</li>
 *   <li>Zip the final pack and compute its SHA-1 checksum for delivery to clients.</li>
 * </ul>
 *
 * <p>This service runs automatically on plugin load and is used internally by
 * higher-level systems to ensure all resource assets are bundled correctly.</p>
 *
 * @see PackLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.font.FontLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService
 *
 * @author Dreamin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@DreaminAutoService(value = PackLoaderService.class, dependencies = {ICodexService.class})
public final class IPackLoaderService implements DreaminService, PackLoaderService {

  private final @NotNull DreamHud plugin;

  private final @NotNull CodexService codexService = DreamHud.getService(CodexService.class);

  private File buildDir, fontDir, texturesFontDir, texturesBackgroundDir;

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void onLoad(@NotNull Plugin plugin) {
    generateBasePack();
  }

  @Override
  public void generateBasePack() {
    this.buildDir = new File(this.plugin.getDataFolder(), this.codexService.getConfig().build_folder_location);

    if (this.buildDir.exists()) {
      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info("Nettoyage du dossier build...");
      clearDirectory(this.buildDir);
    }

    if (!this.buildDir.exists() && !this.buildDir.mkdirs()) {
      this.plugin.getLogger().severe("Impossible de créer le dossier build !");
      return;
    }

    final var mcmeta = new File(this.buildDir, "pack.mcmeta");
    final var desc = this.codexService.getConfig().desc;
    final var json = Map.of(
      "pack", Map.of(
        "pack_format", 64,
        "description", desc != null ? desc : "Dreamin HudLib Pack"
      )
    );

    try {
      Configurations.MAPPER.writerWithDefaultPrettyPrinter().writeValue(mcmeta, json);
    } catch (IOException e) {
      this.plugin.getLogger().severe("Erreur d'écriture du fichier pack.mcmeta : " + e.getMessage());
    }

    final var assetsDir = new File(this.buildDir, "assets");
    final var hudlibDir = new File(assetsDir, this.codexService.getConfig().namespace);
    this.fontDir = new File(hudlibDir, "font");
    final var textureDir = new File(hudlibDir, "textures");
    this.texturesFontDir = new File(textureDir, "font");
    this.texturesBackgroundDir = new File((textureDir), "background");

    this.fontDir.mkdirs();
    this.texturesFontDir.mkdirs();
    this.texturesBackgroundDir.mkdirs();

    generateBossBarTextures();

    if (this.codexService.getConfig().debug)
      this.plugin.getLogger().info(String.format("Pack de base généré dans %s", this.buildDir.getAbsolutePath()));
  }

  @Override
  public void zipResourcePack(final @NotNull File sourceDir) {
    final var zipFile = new File(sourceDir.getParentFile(), "pack.zip");

    try (
      var fos = new FileOutputStream(zipFile);
      var zos = new ZipOutputStream(fos)
    ) {
      zipFolder(sourceDir, "", zos);

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("Pack compressé : %s", zipFile.getAbsolutePath()));
    } catch (IOException e) {
      this.plugin.getLogger().severe(String.format("Erreur lors de la création du ZIP : %s", e.getMessage()));
    }
  }

  @Override
  public File getBuildFolder() {
    return this.buildDir;
  }

  @Override
  public File getFontFolder() {
    return this.fontDir;
  }

  @Override
  public File getFontTexturesFolder() {
    return this.texturesFontDir;
  }

  @Override
  public File getBackgroundTexturesFolder() {
    return this.texturesBackgroundDir;
  }

  @Override
  public void sendToFont(@NotNull File file) {
    sendTo(
      file,
      String.format("assets/%s/font/%s", this.codexService.getConfig().namespace, file.getName())
    );
  }

  @Override
  public void sendToFontTextures(@NotNull File file) {
    sendTo(
      file,
      String.format("assets/%s/textures/font/%s", this.codexService.getConfig().namespace, file.getName())
    );
  }

  @Override
  public void sendToBackgroundTextures(@NotNull File file, @NotNull String name) {
    sendTo(
      file,
      String.format("assets/%s/textures/background/%s/%s", this.codexService.getConfig().namespace, name, file.getName())
    );
  }

  @Override
  public void sendTo(@NotNull File file, @NotNull String relativePath) {
    if (!file.exists()) {
      this.plugin.getLogger().warning(String.format("Fichier invalide : " + file.getPath()));
      return;
    }

    final var target = new File(this.buildDir, relativePath);
    target.getParentFile().mkdirs();

    try {
      Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info("Copié vers pack : " + relativePath);
    } catch (IOException e) {
      this.plugin.getLogger().severe("Erreur de copie du fichier vers le pack : " + e.getMessage());
    }

  }

  @Override
  public void copyFileTo(@NotNull File source, @NotNull File target) {
    try {
      if (target.getParentFile() != null && !target.getParentFile().exists())
        target.getParentFile().mkdirs();

      Files.copy(
        source.toPath(),
        target.toPath(),
        StandardCopyOption.REPLACE_EXISTING
      );

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("Copié dans le pack : %s", target.getPath()));
    } catch (Exception e) {
      this.plugin.getLogger().severe(String.format("Erreur lors de la copie de %s : %s", source.getName(), e.getMessage()));
    }
  }

  @Override
  public String getPackSha1() {
    final var zipFile = new File(this.plugin.getDataFolder(), "pack.zip");
    if (!zipFile.exists()) {
      this.plugin.getLogger().warning("Aucun pack.zip trouvé pour calcul du SHA-1 !");
      return null;
    }

    try (var fis = new FileInputStream(zipFile)) {
      final var digest = MessageDigest.getInstance("SHA-1");
      final var buffer = new byte[8192];
      int read;
      while ((read = fis.read(buffer)) != -1) {
        digest.update(buffer, 0, read);
      }

      var hashByes = digest.digest();
      var sb = new StringBuilder();
      for (var b : hashByes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();

    } catch (IOException | NoSuchAlgorithmException e) {
      this.plugin.getLogger().severe(String.format("Erreur lors du calcul du SHA-1 : %s", e.getMessage()));
      return null;
    }
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Recursively deletes all files and subdirectories inside the provided directory.
   * Used to reset the build folder before regenerating a pack.
   *
   * @param dir the directory to clear
   */
  private void clearDirectory(final @NotNull File dir) {
    if (!dir.exists()) return;

    final var files = dir.listFiles();
    if (files == null) return;

    for (final var file : files) {
      if (file.isDirectory())
        clearDirectory(file);

      if (!file.delete())
        this.plugin.getLogger().warning(String.format("Impossible de supprimer %s", file.getAbsolutePath()));

    }

  }

  /**
   * Generates the default bossbar textures inside the resource pack.
   *
   * <p>These textures are generated based on the configured {@link net.kyori.adventure.bossbar.BossBar.Color}
   * and written to the {@code assets/minecraft/textures/gui/sprites/boss_bar} directory
   * to ensure proper visual alignment with DreamHud HUD overlays.</p>
   */
  private void generateBossBarTextures() {
    final var color = this.codexService.getConfig().bar_color.name().toLowerCase();

    final var bossbarDir = new File(this.buildDir, "assets/minecraft/textures/gui/sprites/boss_bar");
    bossbarDir.mkdirs();

    final var progressFile = new File(bossbarDir, String.format("%s_progress.png", color));
    final var backgroundFile = new File(bossbarDir, String.format("%s_background.png", color));

    try (var inputStream = this.plugin.getResource("bossbar.png")) {
      if (inputStream == null) {
        this.plugin.getLogger().warning("Impossible de trouver bossbar.png dans les resources du plugin !");
        return;
      }

      final var img = ImageIO.read(inputStream);
      ImageIO.write(img, "png", progressFile);
      ImageIO.write(img, "png", backgroundFile);

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info("Bossbar textures générées pour la couleur : " + color);
    } catch (IOException e) {
      this.plugin.getLogger().severe("Erreur lors de la génération des bossbars : " + e.getMessage());
    }

  }

  /**
   * Recursively zips the provided folder and all its subfiles into the given {@link ZipOutputStream}.
   *
   * @param folder      the folder to zip
   * @param parentPath  the parent path inside the ZIP archive
   * @param zos         the open ZIP output stream
   * @throws IOException if an error occurs during compression
   */
  private void zipFolder(final @NotNull File folder, final @NotNull String parentPath, final @NotNull ZipOutputStream zos) throws IOException {
    final var files = folder.listFiles();
    if (files == null) return;

    for (final var file : files) {
      final var entryName = parentPath + file.getName();

      if (file.isDirectory()) {
        zipFolder(file, String.format("%s/", entryName), zos);
        continue;
      }

      try (var fis = new FileInputStream(file)) {
        var entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        fis.transferTo(zos);
        zos.closeEntry();
      }
    }
  }

}
