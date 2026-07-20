### Repository layout

`apps/backend` contains the Spring Boot backend.
The Gradle build and wrapper are located at the repository root.

Run backend Gradle commands from the repository root:

```bash
./gradlew :apps:backend:test
./gradlew :apps:backend:bootRun
```

### API documentation

Generate the backend API documentation with Dokka:

```bash
./gradlew :apps:backend:dokkaGenerate
```

Open `apps/backend/build/dokka/html/index.html` in a browser to view the generated documentation.