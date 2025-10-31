package fr.dreamin.dreamHud.internal.service;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.pack.font.FontLoaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.OptionalInt;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationMetaServiceImplTest {

  @Test
  void loadCreatesUnmodifiableMetadata(@TempDir Path tempDir) throws Exception {
    final var plugin = mock(DreamHud.class);
    final var codexService = mock(CodexService.class);
    final var fontLoader = mock(FontLoaderService.class);
    final var config = new CodexService.PluginConfig();
    config.debug = true;
    config.default_font_name = "main";

    when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
    when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
    when(codexService.getConfig()).thenReturn(config);

    final var metaFile = tempDir.resolve("translation_meta.json");
    Files.writeString(metaFile, "{\n  \"hud.test\": 42\n}");

    final var service = new TranslationMetaServiceImpl(plugin, codexService, () -> fontLoader);
    service.onLoad(plugin);

    final var metaMap = extractMetaMap(service);
    assertEquals(42, metaMap.get("hud.test"));
    assertThrows(UnsupportedOperationException.class, () -> metaMap.put("other", 1));
  }

  @Test
  void fallbackUsesFontLoaderWhenKeyMissing(@TempDir Path tempDir) throws Exception {
    final var plugin = mock(DreamHud.class);
    final var codexService = mock(CodexService.class);
    final var fontLoader = mock(FontLoaderService.class);
    final var config = new CodexService.PluginConfig();
    config.default_font_name = "main";

    when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
    when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
    when(codexService.getConfig()).thenReturn(config);
    when(fontLoader.getStringWidth("missing.key", "main")).thenReturn(27);

    final var metaFile = tempDir.resolve("translation_meta.json");
    Files.writeString(metaFile, "{ }");

    final var service = new TranslationMetaServiceImpl(plugin, codexService, () -> fontLoader);
    service.onLoad(plugin);

    final OptionalInt width = service.getWidthForKey("missing.key");
    assertTrue(width.isPresent());
    assertEquals(27, width.getAsInt());
    verify(fontLoader).getStringWidth("missing.key", "main");
  }

  @Test
  void returnsEmptyOptionalWhenNoFallbackFontConfigured(@TempDir Path tempDir) throws Exception {
    final var plugin = mock(DreamHud.class);
    final var codexService = mock(CodexService.class);
    final var fontLoader = mock(FontLoaderService.class);
    final var config = new CodexService.PluginConfig();
    config.default_font_name = null;

    when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
    when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
    when(codexService.getConfig()).thenReturn(config);

    final var metaFile = tempDir.resolve("translation_meta.json");
    Files.writeString(metaFile, "{ }");

    final var service = new TranslationMetaServiceImpl(plugin, codexService, () -> fontLoader);
    service.onLoad(plugin);

    final OptionalInt width = service.getWidthForKey("missing.key");
    assertTrue(width.isEmpty());
    verifyNoInteractions(fontLoader);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Integer> extractMetaMap(TranslationMetaServiceImpl service) throws NoSuchFieldException, IllegalAccessException {
    final Field field = TranslationMetaServiceImpl.class.getDeclaredField("metaMap");
    field.setAccessible(true);
    return (Map<String, Integer>) field.get(service);
  }
}
