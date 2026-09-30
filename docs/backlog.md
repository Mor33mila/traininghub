# Backlog iniziale

Il backlog traduce la traccia in storie verificabili. Il gruppo deve assegnare responsabili, punti e stati reali prima della consegna.

| Priorita' | User story | Criteri di accettazione | Stato codice |
| --- | --- | --- | --- |
| P0 | Come amministratore, voglio autenticarmi per accedere alle funzioni autorizzate. | Credenziali valide producono JWT; credenziali errate sono rifiutate; endpoint protetti respingono anonimi. | API presente; ampliare test di autorizzazione |
| P0 | Come amministratore/tutor, voglio creare e consultare corsi per pianificare l'offerta formativa. | Codice univoco; date coerenti; ore/capienza positive; ricerca e filtro stato; UI CRUD. | API e UI presenti |
| P0 | Come amministratore, voglio gestire e cercare partecipanti. | Campi validati; codice fiscale/e-mail univoci; ricerca su cognome, CF ed e-mail; disattivazione. | API e UI presenti |
| P0 | Come tutor, voglio iscrivere un partecipante e aggiornare lo stato. | Verifica esistenza e attivita'; nessun duplicato attivo; capienza rispettata; data e stato salvati. | API e UI presenti |
| P0 | Come tutor, voglio registrare presenza o assenza per un iscritto valido. | Iscrizione confermata/conclusa; data nel periodo; ore da orari validi; assenza senza orari. | API e UI presenti; test di servizio |
| P0 | Come tutor, voglio vedere frequenza e persone a rischio. | Ore, percentuali e soglia configurabile sono coerenti; ritirati e richiesti non entrano nell'aggregato. | API e dashboard presenti |
| P0 | Come valutatore, voglio avviare il prodotto con una procedura ripetibile. | `docker compose up --build -d` rende disponibili DB, servizi e frontend; il README descrive verifica e arresto. | Compose avviato nell'ambiente corrente; ripetere su installazione pulita |
| P1 | Come amministratore/tutor, voglio confrontare i dati tramite grafici e filtri. | Grafici per stato e frequenza; criteri combinabili per corsi e partecipanti; reset risultati. | Implementato e verificato manualmente |
| P1 | Come amministratore, voglio consultare un audit delle modifiche. | Attore, azione, risorsa, ID e timestamp; ricerca e filtri paginati. | Implementato; registra mutazioni inviate dalla UI, non chiamate esterne |
| P1 | Come amministratore, voglio scaricare report in formati comuni. | Dataset selezionabili; PDF, Excel, CSV, JSON; CSV multipli in ZIP. | Implementato e verificato manualmente |
| P1 | Come valutatore, voglio capire architettura e dati. | README, API, diagrammi e manuale corrispondono alla release consegnata. | Documenti aggiornati; revisione indipendente del gruppo ancora richiesta |
| P1 | Come gruppo, vogliamo dimostrare qualita' e tracciabilita'. | Suite automatica, contributi Git e report AI sono verificabili. | 30 test Maven passati; aggiornamento remoto e approvazione AI a carico del gruppo |