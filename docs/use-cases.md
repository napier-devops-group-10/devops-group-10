# SET09803 DevOps Group Coursework

## Population Reporting System

### Formal Use Case Specifications and UML Use Case Documentation

| Document Information | Details |
|---|---|
| **Module** | SET09803 DevOps |
| **Project** | Population Reporting System |
| **Document Type** | System Requirements Documentation |
| **Prepared By** | Ali Octave |
| **Role** | Systems Analyst and Documentation Lead |
| **Version** | 1.0 |
| **Date** | September 2026 |
| **Document Location** | `docs/use-cases.md` |

---

## Table of Contents

1. [Introduction](#1-introduction)
   - [1.1 Document Purpose](#11-document-purpose)
   - [1.2 System Overview](#12-system-overview)
2. [System Actors](#2-system-actors)
3. [Formal Use Case Specifications](#3-formal-use-case-specifications)
   - [3.1 UC-001: Generate Population Report](#31-use-case-uc-001-generate-population-report)
   - [3.2 UC-002: Search Population Data](#32-use-case-uc-002-search-population-data)
   - [3.3 UC-003: Maintain Population Records](#33-use-case-uc-003-maintain-population-records)
   - [3.4 UC-004: Export Population Report](#34-use-case-uc-004-export-population-report)
4. [UML Use Case Diagram](#4-uml-use-case-diagram)
5. [Conclusion](#5-conclusion)

---

## 1. Introduction

### 1.1 Document Purpose

This document presents the formal use case specifications for the Population Reporting System. Its purpose is to define the system's functional requirements by describing interactions between users and system components.

The use cases provide structured descriptions of how users interact with the system to search population information, generate reports, export results, and maintain population records.

This documentation supports the software development process by establishing a clear understanding of expected system behaviour before implementation. It also provides a foundation for system testing, validation, and future development.

### 1.2 System Overview

The Population Reporting System is a database-driven application designed to store, retrieve, process, and report population information. It enables authorised users to access population records and generate reports based on specified criteria.

The proposed system consists of the following components:

- **User interface:** Allows users to interact with the application.
- **Application logic:** Processes user requests and applies system rules.
- **Database layer:** Stores and retrieves population information.
- **Reporting functionality:** Generates population reports based on selected criteria.
- **Export functionality:** Allows generated reports to be saved in external formats.

These components work together to support population data retrieval, record management, reporting, and data export.

---

## 2. System Actors

The following table identifies the primary actors and supporting components involved in the Population Reporting System.

| Actor | Description |
|---|---|
| **Report User** | A user who searches population information and generates or exports reports based on selected criteria. |
| **System Administrator** | A privileged user responsible for maintaining population records, managing system information, and supporting data integrity. |
| **Database System** | A supporting system component responsible for storing and retrieving population records required by system operations. |

The Report User and System Administrator represent the main human users of the system. The Database System supports the application's data storage and retrieval operations.

---

## 3. Formal Use Case Specifications

This section defines the functional behaviour of the Population Reporting System through four formal use case specifications.

### 3.1 Use Case UC-001: Generate Population Report

#### Use Case Summary

| Attribute | Description |
|---|---|
| **Use Case ID** | UC-001 |
| **Use Case Name** | Generate Population Report |
| **Primary Actor** | Report User |
| **Goal** | Generate population reports based on selected parameters. |
| **Description** | The user provides reporting criteria. The system retrieves relevant population information from the database, processes the results, and presents the generated report. |
| **Preconditions** | 1. The user has access to the system. 2. The database connection is available. 3. Relevant population records exist. |
| **Postconditions** | A population report is generated and displayed to the user. |
| **Trigger** | The user selects the report generation option. |

#### Main Success Scenario

1. The user logs into the system.
2. The user selects **Generate Population Report**.
3. The user enters the required reporting parameters.
4. The system validates the user's input.
5. The system sends a query to the database.
6. The database returns matching population records.
7. The system processes the retrieved information.
8. The system displays the generated population report.

#### Alternative Flow: Invalid Input

1. The user enters incomplete or invalid reporting information.
2. The system validates the submitted information.
3. The system displays an appropriate error message.
4. The user corrects the input.
5. The user submits the request again.

#### Exceptions

- **Database unavailable:** The system cannot retrieve population information.
- **No matching records:** The system informs the user that no records match the selected criteria.
- **Processing failure:** The system reports that the request could not be completed.

---

### 3.2 Use Case UC-002: Search Population Data

#### Use Case Summary

| Attribute | Description |
|---|---|
| **Use Case ID** | UC-002 |
| **Use Case Name** | Search Population Data |
| **Primary Actor** | Report User |
| **Goal** | Search population information using defined filters. |
| **Description** | The user supplies search criteria, and the system retrieves and displays matching population records. |
| **Preconditions** | The user can access the system, and the database connection is active. |
| **Postconditions** | Matching population records are displayed, or the system indicates that no matches were found. |
| **Trigger** | The user selects the population search function. |

#### Main Success Scenario

1. The user opens the population search function.
2. The user enters the required search criteria.
3. The system validates the search parameters.
4. The system sends a search query to the database.
5. The database returns matching records.
6. The system displays the matching population information.

#### Alternative Flow: No Matching Records

1. The user submits valid search criteria.
2. The system searches the database.
3. No matching records are found.
4. The system displays a message indicating that no results are available.

#### Exceptions

- The database connection is unavailable.
- The search criteria cannot be processed.
- An unexpected system error prevents the search from completing.

---

### 3.3 Use Case UC-003: Maintain Population Records

#### Use Case Summary

| Attribute | Description |
|---|---|
| **Use Case ID** | UC-003 |
| **Use Case Name** | Maintain Population Records |
| **Primary Actor** | System Administrator |
| **Goal** | Add, update, and delete population records. |
| **Description** | The administrator manages population records through authorised record-management functions. |
| **Preconditions** | The administrator is authenticated and has the required permissions. The database is available. |
| **Postconditions** | Valid changes are saved to the database, or the system reports why the operation failed. |
| **Trigger** | The administrator selects a population record-management function. |

#### Main Success Scenario

1. The administrator logs into the system.
2. The administrator opens the record-management functions.
3. The administrator chooses to add, update, or delete a population record.
4. The system validates the requested changes.
5. The system checks that the operation is authorised.
6. The system saves the changes to the database.
7. The system confirms that the operation has completed successfully.

#### Alternative Flow: Invalid Record Information

1. The administrator enters incomplete or invalid information.
2. The system validates the submitted information.
3. The system displays an appropriate validation message.
4. The administrator corrects the information.
5. The administrator resubmits the changes.

#### Exceptions

- The administrator does not have the required permissions.
- The database is unavailable.
- The requested record cannot be found.
- The changes cannot be saved because of a system error.

---

### 3.4 Use Case UC-004: Export Population Report

#### Use Case Summary

| Attribute | Description |
|---|---|
| **Use Case ID** | UC-004 |
| **Use Case Name** | Export Population Report |
| **Primary Actor** | Report User |
| **Goal** | Save a generated population report in an external format. |
| **Description** | The user selects an existing report and an output format. The system prepares the report for export and makes the resulting file available to the user. |
| **Preconditions** | A population report has been generated and is available for export. |
| **Postconditions** | An export file is generated and made available to the user, or an error is reported. |
| **Trigger** | The user selects the report export option. |

#### Main Success Scenario

1. The user opens a generated population report.
2. The user selects the export option.
3. The user chooses a supported output format.
4. The system prepares the report data for export.
5. The system generates the export file.
6. The system makes the file available for download.
7. The user downloads the report.

#### Alternative Flow: Unsupported Format

1. The user selects an unsupported output format.
2. The system identifies that the format is unavailable.
3. The system displays a message explaining the issue.
4. The user selects a supported format.

#### Exceptions

- The report is no longer available.
- The selected export format cannot be processed.
- File generation fails.
- The export operation is interrupted by a system error.

---

## 4. UML Use Case Diagram

The UML use case diagram illustrates the relationship between the Population Reporting System's actors and its principal functions.

The diagram represents the following interactions:

- **Report User:** Generates population reports, searches population data, and exports reports.
- **System Administrator:** Maintains population records.
- **Database System:** Supports the retrieval and storage of population information required by system operations.

### Figure 4.1: Population Reporting System UML Use Case Diagram

![Population Reporting System UML Use Case Diagram](img.png)

*Figure 4.1: UML use case diagram showing the main actors and functions of the Population Reporting System.*

The diagram complements the formal use case specifications in Section 3 by providing a visual overview of the system's functional scope and actor relationships.

---

## 5. Conclusion

This document presents the formal functional description of the Population Reporting System through four use case specifications: generating population reports, searching population data, maintaining population records, and exporting reports.

Each use case identifies the relevant actors, goals, preconditions, postconditions, main success scenarios, alternative flows, and exceptions where applicable.

Together, the specifications and UML use case diagram provide a foundation for software implementation, system testing, requirements validation, and future documentation updates.

As part of the DevOps workflow, this document can be maintained alongside the project's source code in GitHub, enabling changes to be tracked through version control and reviewed through the team's development process.
