package fr.dreamin.dreamHud.pack.font;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.config.CodexService;
import fr.dreamin.dreamHud.pack.PackLoaderService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IFontLoaderServiceTest {

  @Test
  void getTotalHeightOfReturnsMaxHeightAcrossFonts() throws Exception {
    final var plugin = mock(DreamHud.class);
    final var codexService = mock(CodexService.class);
    final var packLoader = mock(PackLoaderService.class);

    final var config = new CodexService.PluginConfig();
    config.namespace = "hudlib";
    config.default_font_name = "main";

    when(plugin.getLogger()).thenReturn(Logger.getLogger("Test"));
    when(codexService.getConfig()).thenReturn(config);

    try (MockedStatic<DreamHud> hudLibStatic = org.mockito.Mockito.mockStatic(DreamHud.class)) {
      hudLibStatic.when(() -> DreamHud.getService(CodexService.class)).thenReturn(codexService);
      hudLibStatic.when(() -> DreamHud.getService(PackLoaderService.class)).thenReturn(packLoader);

      final var service = new IFontLoaderService(plugin);
      injectFontHeights(service, Map.of(
        "main", 9,
        "font_main", 9,
        "hudlib:font_main", 9,
        "headline", 16,
        "font_headline", 16,
        "hudlib:font_headline", 16,
        "space_split", 2,
        "hudlib:space_split", 2
      ));

      final var component = Component.text("Title")
        .font(Key.key(config.namespace, "font_headline"))
        .append(Component.text(" body").font(Key.key(config.namespace, "font_main")))
        .append(Component.text(" ").font(Key.key(config.namespace, "space_split")));

      assertEquals(16, service.getTotalHeightOf(component));
    }
  }

  @SuppressWarnings("unchecked")
  private static void injectFontHeights(IFontLoaderService service, Map<String, Integer> heights) throws Exception {
    final Field field = IFontLoaderService.class.getDeclaredField("fontHeights");
    field.setAccessible(true);
    final var map = (Map<String, Integer>) field.get(service);
    map.clear();
    map.putAll(heights);
  }
}
