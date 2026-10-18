# Greenhouse 0.3.0-beta.1

La prima beta confeziona l'implementazione verificata della 0.2.9-alpha senza
modifiche alla logica di gioco. Tutte le 12 classi di produzione e i 211 file di
modelli, texture e dati sono identici. Cambiano versione, guide e distribuzione.

Minecraft Java 26.3, Java 25; collaudo corrente su NeoForge 26.3.0.76.
Per installare o aggiornare: [INSTALLAZIONE.md](INSTALLAZIONE.md).
Per ricette, colture e automazione: [USO.md](USO.md).

## Verifiche sul JAR beta

- Build, integrità ZIP/JAR, metadati, 12 classi Java 25, risorse incluse,
  assenza del codice QA nel JAR installabile e cinque gruppi di controlli del
  modello.
- Tutti i 151 controlli del server, comprese 30 colture/colori, 300.000 estrazioni
  con seme fisso, ricette e consumo ingredienti, inventari, trasferimenti,
  rimozione, salvataggio e riavvio.
- Migrazione: checkpoint creati dal JAR 0.2.9-alpha, arresto del server e
  caricamento con la beta. Conservati progresso/fertilizzante e raccolto maturo
  bloccato dalle uscite piene.
- Due processi Minecraft completi indipendenti, con directory nuove e solo il
  JAR Greenhouse sul NeoForge client, collegati al server dedicato locale.
  Entrambi vedono la stessa serra con pioppo al 100%. Un click reale su Raccogli
  nel client A consegna esattamente due tronchi di pioppo; entrambi passano a 0%
  e disabilitano il pulsante. Il client B non riceve un secondo raccolto.
- Prova naturale con 256 serre automatiche e pioppi, compresa la selezione di
  tutti e tre i colori. Il rapporto JSON contiene durata effettiva in tick,
  minimo per macchina, ritardo iniziale di attivazione e verifica della resa
  esatta. Non viene dedotta una durata superata dalla sola attesa a cronometro.
- Pacchetto con un solo JAR, guide italiane, changelog, checksum e manifesto;
  rigenerazioni identiche byte per byte. Una ricevuta di test con SHA obsoleto
  viene rifiutata senza produrre uno ZIP installabile.

Le ricevute di questa versione sono in `docs/evidence/beta-*.json`. Gli screenshot
nativi dei due client sono consegnati nel pacchetto prove; il relativo JSON ne
registra dimensioni e SHA256. Non installare il mod QA o i datapack di prova in
un mondo normale.

## Riproduzione

```powershell
./gradlew.bat releaseBundle
python tests/verify_jar.py build/libs/greenhouse-0.3.0-beta.1.jar C:/path/to/greenhouse-0.2.9-alpha.jar --all-classes
```

`releaseBundle` esegue build e controlli del server prima di creare lo ZIP in
`build/distributions`. Il manifesto si riferisce all'hash del JAR effettivamente
provato. GitHub esegue la stessa procedura e allega sia JAR sia pacchetto ZIP.

Per la sessione completa client/carico, dopo `prepareQaRuntime` avvia
`python tests/run_acceptance_server.py`. Collega due client con profili di test
`GreenhouseNetA` e `GreenhouseNetB` a `127.0.0.1:25576`, poi aggiungi comandi JSONL
al file `build/acceptance/control.jsonl` mostrato dal programma:

```json
{"command":"greenhouseqa networksetup"}
```

Dopo la raccolta dal client A, `greenhouseqa networkassert` verifica i risultati.
Per il carico usa `greenhouseqa poplarsoaksetup`, poi `tick unfreeze`. Controlla
la durata con `greenhouseqa soakstats`; dopo almeno 6.040 tick naturali, congela
con `tick freeze` ed esegui `greenhouseqa poplarsoakassert`. Infine `save-all flush`
e `stop`. Il server QA non registra in questa modalità gli oggetti fittizi usati
solo dalla matrice completa delle colture: i client non richiedono il mod QA.

## Limiti del collaudo

La sessione usa profili di test offline e connessione locale. Non verifica login
di account reali, latenza Internet, tutti i contenitori di altre mod, shader o
resource pack alternativi. La prova di carico è una sessione locale di alcuni
minuti, non una garanzia di funzionamento per mesi o di prestazioni su altri PC.
Il tempo medio del server riportato è una finestra recente, non la media
dell'intera sessione.

Le prove grafiche con NeoForge 26.3.0.85, Jade e JEI sono quelle documentate per
la [0.2.9](crop-acceptance-0.2.9.md), con classi e risorse identiche; non sono
presentate come una nuova esecuzione con la beta. La prova di due client descritta
qui è invece eseguita nuovamente sul JAR beta.

A prove concluse, il server viene salvato e fermato normalmente (codice 0).
I due client di test, minimizzati, vengono poi terminati dal controllore della
sessione (codice 1 da interruzione): non viene dichiarata una loro uscita normale.
