---
layout: page
title: User Guide
---

BoothManagerPro helps **convention organisers manage exhibitor contacts through typed commands in a desktop window**, with command input and text output only. All contact operations use typed commands; no mouse clicks are needed. Contacts are not printed automatically on startup.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that JDK `25` is installed on your computer, as required by the [setup guide](SettingUp.md).

1. Build the development JAR after following the [setup guide](SettingUp.md): run `.\gradlew.bat shadowJar` on Windows or `./gradlew shadowJar` on macOS/Linux. The JAR is generated at `build/libs/boothmanagerpro.jar`.

1. Copy the file to the folder you want to use as the _home folder_ for BoothManagerPro.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar boothmanagerpro.jar`.<br>
   Sample contacts are loaded on first launch, but only a ready prompt is shown.
   Type `list` to print all contacts, `find Alex` to print matches, or `view n/Alex` to view details.<br>
   ![Command interface on startup](images/cli-startup.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to print command usage instructions.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe c/Example Ltd e/johnd@example.com p/98765432` : Adds an exhibitor contact named `John Doe`.

   * `delete Alex Yeoh` : Deletes the contact named Alex Yeoh.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

### Working through commands

* Type a command and press **Enter**. Command words and prefixes are case-sensitive: use `list`, not `List`.
* The command box starts with keyboard focus. Press **Ctrl+L** on Windows/Linux or **Command+L** on macOS to return to it. Use `help` or **F1** for the guide, and `exit` to close the app.
* Each launch starts with a ready prompt. Saved contacts remain available but are not automatically printed.
* Results appear only as text in the output area below the command box. There are no contact cards, contact-list panel, or details panel.
* `list` prints every contact with a number and details. `find` prints the match count and numbered matching contacts; it works without running `list` first.
* `view` prints a matching contact's details or numbered choices. Reply with a number only when `view` asks you to choose.
* Each command replaces the previous output. The output fills the available space and scrolls for longer results. Use **Tab** to focus it and the arrow keys to read it; **Ctrl+L** or **Command+L** returns to command entry.
* For `edit INDEX` and `delete INDEX`, use the numbers from the current `list`, `find`, or `view` matches. A successful `add` or `edit` resets the filter; run `list` or `find` again before choosing another index. After deleting a contact, later indices shift down.
* Company and contact method show `Not specified` when absent. Tags appear in saved order, with `None` for an empty tag set. The legacy address field remains editable using `edit INDEX a/ADDRESS`; newly added contacts use `Not provided`.

![Find results printed as numbered text](images/cli-find.png)

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed fields can be in any order; keep the index immediately after `edit`.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`. The exception is `list`, which rejects any parameters.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Prints command usage instructions and the User Guide link in the text output area. You can also press **F1**. Keyboard focus stays in the command box.

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
Added Alicia Tan at TechNova Pte Ltd.
```

After a successful add:

* Any previous filter is cleared. The output shows the addition confirmation.
* Use `view n/NAME` to print the saved details, or `list` to print all contacts.
* The command box clears and keeps keyboard focus, ready for the next command.

The second example stores `ben@example.com` and `+6587654321`. A subsequent `view n/Ben Lim` shows `Not specified` for the omitted method and `None` for an empty tag set.

If validation or duplicate detection fails, the typed command remains available for correction. No contact is added.

**Duplicate handling**

A contact is rejected if any stored contact has the same normalised email, or the same name and company ignoring case. This includes contacts hidden by the current search. Sharing only a name or company is allowed if the email is different. Legacy contacts without a company are compared by email only.

For a match against Alicia's record above:

```text
This contact may already exist: Alicia Tan at TechNova Pte Ltd.
Use the edit command if you want to update the existing contact.
```

If the email matches one record and the name/company matches another, both conflicting contacts are listed with their emails. No contact is added; correct the input before retrying.

The current `edit` command can update name, email, phone, address, and tags; it cannot yet change company or contact method.

**Input errors**

| Problem | Feedback |
| --- | --- |
| Missing required prefixes (example: company and email) | `Missing required fields: c/, e/. Supply a value after each prefix.` |
| Repeated fields (example: name and phone) | `Repeated fields: n/, p/. Keep one value per field; only t/ may repeat.` |
| Unknown prefix (example: `x/`) | `Unknown field prefix: x/. Use n/, c/, e/, p/, m/, or t/.` |
| Text before the first prefix | `Unexpected text before the first field.` followed by a complete example command. |
| Empty field (example: `c/`) | `Empty field: c/. Supply a value after this prefix.` |
| Invalid name | `Invalid value for n/: Names must be 1 to 80 characters and contain a letter. Use only letters, spaces, hyphens, apostrophes or full stops.` |
| Invalid company | `Invalid value for c/: Company name must contain 1 to 100 characters.` |
| Invalid email | `Invalid value for e/: Email address is invalid.` |
| Invalid phone | `Invalid value for p/: Phone number must contain 7 to 15 digits.` |
| Invalid method | `Invalid value for m/: Contact method must be email, phone, or other.` |
| Invalid tag | `Invalid value for t/: Tag must be 1 to 30 characters and cannot contain '/'.` |

Each missing-field or repeated-field error lists all affected prefixes in n/, c/, e/, p/, m/ order. A present but empty field is reported separately from a missing prefix. Words between prefixes belong to the preceding field; prefix-like tokens are reserved. If several errors occur, unknown prefixes/preamble are checked first, then repeated fields, then missing prefixes, then values in n/, c/, e/, p/, m/, t/ order. Invalid input and duplicates do not add a contact.

### Listing all exhibitor contacts: `list`

Shows every stored exhibitor contact with its name, company, email, phone, preferred contact method, and tags,
followed by the total number of contacts.

Format: `list`

* `list` does not accept parameters. For example, `list 3` is rejected.
* `list` prints numbered contacts in the output area. With no contacts, it prints the no-contacts message below.
* Any filter left by a previous `find` or `view` command is cleared, so all contacts are shown again.
* Tags are shown in the order they are stored. An omitted contact method or a company missing from an older
  record is shown as `Not specified`. Contacts without tags show `None`.

Example: `list`

```text
Exhibitor Contact List
──────────────────────────────────────

1. Alicia Tan at TechNova Pte Ltd
Email: alicia@technova.com
Phone: 91234567
Contact method: email
Tags: technology, high-priority

2. John Smith at GlobalTech Inc
Email: john@globaltech.com
Phone: +6562345678
Contact method: phone
Tags: hardware

──────────────────────────────────────
Total contacts: 2
```

If there are no stored contacts:

```text
No contacts found.
Use the 'add' command to add a new exhibitor contact.
```

### Viewing an exhibitor contact: `view`

Prints the chosen contact's details in the command output area.

Format: `view n/NAME`

* Matching follows unprefixed `find KEYWORD`: case-insensitive whole-name keywords, matching **any** supplied keyword.
  For example, `view n/rhineson` matches both `rhineson` and `rhineson kok`; `rhin` does not match `rhineson`.
* Names must contain 1 to 80 characters and at least one letter. Letters, spaces, hyphens, apostrophes,
  and full stops are supported, for example `Anne-Marie O'Neil`.
* Searches all stored contacts, including those hidden by a previous `find` command.
* If several contacts match, the output lists numbered choices with their companies and asks you to choose.
  Enter the corresponding number as your next command, for example `2`.
  The output is replaced with the chosen contact's details. A single match displays details immediately.
* An invalid number shows the valid range and lets you try again. Another successfully parsed command cancels the pending choice, even if execution fails. A command with invalid syntax leaves the choice pending. Cancelling a choice clears its prompt; a subsequent bare number is no longer a selection reply.
* Only `n/` is accepted. Repeated names, other prefixes, missing/invalid names, and unprefixed requests are rejected.
* No match produces an error and keeps the current filter unchanged.
* Viewing does not change saved data. It filters the current index set to the matches; use `list` to print everyone again. A unique match has index `1`.
* An omitted contact method or a company missing from an older record is shown as `Not specified`.
  Contacts without tags show `None`.

Example: `view n/Alicia Tan`

```text
Exhibitor contact found:
Alicia Tan at TechNova Pte Ltd
Email: alicia@technova.com
Phone: 91234567
Contact method: email
Tags: technology, high-priority
```

Create these records with `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [t/TAG]...`.

### Editing an exhibitor contact: `edit`

Edits an existing contact by its displayed index.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the contact at the specified `INDEX`. The index refers to the index number shown in the current numbered command results. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values. Name, email, phone, and tags follow the validation rules described under `add`.
* Company and contact method are preserved; `edit` does not support `c/` or `m/`. Unsupported prefixes are rejected, including after `a/ADDRESS`. Changes that create a duplicate are rejected.
* The legacy `a/ADDRESS` field accepts nonblank text. Whitespace-delimited prefix-like tokens are reserved, as in `add`. Only `t/` may repeat.
* A successful edit clears the current filter and prints the edited contact. Run `list` or `find` again before choosing another index.
* When editing tags, all of the contact's existing tags are removed; adding tags is not cumulative.
* To remove all of a contact's tags, enter `t/` without a tag after it.

Examples:

*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st contact to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd contact to be `Betsy Crower` and clears all existing tags.

### Finding and filtering contacts: `find`

Use prefixed criteria to match complete field values:

Format: `find [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [t/TAG]...`

* Supply at least one non-empty criterion. Each value must follow the same format as when adding a contact;
  phone values accept spaces and hyphens, just as `add` does. Tabs inside phone numbers are not accepted.
* Names, companies, emails, and tags ignore case and surrounding whitespace. Phone comparisons ignore spaces and hyphens.
* Exact-name searches use the same Unicode case rules as name-keyword searches. For example, `find n/ipek`
  matches a contact whose complete name is `İpek`; partial names still do not match.
* Repeat a prefix for alternatives (OR). Different fields must all match (AND).
* Tags match complete tag names; any matching tag satisfies that field.
* Duplicate criteria have no effect. Results always search all stored contacts, not just the current displayed list.
* Unknown prefixes and empty/invalid values are rejected without changing the filter or stored contacts.
* `find` prints the number of matches followed by each matching contact's number, name, company, email, phone, method, and tags. No preliminary `list` command is needed.
* With no matches, it prints `0 contact(s) found.` and no contact details.
* Company (`c/`) matches the complete stored company name. Legacy contacts without a company do not match this field.
* Enquiry status (`s/`) and contact method (`m/`) are not searchable. Status tracking remains planned.
* `list` restores all contacts. A result count of zero means no contacts matched.

Examples:

* `find n/Alice Pauline` matches the complete name, not just `Alice`.
* `find n/Alice Pauline n/Benson Meier t/friends` returns either named contact if they also have the `friends` tag.
* `find e/alice@example.com p/9435-1253` requires both email and phone to match.
* `find c/TechNova Pte Ltd t/high-priority` requires both company and tag to match.
* `find t/technology t/food` returns contacts tagged with either label.

#### Legacy name-keyword search

Finds contacts whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Contacts matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:

* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh` and `David Li` from the sample contacts. Both matches and their details are printed directly in the output area.

### Deleting an exhibitor contact: `delete`

Deletes the specified contact.

Format: `delete NAME` or `delete INDEX`

* `NAME` must be the contact's full name (matching is case-insensitive); do not include `n/`.
* Name deletion searches all stored contacts, including contacts hidden by a filter. Success feedback includes company and contact method.
* A partial name does not match. For example, `delete Alex` does not match `Alex Yeoh`.
* If no contact has the supplied name, no contact is deleted and an error is shown.
* If multiple contacts match the supplied name ignoring letter case, no contact is deleted. Use `list`, optionally followed by `find`,
  then delete the intended contact by its displayed index.
* Alternatively, deletes the contact at the specified `INDEX`.
* The index refers to the index number shown in the current numbered command results.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:

* `delete Alex Yeoh` deletes the contact named Alex Yeoh.
* `list` followed by `delete 2` deletes the 2nd contact in the contact list.
* `find Betsy` followed by `delete 1` deletes the 1st contact in the results of the `find` command.

### Clearing all entries: `clear`

Deletes all stored contacts, including those hidden by a filter. The action takes effect immediately; there is no confirmation prompt or undo command.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

BoothManagerPro automatically saves data after successful commands, except `view` and its numbered selection replies, which do not write the data file. You do not need to save manually. If saving fails, an error appears; the change may still be present in the running app but is not guaranteed to be on disk.

### Editing the data file

By default, BoothManagerPro saves data as `data/addressbook.json`, relative to the folder from which you launch the app. Close the app before editing or replacing this file so that a running session does not overwrite your changes.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, BoothManagerPro starts with an empty contact collection at the next run. The invalid file remains on disk until a command other than `view` or a numbered view reply executes successfully and saves data (including `list`, `help`, or `exit`). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause BoothManagerPro to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Planned features

Enquiry status, follow-up dates, data archiving, CSV import/export, and undo are not available in the current version. There are no enquiry-management commands in this version.

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Close BoothManagerPro on both computers. Copy `data/addressbook.json` from the old launch folder into the new launch folder's `data` directory, backing up any existing file first. Start the app from the new folder and run `list` to check your contacts.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **If you minimize the Help Window** and then run the `help` command (or use the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [t/TAG]...` <br> e.g., `add n/James Ho c/Example Ltd e/jamesho@example.com p/22224444 m/phone t/technology`
**Clear** | `clear`
**Delete** | `delete NAME` or `delete INDEX`<br> e.g., `delete Alex Yeoh`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [t/TAG]...` or `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find n/Alice Pauline t/friends`
**List** | `list`
**View** | `view n/NAME`<br> e.g., `view n/Alicia Tan`; reply `2` if prompted to choose between matches
**Help** | `help`
**Exit** | `exit`
