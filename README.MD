# 🐞 BugBoard26 – Backend

Backend REST di **BugBoard26**, la piattaforma per la gestione collaborativa delle issue di un progetto software, sviluppata per il corso di Ingegneria del Software (Università degli Studi di Napoli Federico II, A.A. 2025/2026).

> Realizzato da **Simone Catenaccio** e **Luigi De Falchi**.

## 📖 Descrizione generale

BugBoard26 permette ai team di tracciare e risolvere i problemi di un progetto. Gli utenti possono segnalare issue, allegare immagini, classificarle con tag, commentarle e seguirne l'avanzamento lungo un ciclo di vita ben definito.

Questo repository contiene il **backend**: un'API REST stateless, protetta da autenticazione JWT, che gestisce utenti, issue, tag e commenti e li salva su un database PostgreSQL. Un frontend separato consuma queste API.

### Funzionalità principali
- 🔐 **Autenticazione e ruoli**: login con JWT, registrazione dei nuovi utenti riservata agli amministratori (`ADMIN`).
- 📝 **Gestione issue**: creazione, modifica, eliminazione, filtro per tag e vista delle issue assegnate all'utente corrente. È possibile allegare un'immagine.
- 🔄 **Ciclo di vita**: le issue seguono un flusso a stati (State pattern): `TODO → IN_PROGRESS → IN_REVIEW → CLOSED`. Gli admin possono rimandare una issue in lavorazione.
- 👤 **Assegnazione**: le issue possono essere assegnate agli utenti.
- 🏷️ **Tag** e 💬 **commenti** associati alle issue.


## 🛠️ Tecnologie utilizzate

| Ambito | Tecnologia |
|--------|-----------|
| Linguaggio | Java 17 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA, Validation) |
| Sicurezza | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL |
| Build | Maven (Maven Wrapper incluso) |
| Utility | Lombok |
| Test | Spring Boot Starter Test |

## 🌐 Endpoint principali

| Metodo | Percorso | Descrizione |
|--------|----------|-------------|
| POST | `/auth/login` | Login, restituisce il token JWT |
| POST | `/auth/register` | Registra un utente (solo ADMIN) |
| GET | `/auth/verify` | Verifica la validità del token |
| GET | `/issues` | Elenco issue (filtro opzionale `?tag=`) |
| GET | `/issues/me` | Issue assegnate all'utente corrente |
| GET | `/issues/{id}` | Dettaglio issue |
| POST | `/issues` | Crea una issue (multipart, con immagine opzionale) |
| PUT | `/issues/{id}` | Modifica una issue |
| DELETE | `/issues/{id}` | Elimina una issue |
| POST | `/issues/{id}/assign` | Assegna la issue |
| POST | `/issues/{id}/review` | Manda in revisione (`IN_PROGRESS → IN_REVIEW`) |
| POST | `/issues/{id}/send-back` | Rimanda in lavorazione (solo ADMIN) |
| POST | `/issues/{id}/close` | Chiude la issue |

## 🚀 Come si avvia

Per avviare il progetto:

```bash
./mvnw spring-boot:run
```

### Prerequisiti
- Java 17
- PostgreSQL in esecuzione su `localhost:5432` con un database chiamato `bugboard26`
- Un file `src/main/resources/application-dev.properties`, non versionato, con le credenziali:

```properties
spring.datasource.password=<password-del-database>
jwt.secret=<chiave-segreta-lunga-per-i-jwt>
```

## 🐳 Avvio con Docker Compose

L'intero stack del progetto (**Database PostgreSQL**, **Backend Spring Boot** e **Frontend React/Nginx**) è orchestrato tramite **Docker Compose** mediante il file `docker-compose.yml` presente nella cartella principale del workspace (allo stesso livello di `BugBoard26-backend-SL` e `bugboard26-frontend-LS`).

### Configurazione iniziale
Prima del primo avvio, crea il file `.env` nella cartella contenente `docker-compose.yml` a partire dal modello `.env.example` e imposta le tue credenziali (`POSTGRES_PASSWORD`, `JWT_SECRET`, ecc.):

```bash
cp .env.example .env
```

*(Assicurati inoltre che la porta `5432` non sia già occupata da un'istanza locale di PostgreSQL).*

### Comandi principali

- **Avviare con build** (al primo avvio, dopo aver cambiato branch o modificato codice/configurazione):
  ```bash
  docker compose up --build -d
  ```

- **Avviare** (quando le immagini sono già state compilate):
  ```bash
  docker compose up -d
  ```

- **Spegnere i container** (mantenendo i dati salvati nel database):
  ```bash
  docker compose down
  ```
