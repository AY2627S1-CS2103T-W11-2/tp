---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.boothmanagerpro.commons.util.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.boothmanagerpro.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.boothmanagerpro.logic.LogicManagerTest`

## Testing exhibitor additions

| Test class | Coverage focus |
| --- | --- |
| `AddCommandParserTest` | Required/optional fields, prefix errors, validation, and normalisation. |
| `AddCommandTest` | Successful additions and duplicate rejection. |
| `ExhibitorFieldsTest` | Field constraints and exhibitor identity rules. |
| `PersonTest` / `UniquePersonListTest` | Contact identity and collection uniqueness. |
| `ExhibitorAddIntegrationTest` | Parsing through logic, model, JSON saving/reloading, filtered-list duplicates, edit conflicts, and rejected commands leaving storage unchanged. |
| `JsonAdaptedPersonTest` | Stored-field validation, including company and contact method. |

Run the focused integration tests on Windows:

```powershell
.\gradlew.bat test --tests "*ExhibitorAddIntegrationTest"
```

Run the full tests, Checkstyle, and coverage before a pull request:

```powershell
.\gradlew.bat check coverage
```

On macOS use `./gradlew check coverage`. On Linux with no display, use `xvfb-run -a ./gradlew check coverage` with Xvfb installed. Backend add tests do not require a display; JavaFX UI tests do.

Inspect `build/reports/jacoco/coverage/html/index.html` locally. CI generates `build/reports/jacoco/coverage/coverage.xml` before uploading it to Codecov. Check changed-line coverage as well as overall coverage.

For interactive checks, use the [manual add scenarios](DeveloperGuide.md#testing-exhibitor-additions).

## Cross-feature regression checks

`ExhibitorAddIntegrationTest` runs add, company/tag filtering, view selection, list, ambiguous-name deletion, and indexed deletion together. It checks shared attributes and the saved records. It also verifies that legacy contacts without company data do not match company searches.

`FindFieldsTest` covers empty/overlong company criteria and rejects internal phone tabs consistently with add. `ViewCommandParserTest` checks the shared name validator, including the 80-code-point Unicode boundary.

Manual check in a separate test folder:

1. Add two contacts with the same name, different companies and emails, and an optional method/tag on one.
2. Run `find c/COMPANY t/TAG` using that record's complete values; expect one match.
3. Run `view n/NAME`, then select each record in turn; check company, email, phone, method, and tags.
4. Run `list`; expect both records and a total of two (plus any pre-existing contacts).
5. Run `delete NAME`; expect ambiguity and no deletion. Filter by company, then `delete 1`; check the deleted record's company/method in feedback and confirm only that record is removed after restart.
