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

## Testing list sorting

| Test class | Coverage focus |
| --- | --- |
| `SortFieldTest` | Name, company, and status orders, case-insensitivity, tie-breaks, and contacts without a company placed last. |
| `ListCommandParserTest` | `list`, `list o/name`, `list o/company`, `list o/status`, and rejection of unknown, repeated, or extra parameters. |
| `ListCommandTest` | Sorted output, resetting to stored order, unchanged saved order, and `delete 1` acting on the first sorted contact. |
| `ModelManagerTest` | The sorted view, null comparators, and resetting the order. |

## Cross-feature regression checks

`ExhibitorAddIntegrationTest` runs add, company/tag filtering, view selection, list, ambiguous-name deletion, and indexed deletion together. It checks shared attributes and the saved records. It also verifies that legacy contacts without company data do not match company searches.

`FindFieldsTest` covers empty/overlong company criteria and rejects internal phone tabs consistently with add. `ViewCommandParserTest` checks the shared name validator, including the 80-code-point Unicode boundary.

`FindUnicodeTest` verifies that exact-name and keyword searches agree on Unicode case variants, including Turkish,
Greek, and supplementary letters. It also checks that full-name matching and same-field OR / cross-field AND rules
are preserved.

Manual check in a separate test folder:

1. Add two contacts with the same name, different companies and emails, and an optional method/tag on one.
2. Before `list`, run `find c/COMPANY t/TAG` using that record's complete values; expect one numbered match with all exhibitor details in the text output.
3. Run `view n/NAME`, then reply `1` or `2` to choose between the same-name records. Check company, email, phone, method, and tags; repeat for the other record. All choices and details appear in text; no contact panels appear.
4. Run `list`; expect both records and a total of two (plus any pre-existing contacts). There must be no graphical contact list or details panel. Selection uses `view` and numbered replies.
5. Run `delete NAME`; expect ambiguity and no deletion. Filter by company, then `delete 1`; check the deleted record's company/method in feedback and confirm only that record is removed after restart.

Add-error regression coverage also checks grouped missing/repeated prefixes, unknown-prefix identification, empty fields, field-specific validation messages, and error precedence. `ExhibitorAddIntegrationTest` verifies that invalid input and a contact conflicting with two existing records preserve the complete contact list, current filter, and saved JSON.

`MainWindowTest` exercises the actual FXML and typed commands. It verifies an empty startup prompt with saved contacts intact, numbered find/list output, filtered edit/delete indices, view replies and cancellation, error recovery, keyboard focus/help/exit, and the absence of contact-list and details panels. Responsive UI previews are written to `build/reports/ui/cli-startup.png`, `cli-find.png`, `cli-list.png`, `cli-view.png`, and `cli-compact.png`.

Tag-order regression coverage runs `edit` with repeated tags, verifies first-occurrence order after JSON reload, and checks the same order in edit, list, and view feedback. Unsupported edit prefixes must leave the contact and saved JSON unchanged.

Enquiry-field regression coverage: `EnquiryFieldsTest` checks status parsing, date boundaries/leap years, defaults, equality, and duplicate identity. `ExhibitorAddIntegrationTest` covers optional combinations, invalid input without mutation, JSON reload, and preservation through edit. `JsonAdaptedPersonTest` checks legacy defaults and invalid stored values. `MainWindowTest` checks both fields in rendered contact cards.

## Update regression checks

`UpdateIntegrationTest` exercises all eight supported fields through actual command routing and JSON storage. It checks preservation of omitted values, date/tag clearing, duplicate names, hidden duplicate contacts, filtered indices, normalisation, invalid values/prefixes, and unchanged memory/filter/disk on rejection. `MainWindowTest` checks update feedback and rendered enquiry fields.

Manual check: add a contact, update its status/date by name, verify with `view`, then clear the date with `update NAME f/`. Add another contact with the same name but different company/email; an update by name must fail. Use `find` and its displayed index to update only the intended contact. Restart and verify the saved result.
