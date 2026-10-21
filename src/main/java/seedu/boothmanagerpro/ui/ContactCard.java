package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
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

    @FXML
    private Label followUpDate;

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
        showEnquiryStatus(person.getEnquiryStatus());
        followUpDate.setText("Follow-up date: " + Messages.formatFollowUpDate(person));
        if (person.getTags().isEmpty()) {
            Label none = new Label("No tags");
            none.getStyleClass().add("card-detail");
            tags.getChildren().add(none);
        } else {
            person.getTags().forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        }
    }

    /**
     * Shows {@code status} as a coloured badge beside the name and colours the card's edge to match.
     * The badge keeps the status text, so the status does not depend on colour alone.
     */
    private void showEnquiryStatus(EnquiryStatus status) {
        enquiryStatus.setText(status.toString());
        enquiryStatus.setAccessibleText("Enquiry status: " + status);
        String styleClass = statusStyleClass(status);
        enquiryStatus.getStyleClass().add(styleClass);
        getRoot().getStyleClass().add(styleClass);
    }

    /** Returns the style class for {@code status}, e.g. {@code status-needs-follow-up}. */
    static String statusStyleClass(EnquiryStatus status) {
        return "status-" + status.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
