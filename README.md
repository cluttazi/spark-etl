# spark-etl

[![CI](https://github.com/cluttazi/spark-etl/actions/workflows/ci.yml/badge.svg)](https://github.com/cluttazi/spark-etl/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Scala](https://img.shields.io/badge/Scala-2.13-red.svg)](https://www.scala-lang.org/)
[![Spark](https://img.shields.io/badge/Apache%20Spark-4.1-e25a1c.svg)](https://spark.apache.org/)

A lightweight, reusable ETL (Extract–Transform–Load) toolkit for Apache Spark, structured as an sbt multi-project build.

## Overview

The `commons` module provides small, composable traits that encapsulate the three classic ETL stages so Spark jobs can mix in only what they need:

- **`Extractor`** – reads a `DataFrame` from one or more paths, automatically falling back through ORC → Parquet → JSON → CSV until one format succeeds
- **`Transformer`** – functional helpers for transforming `DataFrame`s (e.g. bulk column renaming) built on a generic, tail-recursive `applyToDF` combinator
- **`Loader`** – writes `DataFrame`s according to a target `LoadType` (`orc`, `parquet`, `json`, `csv`), including batched loading of multiple frames

All operations return `scala.util.Try`, keeping failure handling explicit and composable.

## Tech Stack

- Scala 2.13.18
- Apache Spark 4.1.3 (Core, SQL, Hive)
- sbt 1.11 multi-project build
- ScalaTest 3.2
- scalafmt (via sbt-scalafmt)

## Getting Started

### Prerequisites

- JDK 17 or 21 (Spark 4 requires Java 17+)
- [sbt](https://www.scala-sbt.org/) (any recent version; the correct sbt version is picked up from `project/build.properties`)

### Build & Test

```bash
sbt test
```

Code style is enforced with [scalafmt](https://scalameta.org/scalafmt/):

```bash
sbt scalafmtAll scalafmtSbt   # format
sbt scalafmtCheckAll          # verify
```

The test suite spins up a local Spark session and creates its own fixture files, so no external infrastructure is required.

## Project Structure

```
.
├── build.sbt                        # Multi-project definition and shared settings
└── commons/
    └── src/
        ├── main/scala/.../commons/
        │   ├── Extractor.scala      # Format-detecting DataFrame reader
        │   ├── Transformer.scala    # DataFrame transformation helpers
        │   ├── Loader.scala         # Typed DataFrame writers
        │   ├── enums/LoadType.scala # Supported output formats
        │   └── utils/Helper.scala
        └── test/scala/.../commons/  # ScalaTest specs + test-file helpers
```

## Continuous Integration

Every push and pull request runs `sbt test` on JDK 21 via [GitHub Actions](.github/workflows/ci.yml).

## License

This project is licensed under the MIT License – see [LICENSE](LICENSE) for details.
