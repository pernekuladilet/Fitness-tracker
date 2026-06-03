# Fitness Tracker

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue)
![Maven](https://img.shields.io/badge/Maven-3.x-red?logo=apachemaven)
![License](https://img.shields.io/badge/license-MIT-green)

A modular fitness tracking application built with Java, JavaFX, and Maven. Users can record workouts, track activity, and monitor their progress through a clean graphical interface.

Built to demonstrate strong Object-Oriented Programming (OOP) principles, modular architecture, and Java project organization with Maven.

---

## Features

- Record and manage workout sessions
- Calculate and track calories burned
- Review workout history and activity logs
- Monitor personal fitness progress over time
- Clean and intuitive JavaFX graphical interface
- Well-organized modular project structure

---

## Screenshots

> Add screenshots of your application here.

| Login Screen | Main Dashboard |
|---|---|
| _screenshot coming soon_ | _screenshot coming soon_ |

---

## Prerequisites

Before running this project make sure you have:

- [Java 21+](https://adoptium.net/) installed
- [Maven 3.x](https://maven.apache.org/download.cgi) installed (or use the included `mvnw` wrapper)
- [PostgreSQL](https://www.postgresql.org/download/) installed and running

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/pernekuladilet/Fitness-tracker.git
cd Fitness-tracker
```

### 2. Set up the database

Create a PostgreSQL database for the application:

```sql
CREATE DATABASE fitness_tracker;
```

Then update the database connection settings in the source to point to your local PostgreSQL instance (host, port, username, password).

### 3. Build the project

Using the Maven wrapper (no local Maven installation needed):

```bash
# Linux / macOS
./mvnw clean install

# Windows
mvnw.cmd clean install
```

Or with a local Maven installation:

```bash
mvn clean install
```

### 4. Run the application

```bash
# Linux / macOS
./mvnw javafx:run

# Windows
mvnw.cmd javafx:run
```

Or with local Maven:

```bash
mvn javafx:run
```

The JavaFX window will open and you can start tracking your workouts.

---

## Technologies

| Technology | Version |
|---|---|
| Java | 21 |
| JavaFX | 21.0.2 |
| Maven | 3.x |
| PostgreSQL JDBC | 42.7.4 |

---

## License

This project is licensed under the [MIT License](LICENSE).
