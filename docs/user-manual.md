# Manuale utente

## Accesso

Aprire il frontend e accedere con le credenziali iniziali configurate per l'ambiente. In Compose, il default locale e' `admin` / `TrainingHubAdmin123!`; sostituire la password prima di un'esposizione non locale. Il token scaduto riporta alla schermata di accesso.

## Creare il percorso demo

1. In **Corsi**, creare un corso con codice univoco, ore positive, date valide, capienza positiva e stato. L'assegnazione docente e' facoltativa.
2. In **Partecipanti**, inserire dati anagrafici validi. Codice fiscale ed e-mail devono essere univoci.
3. In **Iscrizioni**, selezionare il corso e il partecipante e creare la richiesta. Selezionare il corso per visualizzare l'elenco; impostare lo stato dell'iscrizione su `CONFIRMED` per abilitarne le presenze.
4. In **Presenze**, selezionare corso e iscrizione confermata, indicare la data della lezione e l'entrata/uscita. Per registrare un'assenza, selezionare **Assente**: gli orari vengono disattivati; la giustificazione e' facoltativa.
5. In **Panoramica**, controllare iscrizioni, frequenza media, stato corsi, posti liberi e partecipanti sotto la soglia configurata.

La dashboard mostra tre grafici: corsi per stato, iscrizioni per stato e partecipanti suddivisi per fascia di frequenza (0-59%, 60-79%, 80-100%). Per i docenti, i grafici basati su dati non autorizzati indicano che i dati non sono disponibili.

## Ricerca e filtri

In **Corsi**, la ricerca testuale per codice o titolo si combina con i filtri avanzati per stato, modalita', area formativa, periodo, capienza minima e ore minime. Le date selezionano i corsi che si sovrappongono all'intervallo.

In **Partecipanti**, combinare la ricerca per cognome, codice fiscale o e-mail con stato attivo, titolo di studio, stato occupazionale e intervallo di nascita. Il conteggio indica quanti record corrispondono ai criteri; **Azzera filtri** ripristina tutti i campi.

Nel **Calendario** usare il menu **Corso** per limitare lezioni e agenda. Nell'**Audit modifiche**, l'amministratore puo' cercare per utente o ID e filtrare per risorsa, azione e date; i filtri si applicano anche alle pagine successive.

## Calendario lezioni

Amministratore e tutor possono aprire **Calendario**, filtrare per corso e pianificare una lezione con titolo, data, orario e note. La data deve ricadere nel periodo del corso e l'orario finale deve essere successivo a quello iniziale. Dall'agenda del giorno e' possibile modificare o eliminare una lezione. Il docente vede gli eventi dei soli corsi a lui assegnati e non puo' modificarli.

## Ruoli

- **Amministratore**: gestione utenti, corsi, partecipanti, iscrizioni e presenze; consultazione dell'audit ed esportazione dei dati.
- **Tutor**: gestione corsi, iscrizioni e presenze; consultazione dei partecipanti e della dashboard.
- **Docente**: consultazione dei corsi assegnati e delle presenze dei propri corsi.

La disponibilita' delle sezioni e delle operazioni dipende anche dalle autorizzazioni applicate dalle API; nascondere un controllo nel browser non sostituisce il controllo server.

## Audit ed esportazioni

**Audit modifiche** e **Esportazioni** sono visibili solo all'amministratore. L'audit riporta data e ora, attore, azione, risorsa e ID del record; non conserva il contenuto dei record. Registra le mutazioni inviate dall'interfaccia, non le scritture dirette alle API da client esterni.

In **Esportazioni** selezionare uno o piu' dataset: corsi, partecipanti, iscrizioni, presenze, lezioni, utenti e audit. PDF crea sezioni, Excel un foglio per dataset, JSON un singolo documento. Un solo dataset produce un file CSV; piu' dataset producono uno ZIP con un CSV per dataset. I dataset utenti e audit contengono informazioni riservate: trattare e condividere i file secondo le regole del gruppo. PDF, Excel e ZIP richiedono una connessione Internet per caricare le librerie CDN.

## Verifica manuale ripetibile

Prima della demo, avviare un database vuoto, seguire i passaggi sopra e verificare almeno: corso con date non valide rifiutato; duplicato partecipante/iscrizione rifiutato; capienza rispettata; presenza fuori periodo rifiutata; assenza salvata senza orari; frequenza e stato sotto soglia coerenti con le ore inserite; filtri combinati e reset; audit filtrato per data/azione; download CSV singolo e multiplo, PDF, Excel e JSON. Registrare esito, browser, ambiente e responsabile nel verbale di prova del gruppo.