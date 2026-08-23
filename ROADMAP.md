# Roadmap — Télécommande Android

Cette roadmap suit l'assainissement, la sécurisation et la modernisation du projet **Télécommande**. Elle est mise à jour à chaque jalon important.

## Phase 1 — Stabilisation du projet ✅

- [x] Versionner le projet complet sur GitHub
- [x] Supprimer le dépôt Git imbriqué dans `core`
- [x] Vérifier le build Android
- [x] Vérifier le lancement de l'application
- [x] Vérifier la connexion TV
- [x] Vérifier l'appairage / PIN
- [x] Vérifier les commandes de télécommande

## Phase 2 — Fiabilisation du core ✅

- [x] Nettoyer le cycle de vie des sessions Pairing / Remote
- [x] Corriger les événements `SharedFlow`
- [x] Supprimer les événements rejoués accidentellement
- [x] Fiabiliser la transition pairing → remote
- [x] Garder une seule source de vérité pour l'état de connexion
- [x] Éviter les tentatives de connexion concurrentes
- [x] Fiabiliser reconnexion / changement de TV
- [x] Ajouter des timeouts réseau explicites
- [x] Nettoyer le spam Logcat du heartbeat
- [x] Mettre le KeyStore en cache
- [x] Préchauffer le KeyStore hors du thread principal

## Phase 3 — Sécurité SSL ✅

- [x] Retirer `DummyTrustManager` des connexions normales
- [x] Garder la confiance provisoire uniquement pendant le premier appairage
- [x] Récupérer le certificat de la TV après pairing
- [x] Stocker le certificat par TV
- [x] Associer le certificat au `keystoreAlias` de la TV
- [x] Épingler chaque connexion au certificat de la TV ciblée
- [x] Refuser un certificat inattendu
- [x] Oublier uniquement le certificat de la TV concernée
- [x] Conserver l'identité locale et les autres TV lors d'un oubli

## Phase 4 — Tests automatiques 🟢

- [x] Supprimer les faux tests Android Studio
- [x] Ajouter `ProtocolCoreTest`
- [x] Tester les conversions binaires / hex
- [x] Tester le framing réseau
- [x] Tester les commandes Remote
- [x] Tester le lancement d'application
- [x] Tester `RemoteConfigure`
- [x] Tester les messages de pairing
- [x] Tester l'encodage PIN / secret
- [x] Valider `:core:testDebugUnitTest`
- [ ] Ajouter des tests ciblés sur les repositories / managers
- [ ] Ajouter des scénarios d'erreur réseau / certificat

## Phase 5 — Nettoyage de l'architecture app ✅

- [x] Simplifier `PairingManager`
- [x] Supprimer son cycle d'initialisation artificiel
- [x] Simplifier `SettingsViewModel`
- [x] Unifier connexion / reconnexion dans `RemoteManager`
- [x] Examiner et réduire les UseCases `connection`
- [x] Examiner et réduire les UseCases `remote`
- [x] Examiner et réduire les UseCases `pairing`
- [x] Examiner et réduire les UseCases `discovery`
- [x] Supprimer les UseCases purement passe-plats
- [x] Conserver `ResetPairingUseCase` car il orchestre plusieurs repositories
- [x] Supprimer le relais `RemoteRepository.connectToActiveTv` devenu inutile
- [x] Auditer les repositories et conserver ceux qui portent une vraie responsabilité
- [x] Faire remonter correctement les erreurs du `PairingRepository`
- [x] Remplacer les providers Hilt manuels par des bindings `@Binds`
- [x] Vérifier la séparation `data / domain / ui`

## Phase 6 — Nettoyage général 🟡 EN COURS

- [x] Supprimer une première vague de code mort et d'état inutilisé
- [x] Réduire les logs de debug évidents dans l'UI et la découverte
- [x] Factoriser les commandes courtes de `HomeViewModel`
- [x] Nettoyer la configuration des boutons Home devenue redondante
- [x] Retirer les dépendances app directes `protobuf-javalite` et `constraintlayout-compose` inutilisées
- [x] Vérifier les warnings précédemment signalés (`Room` et barre de statut déjà corrigés)
- [ ] Auditer et simplifier les méthodes inutilisées de `SettingsRepository` / `PairedTvDao`
- [ ] Réduire les logs de debug restants du core et des managers sans masquer les erreurs utiles
- [ ] Vérifier les dépendances Gradle restantes et le version catalog
- [ ] Vérifier les permissions Android et la configuration réseau / sauvegarde
- [ ] Nettoyer les packages et la structure finale

## Phase 7 — UI / UX ⏳

- [ ] Revoir l'écran principal de télécommande
- [ ] Revoir l'écran paramètres / TV
- [ ] Améliorer le feedback connexion / déconnexion
- [ ] Améliorer le flux d'appairage et PIN
- [ ] Revoir navigation et états vides / erreurs
- [ ] Harmoniser le design Compose / Material 3
- [ ] Revoir ergonomie des boutons et commandes

## Phase 8 — Finalisation ⏳

- [ ] Tester TV éteinte / injoignable
- [ ] Tester perte Wi-Fi
- [ ] Tester changement de réseau
- [ ] Tester plusieurs TV
- [ ] Tester reprise après mise en veille
- [ ] Tester build release
- [ ] Nettoyage final
- [ ] Documentation finale du projet

---

### Commandes de validation

```powershell
.\gradlew :core:testDebugUnitTest :app:assembleDebug
```

Puis validation manuelle minimale :

1. lancement de l'application ;
2. connexion automatique à la TV active ;
3. commandes de télécommande ;
4. oubli d'une TV ;
5. nouvel appairage avec PIN.
