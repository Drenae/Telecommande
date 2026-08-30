# Télécommande Android TV

Application Android native de télécommande pour Android TV / Google TV, développée en Kotlin avec Jetpack Compose.

La V1 permet d'appairer une TV compatible sur le réseau local, d'établir une connexion distante sécurisée en SSL, puis d'utiliser les principales commandes d'une télécommande physique depuis un téléphone Android.

## Fonctionnalités V1

- découverte des TV Android / Google TV sur le réseau local via NSD ;
- appairage avec code PIN alphanumérique ;
- connexion SSL avec épinglage du certificat de la TV ;
- mémorisation locale de la TV appairée ;
- reconnexion automatique à la TV active ;
- D-pad avec OK, Haut, Bas, Gauche et Droite ;
- boutons Retour, Accueil et Power ;
- contrôle du volume avec slider et bouton Muet ;
- commandes média Recul, Lecture/Pause, Stop et Avance ;
- lancement rapide de Netflix, YouTube, Plex et Crunchyroll ;
- renommage local de la TV sans modifier son identité technique ;
- gestion de l'oubli / nouvel appairage ;
- thème sombre Material 3 adapté à une utilisation de type télécommande.

## Pré-requis

- téléphone sous Android 8.0 ou supérieur (`minSdk 26`) ;
- Android TV / Google TV compatible avec le protocole Android TV Remote ;
- téléphone et TV connectés au même réseau Wi-Fi ;
- Android Studio récent avec un JDK compatible Gradle ;
- SDK Android 35 pour compiler le projet.

## Architecture

Le projet est séparé en deux modules principaux :

```text
Telecommande/
├── app/      # UI, navigation, ViewModels, Room, repositories applicatifs
├── core/     # découverte, pairing, protocole remote, SSL, framing réseau
├── ROADMAP.md
└── README.md
```

### Module `app`

Le module `app` contient notamment :

- l'interface Jetpack Compose ;
- la navigation ;
- les ViewModels ;
- la base Room contenant les informations de TV appairées ;
- les repositories applicatifs ;
- l'intégration Hilt ;
- les ressources graphiques et le thème.

### Module `core`

Le module `core` contient la partie protocolaire et réseau :

- découverte NSD `_androidtvremote2._tcp.` ;
- gestion du pairing ;
- protocole Android TV Remote ;
- messages Protobuf ;
- sockets SSL ;
- validation et épinglage du certificat par TV ;
- gestion du heartbeat ;
- framing et parsing des messages réseau.

## Sécurité

Lors du premier appairage, l'application établit la confiance nécessaire à la phase de pairing puis récupère le certificat présenté par la TV.

Ce certificat est ensuite associé à la TV concernée et utilisé pour valider les connexions Remote suivantes. Une connexion présentant un certificat inattendu est refusée.

L'application désactive également le trafic HTTP en clair avec `usesCleartextTraffic=false`.

Les données de pairing ne sont pas incluses dans Android Auto Backup afin d'éviter de restaurer une identité ou des certificats sur un autre appareil.

## Compiler le projet

Depuis PowerShell, à la racine du dépôt :

```powershell
.\gradlew :core:testDebugUnitTest :app:assembleDebug
```

APK debug généré dans :

```text
app/build/outputs/apk/debug/
```

Pour compiler la variante release :

```powershell
.\gradlew :app:assembleRelease
```

Un build release réellement distribuable doit être signé avec une clé de signature personnelle.

## Générer une APK release signée

Dans Android Studio :

```text
Build
→ Generate Signed App Bundle or APK
→ APK
```

Créer ou sélectionner un keystore personnel, choisir la variante `release`, puis générer l'APK.

Le keystore et ses mots de passe ne doivent jamais être ajoutés au dépôt Git.

Lors des tests de la V1, l'APK release signée a été installée et validée sur téléphone réel.

## Installer une APK avec ADB

Avec le téléphone connecté et le débogage USB ou sans fil activé :

```powershell
adb install -r "chemin\vers\app-release.apk"
```

Si une version debug de l'application est déjà installée, sa signature sera différente de celle de la release. Android peut alors retourner :

```text
INSTALL_FAILED_UPDATE_INCOMPATIBLE
```

Dans ce cas :

```powershell
adb uninstall com.telecommande
adb install "chemin\vers\app-release.apk"
```

La désinstallation supprime les données locales et nécessite donc un nouvel appairage avec la TV.

## Validation du projet

Commandes de validation principales :

```powershell
.\gradlew :core:testDebugUnitTest :app:assembleDebug
.\gradlew :app:assembleRelease
```

La V1 a également été validée manuellement sur téléphone réel avec :

- installation propre ;
- premier appairage ;
- connexion automatique ;
- commandes D-pad ;
- volume / mute ;
- commandes média ;
- lancement d'applications ;
- extinction et rallumage de la TV ;
- reprise après mise en veille du téléphone ;
- commandes répétées sans déconnexion / reconnexion intempestive.

## Contraintes connues de la V1

La V1 est volontairement centrée sur un usage simple avec une TV principale.

- le téléphone et la TV doivent être sur le même réseau local ;
- la gestion avancée de plusieurs TV est prévue pour une évolution ultérieure ;
- les derniers ajustements visuels multi-tailles de téléphone sont reportés ;
- des tests automatiques supplémentaires sur certains managers, repositories et scénarios d'erreur réseau / certificat pourront être ajoutés après la V1.

## Développement futur

Les pistes prévues après la V1 incluent notamment :

- gestion multi-TV plus complète ;
- validation et ajustements UI sur davantage de tailles d'écran ;
- couverture de tests réseau / certificat plus poussée ;
- amélioration progressive de l'expérience utilisateur sans modifier la stabilité du core validé.

## Version

Version actuelle : **1.0**

- `versionCode = 1`
- `versionName = "1.0"`
- `applicationId = "com.telecommande"`
