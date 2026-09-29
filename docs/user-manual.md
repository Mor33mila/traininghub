# Manuale utente

## Accesso

Aprire il frontend e accedere con le credenziali iniziali configurate per l'ambiente. In Compose, il default locale e' `admin` / `TrainingHubAdmin123!`; sostituire la password prima di un'esposizione non locale. Il token scaduto riporta alla schermata di accesso.

## Creare il percorso demo

1. In **Corsi**, creare un corso con codice univoco, ore positive, date valide, capienza positiva e stato. L'assegnazione docente e' facoltativa.
2. In **Partecipanti**, inserire dati anagrafici validi. Codice fiscale ed e-mail devono essere univoci.
3. In **Iscrizioni**, selezionare il corso e il partecipante e creare la richiesta. Selezionare il corso per visualizzare l'elenco; impostare lo stato dell'iscrizione su `CONFIRMED` per abilitarne le presenze.
4. In **Presenze**, selezionare corso e iscrizione confermata, indicare la data della lezione e l'entrata/uscita. Per registrare un'assenza, selezionare **Assente**: gli orari vengono disattivati; la giustificazione e' facoltativa.
5. In **Panoramica**, controllare iscrizioni, frequenza media, stato corsi, posti liberi e partecipanti sotto la soglia configurata.

## Ruoli

- **Amministratore**: gestione utenti, corsi, partecipanti, iscrizioni e presenze.
- **Tutor**: gestione corsi, iscrizioni e presenze; consultazione dei partecipanti e dei report.
- **Docente**: consultazione dei corsi assegnati e delle presenze dei propri corsi.

La disponibilita' delle sezioni e delle operazioni dipende anche dalle autorizzazioni applicate dalle API; nascondere un controllo nel browser non sostituisce il controllo server.

## Verifica manuale ripetibile

Prima della demo, avviare un database vuoto, seguire i passaggi sopra e verificare almeno: corso con date non valide rifiutato; duplicato partecipante/iscrizione rifiutato; capienza rispettata; presenza fuori periodo rifiutata; assenza salvata senza orari; frequenza e stato sotto soglia coerenti con le ore inserite. Registrare esito e ambiente usato nel verbale di prova del gruppo.