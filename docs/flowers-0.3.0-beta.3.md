# Greenhouse 0.3.0-beta.3: priorità 1–3

L'aggiornamento aggiunge 17 colture a entrambe le serre:
bacche luminose; dente di leone, papavero, orchidea blu, fiore d'aglio,
houstonia, tulipani rosso/arancione/bianco/rosa, margherita, fiordaliso e
mughetto; girasole, lillà, peonia e rosaio. Il totale passa da 31 a 48 voci,
contando separatamente i colori del pioppo.

## Regole

Tutte le nuove colture accettano terra, blocco d'erba, podzol, terra radicata
e terra brulla. Si inserisce la pianta stessa: rimane dopo ogni raccolto.
I fiori producono un fiore dello stesso tipo ogni 600 tick (30 s a 20 TPS).
Le bacche luminose producono 1–2 bacche ogni 900 tick (45 s), come le bacche
dolci. La farina d'ossa dimezza quattro cicli con le regole già approvate.
Non vengono aggiunti semi, terreni, slot, ricette o requisiti di luce/energia.

## Modello e conservazione

Sono aggiunti 68 modelli, quattro stadi per coltura, con riferimenti alle
texture vanilla della 26.3. Le bacche pendono da un sostegno dentro il vaso e
maturano visivamente nell'ultimo stadio. I fiori alti usano parte inferiore e
superiore; il girasole usa anche le texture distinte fronte/retro della testa.

Il confronto dei JAR beta.2/beta.3 conferma che cambia solo Crop.class:
le altre 11 classi di produzione, inclusi GUI, inventario, crescita, trasferimento,
selection/collision shape, sono identiche. Tutte le 31 voci precedenti mantengono
ordine e regole. Tra le risorse precedenti cambiano soltanto i due blockstate
per selezionare i nuovi modelli. Struttura, vetro, vaso, tetto piatto e base
automatica restano identici; anche i modelli kelp sono conservati.

## Verifiche eseguite

- Build Gradle Java 25 riuscita e controllo di integrità/metadati/risorse del JAR.
- Nove gruppi di test sui modelli: limiti del blocco, vetri e selection outline,
  riferimenti, quattro stadi, sostegno delle bacche e testa del girasole.
  Tutte le texture vanilla nuove esistono nel runtime Minecraft 26.3 reale.
  Il generatore è idempotente.
- 276 controlli sul JAR confezionato con NeoForge 26.3.0.76: tutti passati;
  i due processi server salvano e terminano con codice 0. Sono comprese tutte
  le 48 voci, 480000 estrazioni con semi deterministici, tempi esatti,
  fertilizzante, menu, raccolta manuale senza duplicazione, esportazione,
  ripristino NBT e riavvio da disco di tutte le 17 nuove colture.
- Migrazione reale beta.2 → beta.3: il JAR beta.2 e il relativo vecchio modulo
  QA salvano su disco una crescita in corso e un raccolto bloccato. Il JAR
  beta.3 li ripristina conservando progresso, bonus e raccolto; entrambi i
  processi terminano con codice 0.
- Client completo Minecraft 26.3 / NeoForge 26.3.0.85 con solo Greenhouse,
  profilo offline e mondo isolato: modelli e GUI osservati; click reali su
  Raccogli consegnano una bacca e una orchidea blu, conservano gli input e
  riavviano i cicli. Le bacche prodotte da cicli naturali della serra automatica
  sono osservate nella cassa sottostante. Uscita tramite Salva ed esci e poi
  Esci dal gioco: codice 0.

La scena tests/fixtures/flowers-visual include righe di confronto con durata
NBT artificiale di 600000 tick per mantenere visibili gli stadi intermedi.
Non dimostrano i tempi reali: questi sono misurati dai test server.
La serra manuale delle bacche e le tre macchine automatiche della scena usano
invece le durate di produzione. Gli screenshot sono nativi F2; il manifest
con hash e descrizioni è in docs/evidence/flowers-beta3.json.

## Limiti e anomalie osservate

Un primo avvio client è terminato con codice nativo Windows -1073740940, senza
rapporto Java che ne stabilisse la causa. Una seconda esecuzione, con
-XX:StackShadowPages=32, ha completato prove e chiusura normale. Non attribuiamo
la prima interruzione a una causa specifica né affermiamo che il parametro
ne sia una correzione dimostrata.

Il profilo offline produce errori di autenticazione Realms. La copia del vecchio
mondo QA conteneva una regola Jade non riconosciuta dal client senza Jade;
questo non costituisce una verifica di compatibilità con Jade. Non sono stati
provati su beta.3: due client simultanei, carico prolungato del mondo con le
nuove colture, Jade/JEI, shader, resource pack alternativi, account reali e
latenza/multiplayer Internet. Le prove delle versioni precedenti restano
documentate come tali.
