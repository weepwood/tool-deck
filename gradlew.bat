@echo off
setlocal
set GRADLE_VERSION=8.13
set INSTALL_ROOT=%USERPROFILE%\.gradle\tooldeck-bootstrap
set GRADLE_HOME=%INSTALL_ROOT%\gradle-%GRADLE_VERSION%
set ARCHIVE=%INSTALL_ROOT%\gradle-%GRADLE_VERSION%-bin.zip
set URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%INSTALL_ROOT%" mkdir "%INSTALL_ROOT%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%URL%' -OutFile '%ARCHIVE%'; Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%INSTALL_ROOT%' -Force; Remove-Item '%ARCHIVE%'"
  if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
