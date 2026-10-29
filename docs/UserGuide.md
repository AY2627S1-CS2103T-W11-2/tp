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
   ![Command interface on startup](images/Ui%20-%20start%20state.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to print command usage instructions.<br>
   Some example commands you can try:

   * `list` : Lists all contacts. Add `o/name`, `o/company`, or `o/status` to sort them, e.g. `list o/status`.

   * `add n/John Doe c/Example Ltd e/johnd@example.com p/98765432` : Adds an exhibitor contact named `John Doe`.

   * `delete Alex Yeoh` : Deletes the contact named Alex Yeoh.

   * `clear confirm` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

### Working through commands

* Type a command and press **Enter**. Command words and prefixes are case-sensitive: use `list`, not `List`.
* The command box starts with keyboard focus. Press **Ctrl+L** on Windows/Linux or **Command+L** on macOS to return to it. Use `help` or **F1** for the guide, and `exit` to close the app.
* Press **Tab** to complete an unambiguous command word, field prefix, contact method, enquiry status, or list sort
  field. It also supplies the next required `add` prefix, `view n/`, `find n/`, `list o/`, or the first `tag t/` after
  a target. If no completion is available, **Tab** resumes normal focus navigation. When focus returns to the command
  box, the cursor is placed at the end of your input, so typing continues the command instead of replacing it.
* Each launch starts with a ready prompt. Saved contacts remain available but are not automatically printed.
* Results appear in the output area below the command box. `list`, `find`, and `view` show matching contacts as separate cards; other commands and errors show text. Each card shows the enquiry status as a coloured badge beside the name, and the card's left edge uses the same colour: amber for `Needs follow-up`, blue for `In progress`, green for `Completed`, and red for `Cancelled`. Cards are display-only and cannot be clicked or selected; scroll the output area to see more. There is no separate contact-list or details panel.
* `list` prints every contact with a number and details. `find` prints the match count and numbered matching contacts; it works without running `list` first.
* `view` prints a matching contact's details or numbered choices. Reply with a number only when `view` asks you to choose.
* Each command replaces the previous output. The output fills the available space and scrolls for longer results.
  When no autofill is available, use **Tab** to focus it and the arrow keys to read it; **Ctrl+L** or **Command+L**
  returns to command entry.
* For `update INDEX`, `tag INDEX`, and `delete INDEX`, use the numbers from the current `list`, `find`, or `view` matches. A successful `add`, `update`, or `tag` resets the filter; run `list` or `find` again before choosing another index. After deleting a contact, later indices shift down.
* Company and contact method show `Not specified` when absent. Tags appear in saved order, with `None` for an empty tag set. Newly added contacts use `Not provided` for the legacy address field.

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed fields can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help` and `exit`, are ignored.<br>
  For example, `help 123` is interpreted as `help`. The exception is `list`, which accepts only an optional `o/name`, `o/company`, or `o/status` and rejects anything else.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Prints every command's description, format, example, and the User Guide link in the text output area. This includes
optional fields and features such as list sorting, follow-up-date searches, status updates, and tag toggling. You can
also press **F1**. Keyboard focus stays in the command box.

Format: `help`


### Adding an exhibitor contact: `add`

Adds an exhibitor or representative with a required company, email, and phone number.

Format: `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...`

| Field | Rules |
| --- | --- |
| `n/NAME` | 1-80 characters; at least one letter. Allows letters, spaces, hyphens, apostrophes, full stops, and `s/o` or `d/o` between words. |
| `c/COMPANY` | 1-100 characters; cannot be blank. |
| `e/EMAIL` | Valid email format, such as `alicia@example.com`; stored in lowercase. |
| `p/PHONE` | 7-15 digits, optionally starting with `+`. Spaces and hyphens are removed. |
| `m/METHOD` | Optional: `email`, `phone`, or `other`, ignoring case. Omitted method is `Not specified`. |
| `s/STATUS` | Optional: `Needs follow-up` (default), `In progress`, `Completed`, or `Cancelled`, ignoring case. |
| `f/YYYYMMDD` | Optional calendar date, e.g. `20261025`; omitted date displays as `None`. Years 0001-9999; invalid dates are rejected. |
| `t/TAG` | Optional and repeatable: 1-30 characters, nonblank, without `/`. Identical tags are ignored; comparisons are case-sensitive. |

Leading and trailing spaces are removed from field values. Fields can appear in any order. Only `t/` may repeat. Dates contain no time or timezone; past dates are accepted. Omit `f/` for no date (do not enter `f/none`). Status does not advance automatically. The old `a/ADDRESS` field is not accepted by `add`.

Examples (enter each command on one line):

```text
add n/Alicia Tan c/TechNova Pte Ltd e/alicia@technova.com p/91234567 m/email t/technology t/high-priority
add n/Ben Lim c/GreenWorks e/BEN@example.com p/+65 8765-4321 s/In progress f/20261025
```

The first command displays:

```text
Added Alicia Tan at TechNova Pte Ltd.
```

![Add command confirmation](images/Ui%20-%20add%20command.png)

*The output confirms the previous addition of John Lim; the command box contains a separate John Doe command.*

After a successful add:

* Any previous filter is cleared. The output shows the addition confirmation.
* Use `view n/NAME` to print the saved details, or `list` to print all contacts.
* The command box clears and keeps keyboard focus, ready for the next command.

The second example stores `ben@example.com` and `+6587654321`. A subsequent `view n/Ben Lim` shows `Not specified` for the omitted method and `None` for an empty tag set.

If validation or duplicate detection fails, the typed command remains available for correction. No contact is added.

**Duplicate handling**

A contact is rejected if any stored contact has the same normalised email, or the same name and company ignoring case. Names are stored after surrounding whitespace is removed and consecutive spaces are collapsed, so `Chloe Ng` and `chloe  ng` are treated as the same name. This includes contacts hidden by the current search. Sharing only a name or company is allowed if the email is different. Legacy contacts without a company are compared by email only.

For a match against Alicia's record above:

```text
This contact may already exist: Alicia Tan at TechNova Pte Ltd.
Use the update command if you want to change the existing contact.
```

If the email matches one record and the name/company matches another, both conflicting contacts are listed with their emails. No contact is added; correct the input before retrying.

**Input errors**

| Problem | Feedback |
| --- | --- |
| Missing required prefixes (example: company and email) | `Missing required fields: c/, e/. Supply a value after each prefix.` |
| Repeated fields (example: name and phone) | `Repeated fields: n/, p/. Keep one value per field; only t/ may repeat.` |
| Unknown prefix (example: `x/`) | `Unknown field prefix: x/. Use n/, c/, e/, p/, m/, s/, f/, or t/.` |
| Text before the first prefix | `Unexpected text before the first field.` followed by a complete example command. |
| Empty field (example: `c/`) | `Empty field: c/. Supply a value after this prefix.` |
| Invalid name | `Invalid value for n/: Names must be 1 to 80 characters and contain a letter. Use only letters, spaces, hyphens, apostrophes, full stops, or s/o and d/o between words.` |
| Invalid company | `Invalid value for c/: Company name must contain 1 to 100 characters.` |
| Invalid email | `Invalid value for e/: Email address is invalid.` |
| Invalid phone | `Invalid value for p/: Phone number must contain 7 to 15 digits.` |
| Invalid method | `Invalid value for m/: Contact method must be email, phone, or other.` |
| Invalid status | `Invalid value for s/: Enquiry status must be Needs follow-up, In progress, Completed, or Cancelled.` |
| Invalid date | `Invalid value for f/: Follow-up date must be a valid date in YYYYMMDD format (years 0001 to 9999), e.g. 20261025.` |
| Invalid tag | `Invalid value for t/: Tag must be 1 to 30 characters and cannot contain '/'.` |

Each missing-field or repeated-field error lists all affected prefixes in n/, c/, e/, p/, m/, s/, f/ order. A present but empty field is reported separately from a missing prefix. Words between prefixes belong to the preceding field; prefix-like tokens are reserved. If several errors occur, unknown prefixes/preamble are checked first, then repeated fields, then missing prefixes, then values in n/, c/, e/, p/, m/, s/, f/, t/ order. Invalid input and duplicates do not add a contact.

### Listing all exhibitor contacts: `list`

Shows every stored exhibitor contact with its name, company, email, phone, preferred contact method, enquiry status, follow-up date, and tags,
followed by the total number of contacts. You can optionally sort the list by name, company, or enquiry status.

Format: `list`, `list o/name`, `list o/company`, or `list o/status`

* `list` with no parameters shows contacts in the order they were added.
* `list o/name` sorts contacts alphabetically by name. Contacts with the same name are ordered by company, then by email.
* `list o/company` sorts contacts alphabetically by company, and by name within the same company.
  Contacts without a company (possible only in older records) appear last.
* `list o/status` sorts contacts by enquiry status in workflow order: `Needs follow-up` first, then `In progress`,
  then `Completed`, then `Cancelled`, so the contacts you still need to act on come first. Contacts with the same status are ordered by name.
* Sorting ignores upper and lower case, so `alicia tan` and `Alicia Tan` sort together. The sort field itself is also
  case-insensitive: `o/Name` works like `o/name`.
* Sorting changes only the displayed order, not the saved data. The numbers shown follow the sorted order, so
  `list o/name` followed by `delete 1` deletes the first contact alphabetically.
* The sort order stays in effect for later `find` results and after `add`, `update`, or `tag`, until you run `list` with no parameters again.
* Only one `o/` is allowed. Anything else, such as `list 3`, `list o/email`, or `list o/name o/company`, is rejected with an explanation.
* `list` shows numbered contact cards in the output area, followed by the total. With no contacts, it prints the no-contacts message below.
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
Enquiry status: Needs follow-up
Follow-up date: None
Tags: technology, high-priority

2. John Smith at GlobalTech Inc
Email: john@globaltech.com
Phone: +6562345678
Contact method: phone
Enquiry status: Needs follow-up
Follow-up date: None
Tags: hardware

──────────────────────────────────────
Total contacts: 2
```

Example: `list o/company` shows the same contacts ordered by company, with the heading
`Exhibitor Contact List (sorted by company)`. With the contacts above, John Smith (GlobalTech Inc) is listed
before Alicia Tan (TechNova Pte Ltd).

If there are no stored contacts:

```text
No contacts found.
Use the 'add' command to add a new exhibitor contact.
```

![List command showing numbered contact cards and a total count](images/Ui%20-%20list%20command.png)

### Viewing an exhibitor contact: `view`

Shows the chosen contact's details as a card in the command output area.

Format: `view n/NAME`

* Matching follows unprefixed `find KEYWORD`: case-insensitive whole-name keywords, matching **any** supplied keyword.
  For example, `view n/rhineson` matches both `rhineson` and `rhineson kok`; `rhin` does not match `rhineson`.
* Names must contain 1 to 80 characters and at least one letter. Letters, spaces, hyphens, apostrophes,
  full stops, and `s/o` or `d/o` between words are supported, for example `Anne-Marie O'Neil` or `Raj s/o Kumar`.
* Searches all stored contacts, including those hidden by a previous `find` command.
* If several contacts match, the output lists numbered choices with their companies and asks you to choose.
  Enter the corresponding number as your next command, for example `2`.
  Choices follow the current sort order (for example, after `list o/company`), so each number is also that contact's
  index for `update`, `tag`, and `delete`.
  The output is replaced with the chosen contact's details. A single match displays details immediately.
* If exactly one contact's full name equals `NAME`, ignoring case and extra spaces, that contact's details are shown
  immediately, even when other names share a word. For example, `view n/Alex Yeo` shows `Alex Yeo` directly
  instead of also listing `Alex Yeoh`; `view n/Alex` still lists both.
* An invalid number shows the valid range followed by the numbered choices again, and lets you try again. Another successfully parsed command cancels the pending choice, even if execution fails. A command with invalid syntax leaves the choice pending. Cancelling a choice clears its prompt; a subsequent bare number is no longer a selection reply.
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
Enquiry status: Needs follow-up
Follow-up date: None
Tags: technology, high-priority
```

Create these records with `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...`.

![View command showing a matching exhibitor contact](images/Ui%20-%20view%20command.png)

### Updating an exhibitor contact: `update`

Updates only the supplied attributes. All values follow the rules in [Adding an exhibitor contact](#adding-an-exhibitor-contact-add).

Format: `update NAME_OR_INDEX [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...`

- Use an exact full name, ignoring case. Name lookup includes contacts hidden by a filter.
- If names conflict, run `list` or `find` and use the displayed index: `update 2 s/In progress`. This is a current list position, not a permanent contact ID or a numbered view reply.
- Supply at least one field. Omitted attributes stay unchanged. `n/` changes the name; the text before the first prefix selects the original contact.
- Only `t/` may repeat. Supplied tags replace existing tags, with duplicates ignored in input order. Empty `t/` clears tags; do not combine it with nonempty tags.
- Empty `f/` clears the date. Other blank fields are rejected. Status may be changed to any of the four supported values; there is no enforced transition order.
- Unknown prefixes, invalid values, missing/ambiguous targets, and duplicate email or name/company combinations are rejected without changing contacts or saved data. Duplicate checks include hidden contacts.
- Success prints `Updated NAME at COMPANY.`, clears the filter and command input, and retains keyboard focus. Run `list` or `find` before reusing an index, or `view n/NAME` to check details.
- Legacy address is preserved.

```text
update Alicia Tan p/+65 9876-5432 m/phone s/In progress f/20261025
update Alicia Tan n/Alicia Lim c/New Company
update 2 s/Completed f/ t/
update Alicia Tan s/Cancelled
```

### Updating contact tags: `tag`

Toggles one or more tags on a contact: each supplied tag that is not yet on the contact is added, and each supplied tag that already exists on the contact is removed.

Format: `tag NAME_OR_INDEX t/TAG [t/TAG]...`

- Use an exact full name, ignoring case, or a displayed index from `list`, `find`, or `view` (`tag 2 t/vip`). Name lookup includes contacts hidden by a filter; ambiguous names require a displayed index.
- Supply at least one nonblank `t/TAG` field (`1-30` characters, without `/`). Repeated identical `t/TAG` values in the same command are ignored in input order.
- Tag matching is case-sensitive, consistent with `add` and `update`: `t/VIP` and `t/vip` are distinct tags.
- Only `t/` is accepted. Unknown prefixes, missing or blank `t/`, and invalid targets are rejected without changing contacts, filters, or saved data.
- Success prints the exact tag changes made (`Updated tags for NAME at COMPANY. Added: .... Removed: .... Current tags: ....`), clears the filter and command input, and retains keyboard focus. All other contact attributes remain unchanged.

```text
tag Alicia Tan t/sponsor t/high-priority
tag 1 t/technology t/keynote
```

### Finding and filtering contacts: `find`

Use prefixed criteria to match a complete name or a whole name word, and complete values for other fields:

Format: `find [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [t/TAG] [f/YYYYMMDD]...`

* Supply at least one non-empty criterion. Each value must follow the same format as when adding a contact;
  phone values accept spaces and hyphens, just as `add` does. Tabs inside phone numbers are not accepted.
* Names, companies, emails, and tags ignore case and surrounding whitespace. Phone comparisons ignore spaces and hyphens.
* Name criteria (`n/`) match a complete name or a single whole name word, ignoring case and repeated spaces.
  For example, `find n/Alex` matches `Alex Yeo` and `Alex Yeoh`, but not `Alexandra Tan`.
  A multi-word value such as `n/Alex Yeo` must match the complete name.
* Name, company, and tag searches use Unicode-aware case-insensitive matching. For example, `find n/ipek`
  matches the word `İpek`, and `find c/οσ` matches the complete company value `ΟΣ`.
* Repeat a prefix for alternatives (OR). Different fields must all match (AND).
* Tags match complete tag names; any matching tag satisfies that field.
* Duplicate criteria have no effect. Results always search all stored contacts, not just the current displayed list.
* Unknown prefixes and empty/invalid values are rejected without changing the filter or stored contacts.
* `find` prints the number of matches followed by each matching contact's number, name, company, email, phone, method, enquiry status, follow-up date, and tags. No preliminary `list` command is needed.
* With no matches, it prints `0 contact(s) found.` and no contact details.
* Company (`c/`) matches the complete stored company name. Legacy contacts without a company do not match this field.
* Follow-up date (`f/`) matches the exact stored calendar date in `YYYYMMDD` format, using the same date validation as `add` and `update`. Contacts without a follow-up date do not match. Past dates are accepted; date ranges and overdue searches are not supported.
* Repeat `f/` to match any of the supplied dates (OR), or combine it with another field to require both (AND). An empty `f/` is invalid in `find`; it does not search for contacts without dates. Invalid dates are rejected without changing the existing results or saved data.
* To find contacts scheduled for today, supply today's date explicitly, for example `find f/20261029` on 29 October 2026. This matches that date only; it does not include earlier dates or check enquiry status.
* Enquiry status (`s/`) and contact method (`m/`) are not searchable.
* `list` restores all contacts. A result count of zero means no contacts matched.

Examples:

* `find n/Alice Pauline` matches the complete name; `find n/Alice` matches any contact with the whole name word `Alice`.
* `find n/Alice Pauline n/Benson Meier t/friends` returns either named contact if they also have the `friends` tag.
* `find e/alice@example.com p/9435-1253` requires both email and phone to match.
* `find c/TechNova Pte Ltd t/high-priority` requires both company and tag to match.
* `find t/technology t/food` returns contacts tagged with either label.
* `find f/20261025` returns contacts whose follow-up date is 25 October 2026.
* `find f/20261025 f/20261026 t/high-priority` returns high-priority contacts due on either date.
* `update Alicia Tan f/20261028`, then `find f/20261028`, changes Alicia's date using the existing update command and finds contacts due on the new date. Use `update Alicia Tan f/` to clear her date; she will no longer match any date criterion.

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
* `find alex david` returns `Alex Yeoh` and `David Li` from the sample contacts. Both matches are shown as numbered cards in the output area.

![Find command showing two numbered matches for John](images/Ui%20-%20find%20command.png)

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

![Delete command confirming the removal of John Lim](images/Ui%20-%20delete%20command.png)

### Clearing all entries: `clear confirm`

Deletes all stored contacts, including those hidden by a filter. To prevent accidental deletion of all data, confirmation is required.

Format: `clear confirm`

* Running `clear` without `confirm` does not delete any contacts and prompts you to type `clear confirm`.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

BoothManagerPro automatically saves data after successful `add`, `update`, `tag`, `delete`, and `clear`
commands. Read-only commands such as `list`, `find`, `view`, `help`, and `exit` do not write the data file. You do
not need to save manually. If saving fails, the in-memory change is rolled back and an error appears.

If the data file changes after a BoothManagerPro window loads it, that window refuses to save stale data. Restart
that window to load the latest contacts before retrying the command. This prevents an older window from silently
overwriting work saved by another window or process.

### Editing the data file

By default, BoothManagerPro saves data as `data/addressbook.json`, relative to the folder from which you launch the app. Close the app before editing or replacing this file so that a running session does not overwrite your changes.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, BoothManagerPro starts with an empty contact collection and shows a loading-failure warning. Before a later command saves new data, the original file is copied to a unique backup beside it, named `addressbook.json.recovery-<unique suffix>.json`. Existing backups are not replaced. If this backup cannot be created, saving is blocked and an error is shown.<br>
To recover contacts, close BoothManagerPro first. Keep a separate copy of the recovery backup, repair the invalid values, and restore the repaired contents to `data/addressbook.json` before restarting. Backups are not loaded automatically. We still recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause BoothManagerPro to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Planned features

Searching enquiry status, reminders, data archiving, CSV import/export, and undo remain planned. Exact follow-up-date search is available through `find f/YYYYMMDD`.

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Close BoothManagerPro on both computers. Copy `data/addressbook.json` from the old launch folder into the new launch folder's `data` directory, backing up any existing file first. Start the app from the new folder and run `list` to check your contacts.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...` <br> e.g., `add n/James Ho c/Example Ltd e/jamesho@example.com p/22224444 m/phone t/technology`
**Clear** | `clear confirm`
**Delete** | `delete NAME` or `delete INDEX`<br> e.g., `delete Alex Yeoh`
**Update** | `update NAME_OR_INDEX [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...`<br> e.g., `update Alicia Tan s/Completed f/`
**Tag** | `tag NAME_OR_INDEX t/TAG [t/TAG]...`<br> e.g., `tag Alicia Tan t/high-priority t/sponsor`
**Find** | `find [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [t/TAG] [f/YYYYMMDD]...` or `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find f/20261025 t/high-priority`
**List** | `list`, `list o/name`, `list o/company`, or `list o/status`<br> e.g., `list o/status`
**View** | `view n/NAME`<br> e.g., `view n/Alicia Tan`; reply `2` if prompted to choose between matches
**Help** | `help`
**Exit** | `exit`
