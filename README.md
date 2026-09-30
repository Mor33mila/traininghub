# TrainingHub

TrainingHub e' una piattaforma web per gestire utenti, corsi, partecipanti, iscrizioni e presenze. Il backend e' composto da quattro microservizi Spring Boot; il frontend e' HTML, CSS e JavaScript senza build step.

La dashboard include grafici e indicatori di frequenza; catalogo, partecipanti e audit hanno filtri avanzati. Gli amministratori possono consultare l'audit e scaricare report in PDF, Excel, CSV o JSON. Chart.js e le librerie PDF/Excel/ZIP sono caricate da CDN: serve una connessione Internet per i grafici e per quei formati; CSV e JSON sono generati dal browser.

## Requisiti

- Docker Desktop con Docker Compose v2, per l'avvio consigliato.
- In alternativa: Java 21, MySQL 8.x e PowerShell; ogni servizio include il Maven Wrapper.

## Avvio con Docker Compose

Dalla directory principale del repository:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up --build -d
docker compose ps
```

Il controllo su `.env` evita di sovrascrivere una configurazione locale gia' presente. La prima compilazione scarica le immagini e le dipendenze Maven. Attendere che i servizi siano `running` e MySQL sia `healthy`, poi aprire <http://localhost:8088>. L'healthcheck attende MySQL prima di avviare i servizi applicativi. Il frontend chiama le API pubblicate sulle porte `8080-8083`; MySQL resta accessibile solo sulla rete interna Compose.

Per seguire i log: `docker compose logs -f identity-service course-service participant-service enrollment-service frontend`. Per arrestare i container: `docker compose down`. I dati restano nel volume `mysql-data`; `docker compose down -v` li cancella e va usato solo per azzerare l'ambiente.

Credenziali locali iniziali: `admin` / `TrainingHubAdmin123!`. Il valore iniziale e' solo per sviluppo e va cambiato prima di qualsiasi esposizione non locale. Le variabili si configurano nel file `.env`; non committare il file.

Lo script `database/init/01-create-databases.sql` crea i quattro database; Hibernate crea/aggiorna le tabelle all'avvio (`ddl-auto=update`).

## Avvio locale

1. Avviare MySQL 8 e creare i database `traininghub_identity`, `traininghub_courses`, `traininghub_participants` e `traininghub_enrollments` eseguendo `database/init/01-create-databases.sql`.
2. Aprire un terminale PowerShell per ogni servizio. In tutti impostare la stessa password MySQL, chiave JWT e issuer; nell'esempio si avvia Identity:

```powershell
$env:DB_PASSWORD = 'password-mysql'
$env:JWT_SECRET = 'dHJhaW5pbmdodWItZGVtby1qd3Qtc2VjcmV0LTIwMjY='
$env:JWT_ISSUER = 'traininghub-identity'
$env:ADMIN_PASSWORD = 'TrainingHubAdmin123!'
Set-Location .\identity-service
.\mvnw.cmd spring-boot:run
```

3. Aprire tre altri terminali dalla root del repository. In ciascuno impostare `DB_PASSWORD`, `JWT_SECRET` e `JWT_ISSUER` con gli stessi valori usati per Identity, poi avviare un servizio:

```powershell
$env:DB_PASSWORD = 'password-mysql'
$env:JWT_SECRET = 'dHJhaW5pbmdodWItZGVtby1qd3Qtc2VjcmV0LTIwMjY='
$env:JWT_ISSUER = 'traininghub-identity'
Set-Location .\course-service
.\mvnw.cmd spring-boot:run
```

Ripetere sostituendo `course-service` con `participant-service` e poi `enrollment-service`. Le porte predefinite sono `8081`, `8082` e `8083`; enrollment usa per default i servizi course e participant su `localhost:8081` e `localhost:8082`.
4. Servire il frontend sulla porta CORS consentita, ad esempio da un quinto terminale alla root:

```powershell
python -m http.server 5500 --directory frontend
```

Aprire <http://localhost:5500>. L'avvio locale richiede Java 21, MySQL raggiungibile sulla porta `3306` e i quattro database gia' creati.

## Flusso demo

1. Accedere come amministratore.
2. Creare un corso con date coerenti, capienza e ore totali positive.
3. Creare un partecipante.
4. In Iscrizioni, selezionare corso e partecipante e creare l'iscrizione.
5. Nell'elenco iscrizioni dello stesso corso, impostare lo stato su `CONFIRMED`.
6. In Presenze, scegliere corso e iscrizione confermata, selezionare data e registrare entrata/uscita. Per un'assenza selezionare la casella dedicata e inserire facoltativamente la giustificazione.
7. Tornare alla Panoramica per vedere conteggi, disponibilita' e frequenze aggiornate.

In Panoramica sono disponibili i grafici dei corsi per stato, delle iscrizioni per stato e della distribuzione della frequenza. In **Corsi** e **Partecipanti** aprire **Filtri avanzati** per combinare i criteri; **Azzera filtri** ripristina la ricerca. Il calendario puo' essere filtrato per corso.

La sezione **Audit modifiche**, visibile agli amministratori, mostra le operazioni eseguite dalla UI con data, utente, tipo di azione, risorsa e ID dell'elemento. Ricerca e filtro per risorsa si applicano a tutto lo storico.

## Report e audit

**Esportazioni** e **Audit modifiche** sono disponibili solo agli amministratori. In Esportazioni selezionare uno o piu' dataset e il formato: PDF crea sezioni, Excel un foglio per dataset, JSON un unico documento; un singolo dataset produce un CSV, piu' dataset un archivio ZIP con un CSV per ciascuno. PDF, Excel e ZIP richiedono il caricamento delle librerie CDN e quindi una connessione Internet.

L'audit conserva attore, azione, risorsa, ID e timestamp, non il contenuto dei record. Traccia le mutazioni inviate dalla UI; le scritture effettuate direttamente da altri client API non vengono registrate. Non e' un registro anti-manomissione.

La sezione **Calendario** consente ad amministratore e tutor di pianificare, modificare ed eliminare lezioni nel periodo del corso. I docenti consultano il calendario dei soli corsi assegnati; la gestione delle lezioni resta riservata ad amministratore e tutor.

Il primo amministratore e' creato all'avvio da `ADMIN_USERNAME`, `ADMIN_PASSWORD` e `ADMIN_EMAIL`. Altri dati demo si possono inserire dalla UI seguendo la procedura sopra.

## Servizi e API

| Servizio | Porta | Responsabilita' | Swagger UI |
| --- | ---: | --- | --- |
| Identity | 8080 | Utenti, login JWT, ruoli, audit | `/swagger-ui/index.html` |
| Course | 8081 | Corsi, stati, capienza e docenti | `/swagger-ui/index.html` |
| Participant | 8082 | Anagrafica e ricerca partecipanti | `/swagger-ui/index.html` |
| Enrollment | 8083 | Iscrizioni, presenze e frequenza | `/swagger-ui/index.html` |
| Frontend | 8088 (Compose) / 5500 (locale) | Interfaccia web, dashboard, filtri e report | `/` |

Gli endpoint OpenAPI sono disponibili su `/v3/api-docs` per ciascun servizio. Le porte applicative sono pubblicate sul loopback; non esporre questo setup demo in rete senza rivedere autenticazione, segreti, CORS e credenziali MySQL.

Swagger UI: `http://localhost:8080/swagger-ui/index.html` (Identity), `http://localhost:8081/swagger-ui/index.html` (Course), `http://localhost:8082/swagger-ui/index.html` (Participant) e `http://localhost:8083/swagger-ui/index.html` (Enrollment).

## Configurazione

Le configurazioni Spring leggono le variabili d'ambiente; `.env.example` documenta i valori per Compose. `ATTENDANCE_MINIMUM_THRESHOLD` imposta la soglia di frequenza (predefinita 80%). La chiave JWT e' una stringa Base64 condivisa da tutti i servizi.

## Test

Eseguire in ogni directory di servizio:

```powershell
.\mvnw.cmd test
```

I test usano H2 e non richiedono un MySQL attivo. Il frontend non ha ancora una suite automatizzata browser; la demo end-to-end e' descritta in `docs/user-manual.md`.

Ultima verifica documentata: 30 test Maven superati (Identity 7, Course 8, Participant 3, Enrollment 12). I test browser non sono automatizzati; i controlli manuali di dashboard, filtri, audit ed esportazioni non sostituiscono una suite E2E.

## Documentazione di consegna

- `docs/architecture.md`: servizi, comunicazioni e decisioni architetturali.
- `docs/data-model.md`: modello dati e vincoli.
- `docs/project-analysis.md` e `docs/backlog.md`: analisi e storie di lavoro.
- `docs/project-diary.md`: modello da compilare con le attivita' reali del gruppo.
- `docs/user-manual.md`: guida operativa e verifica della demo.
- `docs/requirements-checklist.md`: mappatura dei requisiti al codice e ai test.
- `docs/ai-usage.md`: registro dell'assistenza AI da rivedere e approvare dal gruppo.
- `docs/presentation.md`: scaletta per la presentazione tecnica.

Diagrammi e documenti sono sorgenti Markdown: il gruppo deve verificare che rappresentino la release finale, aggiungere i nomi dei corsisti e provare la procedura da un ambiente pulito prima della consegna.