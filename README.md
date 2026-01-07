# Kriegspiel Project

> [!IMPORTANT]
> **Compliance Notice**: This is a strict **source-only** repository. No pre-compiled binaries, executables, or build artifacts are included.
> Users must build the application from source using the provided Maven instructions.

## 📋 Overview
A modern, cross-platform Java implementation of the 'Game of War' (Le Jeu de la Guerre), engineered for PvE strategy.

## ✨ Key Features
*   **Cross-Platform Compatibility:** 
    *   Full support for Windows, macOS (Apple Silicon/Intel), and Linux.
    *   **Robust Resource Loading**: Assets are loaded via standard classpath mechanisms, removing OS-dependent file path assumptions.
    *   **Case-Safety**: File handling is normalized to support strict case-sensitive filesystems.
*   **Heuristic AI:** Features a custom 'Desperation Mode' AI that evaluates board states.
*   **Thread-Safe Engine:** Custom rendering system preventing UI freezes.

## 🛠 Build & Run Instructions
This project follows standard Maven conventions and requires a local build.

### Prerequisites
*   Java Development Kit (JDK) 21 or higher
*   Maven 3.6+

### Building from Source
1.  Clone the repository.
2.  Open a terminal in the project root.
3.  Run the clean build command:
    ```bash
    mvn clean install
    ```

### Running the Application
Once the build completes successfully, launch the client using the shaded jar (which includes dependencies):

```bash
java -jar kriegspiel-client/target/kriegspiel-client-1.0-SNAPSHOT-shaded.jar
```
*(Note: Run this command from the project root directory)*

## 🧩 Architectural & Compliance Status
*   **Source Integrity**: All logic is plain Java. No native code (JNI) or platform-specific binaries.
*   **Resource Loading**: Standardized JVM `getResource` usage ensures deterministic runtime behavior across environments.
*   **Maven Structure**: Adheres to standard Maven dictionary structures for dependency management.

---

### Tech Stack
*   **Language:** Java 21
*   **GUI:** Java Swing
*   **Build:** Maven

<img width="1512" height="982" alt="Screenshot 2026-01-04 at 1 25 06 AM" src="https://github.com/user-attachments/assets/22cc3af6-7a11-4328-93a1-ce3c4da3be22" />
<img width="1512" height="982" alt="Screenshot 2026-01-04 at 1 24 55 AM" src="https://github.com/user-attachments/assets/9f8a15ba-bda8-4ebd-b126-6b1c569bcbae" />
<img width="1512" height="982" alt="Screenshot 2026-01-04 at 1 24 37 AM" src="https://github.com/user-attachments/assets/f8c99400-6e76-46c0-b0b6-ea7d35e3d862" />
