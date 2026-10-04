package seedu.boothmanagerpro.ui;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
        Label emptyState = new Label("No contacts to show.\nType list to show all contacts, or add a new contact.");
        emptyState.setWrapText(true);
        emptyState.getStyleClass().add("empty-state");
        personListView.setPlaceholder(emptyState);
        personListView.setAccessibleText("Exhibitor contacts. Use arrow keys to select a contact.");
    }

    /** Exposes selection for detail panels without coupling them to the list implementation. */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /** Selects the first contact when a list update leaves no selection. */
    public void ensureSelection() {
        if (personListView.getSelectionModel().getSelectedItem() == null && !personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().selectFirst();
        }
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
