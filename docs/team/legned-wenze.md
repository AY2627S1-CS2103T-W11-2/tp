---
layout: page
title: Li Wenze's Project Portfolio Page
---

### Project: BoothManagerPro

BoothManagerPro helps convention organisers manage exhibitor contacts through typed commands in a desktop app.

This page records my contributions, with links to the original pull requests. Some implementations were later extended or reorganised by teammates, so the current line attribution does not show all of the original work.

**Code contributed:** [RepoSense](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=legned-wenze&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

### Features and fixes

* **Multi-field search:** I implemented the first field-based `find` command, covering names, companies, emails, phone numbers and tags. Repeated values within a field are alternatives (OR), while different fields must all match (AND). I added parsing, validation and integration tests. [PR #62](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/62)
* **Follow-up date filtering:** I added exact-date searches through `f/YYYYMMDD`, including repeated dates, combinations with other fields and invalid-date checks. Tests cover updates, cleared dates, reloading saved contacts and indices after sorting. [PR #114](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/114)
* **Name and Unicode matching:** I fixed Unicode case-insensitive name searches, then added complete-name-word matching and Unicode case equivalence for company and tag searches. I updated the related instructions and tests. The case-folding implementation was later moved into `StringUtil` for reuse. Teammates subsequently extended search to partial company/email/phone values and additional fields. [PR #86](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/86), [PR #161](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/161)
* **Display contacts on request:** I changed command results and the main window so the contact list is shown after an explicit display request rather than remaining visible after every command. The card layout was developed further by teammates. [PR #76](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/76)
* **Recovery after invalid saved data:** I handled null entries and invalid JSON data without an uncaught startup failure. I implemented recovery backups that preserve the original bytes before replacement and block saving if the backup fails, together with startup feedback and regression tests. This is backup protection, not automatic restoration of damaged contacts. [PR #163](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/163)
* **Rollback of display order:** I added comparator snapshots and restoration when command execution or saving fails, so a failed operation does not leave displayed indices in a different order. [PR #194](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/194)
* **Safer JSON writes:** I implemented writing to a temporary sibling file before atomically replacing the destination. Tests check replacement, write failure, preservation of the original file and temporary-file cleanup errors. Teammates later added missing-parent-folder handling and related storage fixes. [PR #195](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/195)

### Testing and bug reports

* I added integration tests for searching, date filtering and recovery that exercise real command routing and JSON storage. They check saved data as well as feedback and in-memory results. The tests are included in the feature and fix PRs linked above.
* I added parser boundary and update-feedback regression tests, and extracted common storage/logic setup used by ten test classes. [PR #193](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/193), [PR #184](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/184)
* For alpha testing, I reported five issues: null saved-data failures, recovery warnings/backups, concurrent-session overwrites, Unicode field matching and outdated date-filter documentation. The team addressed them; reporting an issue and implementing its fix are separate contributions. In particular, Cedric implemented the concurrent-session protection. [#151](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/151), [#152](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/152), [#153](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/153), [#154](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/154), [#155](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/155)

### User Guide and Developer Guide

* I documented field-based search, exact follow-up date filtering and recovery behavior, and kept the corresponding command examples consistent with those changes.
* I refreshed the User Guide introduction, download instructions, examples and add-command guidance. I replaced six screenshots for startup, add, list, view, find and delete using the running interface. I also removed an obsolete Help Window workaround. [PR #184](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/184), [PR #87](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/87)
* I added the follow-up-date class and sequence diagrams, their rendered images and the accompanying Developer Guide explanation. I also added search assertions and logging. [PR #117](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/117)
* I updated recovery and testing instructions with the storage fixes. [PR #163](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/163)

### Team contributions and reviews

* I contributed the initial UI mockup and refined the non-functional requirements. [PR #45](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/45), [PR #46](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/46)
* I left five inline review comments on teammates' PRs: [assertions](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/115#discussion_r4356744177), [update logging](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/115#discussion_r4356744182), [unambiguous completion](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/99#discussion_r4358062229), [rejected-command tests](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/112#discussion_r4414343643), and [display-only sorting](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/103#discussion_r4414344911).
