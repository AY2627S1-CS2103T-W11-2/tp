package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

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
        resultDisplay.positionCaret(0);
    }

}
