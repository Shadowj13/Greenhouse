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
è una serra automatica. Recupera contenuto e raccolto prima di trasformare la
serra normale nel componente della ricetta.

## Coltivazione

Inserisci un substrato nello slot **Terreno**, un alberello/seme/pianta nello
slot **Pianta** e, se vuoi, farina d'ossa nello slot **Ossa**. Terreno e pianta
rimangono disponibili dopo il raccolto. Raccogli prima di cambiare coltura.

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
| Semi di zucca | Terra* | 1 zucca | 45 s |
| Semi di anguria | Terra* | 2–4 fette | 45 s |
| Bacche dolci | Terra* | 1–2 bacche | 45 s |
| Fungo rosso o marrone | Micelio o podzol | 1–2 funghi dello stesso tipo | 45 s |

*Terra comprende anche blocco d'erba, podzol, terra radicata e terra brulla.
I tempi indicati corrispondono a 20 tick al secondo; un server rallentato può
impiegare più tempo reale. Gli alberi possono dare bonus come bastoncini,
foglie e alberelli; alcune colture hanno bonus specifici. La tabella indica il
prodotto principale e non promette tutti i bonus a ogni raccolto.

Il pioppo usa un solo alberello: quando viene stabilito sceglie rosso, arancione
o giallo con pari probabilità e conserva il colore tra raccolte e salvataggi.
Le tre varianti portano il totale a 30 voci di coltura/colore.

Una farina d'ossa dimezza il tempo di **quattro cicli**, incluso quello che la
consuma. Il bonus visualizzato indica i cicli accelerati ancora disponibili
dopo quello in corso. Aggiungere farina durante la crescita non cambia la
durata del ciclo già iniziato: viene usata all'avvio di un ciclo successivo.

## Raccolta e automazione

Nella serra normale, a crescita completa premi **Raccogli**: i prodotti entrano
nell'inventario del giocatore; se non c'è spazio, l'eccedenza cade vicino alla
serra. Non basta una tramoggia per raccogliere la serra normale.
Puoi anche raccogliere una serra normale matura usando il blocco mentre sei
accovacciato. Sulla serra automatica, lo stesso gesto apre il contenitore sotto
quando è presente e può essere aperto.

La serra automatica deposita il raccolto nei suoi nove slot di uscita e riparte.
Se metti un contenitore sotto, trasferisce i prodotti dall'alto rispettando i
suoi slot accessibili. Se non può contenere il prossimo raccolto completo,
attende spazio senza spendere altro fertilizzante per un nuovo ciclo.

L'accesso automatico alla serra avviene così:

- dall'alto: terreno, pianta e farina d'ossa;
- dai lati: farina d'ossa;
- dal basso: estrazione dai nove slot di uscita.

Le serre funzionano quando il loro chunk è caricato e attivo. Greenhouse non
carica autonomamente i chunk e non richiede energia, acqua o un orario solare.
