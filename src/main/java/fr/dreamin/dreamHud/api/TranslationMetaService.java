package fr.dreamin.dreamHud.api;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

/**
 * Service permettant de récupérer les métadonnées associées aux traductions côté client.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>Les largeurs retournées sont exprimées en pixels Adventure et sont toujours positives.</li>
 *   <li>Une valeur vide signifie qu'aucun fallback fiable n'a pu être calculé.</li>
 * </ul>
 *
 * <p>Chaque clé correspond à une entrée du fichier {@code translation_meta.json} généré dans le dossier
 * de données du plugin. Lorsqu'une clé est présente, la largeur de base (exprimée en pixels) est retournée.
 * Si la clé est absente, l'implémentation tente d'estimer la largeur à l'aide du
 * {@link fr.dreamin.dreamHud.internal.pack.font.FontLoaderService FontLoaderService} et du nom de police par défaut.
 * En cas d'échec de ce fallback, une valeur vide est renvoyée.</p>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * TranslationMetaService meta = HudLib.getService(TranslationMetaService.class);
 * OptionalInt width = meta.getWidthForKey("hud.example");
 * width.orElseThrow(() -> new IllegalStateException("Missing metadata"));
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
public interface TranslationMetaService {

  /**
   * Récupère la largeur en pixels associée à la clé de traduction fournie.
   *
   * @param key clé de traduction Adventure utilisée côté client (ex: {@code hud.example}).
   * @return {@link OptionalInt} contenant la largeur lorsqu'elle est connue ou calculable, ou vide si aucune
   * donnée ni fallback valide n'ont pu être déterminés.
   *
   * @since 1.0.0
   *
   */
  @NotNull OptionalInt getWidthForKey(final @NotNull String key);
}
