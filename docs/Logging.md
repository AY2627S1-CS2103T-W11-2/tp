---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Contact-data privacy

Command logs record parsing stages, outcomes, exception types and result counts.
Keep full command text, contact names, companies, email addresses, phone numbers and tags out of log messages.
User-facing feedback still shows the details needed to complete the operation.

For example, a successful addition records `Successfully added exhibitor contact.`
A rejected command records its exception type, and a completed search records its match count.
These messages help trace execution without copying the contact record into the log.

JSON read failures record the file path and exception type. Raw parser exception messages can quote values
from the saved file, so they are kept out of routine read-failure logs. The original exception remains
available to the recovery path, which preserves the source file before replacement.

`ContactLoggingPrivacyTest` captures application logs while exercising commands and a malformed JSON file.
`MainWindowTest` also checks success and error feedback through the command box. The tests verify both
that contact details stay out of the log and that feedback and saved data retain the expected values.
