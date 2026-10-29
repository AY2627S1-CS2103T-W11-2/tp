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

`FindUnicodeTest` verifies that prefixed-name and keyword searches agree on Unicode case variants, including Turkish,
Greek, and supplementary letters. It also checks full-name and whole-word matching and same-field OR / cross-field AND.

`FindNameMatchingIntegrationTest` checks that `n/Alex` finds different surnames without matching `Alexandra`,
full-name queries still select the complete name, combined date/tag criteria retain OR/AND behavior, sorted indices
update the intended contact, and searches continue to work after storage reload.

`FindCompanyTagUnicodeIntegrationTest` checks Greek sigma, Turkish I and supplementary-letter case variants in company
and tag searches. It verifies complete-value matching, OR/AND combinations, matching after reload, and preservation
of stored company/tag spelling and JSON during search.

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

## Follow-up-date search regression checks

`FindFollowUpDateParserTest` checks valid leap dates and years 0001/9999, rejects impossible dates, non-YYYYMMDD
formats and empty values, and verifies repeated-date OR criteria combined with another field.
`FindFollowUpDateIntegrationTest` runs the real command parser, model, and JSON storage together. It verifies
multiple contacts on one date, no matches, absent dates (including legacy records), cross-field AND, searching
all contacts rather than only previous results, sorted displayed indices, and unchanged results/data on errors.
It also verifies that the existing `update` date replacement and clearing survive reload and affect later searches.

Manual check in a separate test folder:

1. Add two contacts with `f/20261025`, one with `f/20261026`, and one without a date, using distinct emails.
2. Run `find f/20261025`; expect only the two contacts on that date. Run `find f/20261027`; expect zero matches.
3. Run `find f/20261025 f/20261026`; expect three contacts. Add `c/COMPANY` or `t/TAG`; only contacts also matching that field should remain.
4. Run `list o/name`, then `find f/20261025`. Use `update 1 f/20261028`; verify that the first displayed contact, not the first stored contact, changed and its other fields were preserved.
5. Restart the app. `find f/20261028` must find the updated contact. Clear its date with `update NAME f/`, restart, and verify it no longer matches that date.
6. With valid results visible, try `find f/`, `find f/20230229`, and `find f/2026-10-25`. Expect errors, no data changes, and no change to the model's current filtered index set. Error text may replace the visible output.

## Storage recovery regression checks

`StorageRecoveryTest` checks null JSON roots, null contact/tag entries, invalid JSON, unchanged source bytes after failed reads, unique recovery backups, failed-copy retry, successful manual repair, and saves through the command layer. `StartupRecoveryTest` checks the real startup fallbacks for contacts and preferences. `MainWindowTest` checks the visible loading warning and continued command entry.

Manual check in a disposable folder: write `null` to `data/addressbook.json`, launch the JAR, and check the loading warning. Run `list`; verify that a same-directory `.recovery-` backup contains exactly `null`, while the active data file contains a valid empty collection. Restart and check ordinary startup. Keep all real contact files outside the test folder.

## Update regression checks

`UpdateIntegrationTest` exercises all eight supported fields through actual command routing and JSON storage. It checks preservation of omitted values, date/tag clearing, duplicate names, hidden duplicate contacts, filtered indices, normalisation, invalid values/prefixes, and unchanged memory/filter/disk on rejection. `TagIntegrationTest` covers `tag` toggling (adding new tags and removing existing ones), change summaries, target resolution, and rejection preservation. `MainWindowTest` checks update and tag feedback and rendered enquiry fields.

Manual check: add a contact, update its status/date by name, verify with `view`, then clear the date with `update NAME f/`. Add another contact with the same name but different company/email; an update by name must fail. Use `find` and its displayed index to update only the intended contact. Restart and verify the saved result.
