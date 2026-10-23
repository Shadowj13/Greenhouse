# Installazione Greenhouse 0.3.0-beta.3

Per Minecraft Java **26.3**, NeoForge **26.3.0.76 o successivo della linea 26.3**,
Java **25**. Questa è una beta: le verifiche effettuate e i limiti sono descritti
nel rapporto di collaudo. Non usare il JAR con Fabric, Forge o Bedrock.

## Installazione in un'istanza NeoForge

1. Chiudi Minecraft. Apri la cartella dell'istanza che vuoi usare; in Prism
   Launcher puoi selezionare l'istanza e aprire la sua cartella Minecraft.
2. Copia **solo** `greenhouse-0.3.0-beta.3.jar` nella sottocartella `mods`.
   Non copiare lo ZIP completo nella cartella `mods`.
3. Avvia quell'istanza e verifica che nella lista delle mod compaia Greenhouse
   con versione `0.3.0-beta.3`.

Greenhouse non richiede Jade o JEI: sono compatibilità opzionali provate, non
dipendenze obbligatorie. NeoForge e Java devono già essere installati/configurati
nel launcher. Questo pacchetto non include un installer né Minecraft o Java.

## Aggiornamento dalle versioni precedenti

1. Con il gioco e l'eventuale server spenti, conserva una copia del mondo.
2. Nella cartella `mods`, sposta la vecchia versione di Greenhouse fuori
   dall'istanza e inserisci il nuovo JAR. Mantieni **un solo JAR Greenhouse**.
3. Riavvia l'istanza. Blocchi, inventari, progresso e farina d'ossa usano lo
   stesso formato di salvataggio; non devi rompere e riposizionare le serre.

La beta.3 aggiunge bacche luminose, dodici fiori bassi e quattro fiori alti.
Le colture precedenti, comprese le kelp della beta.2, mantengono le loro regole.
Un mondo usato con le nuove colture richiede la beta.3: per un ritorno alla beta.2
ripristina la copia del mondo precedente all'aggiornamento.
Non dichiariamo compatibilità con versioni
Minecraft diverse dalla 26.3. Per tornare a una versione precedente, usa la copia
del mondo effettuata prima dell'aggiornamento.

## Server dedicato

Installa lo stesso JAR sia nel server NeoForge sia nei client che si collegano.
Ferma il server prima di sostituire il file; riavvialo prima di riconnettere i
client. Non installare il JAR di collaudo `greenhouse-qa.jar` nei mondi normali.

## Se il gioco non parte

Controlla versione Minecraft, loader NeoForge, Java 25 e assenza di doppioni.
Per una segnalazione utile allega `logs/latest.log` e, se presente, il file in
`crash-reports`, indicando versione Greenhouse e le altre mod installate.

Nel pacchetto `SHA256SUMS.txt` permette di verificare che il JAR non sia cambiato
durante il trasferimento. Ricette, terreni e uso sono spiegati in `USO.md`.
