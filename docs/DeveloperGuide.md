---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* Based on the [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org), which provided the initial architecture, code structure, and documentation templates.
* Third-party libraries and build tools:
  * [JavaFX](https://openjfx.io/) for the desktop user interface.
  * [Jackson](https://github.com/FasterXML/jackson) (`jackson-databind` and `jackson-datatype-jsr310`) for JSON serialisation and deserialisation.
  * [JUnit 5](https://junit.org/junit5/) for automated unit and integration testing.
  * [Gradle](https://gradle.org/), [Shadow](https://gradleup.com/shadow/), [Checkstyle](https://checkstyle.org/), and [JaCoCo](https://www.jacoco.org/jacoco/) for build automation, JAR packaging, style verification, and test coverage reporting.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](../src/main/java/seedu/boothmanagerpro/Main.java) and [`MainApp`](../src/main/java/seedu/boothmanagerpro/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" alt="Architecture component interactions for delete 1" width="600" style="max-width: 100%; height: auto;" />

For this command, `Logic` obtains the contacts in their current displayed order from `Model` and resolves `1` to the
first displayed contact. After deleting that contact, `Logic` obtains the updated address book and asks `Storage` to
save it. The resulting `CommandResult` is then returned to `UI`, which displays the feedback to the user.

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](../src/main/java/seedu/boothmanagerpro/ui/Ui.java)

The command interface is composed of `MainWindow`, `CommandBox`, `ResultDisplay`, `StatusBarFooter`, and `ContactCard`. Help is displayed in the same output area.

<img src="images/UiClassDiagram.png" width="550"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, and `StatusBarFooter`. `ResultDisplay` shows the contacts returned by `list`, `find`, and `view` as static `ContactCard`s, and displays text feedback in its `TextArea` for all other results. All of these UI components, including `MainWindow` and `ContactCard`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](../src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](../src/main/java/seedu/boothmanagerpro/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* displays each `CommandResult` returned by `Logic.execute()` in `ResultDisplay`: non-empty `list`, `find`, and single-contact `view` results render static `ContactCard`s beneath a summary heading (with a total count below `list` cards), while confirmations, `help`, numbered `view` prompts, empty results, and errors appear as text.
* keeps a reference to the `Logic` component, because the `UI` relies on `Logic` to execute commands.
* depends on classes in the `Model` component (`Person` and `EnquiryStatus`), because `ContactCard` renders a contact's attributes and coloured status badge directly from a `Person` object.

#### UI extension points

| Component | Responsibility |
| --- | --- |
| `MainWindow.fxml` / `BoothManagerPro.css` | Command-driven layout, colours, spacing, and a single output area that fills available space. |
| `ContactCard.fxml` / `ContactCard` | A static, read-only card for one contact, shown by `ResultDisplay` when a `CommandResult` carries contacts to show. The enquiry status appears as a badge beside the name; `ContactCard` adds a `status-*` style class (e.g. `status-in-progress`) to the badge and card, and `BoothManagerPro.css` colours both. |
| `CommandBox` | Typed input through `Logic.execute()` with `Tab` autocomplete; successful input clears, failed input remains editable. |
| `ResultDisplay` | Summary heading, scrollable static `ContactCard` container (with an optional total label for `list`), read-only `TextArea` output, and error styling. Each result replaces the previous output. |

- Startup shows only a ready prompt; contacts are loaded into the model without being displayed.
- There are no separate persistent contact-list or details side panels, no contact-selection listeners, and no visibility flag; `ContactCard`s are static and non-selectable.
- Non-empty `list` and `find` results render numbered `ContactCard`s; `view` renders an unnumbered `ContactCard` for a single or selected match, or displays numbered text choices when multiple contacts match.
- `Enter` executes commands, `Tab` autocompletes unambiguous input or moves focus between the command box and output area when no completion applies, `Ctrl+L` (`Command+L` on macOS) focuses command input, and `F1` displays help.
- `MainWindowTest` checks FXML, `ContactCard` rendering and text output, filtered indices, keyboard controls, persistence, and the absence of persistent side panels. It writes previews to `build/reports/ui/`; Linux needs a display such as Xvfb.

### Logic component

**API** : [`Logic.java`](../src/main/java/seedu/boothmanagerpro/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component. `LogicManager` owns one `AddressBookParser`, which creates
the command-specific parser and command required for the user's input. The concrete command operates on `Model` and
creates a `CommandResult`, while `LogicManager` coordinates persistence through `Storage`.

<img src="images/LogicClassDiagram.png" alt="Partial class diagram of the Logic component" width="700" style="max-width: 100%; height: auto;" />

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<img src="images/DeleteSequenceDiagram.png" alt="Interactions inside Logic for the delete 1 command" width="950" style="max-width: 100%; height: auto;" />

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. `LogicManager` passes `delete 1` to `AddressBookParser`. It creates a `DeleteCommandParser`, which parses the
   argument as an `Index` and creates a `DeleteCommand`.
1. `LogicManager` executes the `DeleteCommand` with `Model`.
1. `DeleteCommand` obtains the filtered and sorted contact list from `Model`, resolves the one-based displayed index
   to `personToDelete`, and asks `Model` to delete that `Person`.
1. `DeleteCommand` creates and returns a `CommandResult` containing the deleted contact's details.
1. `LogicManager` obtains the updated address book from `Model` and asks `Storage` to save it.
1. After persistence succeeds, `LogicManager` returns the `CommandResult` to its caller.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](../src/main/java/seedu/boothmanagerpro/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list, wrapped in a _sorted_ view that `list o/FIELD` can reorder. It exposes this view as an unmodifiable `ObservableList<Person>` used by commands for filtering, numbered output, and indexed updates, tag toggling, and deletion.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](../src/main/java/seedu/boothmanagerpro/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).

Invalid JSON objects and null contact/tag entries produce controlled loading errors. `MainApp` falls back to an empty contact collection and passes a visible loading warning to the UI; invalid preferences fall back to defaults. Optional legacy contact fields retain their existing defaults.

`JsonAddressBookStorage` records failed loads using `DataFileRecovery`. Before the first subsequent save to that path, it copies the original bytes to a unique `.recovery-` JSON file in the same directory. Backup failure blocks the save and retains recovery protection for a retry. A successful backup or a successful reload clears that protection. Recovery files are never automatically loaded or overwritten; users must close the app and repair/restore data manually. This protection does not coordinate concurrent app instances.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.boothmanagerpro.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how key features are implemented.

### Command-only contact display

`MainWindow` contains `CommandBox`, `ResultDisplay`, and `StatusBarFooter`, with no separate persistent contact-list or
details side panels. Startup displays only a ready prompt in `ResultDisplay`'s `TextArea`. The model still loads all saved
contacts, without any persistent contact-panel visibility state.

`ListCommand` restores the full model filter, applies or resets the sort order, and returns
`CommandResult.showingContactList(...)` with both formatted text feedback and the displayed `List<Person>`.
`FindCommand` updates the filter and returns `CommandResult.showingContacts(...)` with the match count, formatted text,
and matching `List<Person>`. Both format text using `Messages.formatNumberedContacts()` and number their cards using the
same one-based indices accepted by `update`, `tag`, and `delete`. Empty `list` and `find` results return an empty
`contactsToShow` list and display a message without cards.

`CommandResult` carries `feedbackToUser`, an `exit` flag, an immutable `viewChoices` snapshot when awaiting a numbered
`view` reply, an immutable `contactsToShow` snapshot, and `numberedContacts` / `showingContactTotal` flags. `MainWindow`
passes `feedbackToUser` and the contact snapshot to `ResultDisplay`: when `contactsToShow` is non-empty, `ResultDisplay`
renders scrollable static `ContactCard`s beneath its summary heading (plus the total count below `list` cards);
otherwise, it shows the read-only `TextArea`. Each command replaces the previous output rather than appending a command
history transcript.

`add`, `update`, and `tag` reset the model filter, and `delete` shifts subsequent indices. Users should run `list` or
`find` again before choosing another index. A restart preserves saved contacts and resets the output to the ready prompt.

Regression tests cover startup/restart, `find` before `list`, rendered `ContactCard`s and text output, filtered
`update`/`tag`/`delete` indices, `view` replies, errors, persistence, and keyboard-only operation. UI snapshots are
generated as `build/reports/ui/cli-*.png`.

### Viewing an exhibitor contact

`view n/NAME` displays a contact's full details and supports a numbered reply when several contacts match. Matching
behaves like unprefixed `find`: case-insensitive whole-name keywords combined with OR.

`ViewCommandParser` enforces the specified name length and permitted characters, one `n/` field, and no other fields.
`ViewCommand` searches the full address book using `NameContainsKeywordsPredicate` and updates the filtered list to
those matches without modifying stored contacts. A unique match returns `CommandResult.showingContact(...)`, rendering
an unnumbered `ContactCard` in `ResultDisplay` alongside the formatted summary. If exactly one match has a full name
equal to the query, ignoring case, `ViewCommand` filters to and displays that contact directly, so overlapping names
such as `Alex Yeo` and `Alex Yeoh` can each be viewed without a numbered reply. Multiple matches return a numbered
name-and-company prompt plus an immutable `CommandResult.viewChoices` snapshot displayed as text in `ResultDisplay`.

`LogicManager` retains those choices and their prompt for the next numbered reply. Invalid replies report the valid
range followed by the numbered prompt again, keeping the choices available. A successful selection filters to the
selected contact and returns `CommandResult.showingContact(...)` to render its `ContactCard`. Another successfully
parsed command clears the pending choices, even if execution fails, whereas invalid command syntax preserves them.
If the selected record has changed or disappeared, the user must run `view` again. Each `LogicManager` holds its own
selection state. Viewing and numbered replies bypass storage writes, because neither changes contact data.

Both the numbered choice prompt and the resulting `ContactCard` use the same `ResultDisplay` output area. A new
command's output replaces the previous prompt or card, requiring no persistent selection widget or list listener.

#### Shared contact attributes

All contact workflows share `Person` and its validated value objects.

| Attribute | Shared contract |
| --- | --- |
| Name | `Name` owns validation: 1-80 Unicode code points, at least one letter, supported punctuation, and `s/o` or `d/o` between words. `view` uses the same validator as `add` and prefixed `find`. |
| Company | `Optional<Company>`; required by `add`, absent in legacy records. `find c/` matches the complete value ignoring case. |
| Email / phone | Stored lowercase email and normalised phone; search validates through the same value objects as `add`. |
| Contact method | `Optional<ContactMethod>`; `email`, `phone`, or `other`. An absent method is shown as `Not specified`. Not a search criterion. |
| Follow-up date | `Optional<FollowUpDate>`; validated and stored in JSON as `YYYYMMDD`, formatted as `d MMM uuuu` (e.g. `25 Oct 2026`) for display, and shown as `None` when absent. |
| Tags | Exact duplicates collapse on `add`. Search ignores case and matches whole tags. `add`, `update`, `tag`, storage, and command feedback preserve stored order. Empty tags show `None` in text and `No tags` on cards. |
| Storage | JSON stores `company`, `contactMethod`, `enquiryStatus`, and `followUpDate`; absent optional fields become empty optionals or default to `Needs follow-up`. The legacy address remains for compatibility. |

| Feature | Selection / result |
| --- | --- |
| Add | Rejects normalised-email or case-insensitive name/company duplicates across all records. |
| Delete | Full-name match across all records, or index in the displayed list. Ambiguous names require an index; feedback prints the deleted contact's exhibitor details without its legacy address. |
| View | Whole-name keywords with OR; a unique exact full name shows that contact's card directly, otherwise multiple matches prompt for a numbered selection. |
| List | Restores every contact and displays numbered `ContactCard`s plus total; optionally sorts by name, company, or enquiry status (`o/name`, `o/company`, `o/status`); rejects other arguments. |
| Find | Full names or whole name words and complete other field values, OR within each field and AND across fields; unprefixed keywords retain name-word matching. |

`update` preserves unmodified fields, rejects unsupported prefixes, and replaces tags in input order; `tag` toggles
individual tags while preserving all other fields. `Messages.formatContactDetails()` supplies the shared text format,
 while `Messages.formatTags()`, `Messages.formatFollowUpDate()`, and `Messages.formatOptional()` keep placeholders
consistent across text feedback and `ContactCard`.

Tests cover persisted exhibitor records, numbered selection and retries, cancelled/stale choices, invalid syntax,
matching parity with `find`, legacy records, data preservation, and `ContactCard` output. The JavaFX regression test
covers same-name contacts at different companies, numbered replies, and cancellation.

### Updating an exhibitor contact

`update NAME_OR_INDEX [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...`

1. `AddressBookParser` routes `update` to `UpdateCommandParser`, which validates the target, field cardinality, and supplied values.
2. `ContactFieldParser` shares prefix-specific validation feedback with `add`; domain value types remain the source of constraints and normalisation.
3. Immutable `ContactUpdate` represents partial changes. An absent optional preserves the value; a present empty date or tag set explicitly clears it. Tags are defensively copied.
4. `UpdateCommand` resolves an exact case-insensitive full name across all contacts or a one-based index in the current displayed list. Missing or ambiguous targets fail before mutation.
5. The command constructs a replacement `Person` via `ContactUpdate#applyTo` and checks every other contact for duplicate email or name/company, including hidden records. Target resolution, duplicate warnings, and successful updates are logged via `LogsCenter`. Only then does it call `Model#setPerson` and reset the filter.
6. `LogicManager` persists the updated address book through `Storage#saveAddressBook` (rolling back the in-memory model and filter if saving fails), and the UI displays a concise confirmation. Subsequent `view`, `list`, and `find` commands display the updated attributes on `ContactCard`s.

The sequence diagram below illustrates a successful `update` request, showing both index-based and name-based target resolution.

<img src="images/UpdateSequenceDiagram.png" alt="Interactions for updating an exhibitor contact" width="900" style="max-width: 100%; height: auto;" />

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UpdateCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

Legacy address is preserved. Supplied tags replace the tag set; a sole blank `t/` clears it. Empty `f/` clears the follow-up date. Other empty fields fail validation. There is no new storage schema or persistent contact ID.

`UpdateIntegrationTest` covers command routing, normalisation, partial updates, clearing, duplicate rejection, ambiguity, filtered indices, and persisted JSON. Rejected requests preserve contacts, filters, and disk content. `MainWindowTest` checks confirmation, updated cards, and retained input after invalid updates.

### Updating contact tags

`tag NAME_OR_INDEX t/TAG [t/TAG]...`

1. `AddressBookParser` routes `tag` to `TagCommandParser`, which rejects unsupported prefixes, requires a target and at least one `t/TAG` field, and validates each tag through `ContactFieldParser` and `Tag`. Duplicate `t/TAG` inputs in the same command collapse in input order.
2. `TagCommand` follows the same execution flow illustrated in the update sequence diagram above: `resolveTarget` resolves a one-based index against `Model#getFilteredPersonList()` or an exact case-insensitive full name against `Model#getAddressBook()`, failing before mutation when the target is missing or ambiguous.
3. Unlike `UpdateCommand`, `TagCommand` does not construct a `ContactUpdate` or run `rejectDuplicates`, because toggling tags never alters email, name, or company identity. Instead, each supplied tag is toggled against a copy of the contact's existing tag set using `Tag#equals` (case-sensitive): absent tags are appended and recorded as added, while present tags are removed and recorded as removed.
4. `TagCommand` constructs a replacement `Person` with all other attributes preserved, calls `Model#setPerson`, resets the filtered list, and returns `Updated tags for NAME at COMPANY. Added: .... Removed: .... Current tags: ....` (`None` when a tag list is empty), after which `LogicManager` saves the updated address book.
5. `TagIntegrationTest` and `MainWindowTest` verify toggling, exact change summaries, target resolution, persistence, and unchanged state on rejection.

### Adding an exhibitor contact

`add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...`

1. `AddressBookParser` routes `add` to `AddCommandParser`.
2. The parser rejects unknown prefixes, preambles, repeated non-tag fields, and missing required prefixes.
3. Value objects validate fields, normalise Unicode and whitespace in names/companies/tags, lowercase email, and remove phone spaces/hyphens. Repeated tags are ignored.
4. `AddCommand` checks the complete address book for matching email or name/company, logging execution, duplicate rejections, and successful additions via `LogsCenter`, then adds the contact or reports the existing match.
5. `LogicManager` saves through `JsonAddressBookStorage` before returning the success result, rolling back the in-memory model and filter if saving fails.
6. The result contains a concise identity confirmation. `MainWindow` displays it in `ResultDisplay` and focuses command input. `CommandBox` clears successful input and retains failed input. A later `view`, `find`, or `list` displays the saved contact as a `ContactCard`.

Enquiry fields:

- `EnquiryStatus` validates the four case-insensitive labels; omission defaults to `NEEDS_FOLLOW_UP`.
- `FollowUpDate` wraps `LocalDate`, validates real calendar dates in `YYYYMMDD` (years 0001-9999), formats display output as `d MMM uuuu` (e.g. `25 Oct 2026`), and accepts past dates. Omission is `Optional.empty()`. No time or timezone is stored.
- JSON stores `enquiryStatus` as its display label and `followUpDate` as its `YYYYMMDD` string (`toIsoDate()`) or `null`. Missing/null fields in older files receive the defaults; invalid non-null values are rejected.
- The `update` command changes status/date; empty `f/` clears the date. No transition order is enforced.
- Neither field changes duplicate identity. `list`/`find`/`view` output and `ContactCard`s display both fields.

Error-handling responsibilities:

- **Parser:** tokenises, checks structure, then converts values. Missing/repeated prefixes are grouped by error type in a stable order; empty and invalid values identify their prefix.
- **Value objects:** own validation and normalisation. `parseValue` invokes the same constructors used by storage and adds field context to constraint messages.
- **Model:** `Person.createExhibitor()` owns the legacy address default; the collection independently enforces uniqueness.
- **Command:** collects every duplicate before addition. One match retains the existing warning; multiple matches list each identity/email. Parse and duplicate rejection preserve contacts, the filter, and saved JSON.
- **Storage:** converts validated contacts to and from JSON.

Integration notes:

- `Person.getCompany()` and `getContactMethod()` return optional values. New additions require company; legacy records may lack it. Omitted method displays as `Not specified`.
- Name/company duplicate matching ignores case and normalises Unicode (NFC); contacts without a company match only by normalised email.
- The legacy address field remains for compatibility, using `Not provided` for new contacts. `add` no longer accepts `a/`.
- Single-letter prefix tokens are reserved except `s/o` and `d/o` between name words. Other words between prefixes belong to that field, allowing multi-word names and companies.
- JSON includes `company` and `contactMethod`; legacy files without them are accepted if their other values meet current validation rules.
- `list`, `find`, and `view` output includes company and contact method. Logic tests run without a display server.

Verification: see [automated add tests](Testing.md#testing-exhibitor-additions) and [manual add checks](#testing-exhibitor-additions). These cover US-01, US-05, US-07, US-16, and US-18 for the add workflow.

### Sorting the contact list

`list o/name`, `list o/company`, and `list o/status` sort the displayed contacts. Plain `list` shows them in stored order, which is
the order they were added.

**How it works**

1. `AddressBookParser` passes the arguments of `list` to `ListCommandParser`. Blank arguments produce a `ListCommand`
   with no sort field. Otherwise, the parser tokenises `o/`, rejects any preamble or repeated `o/`, and converts the
   value to a `SortField` with `SortField.fromKeyword()`, ignoring case. Unknown values are rejected with
   `SortField.MESSAGE_CONSTRAINTS`.
2. `ListCommand#execute()` clears the filter, then calls `Model#updateSortedPersonList(comparator)` for a sort field,
   or `Model#resetPersonListOrder()` for plain `list`.
3. `ModelManager` wraps its `FilteredList` in a JavaFX `SortedList`. `getFilteredPersonList()` returns the sorted
   view, so every command that reads indices, such as `delete`, `tag`, and `update`, uses the order the user sees.
   A `null` comparator keeps the stored order.

The sequence diagram below shows how `list o/name` is executed.

<img src="images/ListSortSequenceDiagram.png" width="750" />

`SortField` defines each order as a `Comparator<Person>`:

| Field | Order |
| --- | --- |
| `NAME` | Name ignoring case, then company (contacts without a company last), then email. |
| `COMPANY` | Company ignoring case with contacts without a company last, then name, then email. |
| `STATUS` | Enquiry status in workflow order (the declaration order of `EnquiryStatus`: Needs follow-up, In progress, Completed, Cancelled), then name, then email. |

Email is unique per contact, so the final email comparison means no two contacts tie and the displayed indices are
stable between runs.

Status uses workflow order rather than alphabetical order, which would place `Completed` first. Adding a new
`EnquiryStatus` value therefore needs it declared in the right position to sort correctly.

The comparator stays on the `SortedList` until the next plain `list`. Filters from `find` and `view`, and the filter
reset after `add`, `update`, and `tag`, apply beneath it, so their results keep the chosen order.

**Design considerations**

* **Alternative 1 (current choice):** Sort a view of the filtered list.
  * Pros: Saved data and the "date added" order are untouched. Indices always match the displayed order.
  * Cons: The sort order is session state that later commands inherit, which users need to be told about.
* **Alternative 2:** Sort the `UniquePersonList` in `AddressBook`.
  * Pros: Simple, with no extra model state.
  * Cons: Changes the saved file order and permanently loses the order in which contacts were added.
* **Alternative 3:** Sort only the output inside `ListCommand`.
  * Pros: No model changes.
  * Cons: Displayed numbers would no longer match the indices used by `delete`, `tag`, and `update`.

### Finding and filtering contacts

`find` supports legacy unprefixed name keywords (`find KEYWORD...`) as well as prefixed criteria for full names or
whole name words (`n/`), and complete company (`c/`), email (`e/`), phone (`p/`), tag (`t/`), and follow-up date
(`f/YYYYMMDD`) values, with OR within a field and AND across fields.

`FindCommandParser` routes unprefixed queries to `NameContainsKeywordsPredicate` for compatibility, and builds a
`ContactMatchesFieldsPredicate` when field prefixes are supplied. It rejects empty arguments, unprefixed text preceding
field prefixes, blank field values, and unsupported prefixes such as `s/` and `m/` (enquiry-status filtering remains
planned). Name, company, and tag criteria apply NFC Unicode normalisation and per-code-point case folding consistent
with `String#equalsIgnoreCase`. Name criteria collapse repeated whitespace and match either the complete name or any
whole name word, whereas stored company and tag spelling is preserved and matched as complete values. `FindCommand`
applies the predicate to the full address book through `Model#updateFilteredPersonList` and returns
`CommandResult.showingContacts(...)`, rendering numbered `ContactCard`s directly without requiring a preliminary `list`.

For follow-up date criteria, `FindCommandParser` reuses `FollowUpDate` via `ContactFieldParser#parseValue` to reject
invalid calendar dates before updating the filter. `ContactMatchesFieldsPredicate` compares the canonical `YYYYMMDD`
date string (`FollowUpDate#toIsoDate()`) against each contact's optional follow-up date; contacts without a follow-up
date never match. Repeated `f/` values are deduplicated and combined with OR, while `f/` combined with `n/`, `c/`,
`t/`, or another field uses AND. Existing `SortedList` order and displayed indices are preserved, and date updates or
clearing remain owned by `update`.

The class diagram below shows the main classes involved in follow-up-date search. `FindCommandParser` validates date
criteria using `FollowUpDate` and creates a `FindCommand` with `ContactMatchesFieldsPredicate`, which implements
`Predicate<Person>`.

![Classes involved in follow-up-date search](images/FindFollowUpDateClass.png)

The sequence diagram below illustrates a successful `find f/20261025` request. The command updates the filtered contact
list and returns the matching contacts. Shared validation helpers, predicate evaluation, result formatting, and
unchanged `LogicManager` persistence calls are omitted for clarity.

![Successful follow-up-date search](images/FindFollowUpDateSequence.png)

`FindFollowUpDateParserTest` covers date boundaries, leap years, repeated dates, invalid formats, and empty values.
`FindFollowUpDateIntegrationTest` covers exact and combined matches, zero results, undated legacy contacts, sorted
`update`/`delete` indices, preservation on rejected input, and `update` date changes/clearing across JSON reloads.


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

#### Target user profile

* Convention organisers managing many exhibitor representatives
* Comfortable typing commands for repeated contact-management tasks
* Need to store and retrieve contact information quickly

**Value proposition:** Manage exhibitor contacts through typed commands, with clear feedback and local storage. Contacts can record enquiry status and an optional follow-up date during addition. Startup shows no contacts; `list`, `find`, and `view` display requested contacts as static cards in a single output area, while confirmations, prompts, and errors appear as text.

### User stories

<!-- Owner: Gerard. Keep story IDs stable for cross-references. -->

Planned requirements for BoothManagerPro; implementation status is tracked separately. Here, **organiser** means convention organiser.

**Priorities:** `***` High (must have), `**` Medium (should have), `*` Low (nice to have).

- Priorities follow the feature list and explicit MVP requirements.
- Considered stories are candidates, not commitments.
- Out-of-scope stories are excluded regardless of priority.

#### Planned user stories

| ID | Priority | As a... | I want to... | So that... |
| --- | --- | --- | --- | --- |
| US-01 | `***` | organiser | add a representative's name, company, email, and phone | I have their essential contact details. |
| US-02 | `***` | organiser | delete outdated contacts | my records stay relevant. |
| US-03 | `***` | organiser | list all exhibitor contacts | I can review my records. |
| US-04 | `***` | organiser | view a contact's full details | I can contact the right representative. |
| US-05 | `***` | organiser | have duplicate contacts rejected with an explanation | I avoid repeated records. |
| US-06 | `***` | organiser | distinguish same-name contacts when viewing or deleting | I select the correct record. |
| US-07 | `***` | organiser | see clear errors for missing or invalid fields | I can correct my input. |
| US-08 | `**` | first-time organiser | see features and usage instructions | I know how to begin. |
| US-09 | `**` | organiser | update contact details | my records stay accurate. |
| US-10 | `**` | organiser | find contacts by full names or whole name words, company, email, or phone | I can locate contacts quickly. |
| US-11 | `**` | organiser | filter contacts by enquiry status | I can review each enquiry stage. |
| US-12 | `**` | organiser | combine search fields and alternatives | I can narrow my results. |
| US-13 | `**` | organiser | set enquiry status to Needs follow-up, In progress, Completed, or Cancelled | I can track progress. |
| US-14 | `**` | organiser | set a next follow-up date | I know when to contact someone again. |
| US-15 | `**` | organiser | view due or overdue follow-ups | I can prioritise outreach. |
| US-16 | `**` | organiser | tag contacts by industry, priority, or need | I can group exhibitors. |
| US-17 | `**` | organiser | filter contacts by tags | I can retrieve relevant groups. |
| US-18 | `**` | organiser | record a preferred contact method | I can use the appropriate channel. |
| US-36 | `**` | organiser | sort the contact list by name, company, or enquiry status | I can scan contacts in a predictable order and see who needs follow-up first. |

- **US-05:** Duplicates share a normalised email or a name-and-company combination.
- **US-11:** Enquiry-status filtering is planned. `list o/status` sorts all contacts by status; it does not filter them.
- **US-12:** Repeated values within a field use OR; different fields use AND.
- **US-15:** `find f/YYYYMMDD` matches one exact follow-up date, including a date supplied by the user for today. It does not automatically select today or include earlier dates. Automatic due or overdue filtering remains planned.

#### Considered user stories

| ID | Priority | As a... | I want to... | So that... | Scope note |
| --- | --- | --- | --- | --- | --- |
| US-19 | `*` | organiser | import contacts from CSV | I avoid manual entry. | Optional enhancement. |
| US-20 | `*` | organiser | export contacts to CSV | I can share records. | Optional enhancement. |
| US-21 | `*` | organiser | undo my last change | I can recover from mistakes. | Optional enhancement. |
| US-22 | `**` | organiser | record notes and booth requirements | I retain context for follow-ups. | Behaviour to be defined. |
| US-23 | `**` | organiser | link representatives from one organisation | I understand their relationships. | Beyond recording company names. |
| US-24 | `**` | organiser | mark a primary contact | I know whom to contact first. | Outside the selected feature list. |
| US-25 | `**` | organiser | record a confirmed exhibitor's booth number | I can reference their allocation. | Metadata only. |
| US-26 | `**` | fellow organiser | see who last contacted an exhibitor and when | I avoid duplicate outreach. | Contact-history design needed. |
| US-27 | `*` | sales team member | export confirmed exhibitors | I can prepare invoices elsewhere. | Depends on CSV export. |
| US-28 | `*` | operations staff member | receive exhibitor lists with booth numbers | I can plan services elsewhere. | Depends on export and booth metadata. |
| US-29 | `**` | system maintainer | back up and restore records | I can recover lost data. | Recovery design needed. |

#### Out-of-scope user stories

| ID | Priority | As a... | I want to... | So that... | Reason excluded |
| --- | --- | --- | --- | --- | --- |
| US-30 | `*` | compliance officer | control data access and exports | I can restrict sharing. | Multi-user permissions outside scope. |
| US-31 | `*` | organiser | design floor plans and allocate booths spatially | I can plan the layout. | Floor-plan management outside scope. |
| US-32 | `*` | sales team member | invoice exhibitors and process payments | I can collect booth fees. | Financial workflows outside scope. |
| US-33 | `*` | organiser | manage exhibitor contracts | I can administer agreements. | Contract management outside scope. |
| US-34 | `*` | organiser | sell tickets and manage admission | I can run registration. | Ticketing outside scope. |
| US-35 | `*` | operations staff member | schedule venue services and equipment | I can coordinate operations. | Logistics outside scope. |

Exhibitors benefit from accurate statuses and follow-ups (US-13 to US-15); they are not direct application users.

### Use cases

For all use cases below, the **System** is **BoothManagerPro** and the **Actor** is a **convention organiser**.
**Precondition:** The application is running.

A contact represents an exhibitor or an exhibitor representative.
**MSS** means **Main Success Scenario**; **extensions** describe alternative or unsuccessful interactions.

UC01–UC08 describe the implemented contact-management workflows. Refer to the [User Guide](UserGuide.md) for supported commands.
**Interaction rule:** All contact actions use typed commands. `list`, `find`, and `view` show matching contacts as static, non-interactive cards in the single output area; other results are text. There are no separate contact panels and no automatic startup listing.
**Guarantee:** Input-validation, target-resolution, and duplicate rejections leave contacts unchanged. If saving to disk fails, BoothManagerPro reports the storage error and rolls back the in-memory contacts and filter to their previous state.

#### UC01: Add an exhibitor contact

**MSS**

1. Organiser requests to add a contact, supplying the name, company, email, phone, and any optional contact method, enquiry status, follow-up date, or tags.
2. BoothManagerPro adds the contact, saves the updated contact data, and displays a confirmation containing its name and company.

Use case ends.

**Extensions**

* 1a. A required field is missing or a supplied value is invalid.
  * 1a1. BoothManagerPro explains the input error and rejects the request.

  Use case resumes at step 1.

* 1b. A contact with the same normalised email address or the same combination of name and company already exists.
  * 1b1. BoothManagerPro rejects the new contact and identifies the possible duplicate.

  Use case ends.

* 1c. The request contains an unsupported field, an unprefixed value, or a repeated field other than tags.
  * 1c1. BoothManagerPro explains the input error and rejects the request.

  Use case resumes at step 1.

#### UC02: View an exhibitor contact

**MSS**

1. Organiser requests to view a contact by name.
2. BoothManagerPro displays the matching contact's name, company, email, phone, preferred contact method, enquiry status, follow-up date, and tags.

Use case ends.

**Extensions**

* 1a. The name is missing or invalid.
  * 1a1. BoothManagerPro explains the input error.

  Use case resumes at step 1.

* 1b. No contact matches the supplied name, including when there are no stored contacts.
  * 1b1. BoothManagerPro informs the organiser that no matching contact was found.

  Use case resumes at step 1.

* 1c. More than one contact matches the supplied name (and none is a unique exact full-name match).
  * 1c1. BoothManagerPro displays the numbered matching contacts with their companies and asks the organiser to select one.
  * 1c2. Organiser types the corresponding number for the intended contact.
  * 1c3. If the selection is invalid, BoothManagerPro states the valid range, repeats the numbered choices, and requests another selection.

  Steps 1c2 to 1c3 repeat until a valid contact is selected.

  Use case resumes at step 2 for the selected contact.

* 1d. The view request contains an unsupported field, an unprefixed value, or a repeated name field.
  * 1d1. BoothManagerPro rejects the request and explains the accepted request format.

  Use case resumes at step 1.

#### UC03: Delete an exhibitor contact

**MSS**

1. Organiser requests to delete a contact using its displayed index.
2. BoothManagerPro resolves the index against the contacts in their current displayed order.
3. BoothManagerPro deletes the selected contact and saves the updated contact data.
4. BoothManagerPro displays the deleted contact's details.

Use case ends.

**Extensions**

* 1a. The argument is missing or is neither a valid name nor a positive integer.
  * 1a1. BoothManagerPro explains the input error.

  Use case resumes at step 1.

* 1b. Organiser supplies a full name instead of an index.
  * 1b1. BoothManagerPro searches all stored contacts for an exact full-name match, ignoring letter case.
  * 1b2. Exactly one contact matches.

  Use case resumes at step 3 for the matching contact.

* 1b1a. No contact matches the supplied full name, including when there are no stored contacts.
  * 1b1a1. BoothManagerPro informs the organiser that no matching contact was found.

  Use case resumes at step 1.

* 1b1b. More than one contact matches the supplied full name.
  * 1b1b1. BoothManagerPro rejects the deletion and asks the organiser to use a displayed index.
  * 1b1b2. Organiser runs `list`, optionally followed by `find`, to identify the intended contact.
  * 1b1b3. Organiser enters `delete INDEX` using its displayed index.

  Use case resumes at step 2. A bare number is not a delete confirmation.

* 2a. The supplied index is outside the currently displayed list.
  * 2a1. BoothManagerPro reports that the displayed index is invalid and does not delete or save anything.

  Use case resumes at step 1.

#### UC04: List exhibitor contacts

**MSS**

1. Organiser requests to list all exhibitor contacts, optionally sorted by name, company, or enquiry status.
2. BoothManagerPro displays all stored contacts with their names, companies, emails, phones, preferred contact methods,
   enquiry statuses, follow-up dates, and tags, in the requested order or in the order they were added, followed by the
   total number of contacts.

Use case ends.

**Extensions**

* 1a. The organiser supplies parameters other than a single sort field of name, company, or status.
  * 1a1. BoothManagerPro rejects the request and explains the accepted sort fields.

  Use case resumes at step 1.

* 1b. There are no stored contacts.
  * 1b1. BoothManagerPro informs the organiser that no contacts were found and suggests using the add command to add one.

  Use case ends.

#### UC05: Find and filter contacts

**MSS**

1. Organiser requests to find contacts using name keywords, or one or more name, company, email, phone, tag, or follow-up date criteria.
2. BoothManagerPro reports the number of matching contacts, updates the displayed contact index set, and displays each match with its index and contact details.

Use case ends.

**Matching rules**

* Values for the same field are combined using OR; criteria for different fields are combined using AND.
* Unprefixed keywords match whole name words using OR. Prefixed name criteria match a complete name or a single whole name word; other fields match complete values.
* Name, company, email, and tag comparisons ignore case and surrounding spaces; phone comparisons ignore spaces and hyphens; follow-up date criteria match the exact calendar date (`YYYYMMDD`) and exclude contacts without a date.
* Repeated identical search values are ignored.

**Extensions**

* 1a. No search keyword or field criterion is supplied.
  * 1a1. BoothManagerPro rejects the request and shows the expected command format.

  Use case resumes at step 1.

* 1b. A search field is supplied without a value.
  * 1b1. BoothManagerPro rejects the request and explains that search values cannot be empty.

  Use case resumes at step 1.

* 1c. A supplied search value does not meet the required format for its field (including invalid calendar dates).
  * 1c1. BoothManagerPro rejects the request and explains the required format without changing the existing results.

  Use case resumes at step 1.

* 1d. The request contains an unsupported field prefix or combines unprefixed text with prefixed criteria.
  * 1d1. BoothManagerPro rejects the request and explains the supported search fields.

  Use case resumes at step 1.

* 1e. No contacts match the search criteria.
  * 1e1. BoothManagerPro reports `0 contact(s) found.` without displaying contact cards.

  Use case ends.

#### UC06: Update an exhibitor contact

**MSS**

1. Organiser requests to update one or more fields of a contact identified by its exact full name or displayed index.
2. BoothManagerPro resolves the target contact, applies the supplied changes while preserving all omitted attributes, resets the contact filter, and saves the updated contact data.
3. BoothManagerPro displays a confirmation with the updated contact's name and company.

Use case ends.

**Extensions**

* 1a. The target is missing, no fields to update are supplied, an unsupported field prefix is used, a non-tag field is repeated, or a supplied value is invalid.
  * 1a1. BoothManagerPro explains the input error and rejects the request.

  Use case resumes at step 1.

* 1b. The supplied index is outside the currently displayed list, or no stored contact matches the supplied full name.
  * 1b1. BoothManagerPro informs the organiser that the target contact is invalid or could not be found, and makes no changes.

  Use case resumes at step 1.

* 1c. More than one stored contact matches the supplied full name.
  * 1c1. BoothManagerPro rejects the update and asks the organiser to run `list` or `find` and retry using the contact's displayed index.

  Use case resumes at step 1.

* 2a. Updating the contact would match another stored contact's normalised email address or name-and-company combination.
  * 2a1. BoothManagerPro identifies the conflicting contact and rejects the update without modifying stored data.

  Use case resumes at step 1.

#### UC07: Toggle contact tags

**MSS**

1. Organiser requests to toggle one or more tags on a contact identified by its exact full name or displayed index.
2. BoothManagerPro resolves the target contact, adds each supplied tag that is not yet on the contact, removes each supplied tag that is already on the contact, resets the contact filter, and saves the updated contact data.
3. BoothManagerPro displays the contact's name and company together with the added tags, removed tags, and resulting tags.

Use case ends.

**Extensions**

* 1a. The target is missing, no tag fields are supplied, an unsupported field prefix is used, or any tag value is blank or invalid.
  * 1a1. BoothManagerPro explains the input error and rejects the request.

  Use case resumes at step 1.

* 1b. The supplied index is outside the currently displayed list, or no stored contact matches the supplied full name.
  * 1b1. BoothManagerPro informs the organiser that the target contact is invalid or could not be found, and makes no changes.

  Use case resumes at step 1.

* 1c. More than one stored contact matches the supplied full name.
  * 1c1. BoothManagerPro rejects the request and asks the organiser to run `list` or `find` and retry using the contact's displayed index.

  Use case resumes at step 1.

#### UC08: Clear all contacts

**MSS**

1. Organiser requests to clear all contacts with explicit confirmation (`clear confirm`).
2. BoothManagerPro deletes all stored contacts, saves the empty address book, and confirms that all contacts have been cleared.

Use case ends.

**Extensions**

* 1a. Organiser enters `clear` without confirmation.
  * 1a1. BoothManagerPro warns that clearing deletes all contacts, instructs the organiser to enter `clear confirm`, and leaves all contacts intact.

  Use case resumes at step 1.

* 1b. Organiser supplies any argument other than `confirm`.
  * 1b1. BoothManagerPro explains the accepted command format and leaves all contacts intact.

  Use case resumes at step 1.

### Non-Functional Requirements

These are requirements for the intended product, not claims that every target has already been implemented or tested.

1. **Platform compatibility:** The application shall run on Windows, Linux, and macOS with Java `25` installed, without requiring another Java version or OS-specific software.
2. **Portable distribution:** The application shall be distributed as a single JAR and shall not require an installer. The distributed JAR shall not exceed 100 MB.
3. **Single-user, offline operation:** The application shall serve one convention organiser on their own computer. Core contact-management operations shall work without an Internet connection, a user account, or a remote server. Concurrent multi-user access and shared live data files are outside scope.
4. **Local, human-editable storage:** Contact data shall be stored locally in a human-editable text format such as JSON, without a DBMS. Successfully saved records shall remain available after a normal shutdown and restart.
5. **Performance:** The application should support at least 1,000 exhibitor contacts. As a proposed acceptance target, typical add, list, view, delete, and search operations should display their result within two seconds of command submission with a representative 1,000-contact dataset. Record the test machine specifications when measuring this target.
6. **Keyboard usability:** All core contact-management operations shall be accessible through typed commands. An organiser who types faster than average should be able to perform repeated contact-management tasks efficiently without switching to the mouse. This requirement concerns application operations, not OS window controls.
7. **Clear feedback and data integrity:** Invalid commands shall identify the input problem and leave stored contacts unchanged. Storage read/write failures shall be reported rather than presented as successful saves. Command feedback shall distinguish successful operations, invalid input, and empty search results.
8. **Display compatibility:** The GUI shall work without resolution-related inconvenience at 1920 x 1080 or higher with 100% and 125% scaling, and remain usable at 1280 x 720 or higher with 150% scaling, as required by the course screen-resolution constraint.
9. **Data minimisation:** Stored information shall be limited to exhibitor contact and enquiry-management needs. The application shall not require unrelated sensitive information such as identity-document numbers or payment-card details.

### Glossary

* **Booth allocation**: The process of assigning booths to exhibitors, from first enquiry through to confirmation
* **CLI (Command Line Interface)**: A way of using the application by typing text commands instead of clicking through menus
* **Company**: The exhibitor organisation a contact belongs to. Also called *organisation*; this project uses *company*
* **Contact**: A single record in the address book representing one exhibitor representative. Also called *client* or *person*; this project uses *contact*
* **In progress**: An enquiry currently being handled by the organiser
* **Convention organiser**: The primary user of the application: a person at a convention centre who manages exhibitor enquiries and booth allocation. Also referred to as *organiser*
* **CSV (Comma-Separated Values)**: A plain-text file format for tabular data, used to import and export contact lists
* **Duplicate (possible duplicate)**: A contact with the same normalised email address, or the same name and company, as an existing contact. Does not mean two contacts that merely share a name
* **Enquiry**: An exchange between an organiser and an exhibitor about the exhibitor taking part in a convention
* **Enquiry status**: The stage an exhibitor's enquiry has reached: `Needs follow-up`, `In progress`, `Completed`, or `Cancelled`. Also referred to as *status*
* **Exhibitor**: An organisation that is interested in, or has been invited to, take a booth at a convention
* **Exact match**: A search result where the search value matches the complete field value after its field-specific normalisation, not just part of it. For example, `find c/Tech` does not match company "TechNova". Name criteria additionally accept a whole name word, so `find n/Alicia` matches "Alicia Tan".
* **Index**: The number shown beside a contact in a displayed list, used to select a specific contact when several share the same name
* **Mainstream OS**: Windows, Linux, Unix, and macOS
* **Normalisation**: Converting input to a standard form before storing or comparing it, such as lowercasing emails, collapsing repeated spaces in names, and removing spaces and hyphens from phone numbers
* **Prefix**: A marker ending in `/` that identifies which field a value belongs to, e.g. `n/` for name or `t/` for tag
* **Primary contact**: The representative to contact first when an exhibitor has several representatives
* **Representative**: A person who acts on behalf of an exhibitor. One exhibitor may have several
* **Tag**: A short label attached to a contact to group it beyond enquiry status, for example by industry or priority
* **User**: The person operating BoothManagerPro (the convention organiser). Does not refer to the contacts stored in the app

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the latest `boothmanagerpro.jar` from the [GitHub releases page](https://github.com/AY2627S1-CS2103T-W11-2/tp/releases) and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar boothmanagerpro.jar` (requires Java `25`).<br>
      Expected: The command window opens with a ready prompt and no displayed contacts or separate contact panels.
      Sample contacts are loaded into memory; running `list` displays them as numbered cards. The initial window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window (or enter `exit`).

   1. Relaunch the app with `java -jar boothmanagerpro.jar` from the same folder.<br>
      Expected: The most recent window size and location are retained when they fit an available screen. Off-screen positions are moved onto an available screen, and oversized windows are resized to fit.

### Testing command-only output

Use a disposable data folder. Do not run `clear confirm` against real contact data.

| Scenario | Steps | Expected |
| --- | --- | --- |
| Populated startup | Launch with saved contacts. | A ready prompt appears. No contacts are displayed on startup and no side panels exist. |
| Invalid request | Try `list extra`, `find`, or `view n/NobodyMatches`. | Error text replaces the output; input remains for correction and data is unchanged. |
| Add | Add a valid contact. | Brief identity confirmation only; `view` can display its card. |
| Show all | Run `list`. | Every contact appears as a numbered card with all attributes, followed by the total count. |
| Sort | Run `list o/name`, `list o/company`, then `list o/status`. | Cards appear alphabetically by name, then by company with contacts without a company last, then by status with `Needs follow-up` first and `Cancelled` last. The heading names the sort field. |
| Sorted indices | After `list o/name`, run `delete 1`. | The first contact alphabetically is deleted. Running `list` restores the order contacts were added. |
| Invalid sort | Try `list o/email` and `list o/name o/company`. | Error text explains the accepted sort fields or the repeated prefix. Data and order are unchanged. |
| Find before list | Restart, then run `find Alex David` with sample data. | Two numbered contact cards appear in the output. No preliminary `list` is needed. |
| Filtered indices | After `find Alex David`, run `delete 2`. | Only David is deleted. A later `list` shows the remaining contacts. |
| Empty search | Run `find NobodyMatches`. | `0 contact(s) found.` in text, with no stale contact cards. |
| View | Run `view n/NAME` for multiple matches, then reply with a valid number. | Numbered text choices are replaced by the chosen contact's unnumbered card. |
| Cancel choice | Request multiple view matches, then run `find NAME` or an out-of-range `delete INDEX`. | The new feedback replaces the choice prompt. A subsequent bare number is no longer accepted. |
| Empty data | Run `clear confirm`, restart, then `list`. | Ready prompt on restart; `list` shows no-contacts guidance in text. |
| Restart | Add contacts, exit, and relaunch from the same folder. | Only a ready prompt appears; `list` displays the saved records. |

### Testing exhibitor additions

Use a separate test data folder with no Alicia Tan or Ben Lim records. Run each command separately, in order.

| Check | Command / action | Expected result |
| --- | --- | --- |
| Full record and normalisation | `add n/Alicia Tan c/TechNova Pte Ltd e/ALICIA@example.com p/+65 9123-4567 m/EMAIL t/technology t/technology t/high-priority` | Contact added; a subsequent `view n/Alicia Tan` card shows `alicia@example.com`, `+6591234567`, `email`, and each tag once. |
| Optional fields omitted | `add n/Ben Lim c/GreenWorks e/ben@example.com p/87654321` | Contact added; a subsequent `view n/Ben Lim` card shows status `Needs follow-up`, method `Not specified`, follow-up date `None`, and `No tags`. |
| Duplicate email, even when hidden | Run `find Ben`, then `add n/Another Person c/Another Company e/ALICIA@example.com p/98765432` | Rejected; identifies Alicia Tan at TechNova Pte Ltd. |
| Duplicate name and company | `add n/alicia tan c/technova pte ltd e/different@example.com p/98765432` | Rejected despite different email and letter case. |
| Missing field | `add n/Chris Tan e/chris@example.com p/98765432` | Missing-required-field error; no contact added. |
| Repeated field | `add n/Chris Tan n/Chris Lim c/Example Ltd e/chris@example.com p/98765432` | Each-field-once error; no contact added. |
| Unknown prefix | `add n/Chris Tan c/Example Ltd e/chris@example.com p/98765432 a/Somewhere` | Unknown-field-prefix error; no contact added. |
| Invalid phone | `add n/Chris Tan c/Example Ltd e/chris@example.com p/123` | Phone-length error; no contact added. |
| Persistence | Exit and relaunch from the same folder; run `list`, then repeat the first add command. | Both added contacts remain; repeated add is rejected. The saved `data/addressbook.json` retains company and contact method. |

Compare feedback with the [add command reference](UserGuide.md#adding-an-exhibitor-contact-add). Verify the brief identity confirmation, saved JSON, and the card shown by a subsequent `view`. Repeat an add after `find Nobody`; the filter should reset, the input should clear and retain focus, and no persistent side panels should appear.

### Viewing an exhibitor contact

1. With sample data, run `list`, then `find Nobody`, followed by `view n/alex yeoh`.<br>
   Expected: Alex Yeoh's contact card is displayed in the output area despite the preceding empty filter.
1. Try `view`, `view c/TechNova`, `view n/Alex n/Yeoh`, `view Alex Yeoh`, and `view n/123`.<br>
   Expected: Each command reports the appropriate input error without changing any contact data.
1. Add two contacts sharing a name word (`add n/Rhineson Tan c/AlphaCo e/rtan@example.com p/91112222` and `add n/Rhineson Kok c/BetaCo e/rkok@example.com p/93334444`), then run `view n/Rhineson`.<br>
   Expected: Both appear as numbered text choices (`1. Rhineson Tan at AlphaCo`, `2. Rhineson Kok at BetaCo`). Reply `2` to display `Rhineson Kok`'s contact card.
1. Add a contact whose full name is `Rhineson` (`add n/Rhineson c/GammaCo e/rhineson@example.com p/95556666`), then run `view n/Rhineson`.<br>
   Expected: `Rhineson` at `GammaCo` is displayed directly as a card without prompting, because its full name matches `Rhineson` uniquely; `view n/Rhineson Kok` likewise displays `Rhineson Kok` directly.
1. With two stored contacts named `Alicia Tan` at different companies and with different emails, run `view n/Alicia Tan`.<br>
   Expected: Numbered company choices appear. Enter `0`, then `3`, then `2`.<br>
   Expected: The invalid replies (`0` and `3`) show `Please enter a number between 1 and 2.` followed by the numbered choices again; `2` displays the second contact's card.
1. Run `view n/Alicia Tan`, then `list`, then `2`.<br>
   Expected: `list` cancels the pending choice; the final `2` is rejected as an unknown command rather than treated as a view selection.

### Updating an exhibitor contact

1. Prerequisites: Add `Alicia Tan` (`add n/Alicia Tan c/TechNova Pte Ltd e/alicia@example.com p/91234567 t/tech`).
1. Update by full name: `update Alicia Tan m/phone s/In progress f/20261025`<br>
   Expected: Output confirms `Updated Alicia Tan at TechNova Pte Ltd.` Running `view n/Alicia Tan` shows a card with badge `In progress`, contact method `phone`, follow-up date `25 Oct 2026`, and existing email, phone, and `tech` tag preserved.
1. Clear optional date and tags: `update Alicia Tan f/ t/`<br>
   Expected: Confirmation appears; `view n/Alicia Tan` shows `Follow-up date: None` and `No tags`.
1. Ambiguous name vs displayed index: Add a second `Alicia Tan` (`add n/Alicia Tan c/OtherCo e/alicia2@example.com p/87654321`), then run `update Alicia Tan s/Completed`.<br>
   Expected: Rejected with an ambiguity message; neither contact is updated. Run `find n/Alicia Tan`, then `update 2 s/Completed`; only the second `Alicia Tan` is updated and the filter resets.
1. Invalid and duplicate updates: Try `update Alicia Tan`, `update 1 f/20230229`, and `update 2 e/alicia@example.com` (after `list`).<br>
   Expected: Each command is rejected with a specific error message, typed input remains for editing, and stored contacts are unchanged.

### Toggling contact tags

1. Prerequisites: Ensure a uniquely named contact exists (`add n/Ben Lim c/GreenWorks e/ben@example.com p/87654321 t/priority`).
1. Toggle existing and new tags by name: `tag Ben Lim t/priority t/sponsor t/keynote`<br>
   Expected: Output states `Updated tags for Ben Lim at GreenWorks. Added: sponsor, keynote. Removed: priority. Current tags: sponsor, keynote.` Running `view n/Ben Lim` shows `sponsor` and `keynote` chips on the card.
1. Toggle by displayed index and verify case-sensitivity: Run `list`, then `tag 1 t/VIP` followed by `tag 1 t/vip` (adjust index if needed).<br>
   Expected: Both `VIP` and `vip` are added as distinct tags; repeating `tag 1 t/VIP` removes only `VIP`.
1. Invalid tag commands: Try `tag Ben Lim`, `tag Ben Lim t/`, `tag Ben Lim s/Completed`, and `tag 999999 t/vip`.<br>
   Expected: Rejected with an error explanation; contacts, filter, and `data/addressbook.json` remain unchanged.

### Finding contacts by fields and follow-up dates

1. Prerequisites: Start from an empty folder (`clear confirm`), then add:
   * `add n/Alex Yeo c/TechNova e/alexyeo@example.com p/91234567 f/20261025 t/vip`
   * `add n/Alexandra Tan c/TechNova e/alexandra@example.com p/92345678 f/20261026 t/vip t/booth`
   * `add n/Ben Lim c/GreenWorks e/ben@example.com p/93456789 t/booth`
1. Whole-word vs full-name matching: Run `find n/Alex`, then `find n/Alex Yeo`.<br>
   Expected: Both match only `Alex Yeo` (`Alexandra Tan` is not matched by whole-word `Alex`).
1. Complete field matching: Run `find c/technova`, `find e/BEN@example.com`, `find p/9123-4567`, and `find t/VIP`.<br>
   Expected: `c/technova` and `t/VIP` return the two `TechNova` contacts; `e/` returns `Ben Lim`; `p/` returns `Alex Yeo`. Partial `find c/Tech` returns `0 contact(s) found.`
1. Exact follow-up date and OR/AND combinations: Run `find f/20261025`, then `find f/20261025 f/20261026`, then `find f/20261025 f/20261026 t/booth`.<br>
   Expected: First returns `Alex Yeo` (1 match; undated `Ben Lim` never matches `f/`); second returns both dated contacts (OR within `f/`); third returns only `Alexandra Tan` (AND across `f/` and `t/`).
1. Invalid search inputs: With the 1-contact result above displayed, try `find`, `find f/`, `find f/20230229`, `find f/2026-10-25`, `find s/In progress`, and `find Alex t/vip`.<br>
   Expected: Each command shows an error without modifying saved contacts or the current filtered index set.

### Deleting a contact

1. Deleting a contact by displayed index

   1. Prerequisites: List all contacts using `list`, with multiple contacts present.

   1. Test case: `delete 1`<br>
      Expected: The first displayed contact is deleted and saved. Output confirms the deleted contact's exhibitor details.

   1. Test case: `delete 0`, `delete`, `delete 999999` (exceeding list size), and `delete n/Alex Yeoh`<br>
      Expected: No contact is deleted. Output explains the invalid index or format.

1. Deleting a contact by full name

   1. Test case: `delete Bernice Yu` (when exactly one stored contact has that full name, even if hidden by `find`)<br>
      Expected: `Bernice Yu` is deleted and saved. Partial names such as `delete Bernice` are rejected.

   1. Ambiguous full name: Add two contacts named `Alicia Tan` with different companies and emails, then run `delete Alicia Tan`.<br>
      Expected: Deletion is rejected with guidance to run `list` or `find` and delete by displayed index.

### Clearing all contacts

1. Unconfirmed clear: With contacts stored, run `clear`.<br>
   Expected: No contacts are deleted. Output displays `This will delete all contacts! Type 'clear confirm' to proceed.` Running `list` confirms all contacts remain.
1. Invalid argument: Run `clear all`.<br>
   Expected: Rejected with the `clear confirm` usage format; no contacts are deleted.
1. Confirmed clear: Run `find Alex`, then `clear confirm`, then `list`.<br>
   Expected: Output displays `All contacts have been cleared!`, every contact (including contacts hidden by `find`) is removed from memory and `data/addressbook.json`, and `list` displays the empty-list guidance.

### Saving data

1. Missing data file

   1. Close BoothManagerPro and delete `data/addressbook.json` (if present), then relaunch `java -jar boothmanagerpro.jar`.<br>
      Expected: The app starts with the ready prompt and loads the 6 sample contacts into memory (`list` displays them). Executing a non-view command such as `list` creates `data/addressbook.json`.

1. Corrupted data file

   1. Close BoothManagerPro, replace the contents of `data/addressbook.json` with invalid JSON (e.g. `{ corrupted`) or `null`, and relaunch.<br>
      Expected: The app starts with an empty contact list and displays the error message `Contact data could not be loaded. The original file will be backed up before any new data is saved.` The corrupted `data/addressbook.json` is not overwritten on launch.

   1. Run `list` (or add a contact).<br>
      Expected: Before saving, BoothManagerPro copies the unreadable file byte-for-byte to `data/addressbook.json.recovery-<unique suffix>.json` and writes the new valid JSON to `data/addressbook.json`.

1. Missing-field data file

   1. Close BoothManagerPro and edit `data/addressbook.json` so one contact omits optional exhibitor fields (`"company": null`, `"contactMethod": null`, and remove `"enquiryStatus"` and `"followUpDate"`), keeping valid `name`, `phone`, `email`, and `address`. Relaunch and run `list`.<br>
      Expected: The contact loads normally with `Company not specified`, `Contact method: Not specified`, `Needs follow-up`, and `Follow-up date: None`.

   1. Close BoothManagerPro, remove a required field such as `"email"` or `"name"` from a contact in `data/addressbook.json`, and relaunch.<br>
      Expected: Loading fails safely with the same warning banner and empty contact list as a corrupted file, preserving the original file in a `.recovery-` backup before the next save.
