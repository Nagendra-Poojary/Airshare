Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  AirShare: Airport Mobility Dispatch Engine" -ForegroundColor Green
Write-Host "  Starting Spring Boot 3 + PostGIS Backend..." -ForegroundColor Cyan
Write-Host "==================================================="

$mvn = "C:\Users\DEEKSHITH\.m2\wrapper\dists\apache-maven-3.9.7-bin\3k9n615lchs6mp84v355m633uo\apache-maven-3.9.7\bin\mvn.cmd"

if (Test-Path $mvn) {
    & $mvn spring-boot:run
} else {
    mvn spring-boot:run
}
