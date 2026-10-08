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

The planned direction includes enquiry status tracking and follow-up
management, helping organisers keep track of communication progress
and identify whom to contact next.

## UI preview

![Application UI preview](docs/images/Ui.png)

*Planned UI mockup with fictional sample data. Enquiry status and follow-up fields illustrate the planned direction, not implemented functionality.*

## Who it is for

BoothManagerPro is designed for organisers at convention centres who need to keep track of the exhibitors they have invited for booths and the status of each enquiry.
Used to track contact details, it is best suited to organisers who:

* handle dozens to hundreds of potential exhibitors, often with several representatives from the same exhibitor
* frequently update enquiry statuses and need to know which exhibitors are due for a follow-up
* need to pull up an exhibitor's contact details quickly, such as while on a call or replying to an email
* are comfortable with computers and prefer typing commands, as they need to process information quickly
* find full event-management software too slow or complex for day-to-day contact tracking

## Core features

BoothManagerPro currently supports five contact-management workflows:

1. **Add:** Save name, company, email, phone, optional contact method, and tags, with validation and duplicate detection.
1. **Delete:** Remove a contact by full name or displayed index; ambiguous names require an index.
1. **List:** Display all contacts and their details, with a total count.
1. **View:** Search by name keywords and select a contact to see its full details.
1. **Find and filter:** Match complete name, company, email, phone, or tag values; combine alternatives and criteria.

See the [User Guide](docs/UserGuide.md) for syntax and examples. Company and contact method are displayed in the contact details panel. Enquiry status and follow-up tracking remain planned.

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
