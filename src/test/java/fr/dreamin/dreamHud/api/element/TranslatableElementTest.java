package fr.dreamin.dreamHud.api.element;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.TranslationMetaService;
import fr.dreamin.dreamHud.internal.pack.font.FontLoaderService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslatableElementTest {

  @Test
  void getPixelWidthAggregatesBaseAndChildren() {
    final var translationMetaService = mock(TranslationMetaService.class);
    final var fontLoaderService = mock(FontLoaderService.class);

    when(translationMetaService.getWidthForKey("hud.title")).thenReturn(OptionalInt.of(12));
    when(fontLoaderService.getStringWidth("child", "main")).thenReturn(5);

    final var child = TextElement.builder()
      .id("child")
      .font("main")
      .shadow(false)
      .offsetX(0)
      .textSupplier(() -> "child")
      .build();

    final var element = TranslatableElement.builder()
      .id("parent")
      .font("main")
      .shadow(false)
      .offsetX(0)
      .key("hud.title")
      .element(child)
      .build();

    try (MockedStatic<DreamHud> hudLib = mockStatic(DreamHud.class)) {
      hudLib.when(() -> DreamHud.getService(TranslationMetaService.class)).thenReturn(translationMetaService);
      hudLib.when(() -> DreamHud.getService(FontLoaderService.class)).thenReturn(fontLoaderService);

      assertEquals(17, element.getPixelWidth());
    }
  }

  @Test
  void getPixelWidthThrowsWhenMetadataMissing() {
    final var translationMetaService = mock(TranslationMetaService.class);
    when(translationMetaService.getWidthForKey("hud.missing")).thenReturn(OptionalInt.empty());

    final var element = TranslatableElement.builder()
      .id("missing")
      .font("main")
      .shadow(false)
      .offsetX(0)
      .key("hud.missing")
      .build();

    try (MockedStatic<DreamHud> hudLib = mockStatic(DreamHud.class)) {
      hudLib.when(() -> DreamHud.getService(TranslationMetaService.class)).thenReturn(translationMetaService);

      final var exception = assertThrows(IllegalStateException.class, element::getPixelWidth);
      assertTrue(exception.getMessage().contains("hud.missing"));
    }
  }
}
