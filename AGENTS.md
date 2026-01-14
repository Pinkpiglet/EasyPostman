# AGENTS.md

## Purpose
This guide helps agentic coding tools work safely in this repository.
It summarizes build/test commands and local code conventions.

## Repo Facts
- Language: Java 17 (Maven project).
- UI: Swing + FlatLaf.
- Tests: TestNG (see `src/test/java`).
- Logging: SLF4J + Logback (`src/main/resources/logback.xml`).
- Main class: `com.laker.postman.App`.
- No Cursor or Copilot rule files detected.

## Build / Run / Test Commands
### Build
- Clean + package: `mvn clean package`
- Clean + install (pulls deps): `mvn clean install`
- Package without tests: `mvn clean package -DskipTests`

### Run locally
- Run built JAR: `java -jar target/easy-postman-*.jar`
- Run via Maven (dev): `mvn clean compile exec:java -Dexec.mainClass="com.laker.postman.App"`

### Tests (TestNG)
- Run all tests: `mvn test`
- Run a single test class: `mvn -Dtest=ClassName test`
- Run a single test method: `mvn -Dtest=ClassName#methodName test`
  - Example: `mvn -Dtest=JsonUtilTest#testIsTypeJSON test`

### Packaging (installers)
- macOS: `chmod +x build/mac.sh && ./build/mac.sh`
- Windows: `build/win.bat`
- Linux: `build/linux-deb.sh` or `build/linux-rpm.sh`
- Packaging scripts expect a fixed `easy-postman.jar` name.

### Lint / Format
- No explicit lint or formatter configured in the repo.
- Keep formatting consistent with existing Java files.

## Project Layout
- `src/main/java/com/laker/postman/` → application code.
- `src/main/resources/` → themes, i18n, logback config.
- `src/test/java/com/laker/postman/` → TestNG tests.
- `build/` → platform packaging scripts.
- `docs/` → product documentation and images.

## Code Style & Conventions
### General Java Style
- 4-space indentation, braces on same line.
- Keep methods small and focused; use early returns.
- Prefer Javadoc for public classes/methods.
- Use English identifiers; comments may be bilingual.
- Avoid one-letter variable names unless idiomatic (e.g., `i`).

### Imports
- Use explicit imports; avoid wildcards in main code.
- Static imports are used in tests (TestNG assertions).
- Group imports by package with a blank line between groups.

### Naming
- Classes/Enums: `PascalCase`.
- Methods/fields: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- UI components: name by role (`*Panel`, `*Dialog`, `*Frame`).

### Lombok Usage
- Data models often use Lombok (`@Data`, `@Slf4j`, `@UtilityClass`).
- Prefer Lombok for boilerplate getters/setters/loggers.
- Keep Lombok annotations at the top of class definitions.

### Logging
- Use SLF4J (`@Slf4j`) and log at appropriate level.
- Include context in error logs (class/method, identifiers).
- Avoid `System.out` in main code; tests can use it.

### Error Handling
- Prefer early validation + clear user messages.
- Use `ExceptionUtil` when filtering/handling known exceptions.
- Log exceptions and show user-friendly messages (Swing dialogs).

### Internationalization (i18n)
- UI strings should use `I18nUtil` + `MessageKeys`.
- Add new keys to `messages_en.properties` and `messages_zh.properties`.
- Do not hardcode user-visible strings in UI classes.

### UI / Swing
- Initialize UI on the EDT (`SwingUtilities.invokeLater`).
- Avoid long-running work on the EDT; use background tasks.
- Keep UI components modular (`panel`, `dialog`, `frame`).

### Services & Utilities
- Utility classes are `@UtilityClass` or have private constructors.
- Keep stateless helpers in `util` package.
- Prefer small services with clear responsibilities.

### Tests
- Tests use TestNG (`@Test`, `Assert`).
- Test names are descriptive; use the `description` field.
- Keep test data inline and readable (text blocks are common).

## Build Pipeline Notes
- PR checks build a shaded JAR and run `mvn test`.
- Packaging jobs use JDK 17 (JetBrains distribution).
- JAR size is validated to remain under ~70 MB.

## When in Doubt
- Follow patterns in `src/main/java/com/laker/postman/`.
- Keep changes minimal and consistent with the surrounding code.
