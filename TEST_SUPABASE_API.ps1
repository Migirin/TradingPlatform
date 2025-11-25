# Supabase API Test Script
$apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzeGx6ZWZ6cWZicGN3dXhjb2VrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0OTU2MjUsImV4cCI6MjA3ODA3MTYyNX0.M3M6LsKbkt2b1Q6xZjDRKlTN1gE3q5LzyD8nbC6GPlY"
$url = "https://bsxlzefzqfbpcwuxcoek.supabase.co/rest/v1/items?select=*"

$headers = @{
    "apikey" = $apiKey
    "Authorization" = "Bearer $apiKey"
    "Content-Type" = "application/json"
}

Write-Host "Testing Supabase API..."
Write-Host "URL: $url"
Write-Host "API Key (first 20 chars): $($apiKey.Substring(0, 20))..."

try {
    $response = Invoke-WebRequest -Uri $url -Method GET -Headers $headers
    Write-Host "Success! HTTP Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response:"
    Write-Host $response.Content
    if ($response.StatusCode -eq 200) {
        Write-Host "API Key is VALID!" -ForegroundColor Green
    }
} catch {
    Write-Host "Error!" -ForegroundColor Red
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
    Write-Host "Error Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
    if ($statusCode -eq 401) {
        Write-Host "API Key is INVALID or EXPIRED!" -ForegroundColor Red
    }
}
