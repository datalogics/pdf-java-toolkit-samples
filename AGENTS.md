# pdf-java-toolkit-samples – Agent Guide

## Project Snapshot
- Purpose: runnable examples that showcase Datalogics PDF Java Toolkit features (creation, forms, manipulation, rendering, etc.).
- Build system: Maven via the bundled wrapper (`./mvnw`) targeting Java 7 bytecode (`pom.xml`).
- Artifacts: sample jar plus copied runtime dependencies under `target/` once `./mvnw package`/`verify` completes.
- External requirement: proprietary Talkeetna/PDFJT artifacts (`com.datalogics.pdf:talkeetna` or `talkeetna-lm`) must exist in your Maven repo; they are not in Maven Central.

## Repository Layout
- `pom.xml`: top-level Maven project defining dependencies, plugin stack (checkstyle, PMD, FindBugs, animal-sniffer), and profiles.
- `src/main/java/com/datalogics/pdf/samples/`: sample code grouped by category (`creation`, `forms`, `manipulation`, `extraction`, `rendering`, `printing`, `images`, `signature`).
- `src/main/resources/com/datalogics/pdf/samples/`: bundled input assets (PDFs, images, form data) used by the samples.
- `src/test/java/com/datalogics/pdf/samples/`: JUnit 4 test suites per feature area plus integration runners (`AllUnitTests`, `AllIntegrationTests`, `RunMainMethodsFromJarIntegrationTest`).
- `lite/`: secondary Maven project that assembles the "Lite" distribution delivered with PDFJT. Uses the same source tree but trims the file set via the `generate-distribution` profile.
- `fix_throws_javadoc.sh` / `.sed`: helper scripts referenced during releases to normalize Javadoc (kept for distribution builds).

## Build & Tooling
- Standard build: `./mvnw verify` (runs unit tests, copies runtime deps to `target/dependency`, creates jar) — see `README.md` for CLI usage.
- Java version: compiler plugin locks source/target 1.7; ensure `JAVA_HOME` is compatible.
- Static analysis: Checkstyle, PMD, FindBugs, and Animal Sniffer are bound to the lifecycle and will fail the build on violations. Their configurations are unpacked from proprietary `com.datalogics` artifacts during `generate-resources`.
- Logging: SLF4J + Log4j 2 (`log4j-slf4j-impl`, `log4j-core`); default config in `src/main/resources/log4j2.xml`.
- Dependency snapshot (besides PDFJT): `commons-io`, `commons-csv`, `maven-artifact`, and test-time libraries (`junit`, `jmockit`, `junit-toolbox`, `hamcrest`, `reflections`).

## Testing & Execution
- Unit tests: `./mvnw test` (Surefire) — excludes `*IT.java` and `*IntegrationTest.java` by default.
- Integration tests: `./mvnw verify -Pintegration-tests` (Failsafe) — require access to sample resources and, for signing tests, DER key/cert pairs already bundled under `src/main/resources/.../signature/`.
- Running samples manually: after packaging, use `java -cp target/pdf-java-toolkit-samples-<version>.jar com.datalogics.pdf.samples.creation.HelloWorld` (documented in `README.md`).
- Some tests (`PrintPdfTest`, signing) stub external systems with fake classes; no extra setup beyond valid PDFJT license should be needed.

## Operational Notes
- Proprietary dependencies: if Maven cannot resolve `com.datalogics.pdf:talkeetna*`, you need credentials to the private repository or a local copy (often expected at `../repo` for internal builds).
- Licensing: evaluation builds expect the `.l4j` license file in the project root and `.use-pdfjt-lm` present so the `pdfjt-lm` profile activates automatically.
- Generated assets: build output lives under `target/` (`classes`, `dependency`, `integration-test-outputs`, jars). Clean with `./mvnw clean` as needed.
- IDE support: Eclipse Neon project settings and `.p2f` profile are already included for convenience.

## Quick Start Checklist for Agents
1. Ensure Java 7+ JDK and access to the private PDFJT Maven repo (or preinstalled Talkeetna artifacts).
2. Place evaluation license (`*.l4j`) in the repo root and touch `.use-pdfjt-lm` if running the LM edition.
3. Run `./mvnw verify` for a full build; add `-Pintegration-tests` when you need integration coverage.
4. Use samples under `com.datalogics.pdf.samples.*` as references for new functionality; utility helpers live in `src/main/java/com/datalogics/pdf/samples/util/`.

