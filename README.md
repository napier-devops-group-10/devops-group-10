# Population Reporting System

## SET09803 DevOps Group Coursework

The **Population Reporting System** is a database-driven application developed for the SET09803 DevOps group coursework. The system is designed to store, retrieve, process, and report population information while supporting a collaborative DevOps workflow that includes version control, continuous integration, containerisation, documentation, and incremental releases.

The project is being developed by **Group 10** using Scrum-based task allocation and GitFlow-style collaboration.

---

## Project Overview

The Population Reporting System enables authorised users to search population information, generate population reports using selected criteria, export generated reports, and maintain population records.

The solution is structured around three main layers:

- **User Interface** – allows users to interact with the application.
- **Application Logic** – processes user requests and reporting operations.
- **Database Layer** – stores and retrieves population information.

The project also incorporates DevOps practices such as Maven builds, GitHub Actions, Docker, GitHub pull requests, release tagging, and collaborative issue tracking.

---

## Objectives

The main objectives of the project are to:

- Develop a reliable population reporting application.
- Provide structured access to population information.
- Allow users to generate reports based on defined parameters.
- Support searching and filtering of population records.
- Allow authorised administrators to maintain population data.
- Support report export functionality.
- Automate application build processes using Maven and GitHub Actions.
- Containerise the application using Docker.
- Maintain clear technical and system documentation.
- Apply collaborative Git and GitHub development practices.

---

## System Actors

### Report User

A Report User interacts with the system to:

- Search population information.
- Generate population reports.
- Select reporting criteria.
- View generated results.
- Export generated reports.

### System Administrator

A System Administrator is responsible for:

- Maintaining population records.
- Adding population records.
- Updating existing records.
- Removing records when required.
- Supporting data integrity and system configuration.

### Database System

The Database System supports the application by:

- Storing population records.
- Retrieving requested population data.
- Supplying data required for report generation and search operations.

---

## Core Use Cases

| Use Case ID | Use Case | Primary Actor | Purpose |
|---|---|---|---|
| UC-001 | Generate Population Report | Report User | Generate population reports using selected parameters. |
| UC-002 | Search Population Data | Report User | Search population information using defined filters. |
| UC-003 | Maintain Population Records | System Administrator | Add, update, and delete population records. |
| UC-004 | Export Population Report | Report User | Save generated reports in supported external formats. |

Detailed formal use-case specifications are maintained in:

```text
/docs/use-cases.md
```

---

## Functional Requirements

The system is expected to support the following core functions:

1. Authenticate or provide authorised access to system functions.
2. Accept population-reporting parameters from users.
3. Validate user input before processing.
4. Query population information from the database.
5. Process retrieved population information.
6. Display generated population reports.
7. Search population records using defined criteria.
8. Allow authorised administrators to create, update, and remove records.
9. Export previously generated reports.
10. Handle invalid input, unavailable database connections, missing records, and processing failures appropriately.

---

## UML Use Case Model

The UML use-case model describes the interaction between the principal actors and the Population Reporting System.

At a high level:

```text
Report User
   |
   |---- Generate Population Report
   |---- Search Population Data
   |---- Export Population Report

System Administrator
   |
   |---- Maintain Population Records

Database System
   |
   |---- Supports storage and retrieval of population records
```

The complete UML diagram is maintained within the project documentation.

---

## Technology and DevOps Tooling

The project delivery plan identifies the following technologies and practices:

- **Java** – application development.
- **Maven** – dependency management and application build.
- **MySQL** – population data storage.
- **Git** – source control.
- **GitHub** – repository hosting, issues, pull requests, and releases.
- **GitHub Actions** – continuous integration and automated builds.
- **Docker** – application containerisation.
- **Docker Compose** – application and database container configuration.
- **Scrum / Kanban** – collaborative task and sprint management.

---

## Planned Repository Structure

```text
population-reporting-system/
│
├── .github/
│   └── workflows/
│       └── main.yml
│
├── docs/
│   ├── use-cases.md
│   └── uml/
│
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── CODE_OF_CONDUCT.md
└── README.md
```

> The repository structure may evolve as implementation progresses.

---

## Getting Started

### Prerequisites

Before building or running the application, ensure that the required development tools are installed.

Expected project dependencies include:

- Java Development Kit (JDK)
- Apache Maven
- MySQL
- Git
- Docker and Docker Compose, where containerised execution is required

Exact supported versions should be confirmed from the project's `pom.xml`, Docker configuration, and CI workflow.

---

## Clone the Repository

```bash
git clone <repository-url>
cd <repository-directory>
```

Replace `<repository-url>` and `<repository-directory>` with the actual GitHub repository details.

---

## Build with Maven

The project is intended to produce a self-contained executable JAR using Maven.

```bash
mvn clean package
```

After a successful build, the generated application artefact should be available in the Maven `target/` directory.

To verify the project without retaining previous build artefacts:

```bash
mvn clean verify
```

---

## Run the Application

Once the executable JAR has been created, run the generated JAR from the `target/` directory using the filename produced by the Maven configuration.

Example:

```bash
java -jar target/<generated-jar-file>.jar
```

The exact JAR filename should be taken from the completed Maven build configuration.

---

## Docker

The project delivery plan includes containerisation of the Java application and connectivity to a MySQL database.

### Build the Docker Image

```bash
docker build -t population-reporting-system .
```

### Run with Docker Compose

Where `docker-compose.yml` has been configured:

```bash
docker compose up --build
```

To stop the containers:

```bash
docker compose down
```

Database credentials and environment variables should not be committed directly to the repository. Use the configuration approach agreed by the development team.

---

## Continuous Integration

GitHub Actions is planned to automate project validation on repository pushes and pull requests.

The CI pipeline is expected to perform tasks such as:

- Check out the source code.
- Configure the Java build environment.
- Run the Maven build.
- Execute automated tests when available.
- Generate the executable JAR artefact.
- Validate or build the Docker image as the project pipeline develops.

Workflow configuration is maintained under:

```text
.github/workflows/
```

---

## Branching Strategy

The project follows a GitFlow-style workflow.

Main branches include:

```text
master
develop
release
```

Development work should be completed on dedicated feature branches created from `develop`.

Example:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/readme-documentation
```

Developers should avoid committing directly to `master` or `develop`.

---

## Pull Request Workflow

The team collaboration process is:

1. Synchronise the local `develop` branch.
2. Create a dedicated feature branch.
3. Make and test the required changes.
4. Commit changes using meaningful commit messages.
5. Push the feature branch to GitHub.
6. Open a Pull Request against `develop`.
7. Request review from at least one team member.
8. Resolve feedback before merging.

Where applicable, commits should reference the relevant GitHub issue.

Example:

```text
docs: add base README documentation #12
```

---

## Releases

The delivery plan includes creation of the first GitHub release:

```text
v0.1-alpha
```

This release represents an early project milestone containing the initial system structure and Code Review 1 deliverables.

Later releases should document significant functional and DevOps changes using appropriate version tags and release notes.

---

## Documentation

Project documentation includes:

- Formal Use Case Specifications.
- UML Use Case Diagram.
- README technical documentation.
- Code of Conduct.
- GitHub Issues and project-management artefacts.
- CI/CD workflow documentation.
- Docker and containerisation documentation.

The formal requirements documentation defines the expected system behaviour and provides a basis for implementation, testing, and future CI/CD activities.

---

## Code Review 1 Deliverables

The Group 10 delivery plan identifies the following major Code Review 1 artefacts:

- GitFlow repository structure.
- Product backlog and user stories.
- Maven-based executable JAR build.
- Dockerfile and Docker Compose configuration.
- GitHub Actions CI pipeline.
- Code of Conduct.
- Kanban or project-management integration.
- Formal use-case specifications.
- UML use-case documentation.
- GitHub release tag `v0.1-alpha`.
- Base `README.md`.

---

## Team Responsibilities

| Team Member | Role | Main Responsibility |
|---|---|---|
| Ashraf Hoque | Scrum Master & DevOps Lead | Architecture, coordination, Maven configuration, governance, branch policies |
| Adrian Riera Granada | Core Backend Java Developer | Java application structure, database connectivity, data access, reporting queries |
| Nicquan Jolly | CI/CD Automation Engineer | GitHub Actions, automated Maven build, artefact generation, build status |
| Elwin Lessey | Containerisation Engineer | Dockerfile, Docker Compose, MySQL container connectivity |
| Emron Lessey | Product Owner & Requirements Lead | Requirements, user stories, backlog, sprint/project board |
| Ali Octave | Systems Analyst & Documentation Lead | Use cases, UML documentation, README structure, release documentation |

---

## Error and Exception Handling

The documented use cases identify several conditions that the application should handle gracefully, including:

- Invalid or incomplete user input.
- Database connection failure.
- No matching population records.
- Application processing failure.
- Invalid administrative record changes.
- Export failure or unavailable report data.

Implementation should provide clear feedback without exposing sensitive technical or database information to users.

---

## Testing

Testing should evolve alongside implementation.

Expected testing areas include:

- Input validation.
- Database connectivity.
- Population search functionality.
- Report generation.
- Record-management operations.
- Report export.
- Maven build verification.
- Docker image build.
- CI pipeline execution.

Automated tests should be added under the project's test source directory as development progresses.

---

## Contribution Guidelines

All team members are expected to make traceable GitHub contributions.

Contributors should:

- Work from assigned issues or sprint tasks.
- Use dedicated feature branches.
- Write meaningful commit messages.
- Link commits to relevant issues where possible.
- Use Pull Requests for integration.
- Participate in code and documentation reviews.
- Keep local branches synchronised with `develop`.
- Avoid exposing passwords, secrets, or database credentials in commits.

---

## Current Project Status

The project is in active development toward the Code Review 1 milestone.

Documentation completed or prepared includes:

- Formal Use Case Specifications.
- UML Use Case Documentation.
- Base project README.

Implementation, CI/CD, database integration, containerisation, and release artefacts are developed collaboratively by the assigned Scrum team members.

---

## Author / Documentation Lead

**Ali Octave**  
Systems Analyst & Documentation Lead  
SET09803 DevOps – Group 10

---

## Version

**README Version:** 1.0  
**Project Initial Release Target:** `v0.1-alpha`

---

## Licence

No software licence is specified in the current coursework documentation. If the team adopts a licence, the appropriate `LICENSE` file and this section should be updated accordingly.