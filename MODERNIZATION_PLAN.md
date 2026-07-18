# Modernization plan — spark-etl

Audit date: 2026-07-18. Conservative modernization: preserve behavior and
public APIs unless clearly broken.

## Current state (audited, not assumed)

- **Language/build**: Scala 2.11.12, sbt 1.1.6 multi-project (`root`
  aggregates `commons`), no plugins.
- **Code size**: ~200 LOC of Scala (5 main files, 3 test files) — small
  enough for a full in-place Spark/Scala migration with test verification.
- **CI**: `.github/workflows/ci.yml` — checkout@v4, setup-java@v4
  (temurin **8**), sbt/setup-sbt@v1, `sbt test`. Action majors are current;
  the JDK is not.
- **Baseline build status (this environment, Java 21 only)**: **won't run.**
  sbt 1.1.6 fails at launch on Java 21 (`UnsupportedOperationException: The
  Security Manager is deprecated`). Scala 2.11 / Spark 2.3 also require
  JDK 8, so compile/test cannot be executed pre-migration; CI on JDK 8 was
  the only working path.

## Dependency inventory

| Dependency | Current | Latest stable (≤ Jan 2026 cutoff) | Notes |
|---|---|---|---|
| Scala | 2.11.12 | 2.13.16 (2.13 line) | 2.11 EOL since 2017; Spark 4 requires 2.13 |
| sbt | 1.1.6 | 1.11.7 | 1.1.6 cannot launch on modern JDKs |
| spark-core/sql/hive | 2.3.1 (2018) | 4.1.3 (4.2.0 GA just published) | 2.3 EOL; many CVEs fixed since (e.g. CVE-2018-11770, CVE-2018-17190, CVE-2019-10099, CVE-2020-9480 shell-command RCE, CVE-2021-38296 auth bypass, CVE-2022-33891 shell injection, CVE-2023-22946) plus CVE-laden transitive deps (old Jackson, Guava, log4j 1.x, netty) |
| scalactic | 3.0.1 | 3.2.20 | versioned with scalatest |
| scalatest | 3.0.1 | 3.2.20 | 3.0 API removed (`FlatSpec`/`Matchers` moved to `flatspec.AnyFlatSpec` / `matchers.should.Matchers`) |
| Artima resolver | repo.artima.com | — | unused (was for SuperSafe); remove |

## Known code issues found in audit

1. **Real bug — `case nil =>` variable patterns** in
   `Loader.loadList` and `Transformer.applyToDF`: lowercase `nil` binds
   anything (it is not `Nil`), so the first case always matches, the
   `head :: tail` case is unreachable, and the functions silently do
   nothing (`renameColumns` never renames, `loadList` never writes).
   Fix to `case Nil =>` + regression tests.
2. `Extractor` ORC→Parquet→JSON→CSV fallback is behavior to preserve;
   note JSON/CSV readers rarely "fail" on wrong formats, so ordering
   matters (unchanged).
3. Spark 4 defaults `spark.sql.ansi.enabled=true`. Library code performs
   no casts/arithmetic on untrusted data (pure read/write/rename), so no
   behavior change expected; keep ANSI on (do NOT disable).

## Prioritized checklist

- [x] 1. Toolchain: sbt 1.1.6 → 1.11.7 (`project/build.properties`).
- [x] 2. Scala 2.11.12 → 2.13.18; Spark 2.3.1 → 4.1.3;
      scalatest/scalactic 3.0.1 → 3.2.20; remove Artima resolver;
      migrate ScalaTest imports; fork tests with Java-module `--add-opens`
      flags required by Spark on JDK 17+; verify `sbt test` green.
      (2.13.18 rather than 2.13.16: Spark 4.1.3 ships scala-library
      2.13.18 and SIP-51 requires the compiler to be at least that new.)
- [x] 3. Fix the `case nil` bugs with regression tests.
- [x] 4. Best practices: scalafmt (sbt-scalafmt 2.5.5 + `.scalafmt.conf`,
      scalafmt-core 3.9.10), README refresh, `.gitignore` additions.
- [x] 5. CI: setup-java temurin 21 + scalafmt check step (mirrors locally
      verified commands; action majors already current).
- [x] 6. Update this plan with Done vs Deferred + summary.

## Done vs Deferred

### Done

- sbt 1.1.6 → 1.11.7 (old sbt could not even launch on Java 21).
- Scala 2.11.12 → 2.13.18, Spark 2.3.1 → 4.1.3 (Core/SQL/Hive),
  ScalaTest + scalactic 3.0.1 → 3.2.20; unused Artima resolver removed.
- Test JVMs forked with Spark's JDK 17+ `--add-opens` module options.
- Bug fix: `case nil` variable patterns in `Transformer.applyToDF` and
  `Loader.loadList` made `renameColumns` a no-op and `loadList` write
  nothing; fixed to `case Nil` with regression tests (test count 1 → 6).
- scalafmt introduced and applied; formatting enforced in CI.
- README/tooling docs updated; CI on temurin JDK 21.

### Deferred (deliberately)

- **Spark 4.2.0**: GA'd on Maven Central very recently; staying on the
  mature 4.1.x patch line. A bump is a one-line change once 4.2.x has
  patch releases.
- **Scala 3**: Spark's own artifacts are 2.13-only; not applicable.
- **`provided` scope for Spark deps**: would be more idiomatic for a
  library but changes consumers' classpaths — out of scope for a
  behavior-preserving pass.
- **Dropping `spark-hive`**: unused by code/tests but part of the
  published dependency surface; kept for compatibility.
- **`Loader.loadList` error semantics**: it still ignores per-item
  failures and always returns success (original documented intent,
  "take action if failure" was never implemented). Changing it to
  aggregate failures would alter the public contract.

## Summary (PR-style)

Modernizes spark-etl from a 2016-era stack to a current one, preserving
the public API surface:

- **Toolchain**: JDK 8 → 21, sbt 1.1.6 → 1.11.7, Scala 2.11.12 → 2.13.18.
- **Dependencies**: Spark 2.3.1 → 4.1.3 (EOL + many CVEs resolved, e.g.
  CVE-2020-9480, CVE-2021-38296, CVE-2022-33891, CVE-2023-22946, plus
  vulnerable transitive log4j 1.x/Jackson/netty removed by upgrade);
  ScalaTest 3.0.1 → 3.2.20 with the 3.1+ package layout.
- **Bug fixes**: `case nil` variable patterns (silent no-ops in
  `renameColumns`/`loadList`) fixed, with regression tests.
- **Quality**: scalafmt (checked in CI), refreshed README/.gitignore.
- **CI**: temurin 21, formatting check + `sbt test` (same commands
  verified locally: 6/6 tests green on Java 21.0.10).

Risks: Spark 4 runtime behavior differences (ANSI mode on by default)
do not affect this library's code paths — it performs no casts or
arithmetic — but consumers embedding it in Spark 4 jobs inherit Spark's
own migration notes. Untouched: public trait/method signatures, the
Extractor's ORC→Parquet→JSON→CSV fallback order, `loadList`
continue-on-failure semantics, MIT license, project layout.

## Risk notes

- Spark is a `compile`-scope dependency of a library; kept that way
  (changing to `provided` would alter consumers' classpaths).
- `spark-hive` kept for API compatibility, though the test session uses
  native ORC and no Hive features are exercised.
- Spark 4.2.0 GA exists on Maven Central but is newer than the mature
  4.1.x patch line; choosing 4.1.3 deliberately (conservative).
