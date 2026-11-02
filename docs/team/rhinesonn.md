---
layout: page
title: Rhineson Kok's Project Portfolio Page
---

### Project: BoothManagerPro

BoothManagerPro is a desktop contact manager for convention organisers who manage exhibitor representatives through typed commands. Built with Java 25 and JavaFX on AddressBook-Level 3 (AB3), it supports exhibitor company details, enquiry statuses, preferred contact methods, follow-up dates, tags, and local JSON storage.

Given below are my contributions to the project 

* **Code contributed**: [RepoSense link](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=rhinesonn&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

* **Enhancements implemented**:
  * **Interactive contact viewing (`view`)** ([PR #61](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/61), [PR #179](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/179)):
    * _What it does:_ Implemented `view n/NAME` to search all stored contacts using case-insensitive whole-word name matching. A single match displays its details; multiple matches produce numbered name-and-company choices, allowing the user to select a contact in the next command without changing stored data.
    * _Justification:_ Organisers need to retrieve contact details quickly when several representatives share a name word. Numbered choices let them resolve the ambiguity without retyping a longer query.
    * _Highlights:_ Implemented `LogicManager` selection state using `pendingViewChoices`, numeric range validation, stale-contact checks, and state cleanup across commands. Added command, parser, integration, and headless UI tests (`ViewCommandTest`, `ViewCommandParserTest`, `ViewCommandIntegrationTest`, `MainWindowTest`). Later refinements were shared work: Jenna added exact-full-name priority and repeated prompts after invalid selections, while Gerard preserved pending choices across `help` / `F1`.

  * **Tag management (`tag`)** ([PR #112](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/112), [PR #210](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/210)):
    * _What it does:_ Implements `tag NAME_OR_INDEX t/TAG [t/TAG]...`, enabling organisers to quickly update contact labels. Rather than requiring organisers to retype all existing tags, the command toggles tags: each supplied tag not currently on the contact is added, while each supplied tag already present is removed. Targets contacts either by displayed index or exact case-insensitive full name.
    * _Justification:_ During high-volume convention operations, organisers constantly classify contacts (e.g. `vip`, `sponsor`, `booth-ready`) or remove temporary labels without needing to invoke the heavier, multi-field `update` command.
    * _Highlights:_ Implemented dual targeting (index in displayed list vs case-insensitive full name), case-insensitive tag matching with canonical casing preservation, internal whitespace collapsing, and explicit user feedback indicating which tags were added, removed, and the resulting tag list. Authored `TagIntegrationTest` and extended `TagCommandTest` for case-insensitive matching and whitespace normalisation; teammates also contributed command and parser unit tests.

  * **Incremental contact card rendering and UI improvements** ([PR #205](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/205)):
    * _What it does:_ Implemented incremental rendering in `ResultDisplay`: up to 25 `ContactCard`s are created initially, and a scroll listener loads another batch when the vertical scroll value reaches at least `0.7`. Added clipboard copy context menus, layout safeguards, and Tab focus transitions.
    * _Justification:_ Creating every card at once caused lag when displaying large contact lists. Batching reduces the initial rendering work and defers additional card creation until the user scrolls.
    * _Highlights:_ Managed batch state and scroll-listener cleanup when results change, while retaining displayed indices and the final total count. Cards are created on the JavaFX Application Thread and retained until the next result replaces them. Added headless tests in `MainWindowTest` for batch loading, copy actions, layout safeguards, and keyboard focus.

  * **Input validation, text normalisation, and search** ([PR #176](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/176), [PR #179](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/179), [PR #210](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/210)):
    * _What it does:_ Hardened domain constraints and parsing across `Name`, `Company`, `Phone`, `Tag`, and `FindCommandParser`. Added Unicode NFC and whitespace normalisation to names, companies, tags, and search handling, converted the right curly apostrophe (`’`) to a straight apostrophe (`'`) in names, and supported patronymic conventions (`s/o`, `d/o`, `a/l`, `a/p`) in names without letting patronymic tokens act as false standalone search keywords in `find` or `view`.
    * _Justification:_ Exhibitors represent international organisations with varied naming formats and characters. Data copied from emails or PDFs often contains curly quotes or non-breaking spaces that previously broke parsing or duplicate detection.
    * _Highlights:_ Implemented regex and string normalisation utilities in `StringUtil`, broadened acceptable phone and company formats, enhanced `ContactMatchesFieldsPredicate` to support partial company/email/phone searches and status/method filters (`s/`, `m/`), and resolved multiple edge cases uncovered during internal alpha testing and PE Dry Run.

* **Contributions to the User Guide**:
  * Authored and maintained the [Viewing an exhibitor contact: `view`](../UserGuide.md#viewing-an-exhibitor-contact-view) section, including name matching, numbered selection, validation, and read-only behaviour. Teammates also updated this section as they refined the workflow.
  * Authored the documentation for [Updating contact tags: `tag`](../UserGuide.md#updating-contact-tags-tag), describing the toggling mechanism, case-insensitivity, index/name targeting, and example usages.
  * Documented keyboard controls, Tab focus transitions, clipboard copy shortcuts, and text normalisation behaviours across the application.
  * Maintained consistent command summary tables, prefix guidelines, and parameter conventions throughout the User Guide ([PR #81](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/81), [PR #198](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/198), [PR #210](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/210)).

* **Contributions to the Developer Guide**:
  * Restructured the [UI component](../DeveloperGuide.md#ui-component) architecture documentation, explaining the single output area, static `ContactCard` rendering, and the roles of the UI classes ([PR #196](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/196)).
  * Documented the [Viewing an exhibitor contact](../DeveloperGuide.md#viewing-an-exhibitor-contact) design: detailed `ViewCommand` execution, `LogicManager` pending choice retention, predicate filtering, and state cleanup rules.
  * Authored [Appendix: Planned Enhancements](../DeveloperGuide.md#appendix-planned-enhancements), detailing acknowledged limitations and formal future enhancement proposals for relative follow-up dates, undo/redo command history, CSV import/export, and direct contact archiving to distinguish implemented behaviour from future scope.
  * Authored [Appendix: Effort](../DeveloperGuide.md#appendix-effort), detailing the scope of architectural transformation from AB3, domain redesign, GUI restructuring, transactional persistence rollback, multi-instance file locking, and testing quality assurance.
  * Updated the [update sequence diagram](../DeveloperGuide.md#updating-an-exhibitor-contact), correcting constructor messages and activation notation and improving styling ([PR #196](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/196)).
  * Formulated manual testing instructions and expected outcomes for `view`, `tag`, parser error messages, and UI responsiveness.

* **Contributions to team-based tasks**:
  * **Team Lead & Project Management:** Coordinated project milestones, defined deliverable timelines, assigned responsibilities, and tracked issue resolution across iterations v1.1 to v1.6.
  * **Repository integration:** Merged 16 teammate pull requests and resolved integration conflicts across feature, bug-fix, and documentation work ([#69](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/69), [#78](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/78), [#79](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/79), [#89](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/89), [#99](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/99), [#105](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/105), [#163](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/163), [#178](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/178), [#180](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/180), [#193](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/193), [#201](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/201), [#207](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/207), [#211](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/211), [#212](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/212), [#213](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/213), [#233](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/233)).
  * **RepoSense Attribution:** Configured and maintained `_reposense/config.json` with team GitHub IDs, author names, and email aliases to ensure clean, accurate contribution tracking on the course dashboard.
  * **Project Documentation & Infrastructure:** Maintained project documentation site structure, updated `AboutUs.md`, `README.md`, `docs/index.md`, and set up portfolio templates for all team members ([PR #14](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/14), [PR #30](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/30), [PR #32](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/32), [PR #198](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/198)).

* **Review/mentoring contributions**:
  * Reviewed and approved teammate fixes, including storage recovery ([PR #211](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/211)) and bracketed phone labels ([PR #233](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/233)).
  * Coordinated internal alpha testing and triaged bug reports for PE-D resolution.

--------------------------------------------------------------------------------------------------------------------

### Contributions to the Developer Guide (Extracts)

The following passages are from the [view implementation section](../DeveloperGuide.md#viewing-an-exhibitor-contact) that I revised in [PR #196](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/196). They describe the integrated feature, including teammate refinements noted above.

#### Viewing an exhibitor contact

`ViewCommand` searches the full address book using `NameContainsKeywordsPredicate` and updates the filtered list to those matches without modifying stored contacts. A unique match returns `CommandResult.showingContact(...)`, rendering an unnumbered `ContactCard` in `ResultDisplay` alongside the formatted summary. If exactly one match has a full name equal to the query, ignoring case, `ViewCommand` filters to and displays that contact directly, so overlapping names such as `Alex Yeo` and `Alex Yeoh` can each be viewed without a numbered reply. Multiple matches return a numbered name-and-company prompt plus an immutable `CommandResult.viewChoices` snapshot displayed as text in `ResultDisplay`.

Both the numbered choice prompt and the resulting `ContactCard` use the same `ResultDisplay` output area. A new command's output replaces the previous prompt or card, requiring no persistent selection widget or list listener.

--------------------------------------------------------------------------------------------------------------------

### Contributions to the User Guide (Extracts)

Selected passages from the [view](../UserGuide.md#viewing-an-exhibitor-contact-view) and [tag](../UserGuide.md#updating-contact-tags-tag) sections, which I authored and updated alongside the corresponding features.

#### Viewing an exhibitor contact: `view`

Format: `view n/NAME`

* Matching follows unprefixed `find KEYWORD`: case-insensitive whole-name keywords, matching **any** supplied keyword. Patronymic markers (`s/o`, `d/o`, `a/l`, and `a/p`) connect words in a name and do not act as standalone keywords, so `view n/Raj s/o Kumar` matches contacts with `Raj` or `Kumar` without matching unrelated contacts containing `s/o`.
  For example, `view n/rhineson` matches both `rhineson` and `rhineson kok`; `rhin` does not match `rhineson`.
* Searches all stored contacts, including those hidden by a previous `find` command.
* No match produces an error and keeps the current filter unchanged.
* Viewing does not change saved data. It filters the current index set to the matches; use `list` to print everyone again. A unique match has index `1`.

#### Updating contact tags: `tag`

Toggles one or more tags on a contact: each supplied tag that is not yet on the contact is added, and each supplied tag that already exists on the contact is removed.

Format: `tag NAME_OR_INDEX t/TAG [t/TAG]...`

* Tag matching is case-insensitive, consistent with `add`, `update`, and `find`: `t/VIP` and `t/vip` refer to the same tag, and consecutive internal spaces are collapsed.

```text
tag Alicia Tan t/sponsor t/high-priority
tag 1 t/technology t/keynote
```
