package fr.dreamin.dreamHud.internal.pack;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Core service responsible for building, managing, and exporting the
 * DreamHud resource pack at runtime.
 *
 * <p>The {@code PackLoaderService} acts as the foundation of DreamHud’s
 * resource management system. It handles texture registration, font and
 * background integration, and automatic packaging of all assets into a
 * zipped resource pack compatible with Minecraft clients.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Create and maintain the resource pack’s working directory.</li>
 *   <li>Handle runtime injection of fonts, backgrounds, and texture files.</li>
 *   <li>Generate the final zipped resource pack and compute its SHA-1 hash.</li>
 *   <li>Provide access to internal folders such as fonts, backgrounds, and builds.</li>
 * </ul>
 *
 * <p>All generated files (e.g. from {@link fr.dreamin.dreamHud.internal.pack.font.FontLoaderService}
 * or {@link fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService})
 * are sent to this service for integration into the live pack structure.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * PackLoaderService loader = DreamHud.getService(PackLoaderService.class);
 * File buildDir = loader.getBuildFolder();
 *
 * // Add a dynamically generated font
 * loader.sendToFont(new File(buildDir, "font_custom.json"));
 *
 * // Package and hash the resource pack
 * loader.zipResourcePack(buildDir);
 * String sha1 = loader.getPackSha1();
 * }</pre>
 *
 * @see fr.dreamin.dreamHud.internal.pack.font.FontLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.neg.NegSpaceFontService
 * @see fr.dreamin.dreamHud.internal.config.CodexService
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface PackLoaderService {

  /**
   * Initializes and generates the base structure of the DreamHud resource pack.
   * <p>This includes creating necessary folders and copying any bundled default assets.</p>
   */
  void generateBasePack();

  /**
   * Zips the current resource pack folder into a final distributable pack.
   *
   * @param sourceDir the directory containing the built pack structure
   */
  void zipResourcePack(final @NotNull File sourceDir);

  /**
   * @return the root folder used for building the resource pack
   */
  File getBuildFolder();

  /**
   * @return the folder containing all Minecraft font JSON definitions
   */
  File getFontFolder();

  /**
   * @return the folder containing all texture PNGs used by fonts
   */
  File getFontTexturesFolder();

  /**
   * @return the folder containing background texture PNGs used by HUD elements
   */
  File getBackgroundTexturesFolder();

  /**
   * Sends a JSON font definition file (e.g. {@code font_custom.json}) to the pack build.
   *
   * @param file the font JSON file to include
   */
  void sendToFont(final @NotNull File file);

  /**
   * Sends a font texture (e.g. {@code font_custom.png}) to the pack build.
   *
   * @param file the texture file to include
   */
  void sendToFontTextures(final @NotNull File file);

  /**
   * Sends a background texture file to the corresponding subfolder in the pack.
   *
   * @param file the background texture PNG to include
   * @param name the background name (used to determine folder path)
   */
  void sendToBackgroundTextures(final @NotNull File file, final @NotNull String name);

  /**
   * Sends a file to a relative location inside the resource pack folder.
   *
   * @param file          the source file to copy
   * @param relativePath  the destination path relative to the resource pack root
   */
  void sendTo(final @NotNull File file, final @NotNull String relativePath);

  /**
   * Copies a file from source to target within the pack structure.
   *
   * @param source the file to copy
   * @param target the destination file
   */
  void copyFileTo(final @NotNull File source, final @NotNull File target);

  /**
   * Retrieves the SHA-1 hash of the most recently generated resource pack zip.
   *
   * <p>This hash is used by Minecraft to detect and validate resource packs
   * on the client side. It is typically included in the {@code sendResourcePack()}
   * call to ensure integrity verification.</p>
   *
   * @return the SHA-1 hash of the current resource pack, or {@code null} if unavailable
   */
  String getPackSha1();

}
