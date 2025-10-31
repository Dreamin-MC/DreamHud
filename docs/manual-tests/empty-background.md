# Test manuel : dossier de backgrounds vide

## Objectif
Vérifier que l'appel à `IBackgroundLoaderService#loadAllBackgrounds` ne lève plus d'exception lorsqu'aucun sous-dossier n'est présent dans `plugins/HudLib/background`.

## Préconditions
- Serveur Paper configuré avec le plugin HudLib compilé.
- Mode debug activé dans la configuration (`codex.debug = true`).

## Étapes
1. Démarrer le serveur une première fois pour générer le dossier `plugins/HudLib`.
2. Supprimer le contenu du dossier `plugins/HudLib/background` en laissant le dossier vide.
3. Démarrer le serveur Paper.

## Résultat attendu
- Aucun stack trace ni exception dans la console lors du chargement du plugin.
- Un log d'information en mode debug :
  ```
  Aucun background chargé : aucun dossier trouvé dans <chemin-complet>
  ```
