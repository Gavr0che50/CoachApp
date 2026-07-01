# Samsung Health Data SDK

Place the official Samsung Health Data SDK AAR here:

```text
app/libs/samsung-health-data-api-1.1.0.aar
```

The app module loads `src/samsungHealth/java` only when that AAR exists, so the
default project remains buildable without redistributing Samsung's SDK binary.

Samsung constraints to keep in mind:

- Samsung Health Data SDK requires Samsung Health 6.30.2 or later.
- The SDK does not support emulators; validate it on a real Android phone.
- Read-only development can use Samsung Health developer mode.
- Distribution without developer mode requires Samsung partner approval for the
  app package name and signing certificate.
