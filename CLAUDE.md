<!-- grace:begin -->
## Talents Grace (MCP)

Les conventions d'architecture et de code proviennent des **Talents Grace et Custom** servis par le MCP `grace`. Elles priment sur les conventions déduites du dépôt et tes habitudes. Ne les devine pas.

1. **Avant tout code** — Première action, avant même le plan : appelle `grace_prepare_task({ talentIds })`.
   - Cela couvre toute création ou modification de code, y compris tests, configuration-as-code et migrations. La lecture ou l'explication seule est exclue.
   - Désigne tous les talents touchés depuis le répertoire décrit par l'outil ; Grace et Custom se désignent pareil. Ne décris pas la tâche et ne laisse pas la liste vide. Un talent non désigné ne sera ni servi ni vérifié.
   - Conserve le `resolutionId` : il scelle le périmètre de revue.

2. **Si aucun talent n'est actif** — N'écris pas de code. Appelle immédiatement `grace_discover_codebase` et charge la skill homonyme si elle existe. Suis toute la procédure : analyse du dépôt, confirmation des hypothèses par l'utilisateur, validation explicite de la liste, installation, `grace_doctor`, puis installation des prérequis. Ne propose ni ne demande de contourner ce blocage ; une petite tâche ou le silence de l'utilisateur ne sont pas des exceptions. Seule exception : l'utilisateur a spontanément demandé auparavant de travailler sans talents.

3. **Au besoin** — Regroupe les adresses `grace://…` utiles par lots séquentiels de 20 maximum avec `grace_deepen({ uris: [{ uri }, …] })` ; garde `{ uri }` pour une adresse unique.

4. **Avant de terminer** — Charge `grace-review` si elle existe, puis appelle toujours `grace_review({ resolutionId })`, même si la préparation ne signalait aucune règle exploitable. Ce plan global courant fait autorité si le catalogue a changé. Appelle `grace_review({ resolutionId, talent })` uniquement pour les talents Grace pertinents avec des règles exploitables dans ce plan, jamais pour les talents sans règles ni pour les Custom. Les conventions restent applicables. Ces outils ne reçoivent ni fichiers ni diff et ne rendent aucun verdict : exécute leur protocole sur le changement complet, corrige toute violation bloquante et relance les contrôles touchés. Termine par `grace_record_validation` avec les verdicts motivés de toutes les règles Grace vérifiables et de tous les Talents Custom du périmètre.

Cherche les skills dans `.agents/skills/<nom>/SKILL.md`, puis `.claude/skills/<nom>/SKILL.md`. Laisse le harnais les charger automatiquement s'il sait le faire. Une skill présente prime sur ce résumé, jamais sur les règles servies par les outils ; sinon, les instructions du serveur MCP font foi.
<!-- grace:end -->

## Projet jev-demo

Démo de triage de logs ERROR en streaming, classifiés par **jev** (TypeSafe AI,
"System One model" — accès anticipé, doc : https://docs.typesafe.ai/llms.txt).
Code destiné à GitHub et à un REX LinkedIn : rester lisible et proportionné à
une démo, pas un produit.

### API jev (résumé)

- `POST https://api.typesafe.ai/v1/systemone`, `Authorization: Bearer <clé>`.
- Requête : `state` (string/objet/array), `model` (`"jev-latest"`), `questions`
  (map nom → `{type, instructions, criteria}`).
  - `choice` : `criteria` = map option → description (≤255 options) ; réponse
    `choice`, `probabilities`, `confidence` (0–1).
  - `score` : `criteria` = array ordonné de 2 à 10 niveaux ; réponse `score`
    (moyenne pondérée, 0..nb_niveaux-1), `legend`, `probabilities`,
    `confidence`.
  - `noul` : `criteria` optionnel `{true, false}` ; réponse `noul` (0–1,
    probabilité de "vrai"), pas de `confidence` séparée.
- Réponse : `model`, `answers` (mêmes clés que `questions`), `usage`.
- Un seul `state` par requête ; toutes les questions sont évaluées **en
  parallèle** (fan-out sans coût de latence supplémentaire).
- Erreurs : `401` (clé invalide), `422` (requête invalide), `429` (rate
  limit), `529` (surcharge) — retry avec backoff exponentiel sur 429/529.
- **Jaggedness jev-1.13** : instructions lues au pied de la lettre (pas
  d'implicite) ; pas de calcul/dates dans les `criteria` (garder ça côté
  code) ; état limité au contexte pertinent (`message` + `stackTrace`
  tronqués) ; pas de contenu adversarial traité comme suspect par défaut ;
  pas de combinaison arithmétique entre confidences de types différents
  (`noul`/`choice`/`score` non comparables) ; seuils non transférables d'un
  type de question à l'autre → la politique de routage (incrément 4) a un
  seuil configurable **par champ**, jamais un seuil global unique.

### Architecture

Hexagonale : domaine Kotlin pur (`domain/`) sans dépendance Spring/HTTP,
règle de dépendance stricte (tout pointe vers le domaine), ports définis
côté domaine/application, adaptateurs côté infrastructure. `LogClassifier`
est le port dont jev sera un adaptateur (remplaçable par construction).

Glossaire ubiquitaire (`domain/triage/`) :

| Terme | Sens |
|---|---|
| `LogEvent` | Événement de log ERROR applicatif à trier. |
| `Category` | Famille de la cause : bug applicatif, dépendance externe, configuration, bruit (`choice` jev). |
| `Severity` | Position sur un spectre de gravité (`score` jev). |
| `Actionable` | Probabilité brute qu'un développeur doive agir (`noul` jev). |
| `Classification` | `category`/`severity`/`actionable` + leur `Confidence` par champ : résultat du triage d'un `LogEvent`. |
| `Confidence` | Certitude de jev sur un champ `choice`/`score` (0.0–1.0) ; pour `actionable`, dérivée de la distance à 0.5. |
| `LogClassifier` | Port : capacité de classifier un `LogEvent`. |
| `TriageLogEventUseCase` | Use case qui orchestre le port pour produire une `Classification`, puis la route selon `RoutingThresholds`. |
| `RoutingThresholds`/`RoutingDecision` | Seuil de `Confidence` par champ ; décision `AUTOMATIC`/`FOR_REVIEW` qui en résulte. |
| `ReviewCase`/`ReviewQueue` | Un `LogEvent` + sa `Classification` routée `FOR_REVIEW` ; port qui les retient pour une décision humaine. |

### Talents Grace actifs sur ce projet

Installés pour ce projet : `architecture.hexagonal`, `architecture.port-adapter`,
`architecture.use-case`, `architecture.dependency-rule`, `ddd.value-object`,
`ddd.domain-error`, `ddd.ubiquitous-language`, `testing.tdd-direction`,
`testing.acceptance-tests`, `testing.contract-tests`,
`testing.property-based-testing`, `resilience.circuit-breaker`,
`observability.structured-logging`, `observability.telemetry`,
`devops.secret-management`, `git.conventional-commits`.

Écartés à la demande explicite (hors périmètre d'une démo sans base de
données ni messagerie) : `ddd.aggregate`, `ddd.entity`, `ddd.repository`,
`devops.db-migrations`, `architecture.outbox`, `architecture.event-driven`,
`devops.trunk-based`, `devops.artifact-management`.

Écartés par discernement (non demandés, non exclus explicitement) :
`ddd.domain-service` (la logique tient dans le use case), `ddd.tactical`
(son sujet principal — aggregate/entity/repository — est hors périmètre),
`architecture.clean-architecture` (concurrent de hexagonal, pas
complémentaire), `devops.observability` (redondant avec
`observability.structured-logging`/`observability.telemetry`),
`quality.tcr`, `testing.bdd`, `resilience.idempotency`, `design.solid`,
`design.tell-dont-ask`.

**Note** : le projet Grace de ce dépôt avait déjà un jeu de talents plus
large actif avant cette session (dont plusieurs des talents explicitement
écartés ci-dessus — `ddd.aggregate`, `ddd.entity`, `ddd.repository`,
`architecture.event-driven`/`outbox`, `devops.trunk-based`/
`artifact-management`/`db-migrations`/`ci-cd`, `ddd.tactical`/
`domain-service`/`domain-event`, `design.solid`/`tell-dont-ask`,
`quality.code-review`/`technical-debt`, `refactoring.primitive-obsession`,
`resilience.idempotency`, `testing.bdd`, `product.acceptance-criteria`).
Aucun outil disponible ici ne permet de les désactiver : `grace_prepare_task`
ne désigne, pour chaque tâche de code, que les talents listés ci-dessus
comme "installés" — les autres restent inertes tant qu'aucun code ne les
concerne réellement.

### Écart Grace assumé : adaptateurs pilotants hors de `infrastructure/`

`ingestion/` (appender Logback, file bornée, consommateur virtual-thread)
regroupe des adaptateurs **pilotants** (ils déclenchent l'appel au use case)
et non **pilotés** (ils n'implémentent aucun port du domaine). La règle
`grace.architecture.port-adapter:adapter-lives-in-infrastructure-and-implements-port`
(warning) les considère en violation dès qu'ils sont hors de `infrastructure/`
OU qu'ils n'implémentent aucun port — une condition qu'un adaptateur pilotant
ne peut structurellement pas satisfaire, quel que soit son emplacement.
Déplacer ce code dans `infrastructure/` ne lèverait donc pas la règle (il
n'implémenterait toujours aucun port) et brouillerait la distinction
pilotant/piloté que `package-info.kt` documente. Écart assumé, gardé en
`warning` non corrigé plutôt que masqué.

### Conventions

- Gradle **Kotlin DSL** (`build.gradle.kts`, `settings.gradle.kts`).
- Java 21 minimum (toolchain actuel : 25).
- **Conventional Commits** ; petits commits fréquents.
- Tests qui appellent le vrai jev : tag JUnit `"live"`, exclu du build par
  défaut (`build.gradle.kts`) ; à lancer avec
  `JEV_API_KEY=... ./gradlew test -DincludeTags=live`.
- Secrets : `JEV_API_KEY` en variable d'environnement, jamais committée ;
  voir `.env.example`. Jamais porté par une classe dont le `toString()`
  généré pourrait fuiter dans un log (`JevProperties` l'exclut
  explicitement ; la clé ne transite que par `@Value` local dans
  `JevConfiguration`, jamais stockée sur un champ plus large).

### Adaptateur jev (incrément 3)

- `JevLogClassifier` (`infrastructure/classification/jev/`) : implémente
  `LogClassifier` via `SystemOneClient`, une interface `@HttpExchange`
  déclarative. DTO `Question`/`Answer` en interfaces scellées Kotlin,
  discriminées par `type` (Jackson `@JsonTypeInfo`/`@JsonSubTypes` de
  `com.fasterxml.jackson.annotation` — ce module a gardé ses coordonnées
  dans le renommage Jackson 3 ; seuls `jackson-core`/`jackson-databind` sont
  passés sous `tools.jackson.*`).
- `LogEvent.redactedMessage`/`redactedStackTrace` (`domain/triage/`) :
  rédigent emails, tokens/clés d'API et IPv4 avant tout envoi à jev — implémentation
  privée sur `LogEvent` lui-même, pas un domain service : la revue Grace a
  relevé qu'une fonction `String -> String` ne coordonnant aucun objet du
  domaine était le mauvais outil pour un comportement propre à `LogEvent`.
  Le même texte rédigé sert aussi de base à `Fingerprint` (value object,
  hash SHA-256), donc deux erreurs qui ne diffèrent que par une donnée
  sensible partagent la même empreinte.
- `ClassificationMemory` (port, `recall`/`remember`) + `InMemoryClassificationMemory` :
  mémorise une classification par `Fingerprint`, avec un test de contrat
  (`ClassificationMemoryContract`) réutilisable par un futur adaptateur.
  Nommé `Memory` et non `Cache` (revue Grace : `Cache`/`get`/`put` nomment le
  mécanisme de stockage, pas la capacité métier — « reconnaître une erreur
  déjà classée »). `CachingLogClassifier` décore `LogClassifier` avec cette
  mémoire — le use case et le reste du pipeline ignorent qu'elle existe.
- Résilience : Resilience4j (`resilience4j-circuitbreaker`,
  `resilience4j-retry`, cœur pur, sans dépendance à une version de Spring
  Boot). Retry avec backoff exponentiel sur 429/529/erreurs réseau
  (`JevNetworkError`, ex. connexion refusée, DNS) ; 401/422 ne sont jamais
  retentés. `CircuitBreaker` englobe `Retry` (une opération retentée compte
  pour un seul résultat vis-à-vis du disjoncteur). Le prédicat de retry
  (`JevApiException.isTransient()`) est une fonction unique partagée entre
  le câblage réel et les tests, pour qu'ils ne puissent pas diverger en
  silence. Latence pire cas non bornée globalement (pas de `TimeLimiter`
  agrégé au-delà des timeouts par appel) — assumé pour une démo : un
  `classify` lent n'immobilise qu'un des quatre consommateurs virtual-thread,
  pas l'ingestion entière.

### Politique de routage par confiance (incrément 4)

- `Confidence` (value object, `domain/triage/`) : borne 0.0–1.0. Portée par
  `categoryConfidence`/`severityConfidence` sur `Classification` (les
  réponses `choice`/`score` de jev exposent leur propre `confidence`).
  `actionable` (`noul`) n'a pas de champ `confidence` séparé : `Actionable.confidence`
  la dérive de la distance à 0.5 (0.5 = pile ou face, 0/1 = jev certain).
- `RoutingThresholds` (une `Confidence` par champ) + `Classification.route()` :
  `AUTOMATIC` seulement si les trois champs passent leur propre seuil,
  sinon `FOR_REVIEW` — jamais un seuil global unique, les échelles de
  confiance `choice`/`score`/`noul` ne sont pas comparables (jaggedness
  jev-1.13). Seuils par défaut 0.7 partout (`TriageRoutingProperties`,
  `triage.routing.*`), provisoires jusqu'à la calibration de l'incrément 6.
- `ReviewQueue` (port, `enqueue`/`pending`) + `InMemoryReviewQueue` : retient
  les `ReviewCase` routés `FOR_REVIEW`, avec un test de contrat
  (`ReviewQueueContract`), même schéma que `ClassificationMemory`
  (incrément 3).
- `TriageLogEventUseCase` (renommage de `ClassifyLogEventUseCase`) : classifie
  puis route et enqueue si nécessaire — le nom suit maintenant ce que le use
  case fait réellement, pas seulement la classification.
- `GET /api/reviews` (`ReviewController`, `api/`) expose les `ReviewCase` en
  attente. Réponse construite sur `LogEvent.redactedMessage`, jamais
  `message` : l'endpoint n'a aucune authentification devant lui (pas de
  Spring Security dans ce projet), donc la même contrainte PII que le
  logging (incrément 3) s'applique ici.

### Roadmap (6 incréments)

1. ✅ Squelette, domaine, port `LogClassifier` + test d'acceptation avec
   faux classifieur.
2. ✅ Ingestion : appender Logback → file bornée (abandon + compteur) →
   consommation virtual threads.
3. ✅ Adaptateur jev réel (`@HttpExchange`, DTO sealed interfaces) +
   masquage PII + empreinte/cache.
4. ✅ Politique de routage par seuil de confiance (par champ) + `ReviewQueue`
   en mémoire + endpoint REST.
5. Observabilité : port métriques + adaptateur Micrometer/Prometheus,
   exposition Actuator.
6. Jeu de données `errors.jsonl` + `LogSimulator` + évaluation par tranche
   de confiance + README + docker-compose Prometheus/Grafana optionnel.
