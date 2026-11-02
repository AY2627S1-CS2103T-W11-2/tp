---
layout: page
title: Li Wenze's Project Portfolio Page
---

### Project: BoothManagerPro

BoothManagerPro helps convention organisers manage exhibitor contacts through typed commands in a desktop app.

My main work covers contact search, follow-up-date filtering, data recovery, regression testing and user documentation. The original pull requests below record the implementation, tests and documentation delivered at each stage.

**Code contributed:** [RepoSense](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=legned-wenze&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other)

### Implementation, fixes and testing

* **Multi-field search:** I implemented the first field-based `find` command, covering names, companies, emails, phone numbers and tags. Repeated values within a field are alternatives (OR), while different fields must all match (AND). I added parsing, validation and integration tests. [PR #62](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/62)
* **Follow-up date filtering:** I added exact-date searches through `f/YYYYMMDD`, including repeated dates, combinations with other fields and invalid-date checks. Tests cover updates, cleared dates, reloading saved contacts and indices after sorting. [PR #114](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/114)
* **Name and Unicode matching:** I fixed Unicode case-insensitive name searches, then added complete-name-word matching and Unicode case equivalence for company and tag searches. I updated the related instructions and tests. The case-folding implementation was later moved into `StringUtil` for reuse. Teammates subsequently extended search to partial company/email/phone values and additional fields. [PR #86](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/86), [PR #161](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/161)
* **Display contacts on request:** I changed command results and the main window so the contact list is shown after an explicit display request rather than remaining visible after every command. The card layout was developed further by teammates. [PR #76](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/76)
* **Recovery after invalid saved data:** I handled null entries and invalid JSON data without an uncaught startup failure. I implemented recovery backups that preserve the original bytes before replacement and block saving if the backup fails, together with startup feedback and regression tests. The saved backup supports manual repair and recovery. [PR #163](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/163)
* **Rollback of display order:** I added comparator snapshots and restoration when command execution or saving fails, so a failed operation does not leave displayed indices in a different order. [PR #194](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/194)
* **Safer JSON writes:** I implemented writing to a temporary sibling file before atomically replacing the destination. Tests check replacement, write failure, preservation of the original file and temporary-file cleanup errors. Teammates later added missing-parent-folder handling and related storage fixes. [PR #195](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/195)
* **Protection against hidden index operations:** I fixed beta-test finding 1: `delete 1`, `update 1` and `tag 1` could act on contacts before any results were displayed. The window now checks its displayed contact snapshot before an indexed mutation. Tests cover startup, restart, help/error feedback, changed sorting, name targeting and view choices, and check that rejected operations leave saved JSON unchanged. Gerard merged this fix after its Linux, Windows, macOS and Codecov checks passed. [PR #226](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/226)
* **Help under different system languages:** I fixed command-heading capitalisation under Turkish locale using `Locale.ROOT`, with a test that reloads the help text under that locale and restores the previous setting afterwards. [PR #220](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/220)
* **Email search compatibility:** I corrected the search character rule so addresses accepted by the updated email validator, such as `o'connor@example.com` and `sales%expo@example.com`, can also be searched. I reproduced the failure, checked full and partial matching before and after JSON reload, and verified that searches preserve the saved file. Submitted in [PR #235](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/235).

### Search extension development

I implemented and tested the extensions below in [PR #217](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/217). The team chose to keep the final release's existing feature scope, so I withdrew this proposal.

* **Relative follow-up searches and date ranges:** I extended `find` with `f/today`, `f/due`, `f/overdue` and inclusive `f/YYYYMMDD..YYYYMMDD` intervals. The implementation captures the local date once per search, validates range endpoints and order, and parses each range once for reuse across contacts. Repeated date criteria combine with OR; date, status and tag fields combine with AND. [PR #217](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/217)
* **Date-keyword completion:** I added Tab completion for relative keywords in `find`, using the date-filter enum as the source of choices. UI regression tests check keyword case, repeated criteria, literal dates and the separate input rules for `add` and `update`. Included in [PR #217](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/217).
* **Search workflow regressions:** I added tests for filtering followed by indexed updates, tag changes and deletion; rejected duplicate updates; date clearing and restart; stale-session saves; failed delete/clear rollback; and pending view-choice cancellation. Tests verify the full collection, displayed contacts and saved JSON. Range tests cover inclusive endpoints, single-day intervals, leap dates, year limits and invalid ranges. Included in [PR #217](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/217).

I used Codex to run full local `check coverage` verification for the implementation and again after integrating the team's latest changes. The final local run passed 638 tests with 3 skipped, including Checkstyle.

### History of shared search code

The search implementation has evolved through several members' contributions. These links show where my original work entered the project and how it was later reused or extended:

| Original contribution | Later development |
| --- | --- |
| I introduced `ContactMatchesFieldsPredicate`, field parsing, and the same-field OR / cross-field AND structure in [PR #62](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/62). | The predicate was later extended with partial company/email/phone searches and status/contact-method fields in [commit f162d398](https://github.com/AY2627S1-CS2103T-W11-2/tp/commit/f162d398b628fad8e4f060b6e4a316ae00b45760). Those extensions build on the existing search structure. |
| I added Unicode-aware name matching in [PR #86](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/86), then extended case folding to company and tag matching in [commit da8f5c4b](https://github.com/AY2627S1-CS2103T-W11-2/tp/commit/da8f5c4b856d0ace796ab55ba428ac7e2f327467), part of PR #161. | [Commit 2fe5fd7e](https://github.com/AY2627S1-CS2103T-W11-2/tp/commit/2fe5fd7e1330a9e592a525bd054ba7c2fdae7854) moved this algorithm into `StringUtil.foldCase`, added a null check and reused it for shared tag handling. |

The original PRs provide evidence of my implementation and tests alongside the later changes credited to teammates. Both stages contributed to the final behaviour.

### Testing and bug reports

* I added integration tests for searching, date filtering and recovery that exercise real command routing and JSON storage. They check saved data as well as feedback and in-memory results. The tests are included in the feature and fix PRs linked above.
* I added parser boundary and update-feedback regression tests, and extracted common storage/logic setup used by ten test classes. [PR #193](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/193), [PR #184](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/184)
* For alpha testing, I reported five issues: null saved-data failures, recovery warnings/backups, concurrent-session overwrites, Unicode field matching and outdated date-filter documentation. The team addressed them; reporting an issue and implementing its fix are separate contributions. In particular, Cedric implemented the concurrent-session protection. [#151](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/151), [#152](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/152), [#153](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/153), [#154](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/154), [#155](https://github.com/AY2627S1-CS2103T-W11-2/tp/issues/155)

### User Guide and Developer Guide

* I documented field-based search, exact follow-up date filtering and recovery behavior, and kept the corresponding command examples consistent with those changes.
* I refreshed the User Guide introduction, download instructions, examples and add-command guidance. I replaced six screenshots for startup, add, list, view, find and delete using the running interface. I also removed an obsolete Help Window workaround. [PR #184](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/184), [PR #87](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/87)
* I added the follow-up-date class and sequence diagrams, their rendered images and the accompanying Developer Guide explanation. I also added search assertions and logging. [PR #117](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/117)
* I updated recovery and testing instructions with the storage fixes. [PR #163](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/163)
* I clarified how to select the correct contact after searching or sorting and how to preserve, repair and restore a recovery backup. [PR #218](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/218)
* I documented when index commands require fresh contact results, including after startup, help and error feedback, and added manual checks for the beta-test fix. [PR #226](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/226)
* For the additional search implementation, I wrote a due-follow-up workflow, date-range examples, a troubleshooting table and manual regression scenarios. I also corrected the DG's storage description to match byte-level file comparisons and unique recovery-backup names. [PR #217](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/217)

### Team contributions and reviews

* I contributed the initial UI mockup and refined the non-functional requirements. [PR #45](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/45), [PR #46](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/46)
* I left five inline review comments on teammates' PRs: [assertions](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/115#discussion_r4356744177), [update logging](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/115#discussion_r4356744182), [unambiguous completion](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/99#discussion_r4358062229), [rejected-command tests](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/112#discussion_r4414343643), and [display-only sorting](https://github.com/AY2627S1-CS2103T-W11-2/tp/pull/103#discussion_r4414344911).

### Use of development tools

I worked with Codex to discuss requirements, compare implementation approaches and refine the changes. I reviewed proposed behaviour and wording, raised questions and requested revisions as the work developed. Codex edited code, tests and documentation and ran automated checks. I reviewed the results and carried out selected manual checks, including checking reproduced behaviour in the running app. The linked PRs record the implementation, test results and team reviews.
