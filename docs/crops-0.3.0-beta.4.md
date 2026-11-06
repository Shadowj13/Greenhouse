# Greenhouse 0.3.0-beta.4: cetrioli di mare, muschio e chorus

La beta.4 aggiunge tre colture a entrambe le serre, portando il totale da 48 a
51 voci (colori del pioppo contati separatamente). Include le 17 aggiunte della
beta.3. Il JAR richiede Minecraft Java 26.3, NeoForge 26.3.0.76 o successivo
della linea 26.3 e Java 25.

## Regole delle nuove colture

| Coltura e input | Substrato | Durata a 20 TPS | Raccolto |
| --- | --- | --- | --- |
| Cetriolo di mare | Secchio d'acqua | 600 tick / 30 s | 1 cetriolo di mare |
| Blocco di muschio | Pietra, terra, blocco d'erba, podzol, terra radicata o terra brulla | 600 tick / 30 s | 1 blocco di muschio |
| Fiore di chorus | Pietra dell'End | 900 tick / 45 s | 1–2 frutti di chorus |

Input e substrato restano nella serra. La farina d'ossa dimezza quattro cicli
secondo le regole precedenti. Il chorus richiede il fiore originale: frutto,
frutto cotto e ramo non lo sostituiscono. Non produce fiori o prodotti lavorati.
Il muschio pallido e il tappeto di muschio restano esclusi.

Nella serra i cetrioli di mare richiedono soltanto il secchio d'acqua come
substrato: è una regola della mod, senza un ulteriore slot per il corallo.
La crescita vanilla fuori dalla serra non viene modificata.

## Modelli e conservazione

Sono aggiunti 12 modelli delle piante, quattro stadi per ciascuna. I cetrioli
formano colonie da uno a quattro esemplari, il muschio cresce in zolle e il
chorus sviluppa rami e fiori. Tutte le texture sono riferimenti vanilla
verificati nel client Minecraft 26.3; nessuna texture vanilla viene copiata.
I modelli rimangono entro il vaso e sotto il tetto della struttura 1×1×1.

Il cetriolo riusa il fondo acquatico del kelp. Per il chorus sono aggiunte due
varianti del fondo con pietra dell'End, senza modificare alcun elemento della
struttura. I selettori multipart scelgono esattamente una struttura e una
pianta per ogni combinazione coltura/stadio, evitando gusci sovrapposti.

Il confronto binario dei JAR beta.3/beta.4 conferma che cambia solo Crop.class:
le altre 11 classi di produzione sono identiche, comprese GUI, inventari,
crescita, trasferimento, selection e collision shape. Le 48 voci precedenti
mantengono ordine e regole. Tra le vecchie risorse cambiano soltanto i due
blockstate. Vetri trasparenti, tetto piatto, vaso e base automatica sono
conservati. I generatori risultano idempotenti anche eseguiti in sequenza.

## Verifiche eseguite il 4 novembre 2026

- Build Gradle con Java 25 riuscita; integrità, metadati e corrispondenza
  delle risorse confezionate con i sorgenti verificati.
- Dodici gruppi di test dei modelli passati, inclusa la selezione di un solo
  guscio per tutte le 52 proprietà crop (vuoto compreso) e i quattro stadi.
- 300 controlli sul JAR confezionato con NeoForge 26.3.0.76 passati. I due
  server salvano e terminano con codice 0. Sono compresi tempi esatti,
  fertilizzante, menu, raccolta, esportazione, backpressure, persistenza,
  ricaricamento delle 20 aggiunte dalla beta.2 e 510000 estrazioni con semi
  deterministici sulle 51 colture/colori.
- Migrazione reale beta.3 → beta.4: il vecchio JAR con il suo vecchio modulo
  QA salva 19 casi su disco (17 colture e due casi di persistenza generale).
  Il nuovo JAR li ricarica e supera tutti i 19 controlli conservando identità,
  progresso, bonus e raccolti. Entrambi i processi terminano con codice 0.
- Client completo Minecraft 26.3 / NeoForge 26.3.0.85 con solo Greenhouse,
  profilo offline e mondo isolato: osservati i modelli e i quattro stadi.
  Tre click reali su Raccogli consegnano rispettivamente un cetriolo di mare,
  un blocco di muschio e un frutto di chorus, conservando gli input e
  riavviando il ciclo. Nelle tre casse sottostanti sono osservati i prodotti
  generati da cicli automatici naturali. Salva ed esci e chiusura normale:
  codice 0 in entrambe le esecuzioni del client.

La scena riproducibile è in tests/fixtures/crops4-visual. Le righe statiche
usano una durata NBT artificiale di 600000 tick per conservare gli stadi nel
confronto: non dimostrano i tempi produttivi, misurati dai test server. Le tre
macchine di raccolta manuale e le tre automatiche usano invece le durate reali.
Gli screenshot sono nativi F2; hash e descrizioni dei 18 file sono riportati
in docs/evidence/crops-beta4.json e nel pacchetto locale delle prove.

SHA256 del JAR collaudato:
`520c7ef2c95f13fde7222dbb800e31dff2d7ba70d8cdfaa358ebc2d524bdfa3d`.

## Limiti e anomalie osservate

Un errore di input durante l'allestimento ha rotto una macchina nella sola
scena di collaudo: la scena è stata interamente ripristinata prima di tutte
le prove di raccolta conservate. Il tasto Usa è stato impostato su G soltanto
nel profilo isolato per controllare le GUI senza spostare la telecamera.
Entrambe le esecuzioni usano -XX:StackShadowPages=32, già usato nel precedente
collaudo; non viene attribuito a quel parametro un beneficio dimostrato.

Il profilo offline produce errori di autenticazione/Realms. Sono presenti
anche avvisi di accesso ai contatori Windows/WMI, senza impedire il collaudo
e la chiusura normale. Nel log non risultano errori di modelli o texture
mancanti. Non sono stati provati su beta.4: due client simultanei, carico
prolungato di un mondo completo, Jade/JEI, shader, resource pack alternativi,
account reali e multiplayer Internet. Le prove precedenti restano documentate
per le rispettive versioni.

Questo rapporto documenta la build di sviluppo beta.4; non attesta una release
pubblica. La pubblicazione e la fusione della richiesta GitHub sono passaggi
separati.
