# Pudget – Android Expense Manager

**Pudget** is an Android expense-management app focused on simple, local-first personal finance tracking.

## Features

- Add, edit, view, and delete expenses
- Undo deleted expenses
- Separate expense categories, including **Debts** and **Associations**
- Filter by category
- Today / This Week / This Month reports
- Custom date ranges, including ranges across different months
- PDF expense reports with EGP amounts
- Light, Dark, Green, Blue, and Purple themes
- Local data storage
- No Firebase, backend, or paid API required

## Project structure

```text
Pudget/
├── app/
│   └── src/main/
│       ├── java/com/masareefi/app/
│       └── res/
├── .github/workflows/
├── build.gradle
├── settings.gradle
└── README.md
```

## Build

Requirements:

- Android Studio
- JDK 17
- Android SDK 35

Open the repository root in Android Studio and sync Gradle.

From the command line:

```bash
gradle assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Automatic APK build

Every push to `main` runs the GitHub Actions Android build workflow. The generated APK is uploaded as a workflow artifact named **Pudget-debug-apk**.

## Downloads

The repository contains the project source code. For an easy end-user installation, publish a tested APK as a GitHub Release.

## Version

Current project version: **1.1** (versionCode 2).

## License

Add your preferred license before distributing the source publicly.
