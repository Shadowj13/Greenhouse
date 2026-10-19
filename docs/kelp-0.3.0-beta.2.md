# Greenhouse 0.3.0-beta.2: alghe kelp

Le kelp funzionano nelle due serre usando un secchio d'acqua nello slot Terreno
e un'alga nello slot Pianta. Producono una kelp normale ogni 600 tick (30 secondi
a 20 TPS); secchio e pianta restano disponibili. La farina d'ossa mantiene il
bonus di quattro cicli da 300 tick. Le alghe essiccate non sono piantabili.

## Implementazione e aspetto

L'unica classe di produzione cambiata rispetto alla beta.1 è `Crop`: la nuova
voce è aggiunta in fondo, conservando l'ordine e le regole delle 30 precedenti.
Le altre 11 classi, inclusa la GUI, sono identiche byte per byte alla beta.1.
Dei 211 asset e risorse precedenti, 209 sono identici; i due blockstate aggiungono
solo la selezione condizionale del vaso acquatico e i quattro stadi kelp.

Le due varianti acquatiche riprendono i modelli originali, sostituendo soltanto
l'elemento del terreno con una singola superficie d'acqua a Y=4.96. Non viene
sovrapposta al terreno e non introduce facce laterali trasparenti. Struttura,
vetri, vaso, tetto piatto, base automatica e selection/collision box restano
quelli approvati. Le piante riutilizzano le texture vanilla animate `kelp` e
`kelp_plant`; l'acqua usa `water_still` con colore fisso NeoForge.

## Verifiche eseguite

- Build e integrità del JAR Java 25 superate; sei gruppi di controlli sui modelli.
- 156 controlli sul JAR confezionato, con NeoForge 26.3.0.76 su due processi
  server: tutti superati; entrambi i server salvano e terminano con codice 0.
- Tutte le 31 voci di coltura/colore: tempi, raccolta manuale, quattro cicli
  accelerati, resa ed esportazione automatica. Per le kelp: un solo prodotto,
  rifiuto di secchio vuoto/lava/terra/sabbia e alghe essiccate, accettazione nei
  menu, acqua e pianta conservate, identità e progresso dopo il ripristino NBT.
- Migrazione separata beta.1 → beta.2: checkpoint su disco con la beta.1,
  ripristino con la beta.2; progresso e raccolto bloccato conservati.
- Client completo Minecraft 26.3 / NeoForge 26.3.0.85, solo Greenhouse beta.2,
  mondo di prova separato: modelli acquatici di entrambe le serre, animazioni,
  superficie d'acqua e GUI osservati. Un click reale su Raccogli consegna una
  kelp al giocatore, mantiene gli input e avvia il ciclo successivo.

Gli screenshot nativi F2 e le ricevute sono nel pacchetto prove separato; il
manifest degli screenshot è in `docs/evidence/kelp-client-beta2.json`.
La scena `tests/fixtures/kelp-visual` imposta gli stadi iniziali per il confronto
visivo; il mondo prosegue poi con tick naturali. I tempi esatti sono verificati
dal test server, non dedotti dalle immagini. La prima inizializzazione della
scena è stata corretta rimuovendo un comando amministrativo non consentito nei
datapack; non ha richiesto cambiamenti al JAR.

## Limiti

Il client usa un profilo offline e un mondo locale. Shader, resource pack
alternativi, account reali, latenza Internet, due client simultanei e carico
prolungato con kelp non sono stati provati in questa beta. Le precedenti prove
multiplayer e di carico beta.1 sono documentate separatamente e non valgono
come una nuova esecuzione sulla beta.2. Jade e JEI non sono installati nel
client beta.2; i messaggi di autenticazione Realms del profilo offline non
rappresentano una verifica di accesso ai servizi online.
