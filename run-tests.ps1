# SmartFit Test Runner
# Enhanced test execution with beautiful output
# Requires PowerShell 5.1 or higher

# Check PowerShell version
if ($PSVersionTable.PSVersion.Major -lt 5) {
    Write-Host "ERROR: This script requires PowerShell 5.1 or higher" -ForegroundColor Red
    Write-Host "Your version: $($PSVersionTable.PSVersion)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "For Command Prompt, use: run-tests.bat" -ForegroundColor Cyan
    exit 1
}

Write-Host "`n" -NoNewline
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                            ║" -ForegroundColor Cyan
Write-Host "║           🎯 SMARTFIT TEST RUNNER 🎯                      ║" -ForegroundColor Cyan
Write-Host "║                                                            ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Run tests
Write-Host "⏳ Running unit tests..." -ForegroundColor Yellow
Write-Host ""

$errorOccurred = $false
try {
    $output = & .\gradlew.bat :app:testDebugUnitTest 2>&1
    if ($LASTEXITCODE -ne 0) {
        $errorOccurred = $true
    }
} catch {
    $errorOccurred = $true
}

if (-not $errorOccurred) {
    Write-Host "✅ Tests completed successfully!`n" -ForegroundColor Green
    
    # Parse and display results
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║                    TEST RESULTS                            ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    
    $totalTests = 0
    $totalFailures = 0
    $totalErrors = 0
    $totalTime = 0.0
    
    Get-ChildItem "app\build\test-results\testDebugUnitTest\TEST-*.xml" | ForEach-Object {
        [xml]$xml = Get-Content $_
        $tests = [int]$xml.testsuite.tests
        $failures = [int]$xml.testsuite.failures
        $errors = [int]$xml.testsuite.errors
        $time = [double]$xml.testsuite.time
        
        $totalTests += $tests
        $totalFailures += $failures
        $totalErrors += $errors
        $totalTime += $time
        
        $className = $xml.testsuite.name -replace 'com.example.smartfit.', ''
        
        if ($failures -eq 0 -and $errors -eq 0) {
            Write-Host "  ✅ " -NoNewline -ForegroundColor Green
            Write-Host "$className" -NoNewline -ForegroundColor White
            Write-Host " ($tests tests, $($time)s)" -ForegroundColor Gray
        } else {
            Write-Host "  ❌ " -NoNewline -ForegroundColor Red
            Write-Host "$className" -NoNewline -ForegroundColor White
            Write-Host " ($tests tests, $failures failures, $errors errors)" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║                       SUMMARY                              ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  📊 Total Tests:   " -NoNewline -ForegroundColor Cyan
    Write-Host "$totalTests" -ForegroundColor White
    Write-Host "  ✅ Passed:        " -NoNewline -ForegroundColor Green
    Write-Host "$($totalTests - $totalFailures - $totalErrors)" -ForegroundColor White
    Write-Host "  ❌ Failed:        " -NoNewline -ForegroundColor Red
    Write-Host "$totalFailures" -ForegroundColor White
    Write-Host "  ⚠️  Errors:        " -NoNewline -ForegroundColor Yellow
    Write-Host "$totalErrors" -ForegroundColor White
    Write-Host "  ⏱️  Total Time:    " -NoNewline -ForegroundColor Cyan
    Write-Host "$([math]::Round($totalTime, 3))s" -ForegroundColor White
    Write-Host "  📈 Pass Rate:     " -NoNewline -ForegroundColor Magenta
    Write-Host "$(if ($totalTests -gt 0) { [math]::Round((($totalTests - $totalFailures - $totalErrors) / $totalTests) * 100, 1) } else { 0 })%" -ForegroundColor White
    Write-Host ""
    
    if ($totalFailures -eq 0 -and $totalErrors -eq 0) {
        Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
        Write-Host "║                                                            ║" -ForegroundColor Green
        Write-Host "║              🎉 ALL TESTS PASSED! 🎉                      ║" -ForegroundColor Green
        Write-Host "║                                                            ║" -ForegroundColor Green
        Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
    } else {
        Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Red
        Write-Host "║                                                            ║" -ForegroundColor Red
        Write-Host "║              ⚠️  SOME TESTS FAILED ⚠️                     ║" -ForegroundColor Red
        Write-Host "║                                                            ║" -ForegroundColor Red
        Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "📄 Detailed HTML report: " -NoNewline -ForegroundColor Cyan
    Write-Host "test-report.html" -ForegroundColor Yellow
    Write-Host "📂 Test results location: " -NoNewline -ForegroundColor Cyan
    Write-Host "app\build\test-results\testDebugUnitTest\" -ForegroundColor Yellow
    Write-Host ""
    
    # Open HTML report
    Write-Host "🌐 Opening HTML report in browser..." -ForegroundColor Cyan
    Start-Process "test-report.html"
    
}

if ($errorOccurred) {
    Write-Host "❌ Tests failed to execute!`n" -ForegroundColor Red
    Write-Host "Error output:" -ForegroundColor Yellow
    Write-Host $output
}

Write-Host ""
