# KafeServer

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF.svg?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2034-3DDC84.svg?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20MVVM-02569B.svg?style=flat-square)](https://developer.android.com/topic/architecture)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg?style=flat-square)](https://github.com/fauzanabdulhakim/KafeServer)
[![Tests](https://img.shields.io/badge/Tests-29%2F29%20Passing-success.svg?style=flat-square)](https://github.com/fauzanabdulhakim/KafeServer)
[![Gradle](https://img.shields.io/badge/Gradle-8.9-02303A.svg?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

An enterprise-ready Android kitchen display system (KDS) and merchant operations application engineered with 100% Kotlin, Clean Architecture, MVVM with Unidirectional Data Flow (UDF), and Android Jetpack ViewBinding. The platform enables restaurant managers, counter staff, and kitchen personnel to process live orders, configure menu catalogs, manage item availability (86-ing), and resolve customer refund requests in real time via Firebase.

---

## Overview

KafeServer serves as the operational backbone for restaurant and cafe workflows. It provides real-time state synchronization with customer client orders, allowing staff to transition orders across lifecycle states, modify menu items, and adjust inventory flags instantaneously.

The application operates alongside [Kafe](https://github.com/fauzanabdulhakim/Kafe), which serves as the customer-facing mobile ordering application.

---

## Architecture

The project adheres to Clean Architecture guidelines and MVVM separation of concerns:

```
+--------------------------------------------------------------+
|                      Presentation Layer                      |
|       Activities, Fragments, ViewModels, ViewBinding         |
|             LiveData / StateFlow, EventBus events            |
+------------------------------+-------------------------------+
                               |
                               v
+--------------------------------------------------------------+
|                         Domain Layer                         |
|   UseCases (FilterOrdersByStatusUseCase, ValidateFoodInput)  |
|       Pure Kotlin Domain Models, Repository Interfaces       |
|                  (Zero Android SDK imports)                  |
+------------------------------+-------------------------------+
                               |
                               v
+--------------------------------------------------------------+
|                          Data Layer                          |
|          Firebase Realtime Database, Firebase Auth           |
|            Firebase Cloud Storage (Menu Imagery)             |
|         Firebase Cloud Messaging (FCM Dispatching)           |
+--------------------------------------------------------------+
```

### Architectural Principles

- **Pure Kotlin Domain**: Core order status validation, input sanitization, and session contracts reside in pure Kotlin models without Android framework bindings.
- **Unidirectional Data Flow (UDF)**: View emissions trigger ViewModel actions, which execute UseCases, emitting immutable state to observing Views.
- **Thread-Safe Session Isolation**: Replaced legacy static singletons with a scoped `ServerSessionManager`.
- **ViewBinding Standards**: Completely eliminated ButterKnife and synthetic accessors. All binding references are nullified in `onDestroyView()` to prevent memory leaks.
- **Leak-Free Resource Teardown**: Firebase database listeners and RxJava disposable pipelines are cleanly decoupled and disposed upon ViewModel teardown.
- **Responsive Layout Architecture**: Flat `ConstraintLayout` hierarchies with dedicated tablet layout resources (`layout-sw600dp`) designed for wall-mounted kitchen monitors and counter tablets.

---

## Core Capabilities

### Kitchen Display & Order Processing
- **Live Order Pipeline**: Real-time status filtering across deterministic lifecycle states: `Placed (0)`, `Shipping (1)`, `Shipped (2)`, and `Cancelled (-1)`.
- **Gesture Controls**: Touch actions to advance orders through fulfillment stages or initiate cancellations.
- **Tablet Interface**: High-density grid display optimized for kitchen display systems (KDS) and landscape tablets (`layout-sw600dp`).

### Menu & Category Administration
- **Catalog Management**: Add, update, and remove food categories with direct image uploads to Firebase Cloud Storage.
- **Dish Configuration**: Edit dish details, pricing, descriptions, and imagery per category.
- **Sizes and Add-ons**: Configure customizable dish options (e.g., portion sizes, extra toppings, flavor syrups) through `SizeAddonEditActivity`.

### Real-Time Availability (86-ing)
- **Instant Item Toggle**: Toggle item availability flags (`isAvailable: true/false`) in real time. Changes propagate immediately to customer devices to prevent order placement on sold-out items.

### Refund Resolution Panel
- **Refund Management**: Dedicated interface (`RefundFragment`) for reviewing customer cancellation inquiries, timestamps, and customer reasoning.
- **Approval Workflow**: Confirm or dismiss refund requests with automatic order status synchronization back to Firebase Realtime Database.

---

## Tech Stack

| Component | Specification |
|---|---|
| **Language** | Kotlin 1.9.0 (100% Kotlin codebase) |
| **Android SDK** | Minimum SDK 22 (Android 5.1) / Target SDK 34 (Android 14) |
| **Architecture** | Clean Architecture, MVVM, Unidirectional Data Flow (UDF) |
| **Jetpack Libraries** | ViewBinding, ViewModel, LiveData, Navigation Component |
| **Reactive Programming** | RxJava 2, RxAndroid |
| **Cloud Services** | Firebase Realtime Database, Firebase Authentication, Firebase Cloud Storage |
| **Image Loading** | Bumptech Glide 4 |
| **UI Components** | Material Components, Flat ConstraintLayouts, SpotsDialog |
| **Unit Testing** | JUnit 4, Mockito, Mockito-Kotlin, AndroidX Architecture Testing Core |

---

## Project Structure

```
KafeServer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fauzan/kafeserver/
│   │   │   │   ├── adapter/        # Order, category, food, size, and refund adapters
│   │   │   │   ├── callback/       # Repository and UI callback interfaces
│   │   │   │   ├── common/         # Session manager, constants, utility classes
│   │   │   │   ├── domain/         # Clean Architecture UseCases and business logic
│   │   │   │   ├── model/          # Domain entities and event bus models
│   │   │   │   ├── ui/             # MVVM feature packages (order, category, food_list, refund)
│   │   │   │   ├── HomeActivity.kt # Admin dashboard navigation shell
│   │   │   │   ├── MainActivity.kt # Staff authentication entrypoint
│   │   │   │   └── SizeAddonEditActivity.kt # Customization editor
│   │   │   ├── res/                # Layouts, sw600dp tablet layouts, drawables, values
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # JUnit4 and Mockito unit tests
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
└── settings.gradle
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (JetBrains Runtime `jbr-17` or OpenJDK 17)
- Android SDK Platform 34 and Build Tools 34.0.0+
- Valid `google-services.json` from the Firebase Console (matching customer database)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone git@github.com:fauzanabdulhakim/KafeServer.git
   cd KafeServer
   ```

2. **Configure Firebase (Optional for Testing)**
   The repository includes a mock `google-services.json` that enables automated testing and compilation out of the box. To connect to your live Firebase backend:
   - Download your project's `google-services.json` from the [Firebase Console](https://console.firebase.google.com/).
   - Replace `app/google-services.json` (or reference `app/google-services.json.example` as a template).

3. **Open in Android Studio**
   - Open the `KafeServer` directory in Android Studio.
   - Verify Gradle JVM points to **JDK 17** under `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`.
   - Synchronize Gradle dependencies.

4. **Assemble and Run**
   - Select an emulator or connected device (API 22+).
   - Execute `./gradlew assembleDebug` or trigger **Run 'app'** from Android Studio.

---

## Quality Assurance & Testing

The repository contains automated unit tests verifying order processing logic, item validation, and session management.

Execute the test suite using Gradle:

```bash
./gradlew testDebugUnitTest
```

### Coverage Highlights

- `ServerSessionManagerTest`: Validates staff session isolation and thread-safety.
- `OrderStatusMapperTest`: Tests order lifecycle transitions across merchant states.
- `FilterOrdersByStatusUseCaseTest`: Tests order filtering algorithms across active pipelines.
- `ValidateFoodInputUseCaseTest`: Validates input constraints on menu item creations and edits.
- `RefundRequestModelTest`: Tests refund data models and status verification.

---

## Companion Application

This repository represents the merchant and kitchen client. For the consumer mobile application, refer to:

- **[Kafe](https://github.com/fauzanabdulhakim/Kafe)**: Customer Mobile Ordering Application.

---

## Contribution Guidelines

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature-name`.
3. Adhere to Clean Architecture standards and accompany business logic with unit tests.
4. Ensure all unit tests pass: `./gradlew testDebugUnitTest`.
5. Submit a pull request with a comprehensive summary of changes.

---

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
