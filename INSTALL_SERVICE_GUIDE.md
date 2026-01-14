# install-service.bat - Windows Service Installation Guide

## Overview
`install-service.bat` is an automated script that installs/reinstalls the WedKnots Windows service. It automatically detects the latest version in `C:\hosting\wedknots` and creates a service that runs it.

## Features

✅ **Auto-detects Latest Version** - Finds and uses the newest version folder  
✅ **Admin Privilege Check** - Verifies administrator rights before running  
✅ **Clean Installation** - Removes old service before creating new one  
✅ **Service Validation** - Verifies service is running after installation  
✅ **PowerShell Script Generation** - Creates PowerShell script on-the-fly  
✅ **Uninstall Support** - Can remove service cleanly  
✅ **Colored Output** - Clear, readable console messages  

## Usage

### Install/Reinstall Service
```bat
cd C:\dev\projects\wed-knots\scripts
install-service.bat
```

### Uninstall Service
```bat
install-service.bat /uninstall
```

## What It Does

1. **Checks Admin Rights** - Ensures script runs as administrator
2. **Verifies Hosting Directory** - Confirms `C:\hosting\wedknots` exists
3. **Finds Latest Version** - Scans for `wed-knots-*` folders and uses newest
4. **Generates PowerShell Script** - Creates `install-service.ps1` dynamically
5. **Stops Existing Service** - Gracefully stops running WedKnots service
6. **Removes Old Service** - Deletes previous service definition
7. **Creates New Service** - Installs fresh WedKnots service
8. **Starts Service** - Automatically starts the new service
9. **Validates Installation** - Confirms service is running

## Directory Structure

```
C:\
├── hosting\
│   └── wedknots\
│       ├── wed-knots-1.0.2\
│       │   ├── bin\
│       │   │   └── start.bat  ← Service calls this with "service" parameter
│       │   ├── config\
│       │   │   └── config.env
│       │   └── app\
│       │       └── wed-knots-1.0.2.jar
│       └── wed-knots-1.0.3\  ← Latest version, will be used
│           └── ...
└── dev\
    └── projects\
        └── wed-knots\
            └── scripts\
                └── install-service.bat  ← Run this
```

## Generated PowerShell Script

The script automatically generates `install-service.ps1` in the same directory as `install-service.bat`. This PowerShell script:

- Searches for the latest `wed-knots-*` version folder
- Extracts the version path
- Verifies `start.bat` exists
- Creates Windows service entry pointing to `start.bat service`
- Starts the service
- Reports status and service details

## Service Configuration

**Service Name:** `WedKnots`  
**Display Name:** `WedKnots Application Service`  
**Startup Type:** `Automatic`  
**Binary Path:** `cmd.exe /c "<path-to-latest-version>\bin\start.bat" service`  

## Verify Service Installation

### Check Service Status
```bat
sc query WedKnots
```

### View Service Details
```bat
sc qc WedKnots
```

### Open Services Manager
```bat
services.msc
```

### Check Application Logs
```bat
type "C:\hosting\wedknots\wed-knots-1.0.3\logs\wedknots.log"
```

## Troubleshooting

### "Administrator Privileges Required"
- Right-click on Command Prompt
- Select "Run as administrator"
- Navigate to scripts folder
- Run `install-service.bat`

### "Hosting folder not found"
- Verify `C:\hosting\wedknots` exists
- Check folder structure with `dir C:\hosting\wedknots`
- Deploy application using release-deploy.bat first

### "No wed-knots version folders found"
- Ensure application is deployed: `release-deploy.bat 1.0.2`
- Check folder names match pattern `wed-knots-*` (case-sensitive)

### Service created but not running
- Check application log: `wedknots.log`
- Verify `start.bat` exists in bin folder
- Check database connection in config.env
- Run `start.bat` manually to see errors

### Need to switch to different version
Simply run `install-service.bat` again. It will:
1. Stop current service
2. Detect the newest version
3. Install and start the new version

## Integration with Release Workflow

**Deploy new version:**
```bat
release-deploy.bat 1.0.3
```

**Install as service:**
```bat
install-service.bat
```

**Remove service:**
```bat
install-service.bat /uninstall
```

## Related Scripts

- **start.bat** - Application startup script (called by service)
- **release-deploy.bat** - Build, package, and deploy new versions
- **stop.bat** - Stop the application

## Requirements

- Windows 7 or later
- Administrator privileges
- PowerShell 3.0 or later
- Java installed and in PATH
- `C:\hosting\wedknots` directory with deployed application

## Notes

- Service runs with `Automatic` startup type (starts on boot)
- The service monitors the Java process and keeps it running
- Service logs to application's `logs/wedknots.log` file
- To view real-time logs: `tail -f C:\hosting\wedknots\wed-knots-1.0.3\logs\wedknots.log`

