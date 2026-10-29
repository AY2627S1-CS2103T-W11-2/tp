package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.tag.Tag;

/** Toggles tags on a uniquely named contact or a displayed index, adding new tags and removing existing ones. */
public class TagCommand extends Command {
    public static final String COMMAND_WORD = "tag";
    public static final String MESSAGE_USAGE = "tag NAME or INDEX t/TAG [t/TAG]...\n"
            + "Supply at least one t/TAG field. New tags are added and existing tags are removed. "
            + "Use list or find for displayed indices. Example: tag Alicia Tan t/high-priority";
    public static final String MESSAGE_AMBIGUOUS = "More than one contact has the name %s. "
            + "Use list or find, then tag the contact by its displayed index. No tags were updated.";

    private final Index index;
    private final Name name;
    private final Set<Tag> tags;

    /** Targets the current filtered list using a one-based displayed index. */
    public TagCommand(Index index, Set<Tag> tags) {
        this.index = requireNonNull(index);
        this.name = null;
        this.tags = Collections.unmodifiableSet(new LinkedHashSet<>(requireNonNull(tags)));
    }

    /** Targets an exact, case-insensitive full name across all saved contacts. */
    public TagCommand(Name name, Set<Tag> tags) {
        this.index = null;
        this.name = requireNonNull(name);
        this.tags = Collections.unmodifiableSet(new LinkedHashSet<>(requireNonNull(tags)));
    }

    @Override
    public boolean modifiesAddressBook() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person original = resolveTarget(model);
        Set<Tag> updatedTags = new LinkedHashSet<>(original.getTags());
        Set<Tag> added = new LinkedHashSet<>();
        Set<Tag> removed = new LinkedHashSet<>();
        for (Tag tag : tags) {
            Tag existing = findMatchingTag(updatedTags, tag);
            if (existing != null) {
                updatedTags.remove(existing);
                removed.add(existing);
            } else {
                updatedTags.add(tag);
                added.add(tag);
            }
        }
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getAddress(), updatedTags, original.getCompany(), original.getContactMethod(),
                original.getEnquiryStatus(), original.getFollowUpDate());
        model.setPerson(original, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(
                "Updated tags for %s. Added: %s. Removed: %s. Current tags: %s.",
                AddCommand.formatIdentity(updated), formatTagSet(added), formatTagSet(removed),
                Messages.formatTags(updated)));
    }

    /** Resolves ambiguity without selecting an arbitrary matching contact. */
    private Person resolveTarget(Model model) throws CommandException {
        if (index != null) {
            assert index.getZeroBased() >= 0 : "Target index must be non-negative";
            List<Person> displayed = model.getFilteredPersonList();
            if (index.getZeroBased() >= displayed.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            assert index.getZeroBased() < displayed.size() : "Resolved index must be within list bounds";
            return displayed.get(index.getZeroBased());
        }
        assert name != null : "Target name cannot be null when index is null";
        List<Person> matches = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(name.fullName)).toList();
        if (matches.isEmpty()) {
            throw new CommandException(String.format(DeleteCommand.MESSAGE_PERSON_NOT_FOUND, name));
        }
        if (matches.size() > 1) {
            throw new CommandException(String.format(MESSAGE_AMBIGUOUS, name));
        }
        return matches.getFirst();
    }

    private static Tag findMatchingTag(Set<Tag> tags, Tag target) {
        for (Tag candidate : tags) {
            if (candidate.equals(target)) {
                return candidate;
            }
        }
        return null;
    }

    private static String formatTagSet(Set<Tag> tagSet) {
        return tagSet.isEmpty() ? Messages.EMPTY_TAGS
                : tagSet.stream().map(tag -> tag.tagName).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TagCommand otherTagCommand)) {
            return false;
        }
        return Objects.equals(index, otherTagCommand.index)
                && Objects.equals(name, otherTagCommand.name)
                && Objects.equals(tags, otherTagCommand.tags);
    }
}
