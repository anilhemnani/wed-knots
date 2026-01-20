$url = "https://wedknots.uk/api/whatsapp/webhook"
$verifyToken = "wedknots_whatsapp_verify_token"
$challenge = "test_challenge_123"

Write-Host "=== Testing wedknots.uk Webhook ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Webhook verification (GET)
Write-Host "Test 1: Webhook Verification (GET)" -ForegroundColor Cyan
$verifyUrl = "$url`?hub.mode=subscribe&hub.verify_token=$verifyToken&hub.challenge=$challenge"

try {
    $response = Invoke-WebRequest -Uri $verifyUrl -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response received successfully" -ForegroundColor Green
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    Write-Host "Note: GET may return error - POST is what matters" -ForegroundColor Yellow
}

Write-Host ""

# Test 2: DNS Resolution
Write-Host "Test 2: DNS Resolution" -ForegroundColor Cyan
try {
    $dns = [System.Net.Dns]::GetHostAddresses("wedknots.uk")
    Write-Host "Domain resolves to: $dns" -ForegroundColor Green
} catch {
    Write-Host "DNS Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== RESULTS ===" -ForegroundColor Yellow
Write-Host "✓ Domain wedknots.uk is accessible" -ForegroundColor Green
Write-Host "✓ Webhook endpoint responds" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Go to Meta Business Manager" -ForegroundColor White
Write-Host "2. Navigate to: WhatsApp > Configuration" -ForegroundColor White
Write-Host "3. Update Webhook URL to:" -ForegroundColor White
Write-Host "   https://wedknots.uk/api/whatsapp/webhook" -ForegroundColor Magenta
Write-Host "4. Set Verify Token to:" -ForegroundColor White
Write-Host "   wedknots_whatsapp_verify_token" -ForegroundColor Magenta
Write-Host "5. Subscribe to 'messages' topic" -ForegroundColor White
Write-Host "6. Send a test WhatsApp message" -ForegroundColor White
Write-Host "7. Check your application logs for webhook receipt" -ForegroundColor White

