# NotoMind

A modern, modular note-taking app built with Jetpack Compose. The goal was to ship a clean architecture with pragmatic trade-offs under interview time constraints, while keeping the codebase easy to extend.

> **Architecture note:** The **Detail** feature is implemented with **MVI** to demonstrate unidirectional data-flow; the rest of the features use **MVVM** for speed.

---

## ✨ Features

* Create, edit, archive, and restore notes
* Labeling & label selection flows
* List/grid layouts, Material 3 UI, edge-to-edge
* Theming: brand, dark mode config, dynamic color, contrast
* Simple gallery/attachments surface (local only)
* Onboarding toggle & basic settings
* Fully offline (no network/API)

---

## 🧱 Project Structure

```
root
├─ app/                       # Application entry; DI wiring, navigation graph
├─ build-logic/               # Convention plugins & shared Gradle logic
│  └─ convention/             # Kotlin/Android/Compose conventions
├─ feature/
│  ├─ detail/                 # Note details (MVI)
│  ├─ gallery/                # Gallery surface
│  ├─ labelscreen/            # Labels list/CRUD
│  ├─ main/                   # Notes list/home
│  ├─ selectlabelscreen/      # Label picker
│  └─ setting/                # Settings & theming
├─ modules/
│  ├─ analytics/              # Analytics abstractions + no-op/logcat impl
│  ├─ common/                 # Shared utils & extensions
│  ├─ data/                   # Repositories, mappers, use with local sources only
│  ├─ database/               # Room entities, DAO, relations
│  ├─ datastore/              # UserPreferences proto + DataStore serializers/migrations
│  ├─ designsystem/           # Typography, shapes, components
│  ├─ domain/                 # Models + business rules
│  ├─ model/                  # UI/domain models
│  ├─ testing/                # Test utilities & fakes
│  └─ ui/                     # Reusable Compose UI building blocks
└─ …
```

---

## 🏗 Architecture

* **Clean Architecture** with strict module boundaries

    * **Domain**: pure Kotlin models & business logic
    * **Data**: repositories over local sources (Room, DataStore)
    * **UI**: Compose screens + ViewModels
* **State management**

    * **MVI** in `feature/detail` (single source of truth, intents → reducer → state)
    * **MVVM** in other features for faster delivery (StateFlow/Flow + immutable UI state)
* **DI** via Hilt
* **Persistence**

    * **Room** (`modules/database`): normalized entities, DAOs, @Relation/@Junction for note–label
    * **DataStore Proto** (`modules/datastore`): `UserPreferences`, serializer, and migration stubs
* **Design System**

    * Material 3, theme palette, contrast levels, dynamic color, shared components

---

## 📊 Analytics

* Current: lightweight abstraction in `modules/analytics` with a **Logcat** / no-op logger for dev builds.
* **Planned**: full **Firebase Analytics** integration

    * Consolidated event schema (screen views, actions, performance markers)
    * Parameter typing & validation
    * Sessionized reporting + dashboards

---

## 🧪 Testing

* Current coverage:

    * Room DAO tests (insert/upsert/relations/queries)
    * DataStore serializer + migration tests
    * Repository tests with in-memory fakes
* **Planned**:

    * Compose UI tests for critical flows (list, detail, label selection)
    * Contract tests for MVI reducers/effects in `feature/detail`
    * Better `testing` module utilities (fakes, builders, Turbine helpers)

---

## 🖥 Tech Stack

* Kotlin, Coroutines, Flow
* Jetpack Compose, Material 3, Navigation
* Hilt (DI)
* Room (SQLite), DataStore (Proto)
* Gradle Version Catalog + **build-logic/convention** plugins
* JUnit, Turbine, kotlinx-coroutines-test

---

## 🔌 No API / Network

This project is **offline-first** and ships with **no remote API**. All data lives locally through Room and DataStore.

---

## 🚀 Build & Run

1. Open the project in Android Studio (Giraffe+ recommended)
2. Sync Gradle
3. Run the `app` module on a device/emulator (Android 8.0+)

> CI/CD scripts are intentionally omitted to keep the interview task focused on app architecture.

---

## 🔮 Roadmap

* Finish Firebase Analytics events + analysis pipeline
* Expand MVI to more complex flows where it adds value
* Improve test coverage & stability (UI tests, hermetic repos)
* Optional: export/import, cloud sync, richer attachments

---

## 📌 Why these choices?

* **MVI in Detail**: showcases intent/reducer patterns and predictable state, ideal for a screen with edits, side effects, and undo/redo potential.
* **MVVM elsewhere**: faster to implement for list/filters/settings under time limits while staying maintainable.
* **Strict modules**: easier to scale, enforce boundaries, and test in isolation.
* **Design system**: consistent look & feel and easier theming across features.

