# Roadmap — Télécommande Android V2

La V2 part de la V1.0 stable et figée sur la branche `v1.0`. L'objectif principal est d'étendre l'application à plusieurs TV et plusieurs familles de protocoles tout en conservant une API de télécommande commune côté application.

## Phase 1 — Architecture multi-protocole ⏳

- [x] Créer l'abstraction de base `TvProtocol`
- [x] Définir `TvProtocolType` pour identifier les familles de protocoles
- [x] Définir les capacités communes (`TvCapability`) : navigation, volume, mute, power, média, applications, clavier, sources
- [x] Définir les commandes communes de télécommande (`TvCommand`)
- [x] Permettre à chaque protocole d'annoncer ses capacités réelles
- [x] Enregistrer Android TV Remote v2 comme premier adaptateur dans `TvProtocolRegistry`
- [x] Faire transiter les commandes de l'application par l'abstraction commune `TvCommand`
- [x] Isoler les commandes Android TV v2 derrière l'adaptateur / mapping protocolaire
- [ ] Isoler complètement la connexion Android TV v2 derrière une interface client multi-protocole
- [ ] Séparer découverte, appairage/authentification, connexion et commandes pour chaque protocole
- [ ] Stocker le type de protocole avec chaque TV enregistrée

## Phase 2 — Découverte multi-protocole ⏳

- [ ] Remplacer la constante unique `_androidtvremote2._tcp.` par des fournisseurs de découverte
- [x] Remplacer `NsdManager` par une découverte mDNS directe pour Android TV / Google TV v2 : `_androidtvremote2._tcp.`
- [x] Détecter plusieurs TV Android/Google TV simultanément sur le même réseau local
- [x] Résoudre PTR / SRV / TXT / A / AAAA et récupérer les identifiants TXT stables (`bt`)
- [ ] Étudier et ajouter Android TV Remote v1 pour les anciens Android TV
- [ ] Ajouter SSDP/UPnP en complément de mDNS
- [ ] Dédupliquer une même TV découverte par plusieurs mécanismes
- [ ] Identifier automatiquement fabricant, OS/protocole, nom, IP et identifiant stable lorsque disponibles
- [ ] Ajouter une saisie IP manuelle comme fallback

## Phase 3 — Android TV / Google TV ✅

- [x] Android TV Remote Service v2 : mDNS `_androidtvremote2._tcp.`, pairing TLS 6467, remote TLS 6466
- [x] Découverte mDNS directe validée sur appareil physique
- [x] Philips `50PUS8106/12` détectée, appairée et contrôlée
- [x] Thomson / Google TV `GoogleTV8318` détectée, appairée et contrôlée
- [x] Navigation, volume, média, applications et extinction validés sur Thomson
- [x] Conserver l'épinglage certificat et la sécurité existante pour V2
- [x] Déclarer Android TV Remote v2 dans l'abstraction multi-protocole et ses capacités actuelles
- [x] Faire transiter les commandes de l'implémentation V2 actuelle par `TvCommand`
- [x] Valider sur TV réelle le chemin UI → `TvCommand` → Android TV Remote v2
- [ ] Ajouter le protocole Android TV Remote v1 pour les appareils utilisant l'ancien Remote Service
- [ ] Détecter automatiquement V1/V2 lorsque possible
- [ ] Ajouter Wake-on-LAN comme capacité séparée lorsque la TV le permet

## Phase 4 — Samsung Smart TV / Tizen ⏳

- [ ] Ajouter la découverte Samsung (SSDP/réseau local)
- [ ] Implémenter le canal remote WebSocket Samsung
- [ ] Supporter `ws://TV:8001` et `wss://TV:8002` selon le modèle
- [ ] Gérer l'autorisation affichée sur la TV et stocker le token
- [ ] Mapper navigation, volume, mute, power, média et sources vers les `KEY_*` Samsung
- [ ] Ajouter Wake-on-LAN lorsque le modèle le permet
- [ ] Validation matérielle à effectuer lorsqu'une Samsung compatible sera disponible

## Phase 5 — LG webOS ⏳

- [ ] Ajouter la découverte LG via SSDP (`urn:lge-com:service:webos-second-screen:1`) et fallback réseau
- [ ] Implémenter SSAP WebSocket
- [ ] Supporter `ws://TV:3000` pour anciens firmwares et `wss://TV:3001` pour firmwares récents
- [ ] Gérer l'appairage à l'écran et la persistance du `client-key`
- [ ] Implémenter le pointer input socket pour D-pad / OK / Back / Home
- [ ] Mapper volume, mute, média, power et lancement d'applications
- [ ] Prévoir Wake-on-LAN pour l'allumage lorsque disponible
- [ ] Validation matérielle à effectuer lorsqu'une LG webOS sera disponible

## Phase 6 — Roku TV / Roku Player ⏳

- [ ] Ajouter la découverte SSDP Roku avec `ST: roku:ecp`
- [ ] Utiliser l'URL ECP exposée par la TV, généralement sur le port 8060
- [ ] Implémenter les commandes ECP `keypress`, `keydown`, `keyup`
- [ ] Mapper navigation, média, volume et power selon les capacités de l'appareil
- [ ] Gérer la contrainte Roku « Control by mobile apps »
- [ ] Validation matérielle à effectuer lorsqu'un Roku sera disponible

## Phase 7 — Fire TV / Fire OS 🔬

- [ ] Étudier la découverte DIAL/SSDP : `urn:dial-multiscreen-org:service:dial:1`
- [ ] Séparer ce que DIAL permet réellement (découverte/lancement d'apps) du contrôle de télécommande complet
- [ ] Étudier une implémentation ADB réseau comme adaptateur optionnel
- [ ] Ne pas rendre les Developer Options/ADB obligatoires pour les autres protocoles
- [ ] Décider après prototype si Fire TV complet entre dans la V2 ou une V2.x

## Phase 8 — Apple TV 🔬

- [ ] Étudier `_mediaremotetv._tcp.local.` et `_companion-link._tcp.local.`
- [ ] Étudier Media Remote Protocol / Companion pour navigation et média
- [ ] Évaluer le coût de l'appairage/chiffrement avant intégration
- [ ] Décider après prototype si Apple TV entre dans la V2 ou une V2.x

## Phase 9 — Autres écosystèmes TV 🔬

- [ ] Identifier les protocoles réellement accessibles pour VIDAA / Hisense
- [ ] Identifier les modèles Philips non Android/Google TV (Saphi/Titan OS)
- [ ] Identifier les modèles Panasonic non Android/Google TV
- [ ] Ne pas considérer la marque seule comme protocole : TCL, Philips, Hisense, Sony, etc. peuvent utiliser Android TV, Google TV, Roku TV, Fire TV ou un OS propriétaire selon le modèle
- [ ] Ajouter uniquement les protocoles testables et suffisamment stables

## Phase 10 — Multi-TV utilisateur ⏳

- [ ] Enregistrer plusieurs TV simultanément
- [ ] Choisir une TV active rapidement depuis l'écran principal
- [ ] Définir une TV par défaut
- [ ] Mémoriser protocole, identifiant, IP/host, credentials/token/certificat propres à chaque TV
- [ ] Reconnexion automatique à la bonne TV
- [ ] Renommage local indépendant du nom technique
- [ ] Gestion propre des TV indisponibles ou hors réseau
- [ ] Oubli d'une TV sans affecter les autres

## Phase 11 — UI / UX V2 ⏳

- [ ] Reprendre les validations petit / moyen / grand téléphone reportées de V1
- [ ] Ajuster les dimensions finales après tests
- [ ] Afficher les capacités disponibles selon la TV active
- [ ] Masquer ou désactiver les commandes non supportées par le protocole courant
- [ ] Ajouter un sélecteur de TV rapide sans surcharger l'écran principal
- [ ] Ajouter retour haptique et maintien appuyé/répétition pour les commandes adaptées

## Phase 12 — Clavier TV ⏳

- [x] Définir une capacité `TEXT_INPUT` commune
- [ ] Android TV : exploiter le support texte du protocole si disponible
- [ ] Adapter le clavier aux protocoles Samsung/LG/Apple selon leurs possibilités
- [ ] N'afficher le clavier que lorsqu'il est réellement supporté

## Phase 13 — Applications personnalisables ⏳

- [ ] Remplacer la grille d'applications fixe par une configuration par TV/protocole
- [ ] Permettre d'ajouter, supprimer et réordonner les raccourcis
- [ ] Adapter le lancement d'application à chaque protocole
- [ ] Conserver des presets Netflix / YouTube / Plex / Crunchyroll lorsque disponibles

## Phase 14 — Fiabilité et tests ⏳

- [ ] Ajouter tests ciblés repositories/managers reportés de V1
- [ ] Ajouter scénarios d'erreur réseau/certificat
- [ ] Tester la déduplication de découverte multi-protocole
- [ ] Tester les credentials séparés par TV
- [ ] Tester changement de TV sans fuite de session
- [ ] Tester changement de réseau et reprise après veille
- [ ] Ajouter tests par adaptateur de protocole

## Phase 15 — Finalisation V2 ⏳

- [ ] Nettoyage final
- [ ] Documentation protocoles supportés / limitations par modèle
- [ ] Build debug et release
- [ ] Tests réels sur les familles de TV disponibles
- [ ] Générer et installer un APK release signé
- [ ] Figer la V2 stable dans une branche dédiée

---

## Protocoles identifiés au démarrage de la V2

| Famille | Découverte | Contrôle | Priorité |
| --- | --- | --- | --- |
| Android TV / Google TV v2 | mDNS `_androidtvremote2._tcp.` | TLS + Protobuf, 6466/6467 | Validé Philips + Thomson |
| Android TV ancien | protocole Remote v1 à étudier | Remote v1 | Haute |
| Samsung Tizen | SSDP / découverte réseau | WebSocket 8001/8002 | Haute — validation matérielle différée |
| LG webOS | SSDP `webos-second-screen` | SSAP WebSocket 3000/3001 | Haute — validation matérielle différée |
| Roku | SSDP `roku:ecp` | HTTP ECP, généralement 8060 | Haute — validation matérielle différée |
| Fire TV | DIAL/SSDP + éventuellement ADB | DIAL limité / ADB optionnel | Recherche |
| Apple TV | mDNS `_mediaremotetv._tcp.` / `_companion-link._tcp.` | MRP / Companion | Recherche |

### Principe architectural

La V2 ne doit pas chercher une liste de variantes de `_androidtvremote2`. Une Smart TV peut utiliser un mécanisme complètement différent : mDNS, SSDP/UPnP, WebSocket, HTTP, TLS/Protobuf ou un protocole propriétaire. La bonne approche est donc de rendre le `core` extensible avec plusieurs adaptateurs de découverte et de contrôle partageant une interface commune.
