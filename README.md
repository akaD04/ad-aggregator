# Ad Data Aggregator (`adagg`)

A high-performance CLI tool written in Java that reads a large CSV (~1 GB) of advertising performance records, aggregates metrics by campaign, and produces two ranked reports: **Top 10 by CTR** and **Top 10 by lowest CPA**.

---

## Table of Contents

1. [Requirements](#requirements)
2. [Setup](#setup)
3. [How to Run](#how-to-run)
4. [Reports Generated](#reports-generated)
5. [Libraries Used](#libraries-used)
6. [Performance Benchmarks](#performance-benchmarks)
7. [Project Structure](#project-structure)
8. [Running Tests](#running-tests)

---

## Requirements

| Tool   | Version |
|--------|---------|
| Docker | 20+     |

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-org/adagg.git
cd adagg
```

### 2. Build the Docker image

```bash
docker build -t adagg:latest .
```

> The image uses a multi-stage build — Maven compiles the fat JAR in the first stage, and only the slim JRE runtime is included in the final image. No local Java or Maven installation is required.

---

## How to Run

```bash
docker run --rm \                                                                                                                                     
  -v "$(realpath ./ad_data.csv)":/work/ad_data.csv:ro \
  -v "$(realpath ./out)":/work/out \
  ad-aggregator:1.0.0 \
  --input /work/ad_data.csv \
  --output /work/out \
  --reports TOP10_CTR,TOP10_CPA
  ```

Output files will appear in `/path/to/your/data/reports/` on the host.

---

## Reports Generated

| File            | Description                                                                                             |
|-----------------|---------------------------------------------------------------------------------------------------------|
| `top10_ctr.csv` | Top 10 campaigns by CTR (clicks ÷ impressions), ties broken by campaign ID ascending                   |
| `top10_cpa.csv` | Top 10 campaigns with the **lowest** CPA (spend ÷ conversions); campaigns with 0 conversions excluded  |

### Output CSV Columns

```
campaign_id, total_impressions, total_clicks, total_spend, total_conversions, CTR, CPA
```

- `CTR` = `total_clicks / total_impressions`, formatted to 4 decimal places (e.g. `0.1000`)
- `CPA` = `total_spend / total_conversions`, formatted to 2 decimal places (e.g. `6.17`); blank when conversions = 0

---

## Libraries Used

| Library                     | Version  | Purpose                                                   |
|-----------------------------|----------|-----------------------------------------------------------|
| **picocli**                 | 4.7.x    | CLI argument parsing (`--input`, `--output`, `--reports`) |
| **Google Guice**            | 7.x      | Dependency injection / wiring                             |
| **univocity-parsers**       | 2.9.x    | Fast, robust CSV parsing (header extraction, whitespace trimming, line-ending detection) |
| **SLF4J + Logback**         | 2.x / 1.4.x | Structured logging throughout the pipeline             |
| **JUnit 5 (Jupiter)**       | 5.10.x   | Unit testing                                              |
| **Mockito**                 | 5.x      | Mocking in integration-style unit tests                   |

All dependencies are managed via `pom.xml` / Maven.

---

## Performance Benchmarks

Measured on **Linux (x86-64)** via `/usr/bin/time -v` running inside Docker.  
Input: `ad_data.csv` — **1 GB, 26,843,544 rows, 50 unique campaigns**.

| Phase                         | Duration     |
|-------------------------------|--------------|
| CSV read + aggregation        | 10.203 s     |
| Report generation (parallel)  | 0.015 s      |
| CSV write (2 reports)         | 0.011 s      |
| **Total wall-clock time**     | **11.56 s**  |

**Peak RSS (container):** ~30 MB (`/usr/bin/time -v` Maximum resident set size: 30,940 kB)

### Benchmark Log

```
2026-02-10 18:00:24.429 INFO  [main] v.f.adagg.cli.AggregateCommand - CLI args: input=/work/ad_data.csv, outputDir=/work/out, reports=[TOP10_CTR, TOP10_CPA]
2026-02-10 18:00:24.431 INFO  [main] v.f.a.r.processor.AdReportProcessor - Process started: input=/work/ad_data.csv, outputDir=/work/out, reports=[TOP10_CTR, TOP10_CPA]
2026-02-10 18:00:34.634 INFO  [main] v.f.a.r.processor.AdReportProcessor - CSV read completed: rows=26843544, badRows=0, campaigns=50, tookMs=10203
2026-02-10 18:00:34.650 INFO  [report-gen-17] v.f.a.r.processor.AdReportProcessor - Report generation finished: type=TOP10_CPA, rows=10, tookMs=9
2026-02-10 18:00:34.650 INFO  [report-gen-16] v.f.a.r.processor.AdReportProcessor - Report generation finished: type=TOP10_CTR, rows=10, tookMs=10
2026-02-10 18:00:34.651 INFO  [main] v.f.a.r.processor.AdReportProcessor - All reports generated: count=2, tookMs=15
2026-02-10 18:00:34.661 INFO  [main] v.f.a.r.processor.AdReportProcessor - Report written: type=TOP10_CTR, file=/work/out/top10_ctr.csv, rows=10, tookMs=10
2026-02-10 18:00:34.663 INFO  [main] v.f.a.r.processor.AdReportProcessor - Report written: type=TOP10_CPA, file=/work/out/top10_cpa.csv, rows=10, tookMs=1
2026-02-10 18:00:34.664 INFO  [main] v.f.a.r.processor.AdReportProcessor - Process finished: readMs=10203, totalMs=10233
```

---

## Project Structure

```
adagg/
├── src/
│   ├── main/java/vn/flinters/adagg/
│   │   ├── Main.java                         # Entry point
│   │   ├── AppModule.java                    # Guice bindings
│   │   ├── cli/
│   │   │   └── AggregateCommand.java         # picocli command
│   │   ├── domain/
│   │   │   ├── CampaignTotals.java
│   │   │   ├── MutableAdRow.java
│   │   │   ├── ReportRow.java
│   │   │   └── ReportType.java
│   │   ├── io/
│   │   │   ├── ReportCsvWriter.java
│   │   │   ├── UnivocityCsvRowReader.java
│   │   │   └── base/                         # CsvWriter, CsvRowReader, RowMapper interfaces
│   │   ├── reports/
│   │   │   ├── AdRowMapper.java
│   │   │   ├── CampaignAggregator.java
│   │   │   ├── ReportResult.java
│   │   │   ├── base/                         # Aggregator, ReportProcessor, ReportStrategy interfaces
│   │   │   ├── processor/
│   │   │   │   └── AdReportProcessor.java    # Orchestrator (read → aggregate → generate → write)
│   │   │   └── strategy/
│   │   │       ├── ReportStrategyFactory.java
│   │   │       ├── TopCtrReportStrategy.java
│   │   │       └── TopCpaReportStrategy.java
│   │   └── util/
│   │       ├── Metrics.java                  # CTR / CPA calculations
│   │       └── TopK.java                     # Generic min/max heap top-K utility
│   └── test/java/vn/flinters/adagg/
│       ├── io/
│       │   ├── ReportCsvWriterTest.java
│       │   └── UnivocityCsvRowReaderTest.java
│       ├── reports/
│       │   ├── AdRowMapperTest.java
│       │   ├── CampaignAggregatorTest.java
│       │   ├── processor/
│       │   │   └── AdReportProcessorTest.java
│       │   └── strategy/
│       │       ├── ReportStrategyFactoryTest.java
│       │       ├── TopCpaReportStrategyTest.java
│       │       └── TopCtrReportStrategyTest.java
│       └── util/
│           ├── MetricsTest.java
│           └── TopKTest.java
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Running Tests

Tests run inside Docker using the build stage, so no local Java or Maven is needed.

```bash
# Run all unit tests
docker build --target builder -t adagg:test .
docker run --rm adagg:test mvn test

# Run a specific test class
docker run --rm adagg:test mvn test -Dtest=TopKTest

# Run with verbose output
docker run --rm adagg:test mvn test -Dsurefire.failIfNoSpecifiedTests=false
```

All tests use **JUnit 5** with **Mockito** for mock-based assertions in the processor integration tests.