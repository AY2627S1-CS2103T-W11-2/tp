---
layout: page
title: BoothManagerPro
---

[![CI Status](https://github.com/AY2627S1-CS2103T-W11-2/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W11-2/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/github/AY2627S1-CS2103T-W11-2/tp/graph/badge.svg?token=EXE9YQFB7G)](https://codecov.io/github/AY2627S1-CS2103T-W11-2/tp)

![BoothManagerPro contact list view](images/Ui.png)

**BoothManagerPro helps convention organisers manage exhibitor contacts efficiently through typed commands.** Add contacts with company, email, phone, optional contact method, enquiry status, follow-up dates, and tags, with built-in validation and duplicate detection. See the [add command reference](UserGuide.html#adding-an-exhibitor-contact-add).

The screenshot above shows BoothManagerPro displaying exhibitor contacts as static contact cards with coloured enquiry status badges. Startup displays a ready prompt, keeping your workflow clean and focused until you enter a command. Use `list` to view and sort contacts (`list o/name`, `list o/company`, `list o/status`), `find` to filter by fields and exact follow-up dates (`find f/YYYYMMDD`), `view` for full details, and `update` or `tag` to keep records accurate. Future enhancements such as automatic overdue reminders are planned for subsequent iterations.

* If you are interested in using BoothManagerPro, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing BoothManagerPro, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.


**Acknowledgements**

* Based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
