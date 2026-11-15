# UC-001: Generate Population Report

| Attribute | Description |
|---|---|
| Use Case ID | UC-001 |
| Use Case Name | Generate Population Report |
| Primary Actor | Report User |
| Goal | To allow users to generate population reports based on selected parameters. |
| Description | The user provides reporting criteria and the system retrieves relevant population information from the database and presents the results. |
| Preconditions | User has access to the system; database connection is available; population records exist. |
| Postconditions | A population report is successfully generated and displayed. |
| Trigger | The user selects the report generation option. |

## Main Success Scenario

1. User logs into the system.
2. User selects “Generate Population Report”.
3. User enters required search parameters.
4. System validates user input.
5. System sends query request to database.
6. Database returns matching population records.
7. System processes retrieved information.
8. System displays the generated report.

## Alternative Flow

### Invalid Input

1. User enters incomplete or invalid information.
2. System validates the information.
3. System displays an error message.
4. User updates the input and retries.

## Exceptions

- Database unavailable.
- No matching population records found.
- System processing failure.
