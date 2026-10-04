---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](../src/main/java/seedu/boothmanagerpro/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](../src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](../src/main/java/seedu/boothmanagerpro/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](../src/main/java/seedu/boothmanagerpro/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

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
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](../src/main/java/seedu/boothmanagerpro/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.boothmanagerpro.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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

* Manager who are handling large amount of booths 
* Able to type fast
* Interested in storing and finding specific contact from a large database

**Value proposition**: Able to add and delete the status of clients quickly and find their information faster as compared to mouse driven GUI.

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
| US-09 | `**` | organiser | edit contact details | my records stay accurate. |
| US-10 | `**` | organiser | find contacts by exact name, company, email, or phone | I can locate contacts quickly. |
| US-11 | `**` | organiser | filter contacts by enquiry status | I can review each enquiry stage. |
| US-12 | `**` | organiser | combine search fields and alternatives | I can narrow my results. |
| US-13 | `**` | organiser | set enquiry status to new, contacted, confirmed, or rejected | I can track progress. |
| US-14 | `**` | organiser | set a next follow-up date | I know when to contact someone again. |
| US-15 | `**` | organiser | view due or overdue follow-ups | I can prioritise outreach. |
| US-16 | `**` | organiser | tag contacts by industry, priority, or need | I can group exhibitors. |
| US-17 | `**` | organiser | filter contacts by tags | I can retrieve relevant groups. |
| US-18 | `**` | organiser | record a preferred contact method | I can use the appropriate channel. |

- **US-05:** Duplicates share a normalised email or a name-and-company combination.
- **US-12:** Repeated values within a field use OR; different fields use AND.

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

These use cases cover contact-management workflows from the feature specification.
**Guarantee:** Rejected requests leave the stored contacts unchanged.

#### UC01: Add an exhibitor contact

**MSS**

1. Organiser requests to add a contact, supplying the name, company, email, phone, and any optional contact method or tags.
2. BoothManagerPro adds the contact and displays the newly added contact's details.

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
2. BoothManagerPro displays the matching contact's name, company, email, phone, preferred contact method, and tags.

Use case ends.

**Extensions**

* 1a. The name is missing or invalid.
  * 1a1. BoothManagerPro explains the input error.

  Use case resumes at step 1.

* 1b. No contact matches the supplied name, including when there are no stored contacts.
  * 1b1. BoothManagerPro informs the organiser that no matching contact was found.

  Use case resumes at step 1.

* 1c. More than one contact matches the supplied name.
  * 1c1. BoothManagerPro displays the matching contacts with their companies and asks the organiser to select one.
  * 1c2. Organiser selects the intended contact.
  * 1c3. If the selection is invalid, BoothManagerPro explains the valid choices and requests another selection.

  Steps 1c2 to 1c3 repeat until a valid contact is selected.

  Use case resumes at step 2 for the selected contact.

* 1d. The view request contains an unsupported field, an unprefixed value, or a repeated name field.
  * 1d1. BoothManagerPro rejects the request and explains the accepted request format.

  Use case resumes at step 1.

#### UC03: Delete an exhibitor contact

**MSS**

1. Organiser requests to delete a contact by name.
2. BoothManagerPro deletes the matching contact and displays the deleted contact's details.

Use case ends.

**Extensions**

* 1a. The name is missing or invalid.
  * 1a1. BoothManagerPro explains the input error.

  Use case resumes at step 1.

* 1b. No contact matches the supplied name, including when there are no stored contacts.
  * 1b1. BoothManagerPro informs the organiser that no matching contact was found.

  Use case resumes at step 1.

* 1c. More than one contact matches the supplied name.
  * 1c1. BoothManagerPro displays the matching contacts with their companies and asks the organiser to select one.
  * 1c2. Organiser selects the intended contact.
  * 1c3. If the selection is invalid, BoothManagerPro explains the valid choices and requests another selection.

  Steps 1c2 to 1c3 repeat until a valid contact is selected.

  Use case resumes at step 2, deleting only the selected contact.

#### UC04: List exhibitor contacts

**MSS**

1. Organiser requests to list all exhibitor contacts.
2. BoothManagerPro displays all stored contacts with their names, companies, emails, phones, preferred contact methods,
   and tags, followed by the total number of contacts.

Use case ends.

**Extensions**

* 1a. The organiser supplies parameters with the list request.
  * 1a1. BoothManagerPro rejects the request and explains that the list command does not accept parameters.

  Use case resumes at step 1.

* 1b. There are no stored contacts.
  * 1b1. BoothManagerPro informs the organiser that no contacts were found and suggests using the add command to add one.

  Use case ends.

#### UC05: Find and filter contacts

**MSS**

1. Organiser requests to find contacts using one or more names, companies, emails, phone numbers, enquiry statuses, or tags.
2. BoothManagerPro displays all contacts that match the supplied criteria, together with the number of matching contacts.
   Each matching contact is displayed separately with an index and contact details, even if contacts share a name or company.

Use case ends.

**Matching rules**

* Values for the same field are combined using OR; criteria for different fields are combined using AND.
* Matches use complete field values. Name and company comparisons ignore case and leading/trailing spaces.
  Email comparisons ignore case and use normalised addresses; phone comparisons ignore spaces and hyphens.
  Status and tag comparisons ignore case.
* Repeated identical search values are ignored.

**Extensions**

* 1a. No search field is supplied.
  * 1a1. BoothManagerPro rejects the request and asks the organiser to provide at least one search field.

  Use case resumes at step 1.

* 1b. A search field is supplied without a value.
  * 1b1. BoothManagerPro rejects the request and explains that search values cannot be empty.

  Use case resumes at step 1.

* 1c. An enquiry status is not one of the supported values.
  * 1c1. BoothManagerPro rejects the request and explains that the supported statuses are new, contacted, confirmed, and rejected.

  Use case resumes at step 1.

* 1d. Another search value does not meet the specified format for its field.
  * 1d1. BoothManagerPro rejects the request and explains that the value does not match the required format.

  Use case resumes at step 1.

* 1e. The request contains an unknown prefix or an unprefixed value.
  * 1e1. BoothManagerPro rejects the request and explains that every search value must use a supported field prefix.

  Use case resumes at step 1.

* 1f. No contacts match the search criteria.
  * 1f1. BoothManagerPro informs the organiser that no matching exhibitor contacts were found.

  Use case ends.

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
* **Contacted**: The enquiry status for an exhibitor the organiser has reached out to but who has not yet been confirmed or rejected. Refers to this status, not the general act of contacting someone
* **Convention organiser**: The primary user of the application: a person at a convention centre who manages exhibitor enquiries and booth allocation. Also referred to as *organiser*
* **CSV (Comma-Separated Values)**: A plain-text file format for tabular data, used to import and export contact lists
* **Duplicate (possible duplicate)**: A contact with the same normalised email address, or the same name and company, as an existing contact. Does not mean two contacts that merely share a name
* **Enquiry**: An exchange between an organiser and an exhibitor about the exhibitor taking part in a convention
* **Enquiry status**: The stage an exhibitor's enquiry has reached: `new`, `contacted`, `confirmed`, or `rejected`. Also referred to as *status*
* **Exhibitor**: An organisation that is interested in, or has been invited to, take a booth at a convention
* **Exact match**: A search result where the search value matches the complete field value (ignoring case and leading/trailing spaces), not just part of it. For example, `find n/Alicia` does not match "Alicia Tan"
* **Index**: The number shown beside a contact in a displayed list, used to select a specific contact when several share the same name
* **Mainstream OS**: Windows, Linux, Unix, and macOS
* **Normalisation**: Converting input to a standard form before storing or comparing it, such as lowercasing emails and removing spaces and hyphens from phone numbers
* **Prefix**: A marker ending in `/` that identifies which field a value belongs to, e.g. `n/` for name or `s/` for status
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

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
