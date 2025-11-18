@echo off
setlocal enabledelayedexpansion

:: SmartFit Test Runner - Command Prompt Version
:: Works in cmd.exe without PowerShell

echo.
echo ============================================================
echo.
echo           SMARTFIT TEST RUNNER
echo.
echo ============================================================
echo.

echo Running unit tests...
echo.

:: Run tests
call gradlew.bat :app:testDebugUnitTest > nul 2>&1

if %ERRORLEVEL% EQU 0 (
    echo [32mTests completed successfully![0m
    echo.
    echo ============================================================
    echo                    TEST RESULTS
    echo ============================================================
    echo.
    
    :: Parse XML results
    set TOTAL_TESTS=0
    set TOTAL_FAILURES=0
    set TOTAL_ERRORS=0
    
    for %%f in (app\build\test-results\testDebugUnitTest\TEST-*.xml) do (
        :: Use findstr to extract test counts
        for /f "tokens=2 delims==" %%a in ('findstr /r "tests=\"[0-9]*\"" "%%f"') do (
            set "line=%%a"
            for /f "tokens=1 delims=^" %%b in ("!line!") do (
                set /a TOTAL_TESTS+=%%~b
            )
        )
        
        :: Extract test suite name
        for /f "tokens=2 delims==" %%a in ('findstr /r "name=\"com.example.smartfit" "%%f"') do (
            set "line=%%a"
            for /f "tokens=1 delims=^" %%b in ("!line!") do (
                set "name=%%~b"
                set "name=!name:com.example.smartfit.=!"
                echo   [32mPASS[0m !name!
            )
        )
    )
    
    echo.
    echo ============================================================
    echo                       SUMMARY
    echo ============================================================
    echo.
    echo   Total Tests:   !TOTAL_TESTS!
    echo   Status:        ALL TESTS PASSED
    echo   Pass Rate:     100%%
    echo.
    echo ============================================================
    echo              ALL TESTS PASSED!
    echo ============================================================
    echo.
    echo HTML report: test-report.html
    echo Test results: app\build\test-results\testDebugUnitTest\
    echo.
    
    :: Try to open HTML report
    if exist test-report.html (
        start test-report.html
    )
    
) else (
    echo [31mTests failed to execute![0m
    echo.
    echo Run with full output: gradlew.bat :app:testDebugUnitTest
    echo.
)

endlocal
