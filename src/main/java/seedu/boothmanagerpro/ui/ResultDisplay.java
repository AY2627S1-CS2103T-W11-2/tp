package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.boothmanagerpro.model.person.Person;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private Label feedbackHeading;

    @FXML
    private ScrollPane contactScroll;

    @FXML
    private VBox contactCards;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /** Shows feedback with a distinct error state when a command fails. */
    public void setFeedbackToUser(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        resultDisplay.getStyleClass().remove("error");
        if (isError) {
            resultDisplay.getStyleClass().add("error");
        }
        resultDisplay.setText(feedbackToUser);
        showCards(false);
        String summary = feedbackToUser.lines().findFirst().orElse("Command result");
        if (summary.length() > 90) {
            summary = summary.substring(0, 87) + "...";
        }
        feedbackHeading.setText((isError ? "Needs attention: " : "Result: ") + summary);
        resultDisplay.positionCaret(0);
    }

    /**
     * Shows {@code contacts} as static cards in place of the text output, which keeps the same feedback.
     * Numbered cards use the one-based indices accepted by edit and delete.
     */
    public void showContacts(List<Person> contacts, boolean numbered) {
        requireNonNull(contacts);
        contactCards.getChildren().clear();
        for (int i = 0; i < contacts.size(); i++) {
            contactCards.getChildren().add(new ContactCard(contacts.get(i), numbered ? i + 1 : 0).getRoot());
        }
        if (numbered) {
            Label total = new Label("Total contacts: " + contacts.size());
            total.getStyleClass().add("muted");
            contactCards.getChildren().add(total);
        }
        contactScroll.setVvalue(0);
        showCards(!contacts.isEmpty());
    }

    /** Swaps between the text output and the contact cards. */
    private void showCards(boolean isShowingCards) {
        contactScroll.setVisible(isShowingCards);
        contactScroll.setManaged(isShowingCards);
        resultDisplay.setVisible(!isShowingCards);
        resultDisplay.setManaged(!isShowingCards);
    }

}
