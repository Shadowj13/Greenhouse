# Uso delle serre

Le serre occupano un blocco: vetri trasparenti, tetto piatto, vaso interno.
La versione automatica si riconosce dalla base aggiuntiva. Aprile con il tasto
di utilizzo del blocco.

## Ricette nel banco da lavoro

Serra normale:

```text
V V V
V P V
L L L
```

V = pannello di vetro; P = vaso; L = lastra di legno. Puoi mescolare specie di
legno. Il risultato è una serra.

Serra automatica:

```text
V V V
V S V
B T B
```

V = pannello di vetro; S = serra normale; B = barile; T = tramoggia. Il risultato
è una serra automatica. Se la serra normale contiene terreno, pianta, farina o
un raccolto pronto, la serra automatica ottenuta li conserva.

## Spostare una serra

Rompendo una serra (anche con un'esplosione) il suo contenuto resta dentro
l'oggetto, come in una scatola di Shulker: terreno, pianta, farina d'ossa,
prodotti, raccolto pronto e progresso della crescita. Rimettendola a terra
riparte da dove era, comprese le cariche di fertilizzante rimaste. Una serra
vuota, senza progresso né cariche, dà un oggetto normale e impilabile.

## Coltivazione

Inserisci un substrato nello slot **Terreno**, un alberello/seme/pianta nello
slot **Pianta** e, se vuoi, farina d'ossa nello slot **Ossa**. Terreno e pianta
rimangono disponibili dopo il raccolto. Se togli o cambi pianta o terreno
mentre il raccolto è pronto, il raccolto non va perso: resta in attesa e la
nuova coltura parte dopo la raccolta (o l'esportazione nella serra automatica).
Durante la crescita, invece, cambiare pianta o terreno fa ripartire il ciclo da zero.

| Pianta | Substrato | Prodotto principale | Tempo base |
| --- | --- | --- | --- |
| Quercia, abete, betulla, giungla, acacia, quercia scura, ciliegio, mangrovia, quercia pallida, azalea, azalea fiorita, pioppo | Terra* | 2 tronchi del tipo corrispondente; le azalee producono quercia | 45 s |
| Fungo cremisi | Nylium cremisi | 2 gambi cremisi | 45 s |
| Fungo distorto | Nylium distorto | 2 gambi distorti | 45 s |
| Semi di grano | Terra* | 2 grani | 30 s |
| Carota | Terra* | 1–2 carote | 30 s |
| Patata | Terra* | 1–2 patate | 30 s |
| Semi di barbabietola | Terra* | 2 barbabietole | 30 s |
| Verruca del Nether | Sabbia delle anime | 2 verruche | 45 s |
| Fave di cacao | Tronco della giungla | 1–2 fave | 45 s |
| Cactus | Sabbia o sabbia rossa | 1 cactus | 30 s |
| Canna da zucchero | Terra* | 1 canna | 30 s |
| Bambù | Terra* | 1–2 bambù | 45 s |
| Alghe kelp | Secchio d'acqua | 1 alga kelp | 30 s |
| Semi di zucca | Terra* | 1 zucca | 45 s |
| Semi di anguria | Terra* | 2–4 fette | 45 s |
| Bacche dolci | Terra* | 1–2 bacche | 45 s |
| Fungo rosso, marrone o a mensola | Micelio o podzol | 1–2 funghi dello stesso tipo | 45 s |
| Bacche luminose | Terra* | 1–2 bacche luminose | 45 s |
| Fiori comuni bassi (elenco sotto) | Terra* | 1 fiore del tipo inserito | 30 s |
| Girasole, lillà, peonia, rosaio | Terra* | 1 fiore/pianta del tipo inserito | 30 s |
| Cetriolo di mare | Secchio d'acqua | 1 cetriolo di mare | 30 s |
| Fiore di coro | Pietra dell'End | 1–2 frutti di coro | 45 s |
| Rampicante normale | Terra* | 1 rampicante normale | 30 s |
| Lichene luminescente | Pietra | 1 lichene luminescente | 30 s |
| Rampicanti attorcigliati | Nylium distorto | 1 rampicante attorcigliato | 30 s |
| Rampicanti piangenti | Nylium cremisi | 1 rampicante piangente | 30 s |
| Piantaforma grande | Terra* | 1 piantaforma grande | 30 s |

*Terra comprende anche blocco d'erba, podzol, terra radicata e terra brulla.
I tempi indicati corrispondono a 20 tick al secondo; un server rallentato può
impiegare più tempo reale. Gli alberi possono dare bonus come bastoncini,
foglie e alberelli; alcune colture hanno bonus specifici. La tabella indica il
prodotto principale e non promette tutti i bonus a ogni raccolto.

Il pioppo usa un solo alberello: quando viene stabilito sceglie rosso, arancione
o giallo con pari probabilità e conserva il colore tra raccolte e salvataggi.
Se togli e rimetti l'alberello, il colore viene scelto di nuovo e il ciclo
riparte da zero.
Il totale nella beta.6 è di 56 voci di coltura/colore contando i tre colori del pioppo.

I dodici fiori bassi supportati sono: dente di leone, papavero, orchidea blu,
fiore d'aglio, houstonia, tulipano rosso, arancione, bianco e rosa, margherita,
fiordaliso e mughetto. Inserisci il fiore stesso nello slot Pianta: rimane dopo
ogni raccolto, come le altre piante. Non vengono prodotti semi aggiuntivi.
Dente di leone d'oro, rosa di Wither, fiortorcia, pianta carnivora e occhidee
restano esclusi da questo aggiornamento.

Le bacche luminose si inseriscono nello slot Pianta con terra nello slot
Terreno. Nel vaso crescono verso il basso su un piccolo sostegno interno;
le bacche compaiono nell'ultimo dei quattro stadi. Il raccolto produce bacche,
non blocchi di rampicante, e conserva l'esemplare originale.

Per le kelp inserisci un **secchio d'acqua nello slot Terreno** e un'alga kelp
nello slot Pianta. Secchio e pianta rimangono dopo ogni raccolto, senza consumare
acqua o richiedere tubazioni. Il vaso mostra acqua e quattro stadi di crescita.
Si raccolgono alghe normali: le alghe essiccate non si possono piantare.
La serra automatica trasferisce le alghe nei contenitori sottostanti come le
altre colture. La farina d'ossa applica lo stesso bonus di quattro cicli.

Per i cetrioli di mare usa il secchio d'acqua nello slot Terreno e un cetriolo
nello slot Pianta. Il vaso mostra una colonia acquatica in quattro stadi e
produce un cetriolo per raccolto. La serra simula l'ambiente di coltivazione:
non richiede un ulteriore blocco di corallo. Questa è una regola della mod;
le condizioni vanilla di riproduzione con farina d'ossa fuori dalla serra
restano quelle di Minecraft.

Per il fungo a mensola inserisci un esemplare nello slot Pianta e micelio
oppure podzol nello slot Terreno. Il supporto interno di pioppo fa parte del
modello. Il raccolto produce un fungo con il 25% di probabilità di ottenerne
un secondo. La crescita richiede 900 tick e conserva pianta e terreno.

Se una vecchia serra contiene muschio, puoi recuperare terreno, pianta ed
eventuale raccolto già pronto. Il muschio ritirato dalla coltivazione non
avvia nuovi cicli.

Per il coro inserisci pietra dell'End nello slot Terreno e un fiore di coro
nello slot Pianta. Il fiore iniziale va ottenuto nel gioco: frutti, frutti
scoppiati e rami di coro non sono input validi. Il raccolto contiene solo
frutti normali; non produce nuovi fiori o blocchi di purpur. Il vaso ha un
fondo di pietra dell'End e quattro stadi ramificati.

Per i rampicanti normali e la piantaforma grande inserisci un esemplare già
ottenuto nel mondo e una delle terre compatibili. I primi crescono su un
sostegno interno; la piantaforma sviluppa una foglia larga. La piantaforma
piccola non è un input valido e non viene prodotta come sottoprodotto.

Il lichene luminescente richiede pietra e un lichene iniziale: il modello
mostra la colonizzazione di una roccia interna. Le parti del lichene hanno
luminosità visiva propria; la serra non diventa una lampada per il mondo.

I rampicanti attorcigliati richiedono nylium distorto; quelli piangenti un
nylium cremisi. I primi crescono verso l'alto, i secondi
pendono dal sostegno interno. Questi substrati sono le regole di coltivazione
della mod: le condizioni vanilla fuori dalla serra rimangono invariate.
Ogni nuovo raccolto consegna un esemplare della stessa pianta e conserva gli
input. Nelle vecchie serre dei rampicanti piangenti ritira il raccolto già
pronto e sostituisci il blocco di verruche del Nether con nylium cremisi
per riavviare la crescita.

Una farina d'ossa dimezza il tempo di **quattro cicli**, incluso quello che la
consuma. Il bonus visualizzato indica i cicli accelerati ancora disponibili
dopo quello in corso. Aggiungere farina durante la crescita non cambia la
durata del ciclo già iniziato: viene usata all'avvio di un ciclo successivo.
Se interrompi un ciclo accelerato cambiando pianta o terreno, quel ciclo viene
restituito: una farina d'ossa produce sempre quattro raccolti accelerati.

Durante la crescita la schermata mostra, accanto alla percentuale, i secondi
che mancano al raccolto.

La schermata indica lo stato della serra: **Inserisci terreno**, **Inserisci
pianta**, **Terreno non valido**, **In pausa** (segnale redstone), **In
crescita**, **Serra piena** (serra normale con 4 raccolti pronti) oppure
**Uscita piena** (serra automatica in attesa di spazio).

## Raccolta e automazione

La serra normale **accumula fino a 4 raccolti**: dopo ogni raccolto la
crescita riparte subito, e solo al quarto la serra si ferma (**Serra piena**).
Il pulsante mostra quanti raccolti sono pronti, per esempio **Raccogli 2/4**,
e li consegna tutti insieme nell'inventario; se non c'è spazio, l'eccedenza
cade vicino alla serra. I raccolti accumulati si ritirano solo a mano:
tramogge e tubi non possono estrarli dalla serra normale.
Puoi anche raccogliere usando il blocco mentre sei accovacciato. Sulla serra automatica, lo stesso gesto apre il contenitore sotto
quando è presente e può essere aperto.

La serra automatica deposita il raccolto nei suoi nove slot di uscita e riparte.
Se metti un contenitore sotto, trasferisce i prodotti dall'alto rispettando i
suoi slot accessibili. Se non può contenere il prossimo raccolto completo,
attende spazio senza spendere altro fertilizzante per un nuovo ciclo.

L'accesso automatico alla serra avviene così:

- dall'alto: terreno, pianta e farina d'ossa (terreno e pianta solo se lo
  slot è vuoto, un esemplare per slot);
- dai lati: farina d'ossa;
- dal basso: estrazione dai nove slot di uscita.

Le stesse regole valgono per tubi e condotti di altre mod (Create, Mekanism,
AE2 e simili): la serra si collega come una tramoggia. La serra automatica
esporta anche nei contenitori di altre mod posti sotto di essa.

Le serre si rompono più in fretta con un'ascia. Le ricette compaiono nel
ricettario appena raccogli un pannello di vetro o un vaso (serra) oppure una
serra o una tramoggia (serra automatica).

## Redstone e comparatore

Una serra alimentata da un segnale redstone (leva, blocco di redstone,
pulsante…) **va in pausa**: la crescita si ferma, non parte un nuovo ciclo e
non viene consumata farina d'ossa. Un raccolto già pronto si può comunque
raccogliere, e la serra automatica continua a esportarlo. La schermata mostra
**In pausa**. Togliendo il segnale la crescita riprende da dove si era fermata.

Un comparatore accanto alla serra legge:

- **serra normale**: 0 senza coltura in crescita, da 1 a 14 durante la crescita
  (in proporzione al progresso), **15 quando la serra è piena** (4 raccolti
  pronti);
- **serra automatica**: il riempimento dei nove slot di uscita, come per una
  cassa (15 = uscita piena).

## JEI

Con JEI installato compare la categoria **Serra**: per ogni coltura mostra
pianta, terreni accettati, raccolto, bonus con la loro probabilità e il tempo
del ciclo (con e senza farina d'ossa). Cliccando la barra di crescita nella
schermata della serra si apre direttamente il catalogo. JEI è facoltativo: la
mod funziona anche senza.

## Jade

Con Jade installato, guardando una serra compaiono la coltura, lo stato (in
crescita con percentuale e secondi mancanti, serra piena, uscita piena o in
pausa), i raccolti pronti della serra normale (per esempio 2/4)
e i cicli di farina d'ossa rimasti. Jade è facoltativo; se manca sul server,
il tooltip mostra solo la coltura.

## Chunk

Le serre funzionano quando il loro chunk è caricato e attivo. Greenhouse non
carica autonomamente i chunk e non richiede energia, rifornimenti d'acqua o un
orario solare. Kelp e cetrioli di mare richiedono un secchio d'acqua come substrato iniziale.
