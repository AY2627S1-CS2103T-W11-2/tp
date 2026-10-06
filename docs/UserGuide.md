---
layout: page
title: User Guide
---

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar boothmanagerpro.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete Alex Yeoh` : Deletes the contact named Alex Yeoh.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Viewing an exhibitor contact: `view`

Shows matching contacts in the left panel and the chosen contact's details in the right panel, with command feedback in the result area.

Format: `view n/NAME`

* Matching follows `find`: case-insensitive whole-name keywords, matching **any** supplied keyword.
  For example, `view n/rhineson` matches both `rhineson` and `rhineson kok`; `rhin` does not match `rhineson`.
* Names must contain 1 to 80 characters and at least one letter. Letters, spaces, hyphens, apostrophes,
  and full stops are supported, for example `Anne-Marie O'Neil`.
* Searches all stored contacts, including those hidden by a previous `find` command.
* If several contacts match, the left panel and result show the same numbered choices with their companies.
  The right panel prompts you to choose a contact.
  Enter the corresponding number as your next command, for example `2`.
  The chosen contact is highlighted on the left and their full details appear on the right.
  A single match is selected automatically.
* An invalid number shows the valid range and lets you try again. Another recognised command cancels the pending choice.
* Only `n/` is accepted. Repeated names, other prefixes, missing/invalid names, and unprefixed requests are rejected.
* Viewing does not change saved data. It filters the displayed list to the matches; use `list` to show everyone again.
* An omitted contact method or a company missing from an older record is shown as `Not specified`.
  Contacts without tags show `None`.

Example: `view n/Alicia Tan`

```text
Exhibitor contact found:
Alicia Tan at TechNova Pte Ltd
Email: alicia@technova.com
Phone: 91234567
Contact method: email
Tags: high-priority, technology
```

Company and preferred contact method are supported in stored records. The inherited `add` command's syntax
remains unchanged in this increment; entering these fields through `add` depends on the separate add-feature work.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Finding and filtering contacts: `find`

Use prefixed criteria to match complete field values:

Format: `find [n/NAME] [e/EMAIL] [p/PHONE] [t/TAG]...`

* Supply at least one non-empty criterion. Each value must follow the same format as when adding a contact;
  phone search values may additionally contain spaces or hyphens.
* Names, emails, and tags ignore case and surrounding whitespace. Phone comparisons ignore spaces and hyphens.
* Repeat a prefix for alternatives (OR). Different fields must all match (AND).
* Tags match complete tag names; any matching tag satisfies that field.
* Duplicate criteria have no effect. Results always search all stored contacts, not just the current displayed list.
* Unknown prefixes and empty/invalid values are rejected without changing the displayed list or stored contacts.
* Company (`c/`) and enquiry status (`s/`) are not available yet: the current contact model does not store these fields.
* `list` restores all contacts. A result count of zero means no contacts matched.

Examples:

* `find n/Alice Pauline` matches the complete name, not just `Alice`.
* `find n/Alice Pauline n/Benson Meier t/friends` returns either named contact if they also have the `friends` tag.
* `find e/alice@example.com p/9435-1253` requires both email and phone to match.
* `find t/technology t/food` returns contacts tagged with either label.

#### Legacy name-keyword search

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete NAME` or `delete INDEX`

* `NAME` must be the contact's full name (matching is case-insensitive).
* A partial name does not match. For example, `delete Alex` does not match `Alex Yeoh`.
* If no contact has the supplied name, no contact is deleted and an error is shown.
* If multiple contacts match the supplied name ignoring letter case, no contact is deleted. Use `list` or `find`,
  then delete the intended contact by its displayed index.
* Alternatively, deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `delete Alex Yeoh` deletes the contact named Alex Yeoh.
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves changes to contact data. You do not need to save manually.
The `view` command and its numbered selection replies only read data and do not write to the data file.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run.
The invalid file remains on disk until you run a command other than `view` or its numbered selection reply.
Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete NAME` or `delete INDEX`<br> e.g., `delete Alex Yeoh`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find [n/NAME] [e/EMAIL] [p/PHONE] [t/TAG]...` or `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find n/Alice Pauline t/friends`
**List** | `list`
**View** | `view n/NAME`<br> e.g., `view n/Alicia Tan`; reply `2` if prompted to choose between matches
**Help** | `help`
