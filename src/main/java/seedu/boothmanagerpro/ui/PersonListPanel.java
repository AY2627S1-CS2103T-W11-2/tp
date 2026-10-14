package seedu.boothmanagerpro.ui;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        // Contacts are viewed through typed commands. Keep the list display-only while
        // still allowing command results to update its selection programmatically.
        personListView.setFocusTraversable(false);
        personListView.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        Label emptyState = new Label("No contacts to show.\nType list to show all contacts, or add a new contact.");
        emptyState.setWrapText(true);
        emptyState.getStyleClass().add("empty-state");
        personListView.setPlaceholder(emptyState);
        personListView.setAccessibleText("Exhibitor contacts. Use the view command to select a contact.");
        // Read-only display: contacts are selected and scrolled through commands, not the mouse or keyboard.
        personListView.setMouseTransparent(true);
        personListView.setFocusTraversable(false);
    }

    /**
     * Exposes selection for detail panels without coupling them to the list implementation.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Selects the first contact when a list update leaves no selection.
     */
    public void ensureSelection() {
        if (personListView.getSelectionModel().getSelectedItem() == null && !personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().selectFirst();
        }
    }

    /**
     * Selects and scrolls to a contact requested through the command box.
     */
    public void selectPerson(Person person) {
        personListView.getSelectionModel().select(person);
        // Defer until list-change listeners and the virtualised cells have caught up.
        Platform.runLater(() -> {
            if (person.equals(personListView.getSelectionModel().getSelectedItem())) {
                personListView.applyCss();
                personListView.layout();
                personListView.scrollTo(person);
            }
        });
    }

    /**
     * Clears selection while the user chooses between view matches.
     */
    public void clearSelection() {
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region card = new PersonCard(person, getIndex() + 1).getRoot();
                card.prefWidthProperty().bind(widthProperty().subtract(4));
                setGraphic(card);
            }
        }
    }

}
