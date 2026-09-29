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
| P0 | Come valutatore, voglio avviare il prodotto con una procedura ripetibile. | `docker compose up --build` rende disponibili DB, servizi e frontend. | File predisposti; esecuzione da verificare con Docker |
| P1 | Come valutatore, voglio capire architettura e dati. | README, API, diagrammi e manuale corrispondono alla release consegnata. | Documenti redatti; revisione gruppo richiesta |
| P1 | Come gruppo, vogliamo dimostrare qualita' e tracciabilita'. | Suite automatica, contributi Git e report AI sono verificabili. | Test presenti; cronologia Git e revisione report a carico del gruppo |