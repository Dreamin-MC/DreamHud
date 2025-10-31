package fr.dreamin.dreamHud.pack.background;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.config.CodexService;
import fr.dreamin.dreamHud.pack.PackLoaderService;
import fr.dreamin.dreamHud.pack.background.BackgroundLoaderService.Background;
import fr.dreamin.dreamHud.pack.background.BackgroundLoaderService.Segment;
import fr.dreamin.dreamHud.pack.neg.NegSpaceFontService;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class IBackgroundLoaderServiceTest {

  private static final List<String> logMessages = new ArrayList<>();

  @AfterEach
  void resetLogs() {
    logMessages.clear();
  }

  @Test
  void generateBackgroundReturnsEmptyWhenNegativeGlyphMissing() throws Exception {
    final var plugin = mock(DreamHud.class);
    final var codexService = mock(CodexService.class);
    final var packLoader = mock(PackLoaderService.class);
    final var negSpaceFont = mock(NegSpaceFontService.class);

    final var config = new CodexService.PluginConfig();
    config.namespace = "hudlib";

    when(plugin.getLogger()).thenReturn(createLogger());
    when(codexService.getConfig()).thenReturn(config);
    when(negSpaceFont.getCharForHeight(-3)).thenReturn(null);

    try (MockedStatic<DreamHud> hudLibStatic = org.mockito.Mockito.mockStatic(DreamHud.class)) {
      hudLibStatic.when(() -> DreamHud.getService(CodexService.class)).thenReturn(codexService);
      hudLibStatic.when(() -> DreamHud.getService(PackLoaderService.class)).thenReturn(packLoader);
      hudLibStatic.when(() -> DreamHud.getService(NegSpaceFontService.class)).thenReturn(negSpaceFont);

      final var service = new IBackgroundLoaderService(plugin);
      injectBackground(service, createBackground());

      final var result = service.generateBackground(4, "test");
      assertEquals(Component.empty(), result);
      assertEquals(0, service.getTotalNegativeOffset(4, "test"));

      verifyNoMoreInteractions(packLoader);
    }

    assertTrue(logMessages.stream().anyMatch(msg -> msg.contains("glyph négative -3")));
  }

  private static Logger createLogger() {
    final var logger = Logger.getLogger("IBackgroundLoaderServiceTest");
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.ALL);
    for (Handler handler : logger.getHandlers()) {
      logger.removeHandler(handler);
    }
    logger.addHandler(new Handler() {
      @Override
      public void publish(LogRecord record) {
        logMessages.add(record.getMessage());
      }

      @Override
      public void flush() {
        // nothing to flush
      }

      @Override
      public void close() {
        // nothing to close
      }
    });
    return logger;
  }

  private static Background createBackground() {
    final var segments = new LinkedHashMap<String, Segment>();
    segments.put("start", new Segment('\uE000', 2));
    segments.put("middle_4", new Segment('\uE001', 4));
    segments.put("end", new Segment('\uE002', 2));
    return new Background("test", 7, 14, segments);
  }

  @SuppressWarnings("unchecked")
  private static void injectBackground(IBackgroundLoaderService service, Background background) throws Exception {
    final Field field = IBackgroundLoaderService.class.getDeclaredField("backgrounds");
    field.setAccessible(true);
    final var map = (Map<String, Background>) field.get(service);
    map.clear();
    map.put(background.name(), background);
  }
}
