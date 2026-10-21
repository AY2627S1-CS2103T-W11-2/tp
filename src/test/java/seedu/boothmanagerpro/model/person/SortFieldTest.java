package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.testutil.PersonBuilder;

public class SortFieldTest {

    private static final Person ALICIA = new PersonBuilder().withName("Alicia Tan")
            .withCompany("TechNova Pte Ltd").withEmail("alicia@technova.com").build();
    private static final Person BEN = new PersonBuilder().withName("ben lim")
            .withCompany("Paper & Ink Studio").withEmail("ben@paperink.com").build();
    private static final Person CHLOE = new PersonBuilder().withName("Chloe Ng")
            .withCompany("green goods collective").withEmail("chloe@greengoods.com").build();
    private static final Person DAVID = new PersonBuilder().withName("David Li")
            .withEmail("david@example.com").build();
    private static final Person ZOE = new PersonBuilder().withName("Zoe Lee")
            .withCompany("TechNova Pte Ltd").withEmail("zoe@technova.com").build();

    @Test
    public void fromKeyword() {
        assertEquals(Optional.of(SortField.NAME), SortField.fromKeyword("name"));
        assertEquals(Optional.of(SortField.COMPANY), SortField.fromKeyword(" COMPANY "));
        assertEquals(Optional.empty(), SortField.fromKeyword("email"));
        assertEquals(Optional.empty(), SortField.fromKeyword(""));
    }

    @Test
    public void toString_returnsKeyword() {
        assertEquals("name", SortField.NAME.toString());
        assertEquals("company", SortField.COMPANY.toString());
    }

    @Test
    public void name_sortsByNameIgnoringCase() {
        assertEquals(List.of(ALICIA, BEN, CHLOE, DAVID, ZOE), sorted(SortField.NAME, ZOE, DAVID, BEN, CHLOE, ALICIA));
    }

    @Test
    public void name_sameName_breaksTieByCompanyThenEmail() {
        Person atTechNova = new PersonBuilder(ALICIA).withEmail("b@technova.com").build();
        Person atTechNovaFirstEmail = new PersonBuilder(ALICIA).withEmail("a@technova.com").build();
        Person atPaperInk = new PersonBuilder(ALICIA).withCompany("Paper & Ink Studio").build();
        Person withoutCompany = new PersonBuilder().withName("ALICIA TAN").withEmail("x@example.com").build();
        assertEquals(List.of(atPaperInk, atTechNovaFirstEmail, atTechNova, withoutCompany),
                sorted(SortField.NAME, withoutCompany, atTechNova, atPaperInk, atTechNovaFirstEmail));
    }

    @Test
    public void company_sortsByCompanyIgnoringCaseThenName_missingCompanyLast() {
        assertEquals(List.of(CHLOE, BEN, ALICIA, ZOE, DAVID),
                sorted(SortField.COMPANY, DAVID, ZOE, ALICIA, BEN, CHLOE));
    }

    @Test
    public void company_sameCompanyAndName_breaksTieByEmail() {
        Person second = new PersonBuilder(ZOE).withEmail("zoey@technova.com").build();
        assertEquals(List.of(ZOE, second), sorted(SortField.COMPANY, second, ZOE));
    }

    private static List<Person> sorted(SortField field, Person... persons) {
        List<Person> list = new ArrayList<>(List.of(persons));
        list.sort(field.getComparator());
        return list;
    }
}
