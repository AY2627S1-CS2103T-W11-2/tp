package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.boothmanagerpro.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import java.io.InputStream;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void showingHelpMessage_turkishLocale_keepsCommandHeadingsUsable() throws Exception {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            // Initialise the static help text afresh without depending on other tests' class-loading order.
            String className = HelpCommand.class.getName();
            ClassLoader loader = new ClassLoader(HelpCommand.class.getClassLoader()) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    if (!name.equals(className)) {
                        return super.loadClass(name, resolve);
                    }
                    try (InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = stream.readAllBytes();
                        Class<?> loaded = defineClass(name, bytes, 0, bytes.length);
                        if (resolve) {
                            resolveClass(loaded);
                        }
                        return loaded;
                    } catch (java.io.IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
            };
            String help = (String) loader.loadClass(className).getField("SHOWING_HELP_MESSAGE").get(null);
            assertTrue(help.contains("2. LIST\n"));
            assertTrue(help.contains("3. FIND\n"));
            assertTrue(help.contains("4. VIEW\n"));
            assertTrue(help.contains("10. EXIT\n"));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    @Test
    public void showingHelpMessage_usesConsistentEntryFormat() {
        assertTrue(SHOWING_HELP_MESSAGE.contains("1. ADD\n"
                + "   Description: Adds a new exhibitor contact.\n"
                + "   Format:      add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] "
                + "[t/TAG]...\n"
                + "   Example:     add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 "
                + "m/email s/In progress f/20261025 t/high-priority"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("4. VIEW\n"
                + "   Description: Displays full contact details. If several contacts match, reply with a displayed "
                + "number.\n"
                + "   Format:      view n/NAME\n"
                + "   Example:     view n/Alicia Tan"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("6. TAG\n"
                + "   Description: Toggles one or more tags on a contact using their full name or displayed index.\n"
                + "   Format:      tag NAME_OR_INDEX t/TAG [t/TAG]...\n"
                + "   Example:     tag Alicia Tan t/high-priority t/sponsor"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("10. EXIT\n"
                + "   Description: Closes BoothManagerPro.\n"
                + "   Format:      exit\n"
                + "   Example:     exit"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("list [o/name|o/company|o/status]"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("find f/20261025 t/high-priority"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("update Alicia Tan s/Completed f/20261028"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("Empty m/ clears the contact method; "
                + "empty f/ clears the follow-up date; empty t/ clears all tags."));
        assertTrue(SHOWING_HELP_MESSAGE.contains("Repeated fields are alternatives (OR); different fields must all "
                + "match (AND)."));
        assertTrue(SHOWING_HELP_MESSAGE.contains("Use an exact full name or a displayed number for NAME_OR_INDEX."));
        assertTrue(SHOWING_HELP_MESSAGE.contains("clear confirm"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("Enter: run command  |  Tab: autofill or focus output"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("F1: show help       |  Ctrl/Cmd+L: focus command box"));
        assertTrue(SHOWING_HELP_MESSAGE.contains(
                "Use numbers from list, find, or view for update, tag, and delete."));
    }
}
