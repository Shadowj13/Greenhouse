# Greenhouse 0.3.0-beta.5

La beta.5 aggiunge cinque colture alla serra normale e alla serra automatica.
Il totale arriva a 56 voci contando separatamente i tre colori del pioppo.
Il JAR richiede Minecraft Java 26.3, NeoForge 26.3.0.76 o successivo della
stessa linea e Java 25.

| Coltura | Substrato | Raccolto ogni 30 s a 20 TPS |
| --- | --- | --- |
| Rampicante normale | Terra e varianti già supportate | 1 rampicante |
| Lichene luminescente | Pietra | 1 lichene |
| Rampicante attorcigliato | Nylium distorto | 1 rampicante |
| Rampicante piangente | Blocco di verruche del Nether | 1 rampicante |
| Piantaforma grande | Terra e varianti già supportate | 1 piantaforma grande |

Ogni ciclo conserva la pianta di partenza e il substrato. La farina d'ossa
accelera quattro cicli secondo le regole già in uso. Il primo esemplare va
ottenuto nel mondo. Erba acquatica, rosa di Wither e piantaforma piccola
restano fuori dalla coltivazione.

I 20 nuovi modelli mostrano quattro stadi per coltura. Il lichene copre
progressivamente una roccia. Le sue superfici hanno luminosità visiva propria
senza illuminare i blocchi circostanti. I rampicanti normali e piangenti hanno
sostegni interni, gli attorcigliati crescono dal vaso e la piantaforma sviluppa
una foglia larga. I modelli riusano le texture di Minecraft 26.3. Il verde del
rampicante normale è applicato al modello con i dati delle facce di NeoForge.
[Riferimento NeoForge per i modelli](https://docs.neoforged.net/docs/resources/client/models/).

Le sei varianti della base cambiano il substrato visibile e conservano la
geometria del blocco. Ogni combinazione di coltura e stadio seleziona una sola
struttura. Il confronto con il JAR beta.4 trova `Crop.class` come unica classe
modificata. Le altre undici classi e tutte le risorse precedenti coincidono,
a eccezione dei due selettori dei modelli. Restano quindi gli stessi vetri,
tetto piatto, vaso, base automatica, GUI e comportamento dei trasferimenti.

## Esito del collaudo

- Compilazione con Java 25 riuscita. Sono passati 15 gruppi di test dei modelli
  e la verifica delle risorse nel JAR. Due rigenerazioni complete producono
  gli stessi file. Verificati tutti i 13 riferimenti a texture vanilla nuove.
- Tutti i 340 controlli sul JAR confezionato sono passati su NeoForge 26.3.0.76.
  Comprendono crescita e resa, raccolta senza duplicazione, quattro cicli con
  farina d'ossa, esportazione, inventari pieni e ricaricamento. Sono incluse
  560000 estrazioni con semi deterministici sulle 56 voci. Entrambi i server
  di collaudo hanno salvato e sono usciti con codice 0.
- La migrazione beta.4 → beta.5 ha salvato 22 casi con il vecchio JAR e li ha
  ricaricati tutti con quello nuovo. Il nuovo server ha ritrovato identità,
  progresso e bonus delle 20 colture aggiunte dalla beta.2 oltre a due casi
  generali con crescita in corso o raccolto in attesa.
- In Minecraft 26.3 con NeoForge 26.3.0.85 ho ispezionato tutti i 20 stadi.
  I cinque clic reali su Raccogli hanno consegnato ciascuno un esemplare,
  conservato gli input e riavviato il ciclo. Ho aperto anche le cinque casse
  sotto le serre automatiche e osservato il prodotto corretto in ciascuna.
  Il client della sessione ripresa ha salvato il mondo ed è uscito con codice 0.

Le prove grafiche del 4 e 6 novembre 2026 usano un mondo isolato e un profilo
Minecraft offline. Le file di confronto tengono visibili gli stadi con durate
artificiali. I cicli delle serre di raccolta e di esportazione usano 600 tick.
Le durate esatte sono coperte dai test server. Sono conservati 21 screenshot
nativi F2 con descrizioni e impronte nel manifest `docs/evidence/crops-beta5.json`.

Il controllo automatico aveva bloccato il clic sui rampicanti piangenti per
esaurimento del limite d'uso. Il clic è riuscito nella sessione ripresa. La
telecamera del banco di prova usa teletrasporti ripetuti. Per aprire le ultime
serre e le casse ho lasciato cadere l'oggetto che blocca la telecamera. Il
profilo offline riporta errori dei servizi account e Realms. I log non mostrano
modelli o texture mancanti. Multiplayer con due client, carico prolungato,
Jade/JEI, shader e pacchetti di risorse alternativi restano fuori da questo
collaudo.

SHA256 del JAR collaudato
`354e59f66a50b9254180211c1aaf0e7f6660a96fe4231f1c0ab714f3558616a7`.
