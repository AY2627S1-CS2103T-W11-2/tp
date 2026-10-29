# BoothManagerPro

[![CI Status](https://github.com/AY2627S1-CS2103T-W11-2/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-W11-2/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/github/AY2627S1-CS2103T-W11-2/tp/graph/badge.svg?token=EXE9YQFB7G)](https://codecov.io/github/AY2627S1-CS2103T-W11-2/tp)

**Manage exhibitor contacts efficiently through typed commands.**

BoothManagerPro is a desktop application designed for convention organisers
who manage dozens to hundreds of exhibitor contacts and prefer working
with the keyboard. It combines command-line input with a graphical
interface to make contact information easy to enter and view.

## Why BoothManagerPro?

Keeping track of exhibitor representatives and their contact details can
become difficult as a convention grows. BoothManagerPro brings this
information together in one place, helping organisers quickly find the
right person and maintain organised contact records.

## About the project

BoothManagerPro focuses on managing exhibitors and their representatives
throughout the enquiry process. Its initial development includes adding,
listing, viewing, and deleting contact records.

Contacts support an enquiry status and an optional follow-up date at creation.
Use `update NAME_OR_INDEX s/STATUS f/YYYYMMDD` to change these fields.
Use `find f/YYYYMMDD` to find contacts on an exact follow-up date; date ranges and overdue reminders remain planned.

## UI preview

![Application on startup](docs/images/Ui%20-%20start%20state.png)

*Current startup screen. The window contains command input and text output only. `list` prints all contacts, `find` prints numbered matches, and `view` prints details or numbered choices. Contacts are not printed automatically on launch.*

## Who it is for

BoothManagerPro is designed for organisers at convention centres who need to keep track of exhibitors they have invited and their representatives' contact details.
Used to track contact details, it is best suited to organisers who:

* handle dozens to hundreds of potential exhibitors, often with several representatives from the same exhibitor
* frequently update contact details and use tags to organise exhibitors
* need to pull up an exhibitor's contact details quickly, such as while on a call or replying to an email
* are comfortable with computers and prefer typing commands, as they need to process information quickly
* find full event-management software too slow or complex for day-to-day contact tracking

## Core features

BoothManagerPro's MVP centres on five contact-management workflows:

1. **Add:** Save name, company, email, phone, optional contact method, and tags, with validation and duplicate detection.
1. **Delete:** Remove a contact by full name or displayed index; ambiguous names require an index.
1. **List:** Display all contacts and their details, with a total count.
1. **View:** Search by name keywords and select a contact to see its full details.
1. **Find and filter:** Match full names or whole name words and complete company, email, phone, tag, or follow-up date values; combine alternatives and criteria.

The inherited `edit` and `clear` commands are also available. Edit supports name, phone, email, legacy address, and tags; company and contact method are preserved. See the [User Guide](docs/UserGuide.md) for syntax and examples. Company and contact method are included in contact text output. Add supports optional enquiry status and follow-up dates. Use `update` to change supplied contact fields and `tag` to toggle tags by name or displayed index. Exact follow-up-date filtering is available; filtering by status or contact method remains planned.

### Future enhancements

- **CSV import and export:** Bring in existing contacts and share exhibitor lists with colleagues.
- **Undo:** Reverse an accidental action.

## Getting started

To build and run the current development version, see the [development setup guide](docs/SettingUp.md). The [User Guide](docs/UserGuide.md) covers all five workflows and the remaining inherited commands.

## Documentation

<!-- Jenna (Docs & Links, Integration): Review these links alongside the final feature list and integrated README. -->
- [User Guide](docs/UserGuide.md) - Learn how to use the application.
- [Developer Guide](docs/DeveloperGuide.md) - Understand the architecture and development practices.
- [Development setup](docs/SettingUp.md) - Set up the project to build and run locally.
- [About us](docs/AboutUs.md) - Meet the project team.

## Acknowledgements

This project is based on the [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org). We acknowledge its code and documentation as the foundation for this application.

## License

This project is licensed under the [MIT License](LICENSE).
