# Analisi del progetto

## Problema e obiettivo

TrainingHub centralizza le informazioni di un ente che gestisce piu' percorsi formativi. Il sistema deve collegare corsi, docenti, partecipanti, iscrizioni e presenze, rendendo consultabili capienza e frequenza da un'interfaccia web.

## Attori e bisogni

| Attore | Bisogno principale |
| --- | --- |
| Amministratore | Gestire utenti e dati anagrafici, corsi, iscrizioni e presenze |
| Tutor | Gestire corsi, iscrizioni, presenze e consultare report |
| Docente | Consultare i corsi assegnati e le presenze delle proprie attivita' |

## Requisiti funzionali

1. Autenticare gli utenti e applicare i permessi del ruolo.
2. Creare, consultare, modificare e rimuovere corsi con codice univoco, date coerenti, ore e capienza positive, stato, modalita' e docente facoltativo.
3. Gestire anagrafiche partecipanti, cercarle per cognome/codice fiscale/e-mail e impedire duplicati di codice fiscale ed e-mail.
4. Iscrivere un partecipante a un corso, registrare la data, impedire duplicati attivi e superamento della capienza, e gestire quattro stati.
5. Registrare presenze o assenze per iscrizioni ammissibili, calcolare ore e frequenza e segnalare chi scende sotto la soglia configurata.
6. Presentare in dashboard conteggi e grafici per stato di corsi/iscrizioni, posti residui, frequenza media e fasce di frequenza.
7. Ricercare corsi, partecipanti e audit con filtri combinabili; filtrare il calendario per corso.
8. Consentire agli amministratori l'esportazione selettiva di corsi, partecipanti, iscrizioni, presenze, lezioni, utenti e audit in PDF, Excel, CSV e JSON.
9. Consultare un audit delle mutazioni inviate dalla UI, con attore, azione, risorsa, ID e timestamp.

## Requisiti non funzionali e vincoli

- Backend Java 21, Spring Boot 3.x, Spring Security, validazione, JPA/Hibernate e MySQL 8.x.
- API REST JSON protette, DTO in ingresso/uscita, risposte d'errore coerenti e documentazione OpenAPI.
- Separazione dei domini in microservizi e dei relativi database; le dipendenze tra servizi sono chiamate REST.
- Configurazioni per ambiente tramite variabili, test ripetibili e avvio documentato via Docker Compose.
- Password mai salvate in chiaro; segreti reali esclusi da Git.

## Criteri di accettazione end-to-end

Un amministratore accede, crea un corso e un partecipante, crea e conferma un'iscrizione; un tutor registra una lezione o un'assenza. La UI mostra le nuove iscrizioni e i dati di frequenza; le API respingono date incoerenti, duplicati, capienza ecceduta, presenze fuori periodo e ruoli non autorizzati. Il flusso deve poter essere ripetuto seguendo il README su un ambiente pulito.