package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.model.person.Person;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String ERROR_STYLE_CLASS = "error";
    private static final String TOTAL_STYLE_CLASS = "muted";
    private static final String SUCCESS_HEADING_PREFIX = "Result: ";
    private static final String ERROR_HEADING_PREFIX = "Needs attention: ";
    private static final String EMPTY_FEEDBACK_SUMMARY = "Command result";
    /** Longest heading summary shown, including the ellipsis, so long first lines do not wrap the heading. */
    private static final int MAX_SUMMARY_LENGTH = 90;
    private static final String ELLIPSIS = "...";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private Label feedbackHeading;

    @FXML
    private ScrollPane contactScroll;

    @FXML
    private VBox contactCards;

    /** Creates an empty result display; feedback appears once a command runs. */
    public ResultDisplay() {
        super(FXML);
    }

    /** Shows {@code feedbackToUser} as successful command feedback. */
    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /** Shows feedback with a distinct error state when a command fails. */
    public void setFeedbackToUser(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        showErrorStyle(isError);
        resultDisplay.setText(feedbackToUser);
        showCards(false);
        String headingPrefix = isError ? ERROR_HEADING_PREFIX : SUCCESS_HEADING_PREFIX;
        feedbackHeading.setText(headingPrefix + summarise(feedbackToUser));
        resultDisplay.positionCaret(0);
    }

    /**
     * Shows {@code contacts} as static cards in place of the text output, which keeps the same feedback.
     * Numbered cards use the one-based indices accepted by edit and delete.
     */
    public void showContacts(List<Person> contacts, boolean isNumbered) {
        requireNonNull(contacts);
        contactCards.getChildren().clear();
        for (int i = 0; i < contacts.size(); i++) {
            int index = isNumbered ? i + 1 : ContactCard.NO_INDEX;
            contactCards.getChildren().add(new ContactCard(contacts.get(i), index).getRoot());
        }
        if (isNumbered) {
            Label total = new Label(String.format(ListCommand.MESSAGE_TOTAL, contacts.size()));
            total.getStyleClass().add(TOTAL_STYLE_CLASS);
            contactCards.getChildren().add(total);
        }
        contactScroll.setVvalue(0);
        showCards(!contacts.isEmpty());
    }

    /** Adds the error style to the text output when {@code isError}, and removes it otherwise. */
    private void showErrorStyle(boolean isError) {
        resultDisplay.getStyleClass().remove(ERROR_STYLE_CLASS);
        if (isError) {
            resultDisplay.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    /** Returns the first line of {@code feedback}, shortened with an ellipsis if it is too long for the heading. */
    private static String summarise(String feedback) {
        String firstLine = feedback.lines().findFirst().orElse(EMPTY_FEEDBACK_SUMMARY);
        if (firstLine.length() <= MAX_SUMMARY_LENGTH) {
            return firstLine;
        }
        return firstLine.substring(0, MAX_SUMMARY_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
    }

    /** Swaps between the text output and the contact cards. */
    private void showCards(boolean isShowingCards) {
        contactScroll.setVisible(isShowingCards);
        contactScroll.setManaged(isShowingCards);
        resultDisplay.setVisible(!isShowingCards);
        resultDisplay.setManaged(!isShowingCards);
    }

}
