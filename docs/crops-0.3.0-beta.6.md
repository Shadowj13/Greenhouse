# Greenhouse 0.3.0-beta.6

Entrambe le serre coltivano i rampicanti piangenti su nylium cremisi e il
fungo a mensola su micelio o podzol. Il fungo richiede un esemplare iniziale,
impiega 900 tick e rende uno o due esemplari. La probabilità del secondo è
il 25%. I quattro stadi mostrano la crescita su un piccolo supporto di
pioppo, coerente con la collocazione del fungo in Minecraft 26.3.
[Note di Minecraft 26.3](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3).

Il muschio è stato ritirato dalla coltivazione. I salvataggi continuano a
leggere il vecchio stato e a conservare gli input. Un raccolto già pronto
può essere recuperato una volta, con raccolta manuale o esportazione.
Dopo il recupero, il muschio non avvia altri cicli. Per i rampicanti con il
terreno precedente, recupera il raccolto già pronto e sostituisci il blocco
di verruche del Nether con nylium cremisi. Un ciclo incompleto riparte sul
nuovo terreno. La schermata dà precedenza allo stato Pronto quando esiste
un raccolto recuperabile.

I controlli del repository verificano i quattro stadi, i substrati, la resa,
la farina d'ossa, l'esportazione e il recupero dei vecchi raccolti manuali e
automatici. La suite comprende 345 controlli server e 16 gruppi di test dei
modelli. Il comando `gradlew.bat releaseBundle` verifica il JAR e prepara lo
ZIP di installazione dopo il collaudo. I modelli usano risorse vanilla e la
stessa geometria della struttura già approvata.
