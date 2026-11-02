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
| `ExhibitorAddIntegrationTest` | Parsing through logic, model, JSON saving/reloading, filtered-list duplicates, update conflicts, and rejected commands leaving storage unchanged. |
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

`ConcurrentSessionIntegrationTest` loads two independent sessions from one file, saves a contact in the first, and
verifies that a read-only `list` in the stale session does not write. A later stale mutation must be rejected and
rolled back while the newer contact remains on disk. `JsonAddressBookStorageTest` also checks this conflict directly
at the storage boundary.

Manual check in a separate test folder:

1. Add two contacts with the same name, different companies and emails, and an optional method/tag on one.
2. Before `list`, run `find c/COMPANY t/TAG` using that record's complete values; expect one numbered match with all exhibitor details in the text output.
3. Run `view n/NAME`, then reply `1` or `2` to choose between the same-name records. Check company, email, phone, method, and tags; repeat for the other record. All choices and details appear in text; no contact panels appear.
4. Run `list`; expect both records and a total of two (plus any pre-existing contacts). There must be no graphical contact list or details panel. Selection uses `view` and numbered replies.
5. Run `delete NAME`; expect ambiguity and no deletion. Filter by company, then `delete 1`; check the deleted record's company/method in feedback and confirm only that record is removed after restart.

Add-error regression coverage also checks grouped missing/repeated prefixes, unknown-prefix identification, empty fields, field-specific validation messages, and error precedence. `ExhibitorAddIntegrationTest` verifies that invalid input and a contact conflicting with two existing records preserve the complete contact list, current filter, and saved JSON.

`MainWindowTest` exercises the actual FXML and typed commands. It verifies an empty startup prompt with saved contacts intact, numbered find/list output, filtered update/delete indices, view replies and cancellation, error recovery, keyboard focus/help/exit, and the absence of contact-list and details panels. UI previews are written to `build/reports/ui/`: `cli-startup.png`, `cli-find.png`, `cli-find-date.png`, `cli-list.png`, `cli-view.png`, `cli-status.png`, `cli-compact.png`, and `cli-recovery-warning.png`, plus the User Guide screenshots `ug-startup.png`, `ug-add.png`, `ug-list.png`, `ug-view.png`, `ug-find.png`, and `ug-delete.png`.

Tag-order regression coverage runs `update` with repeated tags, verifies first-occurrence order after JSON reload, and checks the same order in update, list, and view feedback. Unsupported update prefixes must leave the contact and saved JSON unchanged.

Enquiry-field regression coverage: `EnquiryFieldsTest` checks status parsing, date boundaries/leap years, defaults, equality, and duplicate identity. `ExhibitorAddIntegrationTest` covers optional combinations, invalid input without mutation, JSON reload, and preservation through update. `JsonAdaptedPersonTest` checks legacy defaults and invalid stored values. `MainWindowTest` checks both fields in rendered contact cards.

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

Manual check in a disposable folder:

1. Write `null` to `data/addressbook.json`, launch the JAR, and check the loading warning.
2. Run `list`; verify that the original file still contains `null`.
3. Run `add n/Recovery Check c/Acme e/recovery@example.com p/91234567`.
4. Verify that a same-directory `.recovery-` backup contains exactly `null` and the active JSON contains the new contact.
5. Restart the app and run `view n/Recovery Check`; check that the saved contact loads normally.

Keep all real contact files outside the test folder.

## Update regression checks

`UpdateIntegrationTest` exercises all eight supported fields through actual command routing and JSON storage. It checks preservation of omitted values, date/tag clearing, duplicate names, hidden duplicate contacts, filtered indices, normalisation, invalid values/prefixes, and unchanged memory/filter/disk on rejection. `TagIntegrationTest` covers `tag` toggling (adding new tags and removing existing ones), change summaries, target resolution, and rejection preservation. `MainWindowTest` checks update and tag feedback and rendered enquiry fields.

Manual check: add a contact, update its status/date by name, verify with `view`, then clear the date with `update NAME f/`. Add another contact with the same name but different company/email; an update by name must fail. Use `find` and its displayed index to update only the intended contact. Restart and verify the saved result.

## Relative follow-up search regression checks

Use these checks when changing date criteria, filtered indices, or command saving.

| Test class | Scenarios |
| --- | --- |
| `RelativeFollowUpFilterTest` | Today/due/overdue boundaries, keyword case, absent dates, combined criteria, time zones, and a search held across midnight. |
| `RelativeFollowUpWorkflowTest` | Real command parsing, displayed-index updates, tag changes, save rollback, cancelled view selection, and JSON reload. |
| `FindFollowUpDateParserTest` | Literal date validation and repeated date criteria. |
| `HelpCommandTest` | Command headings remain readable when the system language is Turkish. |

Run the relative-date tests on Windows:

```powershell
.\gradlew.bat test --tests "*RelativeFollowUpFilterTest" --tests "*RelativeFollowUpWorkflowTest"
```

On macOS or Linux, replace `.\gradlew.bat` with `./gradlew`.
The workflow tests create a temporary contact file for each test.
They exercise `LogicManager` and read the saved JSON back through `JsonAddressBookStorage`.
The failed-save test injects an `IOException`, so it can check rollback without changing filesystem permissions.

### Date boundary expectations

Let D be the computer's current local calendar date.
Use actual dates in `YYYYMMDD` format when creating contacts.

| Contact's follow-up date | `find f/today` | `find f/due` | `find f/overdue` |
| --- | --- | --- | --- |
| D minus one day | Excluded | Included | Included |
| D | Included | Included | Excluded |
| D plus one day | Excluded | Excluded | Excluded |
| No date | Excluded | Excluded | Excluded |

Combine `f/due` with `s/Needs follow-up` to check both the date and enquiry status.
Repeat `f/` to check alternatives, such as `find f/overdue f/YYYYMMDD`.
Replace `YYYYMMDD` with a real date before running that command.

The unit tests use fixed clocks for repeatable calendar boundaries.
The midnight test advances its clock by one day after constructing a search predicate.
It checks that the active predicate keeps its original date and a newly constructed predicate uses the next date.
For a manual check across midnight, rerun the search to refresh its reference date.

### Manual workflow

Use a disposable app folder and four contacts with distinct emails:

| Contact | Follow-up date | Status | Tag |
| --- | --- | --- | --- |
| Alicia Tan | Yesterday | Needs follow-up | urgent |
| Ben Lim | Today | In progress | vip |
| Cora Lim | Tomorrow | Completed | None |
| Dora Lim | None | Needs follow-up | None |

1. Add Cora, Ben, Dora, and Alicia in that order, using `add` and the dates above.
2. Run `list o/name`, then `find f/due`; expect Alicia followed by Ben.
3. Run `update 2 p/89999999`; verify Ben's phone changed and Alicia's phone stayed the same.
4. Run `find f/overdue`, then `tag 1 t/review`; verify Alicia received the new tag.
5. Run `find f/due s/In progress`; expect Ben only.
6. Run `update 1 f/YYYYMMDD` with tomorrow's date, then rerun `find f/today`; expect zero matches.
7. Run `find f/overdue`, then `update 1 f/`; rerun `find f/due` and expect zero matches.
8. Restart, then search for tomorrow's literal date; expect Cora and Ben with their saved details.

### Rejected commands and selection state

Start with valid date results and record the displayed contacts and saved JSON.
Try `find f/tomorrow`, `find f/due soon`, and `find f/over-due`.
Each command should report an error and preserve the filtered index set and saved data.
Try `update 1 f/today`; the update should reject the relative keyword and preserve the contact.

Run `view n/Lim` to request a numbered choice between the Lim contacts.
Before replying, run `find f/today`.
A subsequent bare `2` should report an invalid command instead of selecting from the previous view choices.

After changing these workflows, run `check coverage` and inspect both test failures and changed-line coverage.

### Cross-feature follow-up checks

`RelativeFollowUpWorkflowTest` also checks the following transitions:

| Starting point | Action | Expected result |
| --- | --- | --- |
| Due contacts sorted by name | Delete the second displayed contact | The correct saved contact is removed; restarting preserves that deletion. |
| Due contacts sorted by name | Rename the second contact to an existing identity | The update fails, preserving all contacts, the filter, order, and saved JSON. |
| Overlapping relative and literal date alternatives | Run the combined search | Each matching contact appears once. |
| One matching contact visible | Run `clear confirm` | The complete collection, including hidden contacts, is cleared and remains empty after restart. |
| An overdue search is active | Add an overdue contact | Adding restores all contacts; rerunning the search includes the new contact. |
| A second session has changed the saved file | Delete from the stale session's due results | Saving fails; the stale model rolls back and the newer disk contents remain intact. |
| A filtered, sorted session is closed | Restart and rerun its date search | Saved contacts load in stored order and the search returns the expected subset. |
| Due results are active and saving fails | Delete or clear | Hidden contacts and displayed indices are restored; the file bytes remain unchanged. |

For the two-session check, open both sessions before changing either one's data.
Save a phone update in the first session, then attempt a deletion in the second.
Check the phone value from a newly opened session after the rejected deletion.

When recording a manual failure, include the app version, operating system, local date,
starting contacts, commands in order, expected result, and actual result.
Use disposable data and remove real contact details from screenshots and attached JSON.
If the failure concerns saved data, keep copies from before and after the command.

### Inclusive date range checks

`FollowUpDateRangeTest` checks both included endpoints, dates outside the range, equal endpoints,
leap days, year boundaries, invalid intervals, and defensive copying of search criteria.
The workflow tests exercise ranges through the real parser, model, and JSON storage.

1. Create contacts dated 1, 4, 7, and 8 November 2026, plus a contact with no date.
2. Run `find f/20261101..20261107`; expect the first three dated contacts.
3. Add `s/Needs follow-up`; expect only contacts in the range with that status.
4. Repeat an overlapping `f/` range; check that each matching contact appears once.
5. Run `find f/20261107..20261101` and `find f/20261101..`; expect errors and unchanged data.
6. Sort by name, search the range, then update a displayed index and verify the intended contact after restart.
