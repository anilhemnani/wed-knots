# WhatsApp Flow - Public Key Signing Script
# This script helps generate the RSA key pair needed for WhatsApp Flow publishing

# Check if OpenSSL is available
try {
    $opensslCmd = Get-Command "openssl" -ErrorAction Stop
    $opensslVersion = & $opensslCmd.Source version
    Write-Host "OpenSSL found: $opensslVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: OpenSSL is not installed or not in PATH" -ForegroundColor Red
    Write-Host ""
    Write-Host "To install OpenSSL on Windows:"
    Write-Host "1. Download from: https://slproweb.com/products/Win32OpenSSL.html"
    Write-Host "2. Or install via Chocolatey: choco install openssl"
    Write-Host "3. Or install via WSL: wsl -- sudo apt-get install openssl"
    exit 1
}

# Configuration (portable: resolve repo root from script location)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
$defaultKeyPath = Join-Path $repoRoot "keys"
$keyPath = if (-not [string]::IsNullOrWhiteSpace($env:WEDKNOTS_KEYS_PATH)) { $env:WEDKNOTS_KEYS_PATH } else { $defaultKeyPath }
$privateKeyFile = Join-Path $keyPath "private_key.pem"
$publicKeyFile = Join-Path $keyPath "public_key.pem"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = Join-Path $keyPath "backups\$timestamp"

# Create directory if it doesn't exist
if (!(Test-Path $keyPath)) {
    New-Item -ItemType Directory -Path $keyPath -Force | Out-Null
    Write-Host "Created key directory: $keyPath" -ForegroundColor Green
}

# Ask user if they want to backup existing keys
if ((Test-Path $privateKeyFile) -or (Test-Path $publicKeyFile)) {
    Write-Host ""
    Write-Host "Existing keys found!" -ForegroundColor Yellow
    $backup = Read-Host "Do you want to backup existing keys? (Y/N)"

    if ($backup -eq "Y" -or $backup -eq "y") {
        New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

        if (Test-Path $privateKeyFile) {
            Copy-Item $privateKeyFile "$backupDir\private_key.pem"
        }
        if (Test-Path $publicKeyFile) {
            Copy-Item $publicKeyFile "$backupDir\public_key.pem"
        }

        Write-Host "Keys backed up to: $backupDir" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Generating RSA Key Pair..." -ForegroundColor Cyan
Write-Host "This may take a few seconds..."
Write-Host ""

# Generate private key (2048-bit RSA)
Write-Host "Step 1: Generating 2048-bit RSA private key..." -ForegroundColor Yellow
& $opensslCmd.Source genrsa -out $privateKeyFile 2048

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to generate private key" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Private key generated: $privateKeyFile" -ForegroundColor Green

# Extract public key
Write-Host ""
Write-Host "Step 2: Extracting public key from private key..." -ForegroundColor Yellow
& $opensslCmd.Source rsa -in $privateKeyFile -pubout -out $publicKeyFile

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to extract public key" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Public key extracted: $publicKeyFile" -ForegroundColor Green

# Display the public key
Write-Host ""
Write-Host "=" * 80
Write-Host "PUBLIC KEY (Copy this to Meta Business Manager)" -ForegroundColor Cyan
Write-Host "=" * 80
Write-Host ""

$publicKeyContent = Get-Content $publicKeyFile
$publicKeyContent | Write-Host

Write-Host ""
Write-Host "=" * 80
Write-Host ""

# Ask user if they want to open the public key in notepad
$openInEditor = Read-Host "Do you want to open the public key in Notepad? (Y/N)"
if ($openInEditor -eq "Y" -or $openInEditor -eq "y") {
    notepad $publicKeyFile
}

Write-Host ""
Write-Host "NEXT STEPS:" -ForegroundColor Green
Write-Host "1. Copy the public key above (or from: $publicKeyFile)"
Write-Host "2. Go to Meta Business Manager (https://business.facebook.com)"
Write-Host "3. Navigate to WhatsApp → Settings"
Write-Host "4. Find your phone number and upload the public key"
Write-Host "5. Sign the phone number with the key"
Write-Host "6. Publish your Flow"
Write-Host ""
Write-Host "KEY FILES:" -ForegroundColor Green
Write-Host "Private Key: $privateKeyFile"
Write-Host "Public Key: $publicKeyFile"
Write-Host ""
Write-Host "⚠️  IMPORTANT: Keep your private key safe and never commit it to git!"
Write-Host "Add these to your .gitignore:"
Write-Host "keys/"
Write-Host ""

# Create a README in the keys directory
$readmeContent = @"
# WhatsApp Flow - RSA Keys

## Files
- `private_key.pem` - Private key (KEEP SECRET - DO NOT SHARE OR COMMIT TO GIT)
- `public_key.pem` - Public key (Upload to Meta Business Manager)

## Generated
$(Get-Date)

## Usage
The public_key.pem needs to be uploaded to Meta Business Manager to enable Flow publishing.

### Upload Steps:
1. Go to https://business.facebook.com
2. Navigate to WhatsApp → Settings
3. Find your WhatsApp Business Phone Number
4. Upload the public_key.pem content
5. Sign the phone number

## Security Notes
- Never commit these files to git
- Never share the private key with anyone
- Store in a secure location
- Consider rotating keys annually

## Regeneration
To generate new keys, run:
  .\generate-whatsapp-keys.ps1
"@

Set-Content -Path "$keyPath\README.md" -Value $readmeContent

Write-Host "[OK] Created README.md in $keyPath" -ForegroundColor Green
Write-Host ""
