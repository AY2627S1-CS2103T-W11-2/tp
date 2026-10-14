package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableDoubleValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final double MAX_WINDOW_HEIGHT_SHARE = 0.5;

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

    /**
     * Caps the feedback at half of {@code windowHeight}; output that needs more room scrolls.
     */
    public void bindMaxHeightTo(ObservableDoubleValue windowHeight) {
        requireNonNull(windowHeight);
        feedbackPane.maxHeightProperty().bind(Bindings.multiply(windowHeight, MAX_WINDOW_HEIGHT_SHARE));
    }

    /** Fits the text area to the feedback's line count; the pane's height cap keeps contacts visible. */
    private void resizeForFeedback(String feedback) {
        resultDisplay.setPrefRowCount((int) Math.max(1, feedback.lines().count()));
    }

}
