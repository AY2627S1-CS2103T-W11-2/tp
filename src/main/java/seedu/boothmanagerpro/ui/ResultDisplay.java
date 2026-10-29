package seedu.boothmanagerpro.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private static final int BATCH_SIZE = 25;
    private static final double SCROLL_THRESHOLD = 0.7;

    private List<Person> currentContacts = List.of();
    private boolean isCurrentNumbered;
    private boolean isCurrentShowingTotal;
    private int loadedCount = 0;
    private ChangeListener<Number> scrollListener;

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
        this(null);
    }

    /** Creates a result display with a callback to return focus to the command box. */
    public ResultDisplay(Runnable onFocusCommandBox) {
        super(FXML);
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && onFocusCommandBox != null) {
                onFocusCommandBox.run();
                event.consume();
            }
        });
        ContextMenu scrollMenu = new ContextMenu();
        MenuItem copyAll = new MenuItem("Copy Result as Text");
        copyAll.setOnAction(event -> ContactCard.copyToClipboard(resultDisplay.getText()));
        scrollMenu.getItems().add(copyAll);
        contactScroll.setContextMenu(scrollMenu);
    }

    /** Shows {@code feedbackToUser} as successful command feedback. */
    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /** Shows feedback with a distinct error state when a command fails. */
    public void setFeedbackToUser(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        cleanupBatchState();
        showErrorStyle(isError);
        resultDisplay.setText(feedbackToUser);
        showCards(false);
        String headingPrefix = isError ? ERROR_HEADING_PREFIX : SUCCESS_HEADING_PREFIX;
        feedbackHeading.setText(headingPrefix + summarise(feedbackToUser));
        resultDisplay.positionCaret(0);
    }

    /**
     * Shows {@code contacts} as static cards in place of the text output, which keeps the same feedback.
     * Numbered cards use the one-based indices accepted by update and delete. For large results, cards
     * are loaded in virtualized batches to avoid UI freezes at scale.
     */
    public void showContacts(List<Person> contacts, boolean isNumbered, boolean isShowingTotal) {
        requireNonNull(contacts);
        cleanupBatchState();
        this.currentContacts = contacts;
        this.isCurrentNumbered = isNumbered;
        this.isCurrentShowingTotal = isShowingTotal;
        contactCards.getChildren().clear();

        loadNextBatch();

        if (loadedCount < currentContacts.size()) {
            attachScrollListener();
        }

        contactScroll.setVvalue(0);
        showCards(!contacts.isEmpty());
    }

    private void attachScrollListener() {
        scrollListener = (observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= SCROLL_THRESHOLD) {
                loadNextBatch();
            }
        };
        contactScroll.vvalueProperty().addListener(scrollListener);
    }

    private void cleanupBatchState() {
        if (scrollListener != null) {
            contactScroll.vvalueProperty().removeListener(scrollListener);
            scrollListener = null;
        }
        currentContacts = List.of();
        loadedCount = 0;
    }

    private void loadNextBatch() {
        int target = Math.min(loadedCount + BATCH_SIZE, currentContacts.size());
        for (int i = loadedCount; i < target; i++) {
            int index = isCurrentNumbered ? i + 1 : ContactCard.NO_INDEX;
            contactCards.getChildren().add(new ContactCard(currentContacts.get(i), index).getRoot());
        }
        loadedCount = target;

        if (loadedCount >= currentContacts.size()) {
            if (isCurrentShowingTotal) {
                Label total = new Label(String.format(ListCommand.MESSAGE_TOTAL, currentContacts.size()));
                total.getStyleClass().add(TOTAL_STYLE_CLASS);
                contactCards.getChildren().add(total);
            }
            if (scrollListener != null) {
                contactScroll.vvalueProperty().removeListener(scrollListener);
                scrollListener = null;
            }
        }
    }

    static int getBatchSize() {
        return BATCH_SIZE;
    }

    int getLoadedCount() {
        return loadedCount;
    }

    VBox getContactCards() {
        return contactCards;
    }

    void loadAllRemainingBatches() {
        while (loadedCount < currentContacts.size()) {
            loadNextBatch();
        }
    }

    /** Focuses the currently active result output component. */
    public void focus() {
        if (contactScroll.isVisible()) {
            contactScroll.requestFocus();
        } else {
            resultDisplay.requestFocus();
        }
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
        contactScroll.setFocusTraversable(isShowingCards);
        resultDisplay.setVisible(!isShowingCards);
        resultDisplay.setManaged(!isShowingCards);
        resultDisplay.setFocusTraversable(!isShowingCards);
    }

}
