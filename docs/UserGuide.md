---
layout: page
title: User Guide
---

BoothManagerPro is a **desktop application for convention organisers to manage exhibitor contacts through typed commands**, with a graphical contact list and details panel.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Build the development JAR after following the [setup guide](SettingUp.md): run `.\gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux. The JAR is generated at `build/libs/boothmanagerpro.jar`.

1. Copy the file to the folder you want to use as the _home folder_ for BoothManagerPro.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar boothmanagerpro.jar`.<br>
   The application opens with sample contacts on first launch. The image below is the planned UI mockup; some fields await integration.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe c/Example Ltd e/johnd@example.com p/98765432` : Adds an exhibitor contact named `John Doe`.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

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


### Adding an exhibitor contact: `add`

Adds an exhibitor or representative with a required company, email, and phone number.

Format: `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [t/TAG]...`

| Field | Rules |
| --- | --- |
| `n/NAME` | 1-80 characters; at least one letter. Allows letters, spaces, hyphens, apostrophes, and full stops. |
| `c/COMPANY` | 1-100 characters; cannot be blank. |
| `e/EMAIL` | Valid email format, such as `alicia@example.com`; stored in lowercase. |
| `p/PHONE` | 7-15 digits, optionally starting with `+`. Spaces and hyphens are removed. |
| `m/METHOD` | Optional: `email`, `phone`, or `other`, ignoring case. Omitted method is `Not specified`. |
| `t/TAG` | Optional and repeatable: 1-30 characters, nonblank, without `/`. Identical tags are ignored; comparisons are case-sensitive. |

Leading and trailing spaces are removed from field values. Fields can appear in any order. Only `t/` may repeat. The old `a/ADDRESS` field is not accepted by `add`.

Examples (enter each command on one line):

```text
add n/Alicia Tan c/TechNova Pte Ltd e/alicia@technova.com p/91234567 m/email t/technology t/high-priority
add n/Ben Lim c/GreenWorks e/BEN@example.com p/+65 8765-4321
```

The first command displays:

```text
New exhibitor contact added:
Alicia Tan at TechNova Pte Ltd
Email: alicia@technova.com
Phone: 91234567
Contact method: email
Tags: technology, high-priority
```

The second stores `ben@example.com` and `+6587654321`, with method `Not specified` and tags `None` in the feedback. Company and contact method are saved and included in command feedback; their dedicated details-panel display is pending UI integration.

**Duplicate handling**

A contact is rejected if any stored contact has the same normalised email, or the same name and company ignoring case. This includes contacts hidden by the current search. Sharing only a name or company is allowed if the email is different. Legacy contacts without a company are compared by email only.

For a match against Alicia's record above:

```text
This contact may already exist: Alicia Tan at TechNova Pte Ltd.
Use the edit command if you want to update the existing contact.
```

The current `edit` command can update name, email, phone, address, and tags; it cannot yet change company or contact method.

**Input errors**

| Problem | Feedback |
| --- | --- |
| Missing `n/`, `c/`, `e/`, or `p/` | `Missing required field: NAME, COMPANY, EMAIL, or PHONE.` |
| Repeated field other than `t/` | `Each field can only be specified once.` |
| Unknown prefix or text before the first prefix | `Unknown field prefix. Use n/, c/, e/, p/, m/, or t/.` |
| Invalid name | `Names must be 1 to 80 characters and contain a letter. Use only letters, spaces, hyphens, apostrophes or full stops.` |
| Invalid company | `Company name must contain 1 to 100 characters.` |
| Invalid email | `Email address is invalid.` |
| Invalid phone | `Phone number must contain 7 to 15 digits.` |
| Invalid method | `Contact method must be email, phone, or other.` |
| Invalid tag | `Tag must be 1 to 30 characters and cannot contain '/'.` |

A present but empty field produces its value-validation error. Words between prefixes belong to the preceding field; prefix-like tokens are reserved. If several errors occur, unknown prefixes/preamble are checked first, then repeated fields, then missing prefixes, then values. Invalid input and duplicates do not add a contact.

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values. Name, email, phone, and tags follow the validation rules described under `add`.
* Company and contact method are preserved; `edit` does not yet support `c/` or `m/`. Changes that create a duplicate are rejected.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name: `find`

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

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

BoothManagerPro automatically saves data after each successfully executed command. You do not need to save manually.

### Editing the data file

By default, BoothManagerPro saves data as `data/addressbook.json`, relative to the folder from which you launch the app. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, BoothManagerPro starts with an empty address book at the next run. The invalid file remains on disk until a command executes successfully and saves data. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause BoothManagerPro to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous BoothManagerPro home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [t/TAG]...` <br> e.g., `add n/James Ho c/Example Ltd e/jamesho@example.com p/22224444 m/phone t/technology`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List** | `list`
**Help** | `help`
