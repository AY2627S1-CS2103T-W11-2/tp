---
layout: page
title: Cedric Cheng's Project Portfolio Page
---

## Overview

BoothManagerPro is a desktop contact manager designed for convention organisers managing exhibitor representatives through typed commands, featuring a command-driven UI with static contact cards, robust validation, and multi-field filtering.

## Summary of Contributions

### Code contributed

* [View my code contributions on RepoSense](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=seadrickbug&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-09-18T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=)

### Enhancements implemented

* **Implemented safe deletion by displayed index or full name** ([PR #67](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/67)).
  * Extended the parser and command layers so users can delete either the contact at an index in the current filtered display or the unique contact whose full name matches case-insensitively.
  * Prevented unsafe deletion when a name is partial, absent, or shared by multiple contacts, and returned specific recovery guidance to the user.
  * Completed the feature across command execution, parsing, error feedback, User Guide documentation, and unit/parser tests, including filtered-list indices and ambiguous-name edge cases.

* **Developed context-aware, keyboard-friendly command autocompletion** ([PR #99](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/99), [PR #121](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/121)).
  * Made **Tab** complete an unambiguous command word, supported field prefix, contact method, enquiry status, or list sort field.
  * Added command-specific assistance: supplying missing required `add` prefixes, `n/` for `find`/`view`, `o/` for `list`, and the first `t/` for `tag`, while avoiding duplicate non-repeatable prefixes.
  * Preserved normal keyboard navigation when no completion exists, moved focus to the output when appropriate, and kept the caret at the end when focus returns to the command box. UI tests cover successful, ambiguous, and no-completion paths.

* **Protected saved contacts against stale concurrent application sessions** ([issue #153](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/153), [PR #182](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/182)).
  * Added file-state tracking and OS-level locking so an older BoothManagerPro window cannot silently overwrite contacts saved by a newer window or another process.
  * Introduced an explicit command mutation contract so read-only commands such as `list`, `find`, `view`, and `help` do not rewrite the data file. A conflicting mutation is rejected through the existing save-failure rollback path, keeping the in-memory model consistent with disk.
  * Added storage-boundary tests and a two-session integration test that loads two independent models, saves through one session, verifies that a read-only command does not write, and confirms that a later stale mutation is rejected without data loss.

* **Improved parsing, normalisation, and validation robustness** ([PR #160](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/160), [PR #223](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/223)).
  * Normalised repeated spaces in names so duplicate detection and name search agree with stored display values; fixed numbered `view` selection so the selected contact becomes the active filtered result.
  * Added consistent empty-field handling for legacy edit fields and tags, with focused integration, parser, command, and model tests.
  * Expanded email local-part support to the conventional RFC `atext` punctuation set while deliberately rejecting uncommon quoted/comment forms; updated error guidance and added parameterised validation and add-command tests.

* **Maintained complete in-app help as the product evolved** ([PR #101](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/101), [PR #118](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/118), [PR #185](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/185)).
  * Kept command formats, examples, optional fields, keyboard controls, list sorting, follow-up-date search, status updates, and tag behaviour synchronised between `HelpCommand`, its automated tests, and the User Guide.

### Contributions to the User Guide

* Documented [keyboard operation and Tab completion](../UserGuide.md#working-through-commands) and maintained the [in-app help reference](../UserGuide.md#viewing-help-help) as new commands and fields were introduced.
* Wrote the behaviour, constraints, examples, and error-recovery guidance for [deleting a contact by name or displayed index](../UserGuide.md#deleting-an-exhibitor-contact-delete).
* Documented the stale-session safeguard and mutation-only persistence behaviour under [Saving the data](../UserGuide.md#saving-the-data) and clarified the risks under [Editing the data file](../UserGuide.md#editing-the-data-file).
* Updated the [add-command field rules](../UserGuide.md#adding-an-exhibitor-contact-add) for supported email punctuation and contributed corrections on name normalisation, duplicate feedback, and empty edit fields ([PR #160](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/160), [PR #223](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/223)).

### Contributions to the Developer Guide

* Updated the [Architecture](../DeveloperGuide.md#architecture) and [Logic component](../DeveloperGuide.md#logic-component) explanations using `delete 1` as the end-to-end example, covering parsing, displayed-index resolution, model mutation, persistence, and the returned `CommandResult` ([PR #149](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/149)).
* Specified the main success scenario and extensions for [UC03: Delete an exhibitor contact](../DeveloperGuide.md#uc03-delete-an-exhibitor-contact), including missing/invalid input, exact-name matching, ambiguity, and out-of-range indices.
* Documented the concurrency safeguard in the [Storage component](../DeveloperGuide.md#storage-component), together with its rollback guarantee and cross-session regression-test strategy ([PR #182](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/182)).
* Contributed the initial project scoping for the target user/value proposition ([PR #34](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/34)) and non-functional requirements ([PR #43](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/43)).
* Added or updated these UML diagrams:
  * `DeleteSequenceDiagram` — created and refined the detailed delete-command interaction ([PR #134](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/134)).
  * `LogicClassDiagram` — updated the partial Logic class structure to match the implementation ([PR #137](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/137)).
  * `ArchitectureSequenceDiagram` — updated the cross-component delete-and-save flow ([PR #137](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/137)).
  * Corrected UML notation and implementation alignment in the delete and follow-up-date diagrams in a pushed follow-up ([PR #229](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/229), open at the time of writing).

### Contributions to team-based tasks

* Performed systematic alpha testing and filed ten reproducible defect reports covering slash-containing companies, failed-save state, pathological email inputs, Unicode normalisation/whitespace, legal patronymic names, and parser feedback ([#129](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/129), [#130](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/130), [#131](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/131), [#132](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/132), [#133](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/133), [#169](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/169), [#170](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/170), [#171](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/171), [#173](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/173), [#175](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/175)).
* Acted as repository integrator by merging 14 teammate PRs, including the major `view`, `add`, on-demand list, `update`, list/sort refinement, UML, and regression-test contributions ([#61](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/61), [#66](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/66), [#76](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/76), [#97](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/97), [#123](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/123), [#125](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/125), [#126](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/126), [#165](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/165), [#181](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/181), [#191](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/191)).
* Updated shared project metadata and documentation, including my team profile ([PR #23](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/23)), the README core-feature summary ([PR #37](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/37)), and the initial DG product scope/NFRs ([PR #34](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/34), [PR #43](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/43)).

### Review/mentoring contributions

* Conducted a detailed UML accuracy review of [PR #126](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/126), raising five specific points on dependency direction, composition versus dependency, factory-method calls, object-creation messages, and `Optional` return values ([example review comment](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/126#discussion_r4388536657)).
* Reviewed the code-quality cleanup in [PR #123](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/123), the list/sort refinement in [PR #125](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/125), and approved the team-profile addition in [PR #15](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/15).

### Contributions beyond the project team

* Smoke-tested another student's `kiwi.jar` on macOS with OpenJDK 25, attached screenshots, followed the User Guide from a clean launch, and exercised invalid dates, reversed start/end dates, and deletion of a missing task ([forum evidence](https://github.com/NUS-CS2103-AY2627-S1/forum/issues/222#issuecomment-5690663285)).
* Helped another team reason about whether to merge or branch from a teammate's dependent PR, including the need for review readiness and later rebasing/updating ([forum response](https://github.com/NUS-CS2103-AY2627-S1/forum/issues/589#issuecomment-6038078651)).
* Shared a concise distinction between the Observer pattern and event-driven architecture while noting that the two can be used together ([forum response](https://github.com/NUS-CS2103-AY2627-S1/forum/issues/716#issuecomment-6458751532)).
