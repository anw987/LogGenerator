# 📊 Payment Log Simulation & SLO Analysis Toolkit

This project provides a simple end-to-end simulation and analysis pipeline for payment transaction logs. It includes tools to generate synthetic logs, summarize performance, and calculate Service Level Objective (SLO) metrics, along with basic HTML dashboards for visualization.

---

# 🧩 Components Overview

## 1. LogGenerator

Generates dummy transaction logs for testing and analysis.

### 🔧 Functionality

* Simulates payment API logs
* Generates logs within a configurable time range (default: **last 30 days**)
* Allows control over:

  * Total number of records
  * Success rate (HTTP 200 vs non-200 responses)

### 📄 Output

* File: `payment_logs.txt`
* Format: CSV

---

## 2. ReportSummary

Processes raw logs and produces summarized performance metrics.

### 🔧 Functionality

* Reads `payment_logs.txt`
* Groups data into time windows (default: **8 hours**)
* Calculates **success rate per time window**

### 📄 Output

* File: `summary.json`

---

## 3. SLOCalculator

Generates SLO (Service Level Objective) reports based on summarized data.

### 🧠 What is SLO?

**Service Level Objective (SLO)** is a target level of reliability for a system, typically defined as the percentage of successful requests over a given period.

Example:

* SLO target: **99.9% success rate**
* Means: only **0.1% errors are allowed**

---

### 🔧 Functionality

* Reads log or summary data
* Configurable:

  * Time window
  * SLO target (e.g., 99.9%)
* Calculates:

  * **Compliance** → whether SLO is met
  * **Average Success Rate**
  * **Burn Rate** → how fast error budget is consumed
  * **Error Budget (minutes)** → allowable failure time
  * **Used Error Budget**
  * **Remaining Error Budget**

### 📄 Output

* File: `sloreport.json`

---

# 🌐 Visualization

## index.html

* Displays data from `summary.json`
* Provides time-window-based success rate overview

## slo.html

* Displays data from `sloreport.json`
* Shows SLO compliance and error budget metrics

---

# 🔄 Data Flow

```text
LogGenerator → payment_logs.txt
               ↓
        ReportSummary → summary.json
               ↓
        SLOCalculator → sloreport.json
```

---

# 🚀 Usage Flow

1. **Generate Logs**

   * Run `LogGenerator`
   * Produces `payment_logs.txt`

2. **Create Summary**

   * Run `ReportSummary`
   * Produces `summary.json`

3. **Calculate SLO**

   * Run `SLOCalculator`
   * Produces `sloreport.json`

4. **View Results**

   * Open `index.html` → Summary view
   * Open `slo.html` → SLO analysis

---

# ⚙️ Configuration Notes

| Component     | Configurable Parameters                  |
| ------------- | ---------------------------------------- |
| LogGenerator  | Time range, number of rows, success rate |
| ReportSummary | Time window (default: 8 hours)           |
| SLOCalculator | Time window, SLO target                  |

---

# 🎯 Purpose

This toolkit is useful for:

* Simulating transaction systems
* Testing monitoring and observability pipelines
* Understanding SLO and error budget concepts
* Demonstrating reliability metrics in a controlled environment

---

# 📌 Summary

* Generate realistic logs ✅
* Summarize performance ✅
* Calculate SLO metrics ✅
* Visualize results in browser ✅

---

Feel free to extend this project with:

* real-time data ingestion
* API-based log input
* advanced dashboards (Grafana, etc.)
