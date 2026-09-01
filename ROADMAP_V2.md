# Roadmap — Télécommande Android V2

La V2 part de la V1.0 stable et figée sur la branche `v1.0`.

Après réaudit de l'état réel de l'application, la V2 est recentrée sur **Android TV / Google TV**, seul écosystème actuellement testable sur matériel réel. L'architecture multi-protocole reste en place pour permettre une extension future, mais Samsung, LG, Roku, Fire TV et Apple TV sont reportés tant qu'un appareil compatible n'est pas disponible pour validation.

## Phase 1 — Architecture extensible ✅

- [x] Créer l'abstraction de base `TvProtocol`
- [x] Définir `TvProtocolType`
- [x] Définir les capacités communes `TvCapability`
- [x] Définir les commandes communes `TvCommand`
- [x] Permettre à un protocole d'annoncer ses capacités
- [x] Enregistrer Android TV Remote v2 dans `TvProtocolRegistry`
- [x] Faire transiter les commandes de l'application par `TvCommand`
- [x] Isoler le mapping des commandes Android TV v2
- [x] Isoler la connexion d'une TV enregistrée de l'appairage et router selon son `TvProtocolType`
- [x] Stocker le type de protocole avec chaque TV enregistrée
- [x] Migrer Room 1 → 2 sans perdre les TV existantes
- [x] Créer le contrat commun `TvDiscoveryProvider`
- [x] Transformer `TvDiscoveryManager` en orchestrateur de providers
- [x] Conserver une architecture permettant d'ajouter d'autres protocoles plus tard

> Les abstractions supplémentaires de session/authentification ne sont plus bloquantes pour la V2 Android TV. Elles seront approfondies lorsqu'un second écosystème réellement testable devra être intégré.

## Phase 2 — Découverte Android TV / Google TV ✅

- [x] Remplacer `NsdManager` par une découverte mDNS directe
- [x] Utiliser `_androidtvremote2._tcp.`
- [x] Résoudre PTR / SRV / TXT / A / AAAA
- [x] Récupérer l'identifiant TXT `bt`
- [x] Détecter plusieurs TV simultanément
- [x] Identifier chaque résultat avec son `TvProtocolType`
- [x] Valider la découverte sur appareil Android physique
- [x] Détecter simultanément Philips `50PUS8106/12` et Thomson / Google TV `GoogleTV8318`

### À étudier uniquement si un besoin réel apparaît

- [ ] Android TV Remote v1 pour d'anciens appareils incompatibles avec Remote v2
- [ ] Saisie IP manuelle comme fallback si une TV compatible n'annonce pas son service mDNS

## Phase 3 — Android TV / Google TV Remote v2 ✅

- [x] Pairing TLS
- [x] Connexion Remote TLS + Protobuf
- [x] Épinglage certificat / credentials persistants
- [x] Navigation D-pad + OK
- [x] Back / Home
- [x] Volume + mute
- [x] Média : retour, play/pause, stop, avance
- [x] Lancement d'applications
- [x] Extinction
- [x] Reconnexion à une TV déjà appairée
- [x] Philips testée en conditions réelles
- [x] Thomson / Google TV testée en conditions réelles
- [x] Chemin UI → `TvCommand` → Android TV Remote v2 validé sur TV réelle

### Limitation connue

- [ ] Réveil/allumage réseau : à traiter comme capacité séparée. L'extinction fonctionne sur Thomson, mais son réveil réseau n'est pas validé.

## Phase 4 — Multi-TV utilisateur ✅

- [x] Enregistrer plusieurs TV simultanément
- [x] Afficher toutes les TV enregistrées dans `Mes TV`
- [x] Choisir une TV active en touchant sa carte
- [x] Changer de TV sans refaire l'appairage
- [x] Afficher visuellement la TV active/connectée
- [x] Mémoriser IP, identifiant, credentials/certificat et protocole par TV
- [x] Reconnexion à la TV active
- [x] Renommage local indépendant du nom technique
- [x] Oublier/supprimer une TV individuellement sans supprimer les autres
- [x] Validation réelle avec deux TV enregistrées : Philips `Salon` + Thomson `Chambre Caleb`

> Le bouton/indicateur d'état de l'écran principal ouvre déjà l'écran de choix des TV. Un sélecteur supplémentaire directement dans la télécommande n'est donc pas considéré comme nécessaire actuellement.

## Phase 5 — UI / UX V2 ⏳

### Déjà en place

- [x] Écran `Choisir une TV`
- [x] Sections `Ajouter une TV` et `Mes TV`
- [x] Carte distincte pour chaque TV enregistrée
- [x] État connecté visible
- [x] Renommage depuis la liste des TV
- [x] Suppression/oubli depuis la liste des TV
- [x] Nom de la TV active affiché dans l'en-tête de la télécommande
- [x] Accès à l'écran TV depuis l'indicateur d'état de la télécommande

### Reste à valider/améliorer

- [ ] Validation finale sur petit / moyen / grand téléphone
- [ ] Ajuster les dimensions finales uniquement si les tests montrent un problème
- [ ] Retour haptique sur les commandes si souhaité
- [ ] Maintien appuyé/répétition pour les commandes où cela apporte un vrai gain

## Phase 6 — Clavier TV ⏳

- [x] Définir la capacité commune `TEXT_INPUT`
- [ ] Vérifier ce que permet réellement Android TV Remote v2 pour la saisie de texte
- [ ] Implémenter le clavier uniquement si le comportement est fiable sur les TV de test

## Phase 7 — Applications personnalisables ⏳

### Actuel

- [x] Raccourcis Netflix / YouTube / Plex / Crunchyroll
- [x] Lancement des applications validé avec Android TV Remote v2

### Optionnel pour la V2

- [ ] Permettre de personnaliser les raccourcis
- [ ] Ajouter/supprimer/réordonner les applications
- [ ] Prévoir éventuellement une configuration différente par TV

## Phase 8 — Fiabilité et tests ⏳

### Déjà validé en conditions réelles

- [x] Découverte de plusieurs Android/Google TV sur le même réseau
- [x] Appairage de deux TV distinctes
- [x] Credentials séparés par TV
- [x] Changement de TV active
- [x] Reconnexion après sélection
- [x] Commandes via `TvCommand`
- [x] Migration Room 1 → 2 avec conservation des TV existantes
- [x] Build/tests Gradle validés après les principales migrations d'architecture

### À compléter

- [ ] Ajouter des tests unitaires ciblés sur les repositories/managers lorsque pertinent
- [ ] Ajouter des scénarios d'erreur réseau/certificat
- [ ] Tester changement de réseau et reprise après veille plus largement
- [ ] Vérifier l'absence de fuite de session lors de changements répétés entre les deux TV

## Phase 9 — Finalisation V2 ⏳

- [ ] Nettoyage final du code et des logs de diagnostic
- [ ] Documentation des fonctions Android TV / Google TV supportées
- [ ] Documenter les limitations connues, notamment le réveil réseau
- [ ] Build debug final
- [ ] Build release final
- [ ] Tests réels finaux sur Philips + Thomson
- [ ] Générer et installer l'APK release signé
- [ ] Figer la V2 stable dans une branche dédiée

---

# Protocoles reportés — matériel de test nécessaire

Ces écosystèmes **ne font plus partie du développement actif de la V2**. Aucun ne devra être annoncé comme supporté tant que découverte, appairage et commandes n'auront pas été testés sur une vraie TV ou un vrai appareil compatible.

## Samsung Smart TV / Tizen — REPORTÉ

- Découverte réseau / SSDP selon génération
- WebSocket 8001/8002
- Autorisation TV et token
- Commandes `KEY_*`
- Wake-on-LAN éventuel

**Reprise :** lorsqu'une Samsung compatible sera disponible pour tests.

## LG webOS — REPORTÉ

- SSDP `webos-second-screen`
- SSAP WebSocket 3000/3001
- Appairage et `client-key`
- Pointer input socket
- Applications / média / volume / power

**Reprise :** lorsqu'une LG webOS sera disponible pour tests.

## Roku — PROTOTYPE DE DÉCOUVERTE, SUITE REPORTÉE

- [x] Moteur SSDP M-SEARCH générique créé
- [x] Provider expérimental `ST: roku:ecp` créé
- [ ] Validation de la découverte sur vrai Roku
- [ ] `query/device-info`
- [ ] Commandes ECP

Le provider actuel est un **prototype non validé matériellement** et ne signifie pas que Roku est supporté par l'application.

**Reprise :** lorsqu'un Roku sera disponible pour tests.

## Fire TV / Fire OS — REPORTÉ

- DIAL/SSDP à étudier
- Contrôle complet à distinguer de DIAL
- ADB réseau éventuellement optionnel

**Reprise :** lorsqu'un Fire TV sera disponible pour tests.

## Apple TV — REPORTÉ

- `_mediaremotetv._tcp.local.` / `_companion-link._tcp.local.`
- Media Remote Protocol / Companion
- Appairage/chiffrement

**Reprise :** lorsqu'un Apple TV sera disponible pour tests.

## Autres OS TV — REPORTÉ

VIDAA / Hisense, Philips Saphi/Titan OS, Panasonic et autres systèmes propriétaires seront étudiés uniquement si un appareil testable devient disponible.

---

## État des protocoles

| Famille | État réel |
| --- | --- |
| Android TV / Google TV Remote v2 | **Supporté et validé — Philips + Thomson** |
| Android TV Remote v1 | Non implémenté — à étudier seulement si nécessaire |
| Samsung Tizen | Reporté — aucun matériel de validation |
| LG webOS | Reporté — aucun matériel de validation |
| Roku | Prototype de découverte uniquement — non validé |
| Fire TV | Reporté — aucun matériel de validation |
| Apple TV | Reporté — aucun matériel de validation |

### Principe

Une fonctionnalité/protocole n'est considéré comme supporté que lorsqu'il peut être testé de bout en bout sur du matériel réel. L'architecture reste extensible, mais la V2 active se concentre désormais sur ce qui peut être développé et validé sérieusement : **Android TV / Google TV Remote v2, multi-TV, UX, fiabilité et finalisation**.
