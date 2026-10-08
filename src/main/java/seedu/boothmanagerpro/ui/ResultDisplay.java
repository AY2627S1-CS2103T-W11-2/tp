package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    @FXML
    private TitledPane feedbackPane;

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
        resizeForFeedback(feedbackToUser);
        String summary = feedbackToUser.lines().findFirst().orElse("Command result");
        if (summary.length() > 90) {
            summary = summary.substring(0, 87) + "...";
        }
        feedbackPane.setText((isError ? "Needs attention: " : "Result: ") + summary);
        feedbackPane.setExpanded(true);
        resultDisplay.positionCaret(0);
    }

    /** Keeps short replies compact and caps long output so contacts remain visible; the text area scrolls. */
    private void resizeForFeedback(String feedback) {
        int rows = (int) Math.min(6, Math.max(1, feedback.lines().count()));
        resultDisplay.setPrefRowCount(rows);
    }

}
