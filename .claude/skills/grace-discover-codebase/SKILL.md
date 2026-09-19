---
name: grace-discover-codebase
description: "Onboarding d'un projet sur Grace, à utiliser dès que `grace_prepare_task` ne renvoie aucun talent actif : n'écris pas de code, lis le projet, fais confirmer tes hypothèses d'architecture, puis propose une liste de talents et installe-la après accord explicite."
---

# Découverte de projet — procédure

L'onboarding historique posait cinq questions aveugles (type de projet, langage, architecture,
approche de test, domaine), concaténait les réponses en une chaîne et appariait cette chaîne aux
mots-clés des talents. Aucun modèle ne lisait le projet, et un talent dont les déclencheurs ne
tombaient pas sur les mots du développeur n'était jamais trouvé.

Cette procédure remplace ça. **Tu** lis le projet, **tu** formes les réponses aux axes à partir
des fichiers qui les prouvent, le développeur confirme ou corrige en un appel de questions, et
**tu** en déduis les talents. Rien n'est activé sans un « oui » explicite.

## Discipline de sortie — absolue

Cette procédure produit exactement quatre sorties visibles possibles sur toute son exécution, et
rien d'autre :

1. l'appel (ou les appels) de questions de la phase 3 — un seul, ou plusieurs si l'inventaire
   de dérives ne tient pas dans un appel, mais rien entre eux ;
2. la liste de talents de la phase 6 ;
3. la demande d'installation de la phase 7, qui suit la liste immédiatement ;
4. la ligne unique de la phase 6 quand aucun talent ne survit à la phase 4 — elle remplace
   alors les sorties 2 et 3, et la procédure s'arrête là.

La sortie 3 est une demande d'autorisation avant une écriture. Ce n'est pas un commentaire de
clôture et l'interdiction ci-dessous ne la vise pas : la supprimer laisse la découverte sans effet.

Tout le reste — recon, hypothèses, preuves, contradictions, quasi-manqués, verdict — est du
raisonnement interne. Ne l'imprime pas, ne le résume pas, ne préface ni ne conclus aucune de ces
sorties par un commentaire, une note de progression, un décompte ou une proposition. Pas de
« j'ai analysé… », pas de « voici… ». Si tu as quelque chose à dire qui n'est aucune de ces
quatre sorties, ne dis rien.

## Phase 1 — Recon

Cible : le chemin donné par l'utilisateur, sinon le répertoire de travail courant.

Lis, dans cet ordre, et arrête-toi dès que tes hypothèses tiennent :

1. Manifestes et config : `package.json`, `tsconfig.json`, `go.mod`, `pyproject.toml`,
   `pom.xml`, `Cargo.toml`, lockfiles, `docker-compose*.yml`, workflows CI.
2. Forme : l'arborescence sur deux ou trois niveaux, sans `node_modules`/`dist`/`.git`. Note le
   **vocabulaire réellement employé** (`domain/`, `services/`, `models/`, `handlers/`,
   `usecases/`…) : il servira de requêtes en phase 4.
3. Points d'entrée : ce que pointent `main`/`scripts` du manifeste, plus les fichiers de tête du
   plus gros répertoire source.
4. Tests : où ils vivent, quel runner, découpage unitaire/intégration, nommage.
5. Trois à cinq fichiers sources représentatifs, dans des répertoires différents. **Lis-les en
   entier.** C'est là qu'est le vrai signal.

Lecture seule : n'écris rien, ne lance rien.

## Phase 2 — Hypothèses

Produis entre trois et six hypothèses. Chacune porte :

- **affirmation** — une phrase, la réponse à un axe d'onboarding (type de projet, langage/stack,
  architecture, approche de test, domaine) ;
- **preuve** — deux ou trois références `chemin/fichier.ts:42` et ce que tu y as vu. Une
  hypothèse sans fichier derrière elle est une devinette : abandonne-la ou marque-la `faible` ;
- **confiance** — `haute` | `moyenne` | `faible` ;
- **question** — la seule chose que tu demanderais pour trancher, formulée pour qu'un « non »
  soit informatif.

Une hypothèse s'énonce dans le **vocabulaire du projet** et ne nomme **jamais** un talent. Le
passage aux talents a lieu en phase 4 et nulle part avant.

### Le piège à éviter

Ne déduis pas l'architecture de la surface même que l'audit va noter. « Il y a un dossier
`domain/`, donc c'est hexagonal » ne vaut rien : ça fait bien noter le projet sur une convention
qu'il ne suivait pas.

Les signaux qui portent de l'information sont les endroits où **le code se contredit** :

- un dossier `domain/` qui importe Express, Prisma, `fs`, ou quoi que ce soit qui fasse des I/O ;
- une interface de port à implémentation unique, doublée d'un chemin qui la contourne ;
- une couche que tout le monde importe et qui importe tout le monde en retour ;
- des tests qui montent l'application entière pour tester une fonction pure ;
- deux vocabulaires concurrents pour le même concept dans deux dossiers ;
- une config qui déclare quelque chose que le projet n'a ni installé ni branché.

Inclus toujours au moins une hypothèse de ce type quand tu en trouves une, formulée neutrement
(« `user/domain/account.ts:12` importe le client Prisma — le domaine est-il censé rester sans
dépendance ici ? »). Ce sont les hypothèses qui valent une confirmation, et elles seront aussi
les premières dérives remontées par l'audit.

### L'inventaire de dérives

Les autres hypothèses répondent à un axe et s'arrêtent à trois ou six. Les dérives, c'est autre
chose : ce n'est pas un axe, c'est une **liste**, et elle doit être **exhaustive** — chaque
dérive que le développeur ne voit pas est une violation que tu as décidée à sa place.

Ne t'arrête donc pas à une. Balaie le projet pour chaque endroit où le code se contredit, et
consigne chacun comme un item à part :

- **quoi** — une phrase nommant le symptôme, dans le vocabulaire du projet ;
- **où** — `chemin/fichier.ts:42`, les lignes exactes. Une dérive sans référence de ligne
  n'existe pas : abandonne-la ;
- **pourquoi c'est une dérive** — quel invariant ou quelle frontière elle casse.

Ne groupe rien, ne juge rien à ce stade. Ne décide pas qu'une dérive est « évidemment
volontaire » pour la retirer : ce jugement appartient au développeur en phase 3, et c'est
exactement le jugement que cette phase existe pour collecter. Dix dérives est un compte normal
sur un vrai dépôt. Si tu n'en as trouvé qu'une, tu n'as pas balayé.

## Phase 3 — Reverse prompting

Garde les hypothèses pour toi. La première chose que le développeur voit de toute l'exécution,
c'est l'appel de questions : pas de tableau, pas de préambule, pas de déballage de preuves.

Pose les questions par lots (quatre par appel au maximum), les plus discriminantes d'abord : une
hypothèse dont tu es sûr et qui ne départage rien ne vaut pas une question. Les options doivent
être concrètes et mutuellement exclusives, et l'une d'elles doit être la correction que tu juges
la plus probable, pas un simple « oui »/« non ».

Ordre de priorité : l'architecture, puis l'inventaire de dérives, puis l'approche de test, puis
le domaine, puis le type de projet. Le langage et la stack se lisent dans le manifeste, ils ne
se demandent jamais.

### Les questions de dérive sont une checklist, pas un choix

Les questions d'axe sont à choix unique : le développeur retient une réponse. Les dérives ont la
forme inverse. Chaque item de l'inventaire de la phase 2 doit atteindre le développeur comme
**sa propre case à cocher**, et la sémantique est fixe :

> **cochée = tolérée.** Un item coché est assumé, délibéré, et ne doit **jamais** être remonté
> comme violation. Un item non coché est une violation à corriger.

Énonce cette phrase dans le texte de la question elle-même, à chaque fois, dans ces termes.
C'est le seul élément de cadrage que le développeur ne peut pas déduire des options.

Règles de construction :

- sélection **multiple**, toujours. Ne replie jamais les dérives dans une question unique
  « comment traite-t-on tout ça », et ne demande jamais « est-ce une dérive ? » item par item ;
- **une option par dérive, toutes.** L'inventaire n'est pas une sélection. Onze dérives
  trouvées, onze options présentées ;
- les appels de questions plafonnent à quatre options par question et quatre questions par
  appel : un inventaire complet s'étale donc sur plusieurs questions. Groupe-les par frontière
  cassée (couche applicative, encapsulation du domaine, vocabulaire et frontières, typage et
  tests) et sers-toi de l'intitulé de groupe. Au-delà de seize items, fais un second appel
  plutôt que de tronquer ;
- l'intitulé de l'option est un nom court du symptôme. La description porte la preuve : les
  lignes `fichier.ts:42` et ce qui s'y trouve. Le développeur décide sur la preuve, donc une
  description sans référence de ligne est une option cassée ;
- ne pré-coche jamais, ne classe jamais, n'édite jamais de commentaire. « Évidemment volontaire »
  n'est pas à toi d'en décider.

Si l'hôte n'expose aucun appel de questions, n'improvise pas et ne renonce ni à l'inventaire ni à
sa sémantique : imprime les dérives en texte, numérotées, une par ligne, chacune avec sa preuve de
ligne, énonce la règle dans les mêmes termes (**citée = tolérée**, tout le reste est une violation
à corriger) et demande au développeur de citer les numéros qu'il tolère. Cette liste prend alors
la place de l'appel de questions dans la discipline de sortie : le format change, la question non.

Enregistre l'ensemble coché comme toléré. Il ne sélectionne pas de talent à lui seul, mais il te
dit quelles dérives confirmées tiennent encore, et une dérive non cochée est un argument vivant
pour le talent qui la couvre en phase 4.

Si le développeur te corrige, ne discute pas et n'accuse pas réception en prose. Enregistre la
correction en silence, abandonne ce qu'elle invalide, et passe directement à la phase 4.

## Phase 4 — Mapper les hypothèses sur les talents

Le catalogue entier est en annexe, à la fin de cette procédure : un id et un résumé par talent.
Tu n'as rien à aller chercher. La traduction entre le vocabulaire du projet et celui
du catalogue, c'est toi qui la fais : une hypothèse formulée dans les mots du projet
(`handlers`, `usecases`) ne tombe pas d'elle-même sur un talent qui écrit `adapters`.

L'annexe ne porte que l'id et le résumé. Le manifeste d'un talent non activé reste fermé : ni
`languages`, ni `status`, ni `agents`, ni le graphe de dépendances. Tu décides donc sur le
résumé, et tout ce qui relève du graphe est traité en phase 5.

Travaille par hypothèse, jamais par catalogue : tu réponds à « qu'est-ce qui couvre ce fait
confirmé », pas à « lequel de ces talents pourrait servir ». Une passe par hypothèse confirmée,
plus une par dérive non tolérée. Pour chaque candidat, retiens l'id, l'hypothèse qu'il sert et la
ligne de preuve derrière cette hypothèse. Écarte :

- un talent dont le résumé vise une stack que le projet n'utilise pas ;
- un talent qu'aucune hypothèse confirmée ne soutient. Un talent que tu ne peux pas rattacher à
  une hypothèse confirmée n'a pas sa place dans la sélection, aussi sensé paraisse-t-il ;
- un doublon : quand deux talents couvrent la même hypothèse, garde celui dont le résumé parle
  les mots du projet.

Garde les quasi-manqués pour ton raisonnement, ne les imprime pas.

## Phase 5 — Fermer le graphe

Le catalogue est un graphe, pas une liste : prérequis durs (`requires`), incompatibilités
(`conflicts`), compléments (`recommends`). Ces arêtes ne te sont **pas** lisibles ici, puisque
les manifestes des talents non activés sont fermés.

Ne devine pas et n'invente pas d'ids pour combler : la fermeture se fait après application, avec
`grace_doctor`, qui rapporte prérequis manquants et conflits sur la sélection réellement active.
Ta sortie de phase 6 est donc la sélection **directe**, celle que chaque hypothèse confirmée
justifie, et rien de plus.

## Phase 6 — Sortie

La sélection de la phase 4, en liste plate, et rien d'autre :

```
- <talentId>
- <talentId>
```

Un id par ligne. Pas d'en-tête, pas de tableau, pas de colonne de preuves, pas de décompte, pas
d'ids écartés, pas de verdict, aucune phrase avant la liste. Les ids exactement tels que
l'annexe les écrit. Enchaîne directement sur la phase 7 : rien d'autre entre les deux.

Si rien ne survit à la phase 4, n'imprime qu'une seule ligne disant qu'aucun talent ne correspond,
et arrête-toi là — il n'y a rien à installer.

## Phase 7 — Installer

La liste ne sert à rien tant qu'elle n'est pas appliquée, et c'est toi qui l'appliques. Ne rends
pas la main en laissant au développeur le soin de demander : juste après la liste, demande-lui
s'il veut installer ces talents. Une question, deux issues, pas de troisième option molle.

Sur un « oui », appelle `grace_install_talents({ talents: ["<id>", …] })` — la liste des ids nus,
un par talent retenu, sans objet ni drapeau — en un seul appel
groupé, une seule révision, tout ou rien. Puis appelle
`grace_doctor` : il rapporte les prérequis manquants et les conflits sur la sélection réellement
active, ce que tu ne pouvais pas voir en phase 5. S'il en remonte, présente-les de la même façon
et applique le complément avec un second `grace_install_talents` après accord.

Sur un « non » ou un silence, n'écris rien et n'insiste pas. La découverte s'arrête là.

## Annexe — Le catalogue

Un talent par ligne : son id, puis son résumé. C'est la seule vue du catalogue dont tu disposes
pendant l'onboarding, et elle suffit à la phase 4. Ne l'imprime jamais : la discipline de sortie
vaut aussi pour elle.

_(L'index du catalogue n'est pas dans ce fichier : il dépend du projet et change à chaque talent publié. Appelle `grace_discover_codebase` pour l'obtenir — la réponse porte cette même procédure, index compris.)_


