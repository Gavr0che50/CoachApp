# Samsung Health Setup

CoachApp reads Samsung Health directly when the official Samsung Health Data SDK
AAR is present locally.

## SDK file

1. Sign in to Samsung Developer.
2. Download Samsung Health Data SDK v1.1.0.
3. Copy the SDK AAR to:

```text
app/libs/samsung-health-data-api-1.1.0.aar
```

Then verify locally:

```powershell
$env:GRADLE_USER_HOME='D:\Temp\CoachApp-gradle'
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :app:verifySamsungHealthSdk projectCheck --console=plain
```

## Phone setup

Samsung Health Data SDK must be tested on a real phone. It does not support
emulators.

For development:

1. Open Samsung Health.
2. Go to Settings > About Samsung Health.
3. Tap the version number at least 10 times.
4. Open Developer mode (Samsung Health Data SDK).
5. Enable Developer Mode for Data Read.
6. Install CoachApp and tap the Samsung Health authorization button.

## Distribution

Without Samsung partner approval, an app using Samsung Health Data SDK works only
with Samsung Health developer mode enabled.

Use these values for a partner request:

```text
Package name: com.coachapp
Release key alias: coachapp-v1
```

Compute the release certificate SHA-256 locally with:

```powershell
$props = @{}
Get-Content keystore.properties | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') { $props[$matches[1].Trim()]=$matches[2].Trim() }
}
$store = Join-Path (Get-Location) $props['storeFile']
& "$env:JAVA_HOME\bin\keytool.exe" -list -v `
    -keystore $store `
    -alias $props['keyAlias'] `
    -storepass $props['storePassword'] |
    Select-String -Pattern 'SHA 256'
```
