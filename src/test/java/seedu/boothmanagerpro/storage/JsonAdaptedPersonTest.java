package seedu.boothmanagerpro.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.boothmanagerpro.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;
import static seedu.boothmanagerpro.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;

import seedu.boothmanagerpro.commons.exceptions.IllegalValueException;
import seedu.boothmanagerpro.commons.util.JsonUtil;
import seedu.boothmanagerpro.model.person.Address;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "friend/family";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void fromJsonString_constructorOrderChanges_preservesContact() throws Exception {
        Person expected = new PersonBuilder(BENSON).withCompany("TechNova").withContactMethod("phone").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(expected));
        Comparator<AnnotatedConstructor> byParameterCount =
                Comparator.comparingInt(AnnotatedConstructor::getParameterCount);
        for (Comparator<AnnotatedConstructor> order : List.of(byParameterCount, byParameterCount.reversed())) {
            ObjectMapper mapper = new ObjectMapper();
            // JVMs can expose constructors in different orders. Exercise both rather than relying on this JVM's order.
            mapper.setConfig(mapper.getDeserializationConfig().with(new BasicClassIntrospector() {
                @Override
                protected POJOPropertiesCollector constructPropertyCollector(MapperConfig<?> config,
                        AnnotatedClass annotatedClass, JavaType type,
                        boolean isForSerialization, String mutatorPrefix) {
                    if (type.getRawClass() == JsonAdaptedPerson.class) {
                        annotatedClass.getConstructors().sort(order);
                    }
                    return super.constructPropertyCollector(config, annotatedClass, type,
                            isForSerialization, mutatorPrefix);
                }
            }));
            assertEquals(expected, mapper.readValue(json, JsonAdaptedPerson.class).toModelType());
        }
    }

    @Test
    public void fromJsonString_exhibitorFields_preservesAllDetails() throws Exception {
        Person expected = new PersonBuilder(BENSON).withCompany("TechNova").withContactMethod("phone").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(expected));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(expected, restored.toModelType());
    }

    @Test
    public void fromJsonString_legacyFields_keepsContactWithoutExhibitorFields() throws Exception {
        String json = """
                {"name":"Benson Meier","phone":"98765432","email":"johnd@example.com",
                 "address":"311, Clementi Ave 2, #02-25","tags":["owesMoney","friends"]}
                """;
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(BENSON, restored.toModelType());
    }

    @Test
    public void toModelType_exhibitorFields_roundTrip() throws Exception {
        Person person = new PersonBuilder(BENSON).withCompany("TechNova").withContactMethod("other").build();
        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    @Test
    public void toModelType_missingExhibitorFields_keepsLegacyRecord() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS, null, null);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidExhibitorFields_throwsIllegalValueException() {
        JsonAdaptedPerson company = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS, "A".repeat(101), "email");
        assertThrows(IllegalValueException.class, Person.MESSAGE_COMPANY_CONSTRAINTS, company::toModelType);
        JsonAdaptedPerson method = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS, "TechNova", "fax");
        assertThrows(IllegalValueException.class, Person.MESSAGE_METHOD_CONSTRAINTS, method::toModelType);
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
