package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.Person;

/**
 * A static, read-only card showing one contact's details in the command output.
 */
public class ContactCard extends UiPart<Region> {

    /** Index passed for a card that should not be numbered, such as a single contact's details. */
    public static final int NO_INDEX = 0;

    private static final String FXML = "ContactCard.fxml";
    private static final String NO_COMPANY = "Not specified";
    private static final String NO_TAGS = Messages.EMPTY_TAGS;
    private static final String DETAIL_STYLE_CLASS = "card-detail";

    @FXML
    private Label name;

    @FXML
    private Label enquiryStatus;

    @FXML
    private Label company;

    @FXML
    private Label email;

    @FXML
    private Label phone;

    @FXML
    private Label contactMethod;

    @FXML
    private Label followUpDate;

    @FXML
    private FlowPane tags;

    private ContextMenu cardMenu;

    /**
     * Creates a card for {@code person}, prefixed with {@code index} unless it is {@link #NO_INDEX}.
     */
    public ContactCard(Person person, int index) {
        super(FXML);
        requireNonNull(person);
        String indexPrefix = index == NO_INDEX ? "" : index + ". ";
        name.setText(indexPrefix + person.getName().fullName);
        showEnquiryStatus(person.getEnquiryStatus());
        company.setText(person.getCompany().map(Object::toString).orElse(NO_COMPANY));
        email.setText("Email: " + person.getEmail());
        phone.setText("Phone: " + person.getPhone());
        contactMethod.setText("Contact method: " + Messages.formatOptional(person.getContactMethod()));
        followUpDate.setText("Follow-up date: " + Messages.formatFollowUpDate(person));
        showTags(person);
        setupCopyActions(person);
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

    /** Shows each tag as a chip in stored order, or a muted placeholder when there are none. */
    private void showTags(Person person) {
        if (person.getTags().isEmpty()) {
            Label noTags = new Label(NO_TAGS);
            noTags.getStyleClass().add(DETAIL_STYLE_CLASS);
            tags.getChildren().add(noTags);
            return;
        }
        person.getTags().forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    /** Returns the style class for {@code status}, e.g. {@code status-needs-follow-up}. */
    static String statusStyleClass(EnquiryStatus status) {
        return "status-" + status.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Sets up context menus and double-click actions to allow copying contact details to the clipboard.
     */
    private void setupCopyActions(Person person) {
        String emailValue = person.getEmail().value;
        String phoneValue = person.getPhone().value;
        String nameValue = person.getName().fullName;
        String allDetails = Messages.formatContactDetails(person);

        configureCopyableLabel(email, emailValue, "Copy Email", allDetails);
        configureCopyableLabel(phone, phoneValue, "Copy Phone", allDetails);
        configureCopyableLabel(name, nameValue, "Copy Name", allDetails);
        person.getCompany().ifPresent(c -> configureCopyableLabel(company, c.value, "Copy Company", allDetails));

        cardMenu = new ContextMenu();
        MenuItem copyEmailItem = new MenuItem("Copy Email");
        copyEmailItem.setOnAction(event -> copyToClipboard(emailValue));
        MenuItem copyPhoneItem = new MenuItem("Copy Phone");
        copyPhoneItem.setOnAction(event -> copyToClipboard(phoneValue));
        MenuItem copyDetailsItem = new MenuItem("Copy Contact Details");
        copyDetailsItem.setOnAction(event -> copyToClipboard(allDetails));
        cardMenu.getItems().addAll(copyEmailItem, copyPhoneItem, copyDetailsItem);

        getRoot().setOnContextMenuRequested(event -> {
            cardMenu.show(getRoot(), event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    /**
     * Attaches a context menu and a double-click copy handler to {@code label}.
     */
    private void configureCopyableLabel(Label label, String valueToCopy, String primaryCopyLabel, String allDetails) {
        ContextMenu menu = new ContextMenu();
        MenuItem copyValue = new MenuItem(primaryCopyLabel);
        copyValue.setOnAction(event -> copyToClipboard(valueToCopy));
        MenuItem copyDetails = new MenuItem("Copy Contact Details");
        copyDetails.setOnAction(event -> copyToClipboard(allDetails));
        menu.getItems().addAll(copyValue, copyDetails);
        label.setContextMenu(menu);

        label.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                copyToClipboard(valueToCopy);
                event.consume();
            }
        });
    }

    ContextMenu getCardContextMenu() {
        return cardMenu;
    }

    /**
     * Copies {@code text} to the system clipboard.
     */
    public static void copyToClipboard(String text) {
        if (text == null) {
            return;
        }
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
}
