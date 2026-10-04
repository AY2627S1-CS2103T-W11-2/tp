package seedu.boothmanagerpro.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.model.person.Person;

/** Displays the selected contact. Future exhibitor fields belong in this component. */
public class ContactDetailsPanel extends UiPart<Region> {
    private static final String FXML = "ContactDetailsPanel.fxml";

    @FXML
    private Label contactName;
    @FXML
    private Label email;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private FlowPane tags;

    /** Creates an empty contact details panel. */
    public ContactDetailsPanel() {
        super(FXML);
        showPerson(null);
    }

    /** Shows a contact, or an empty state when no contact is selected. */
    public void showPerson(Person person) {
        contactName.setText(person == null ? "Select a contact" : person.getName().fullName);
        email.setText(person == null ? "No contact selected" : person.getEmail().value);
        phone.setText(person == null ? "—" : person.getPhone().value);
        address.setText(person == null ? "—" : person.getAddress().value);
        tags.getChildren().clear();
        if (person == null || person.getTags().isEmpty()) {
            tags.getChildren().add(new Label("No tags"));
        } else {
            person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName))
                    .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        }
    }
}
