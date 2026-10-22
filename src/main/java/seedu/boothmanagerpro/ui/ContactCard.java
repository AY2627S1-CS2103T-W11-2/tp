package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.person.Person;

/**
 * A static, read-only card showing one contact's details in the command output.
 */
public class ContactCard extends UiPart<Region> {

    private static final String FXML = "ContactCard.fxml";

    @FXML
    private Label name;

    @FXML
    private Label company;

    @FXML
    private Label email;

    @FXML
    private Label phone;

    @FXML
    private Label contactMethod;

    @FXML
    private FlowPane tags;

    @FXML
    private Label enquiryStatus;

    /**
     * Creates a card for {@code person}, prefixed with {@code index} when it is positive.
     */
    public ContactCard(Person person, int index) {
        super(FXML);
        requireNonNull(person);
        name.setText((index > 0 ? index + ". " : "") + person.getName().fullName);
        company.setText(person.getCompany().map(Object::toString).orElse("Company not specified"));
        email.setText("Email: " + person.getEmail());
        phone.setText("Phone: " + person.getPhone());
        contactMethod.setText("Contact method: " + Messages.formatOptional(person.getContactMethod()));
        enquiryStatus.setText("Enquiry status: " + person.getEnquiryStatus());
        if (person.getTags().isEmpty()) {
            Label none = new Label("No tags");
            none.getStyleClass().add("card-detail");
            tags.getChildren().add(none);
        } else {
            person.getTags().forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        }
    }
}
