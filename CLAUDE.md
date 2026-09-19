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
