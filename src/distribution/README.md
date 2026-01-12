# WedKnots Distribution

This archive provides a versioned, self-contained layout:

```
${project.artifactId}-${project.version}/
├─ bin/           # start/stop/setup scripts (Windows/Unix)
├─ app/           # Spring Boot jar (versioned)
├─ config/        # environment overrides (config.env)
├─ logs/          # application logs
├─ VERSION.txt    # build metadata
└─ README.md      # this file
```

## Parent/Child config.env loading

- Parent file: `../config.env` (one level above install dir)
- Child file: `config/config.env` (inside install dir)

Start scripts load the parent first, then child to override specific values.

## Scripts

- Windows: `bin\setup.bat`, `bin\start.bat`, `bin\stop.bat`
- Unix: `bin/setup.sh`, `bin/start.sh`, `bin/stop.sh`

### Setup
Prompts for values, seeds defaults from parent `../config.env`, writes overrides to `config/config.env`.

### Start
Stops any existing Java process and starts the app using values from parent+child config files.

### Stop
Stops the running application.

## Expected variables (mapped from application-prod.yml)

- SPRING_PROFILES_ACTIVE (default: prod)
- JASYPT_ENCRYPTOR_PASSWORD
- DATABASE_URL (default: jdbc:postgresql://localhost:5432/wedknots)
- DATABASE_USERNAME (default: wedknots_user)
- DATABASE_PASSWORD
- PORT (default: 8080)
- LOG_LEVEL (default: INFO)
- LOG_FILE (default: logs/wedknots.log)
- WHATSAPP_VERIFY_TOKEN (optional)
- WHATSAPP_APP_SECRET (optional)

## Usage (Windows)

1. Optional parent defaults: create `..\config.env` next to the installation folder.
2. Run setup: `bin\setup.bat` and answer prompts.
3. Start app: `bin\start.bat`.
4. Stop app: `bin\stop.bat`.

## Usage (Unix)

```bash
chmod +x bin/*.sh
./bin/setup.sh
./bin/start.sh
# later
./bin/stop.sh
```

