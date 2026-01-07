# Kriegspiel Project

> [!NOTE]
> This is a **source-only** repository. No pre-compiled binaries are included.

## 🛠 How to Build & Run
This project is a standard Java Maven project.

### Prerequisites
*   Java Development Kit (JDK) 17 or higher
*   Maven 3.6+

### Build Instructions
Open a terminal in the project root and run:
```bash
mvn clean install
```

### Run the Client
After a successful build, you can run the client using:
```bash
java -jar kriegspiel-client/target/kriegspiel-client-2.1.jar
```
*(Note: The exact JAR name in `target/` may vary slightly based on the version in `pom.xml`)*

---

# Kriegspiel Project (Developer Documentation)

A modern Java implementation of the 'Game of War' (Le Jeu de la Guerre), engineered for PvE strategy.

## 🚀 Key Features
*   **Heuristic AI:** Features a custom 'Desperation Mode' AI that evaluates board states.
*   **Thread-Safe Engine:** Custom rendering system preventing UI freezes.
*   **Cross-Platform:** Assets baked into the executable for Windows/Mac/Linux support.

## 🛠 Tech Stack
*   **Language:** Java 17+
*   **GUI:** Java Swing
*   **Build:** Maven
