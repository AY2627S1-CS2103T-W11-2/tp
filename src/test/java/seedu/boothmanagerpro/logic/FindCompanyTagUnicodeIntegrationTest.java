package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.parser.FindCommandParser;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.StorageManager;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Regressions for Unicode company/tag retrieval without modifying stored spelling or matching scope. */
public class FindCompanyTagUnicodeIntegrationTest {
    @TempDir
    public Path folder;

    @Test
    public void find_company_ignoresGreekTurkishAndSupplementaryCaseVariants() throws Exception {
        assertCaseVariantsMatch("c/");
    }

    @Test
    public void find_tag_ignoresGreekTurkishAndSupplementaryCaseVariants() throws Exception {
        assertCaseVariantsMatch("t/");
    }

    @Test
    public void find_unicodeCriteria_matchPartialCompanyWholeTagsAndOrAndRules() throws Exception {
        Person person = new PersonBuilder().withCompany("\u039f\u03a3 Labs")
                .withTags("\u039f\u03a3", "high-priority").build();
        ModelManager model = new ModelManager();
        model.addPerson(person);
        FindCommandParser parser = new FindCommandParser();
        parser.parse("c/\u03bf\u03c3").execute(model);
        assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()));
        parser.parse("c/Absent").execute(model);
        assertEquals(List.of(), List.copyOf(model.getFilteredPersonList()));
        parser.parse("t/\u03bf").execute(model);
        assertEquals(List.of(), List.copyOf(model.getFilteredPersonList()));
        parser.parse("c/Other c/\u03bf\u03c3 labs t/absent t/\u03bf\u03c3").execute(model);
        assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()));
        parser.parse("c/\u03bf\u03c3 labs t/absent").execute(model);
        assertEquals(List.of(), List.copyOf(model.getFilteredPersonList()));
    }

    @Test
    public void find_afterReload_matchesUnicodeAndPreservesOriginalStoredValues() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        StorageManager manager = IntegrationTestFixture.createStorage(storage, folder.resolve("prefs.json"));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, manager);
        logic.execute("add n/Greek Contact c/\u039f\u03a3 e/greek@example.com p/81234567 "
                + "f/20261029 t/\u039f\u03a3");
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, manager);
        Person original = model.getAddressBook().getPersonList().getFirst();
        String before = Files.readString(storage.getAddressBookFilePath());
        logic.execute("find n/Greek c/\u03bf\u03c3 t/\u03bf\u03c3 f/20261029");
        assertEquals(List.of(original), List.copyOf(model.getFilteredPersonList()));
        assertEquals("\u039f\u03a3", original.getCompany().orElseThrow().value);
        assertEquals("\u039f\u03a3", original.getTags().iterator().next().tagName);
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void find_canonicalEquivalenceAndUnicodeSpaces_matchesStoredContacts() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("nfc-contacts.json"));
        StorageManager manager = IntegrationTestFixture.createStorage(storage, folder.resolve("nfc-prefs.json"));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, manager);

        logic.execute("add n/Caf\u00E9 Owner c/Caf\u00E9 e/cafe@example.com p/81234567 t/r\u00E9sum\u00E9");
        Person stored = model.getAddressBook().getPersonList().getFirst();

        logic.execute("find c/Cafe\u0301");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));

        logic.execute("find c/CAFE\u0301");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));

        logic.execute("find t/re\u0301sume\u0301");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));

        logic.execute("find t/\u2003RE\u0301SUME\u0301\u2003");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));

        logic.execute("find Cafe\u0301");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));
    }

    @Test
    public void find_companyWithInternalMultipleSpacesAndTabs_matchesStoredContacts() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("spaces-contacts.json"));
        StorageManager manager = IntegrationTestFixture.createStorage(storage, folder.resolve("spaces-prefs.json"));
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, manager);

        logic.execute("add n/Tech Guy c/TechNova\n  Pte Ltd e/tech@example.com p/81234567");
        Person stored = model.getAddressBook().getPersonList().getFirst();
        assertEquals("TechNova Pte Ltd", stored.getCompany().orElseThrow().value);

        logic.execute("find c/TechNova Pte Ltd");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));

        logic.execute("find c/TechNova  Pte Ltd");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));

        logic.execute("find c/TechNova\tPte Ltd");
        assertEquals(List.of(stored), List.copyOf(model.getFilteredPersonList()));
    }

    private void assertCaseVariantsMatch(String field) throws Exception {
        for (List<String> pair : List.of(List.of("\u039f\u03a3", "\u03bf\u03c3"),
                List.of("\u03bf\u03c3", "\u039f\u03a3"), List.of("\u03bf\u03c2", "\u03bf\u03c3"),
                List.of("\u0130pek", "ipek"), List.of("Ipek", "\u0131pek"),
                List.of("\ud801\udc00", "\ud801\udc28"))) {
            Person person = new PersonBuilder().withCompany(pair.get(0)).withTags(pair.get(0)).build();
            ModelManager model = new ModelManager();
            model.addPerson(person);
            new FindCommandParser().parse(field + pair.get(1)).execute(model);
            assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()), field + pair);
        }
    }
}
