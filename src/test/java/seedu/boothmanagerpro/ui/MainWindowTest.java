package seedu.boothmanagerpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.boothmanagerpro.logic.LogicManager;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.util.SampleDataUtil;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;

/** Exercises command-only FXML, text results, keyboard controls, and persistence. */
public class MainWindowTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").toLowerCase().contains("linux")
                || System.getenv("DISPLAY") != null, "JavaFX requires a display; run with xvfb on Linux.");
        FutureTask<Void> startup = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        Platform.startup(startup);
        startup.get(15, TimeUnit.SECONDS);
    }

    @Test
    public void add_enquiryMetadata_isVisibleInContactCard() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            enterCommand(root, "add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567"
                    + " s/In progress f/20261025");
            enterCommand(root, "view n/Alicia");
            VBox cards = (VBox) root.lookup("#contactCards");
            Parent card = (Parent) cards.getChildren().getFirst();
            Label status = (Label) card.lookup("#enquiryStatus");
            assertEquals("In progress", status.getText());
            assertEquals("Enquiry status: In progress", status.getAccessibleText());
            assertTrue(status.getStyleClass().contains("status-in-progress"));
            assertTrue(card.getStyleClass().contains("status-in-progress"));
            assertEquals("Follow-up date: 25 Oct 2026", ((Label) card.lookup("#followUpDate")).getText());
        });
    }

    @Test
    public void find_followUpDate_showsMatchingCardsAndRetainsInvalidInput() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            enterCommand(root, "add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 f/20261025");
            enterCommand(root, "add n/Ben Lim c/Other e/ben@example.com p/98765432");
            enterCommand(root, "find f/20261025");
            assertTrue(output(root).startsWith("1 contact(s) found."));
            assertEquals("", input(root).getText());
            VBox cards = (VBox) root.lookup("#contactCards");
            assertEquals(1, cards.getChildren().size(), "One contact and no total, as the heading has the count");
            Parent card = (Parent) cards.getChildren().getFirst();
            assertEquals("1. Alicia Tan", ((Label) card.lookup("#name")).getText());
            assertEquals("Follow-up date: 25 Oct 2026", ((Label) card.lookup("#followUpDate")).getText());
            savePreview(root, 1180, 820, "cli-find-date.png");
            enterCommand(root, "find f/20230229");
            assertTrue(output(root).contains("Invalid value for f/"));
            assertEquals("find f/20230229", input(root).getText());
            enterCommand(root, "update 1 f/20261028");
            enterCommand(root, "find f/20261025");
            assertEquals("0 contact(s) found.", output(root));
            assertTrue(cards.getChildren().isEmpty());
            assertFalse(root.lookup("#contactScroll").isVisible());
            enterCommand(root, "find f/20261028");
            assertEquals(1, cards.getChildren().size());
        });
    }

    @Test
    public void list_eachEnquiryStatus_showsMatchingBadge() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            enterCommand(root, "add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567");
            enterCommand(root, "add n/Ben Lim c/Paper and Ink e/ben@example.com p/92345678 s/In progress");
            enterCommand(root, "add n/Chloe Ng c/Green Goods e/chloe@example.com p/93456789 s/Completed");
            enterCommand(root, "add n/David Lee c/Other e/david@example.com p/94567890 s/Cancelled");
            enterCommand(root, "list");
            VBox cards = (VBox) root.lookup("#contactCards");
            String[] expected = {"status-needs-follow-up", "status-in-progress", "status-completed",
                "status-cancelled"};
            for (int i = 0; i < expected.length; i++) {
                Parent card = (Parent) cards.getChildren().get(i);
                assertTrue(card.getStyleClass().contains(expected[i]));
                assertTrue(card.lookup("#enquiryStatus").getStyleClass().contains(expected[i]));
            }
            savePreview(root, 1180, 820, "cli-status.png");
        });
    }

    @Test
    public void update_successAndFailure_showFeedbackAndSavedDetails() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            enterCommand(root, "add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567");
            enterCommand(root, "update Alicia Tan s/Completed f/20261025");
            assertEquals("Updated Alicia Tan at TechNova.", output(root));
            assertEquals("", input(root).getText());
            enterCommand(root, "view n/Alicia");
            VBox cards = (VBox) root.lookup("#contactCards");
            Parent card = (Parent) cards.getChildren().getFirst();
            Label status = (Label) card.lookup("#enquiryStatus");
            assertEquals("Completed", status.getText());
            assertTrue(status.getStyleClass().contains("status-completed"));
            assertFalse(status.getStyleClass().contains("status-in-progress"));
            assertEquals("Follow-up date: 25 Oct 2026", ((Label) card.lookup("#followUpDate")).getText());
            enterCommand(root, "update Alicia Tan f/20230229");
            assertTrue(output(root).contains("Invalid value for f/"));
            assertEquals("update Alicia Tan f/20230229", input(root).getText());
            enterCommand(root, "tag Alicia Tan t/vip t/sponsor");
            assertEquals("Updated tags for Alicia Tan at TechNova. Added: vip, sponsor. "
                    + "Removed: None. Current tags: vip, sponsor.", output(root));
            enterCommand(root, "tag Alicia Tan t/vip t/keynote");
            assertEquals("Updated tags for Alicia Tan at TechNova. Added: keynote. "
                    + "Removed: vip. Current tags: sponsor, keynote.", output(root));
        });
    }

    @Test
    public void startupAndRestart_showOnlyPromptWithoutLoadingContactsIntoOutput() throws Exception {
        ModelManager model = sampleModel();
        withWindow(model, (window, root, storage) -> {
            assertTrue(output(root).startsWith("Ready."));
            assertFalse(output(root).contains("Alex Yeoh"));
            assertEquals(6, model.getAddressBook().getPersonList().size());
            assertNoContactPanels(root);
            savePreview(root, 1180, 820, "cli-startup.png");
            enterCommand(root, "list");
            assertTrue(output(root).contains("1. Alex Yeoh"));
        });
        ModelManager reloaded = new ModelManager(
                new JsonAddressBookStorage(dataPath()).readAddressBook().orElseThrow(), new UserPrefs());
        withWindow(reloaded, (window, root, storage) -> {
            assertTrue(output(root).startsWith("Ready."));
            assertFalse(output(root).contains("Alex Yeoh"));
            assertEquals(6, reloaded.getAddressBook().getPersonList().size());
            assertNoContactPanels(root);
        });
    }

    @Test
    public void find_printsNumberedMatchesWithoutListCommand_andIndicesWork() throws Exception {
        ModelManager model = sampleModel();
        withWindow(model, (window, root, storage) -> {
            enterCommand(root, "find Alex David");
            String found = output(root);
            assertTrue(found.startsWith("2 contact(s) found.\n\n1. Alex Yeoh"));
            assertTrue(found.contains("2. David Li at Not specified\nEmail: lidavid@example.com"));
            assertFalse(found.contains("Bernice Yu"));
            assertEquals("", input(root).getText());
            assertNoContactPanels(root);
            VBox foundCards = (VBox) root.lookup("#contactCards");
            assertEquals(2, foundCards.getChildren().size(), "2 cards and no total, as the heading has the count");
            savePreview(root, 1180, 820, "cli-find.png");
            savePreview(root, 860, 640, "cli-compact.png");
            enterCommand(root, "update 2 p/91234567");
            assertTrue(output(root).startsWith("Updated David Li"));
            enterCommand(root, "find Alex David");
            assertTrue(output(root).contains("Phone: 91234567"));
            enterCommand(root, "delete 2");
            assertTrue(output(root).startsWith("Deleted contact: David Li"));
            assertEquals(5, storage.readAddressBook().orElseThrow().getPersonList().size());
            enterCommand(root, "list");
            assertTrue(output(root).contains("Total contacts: 5"));
            assertFalse(output(root).contains("David Li"));
            VBox cards = (VBox) root.lookup("#contactCards");
            assertTrue(root.lookup("#contactScroll").isVisible());
            assertFalse(root.lookup("#resultDisplay").isVisible());
            assertTrue(cards.isMouseTransparent());
            assertEquals(6, cards.getChildren().size(), "5 cards and the total");
            savePreview(root, 1180, 820, "cli-list.png");
            enterCommand(root, "not-a-command");
            assertTrue(root.lookup("#resultDisplay").isVisible());
            assertFalse(root.lookup("#contactScroll").isVisible());
        });
    }

    @Test
    public void find_emptyAndInvalidRequests_doNotShowStaleContacts() throws Exception {
        ModelManager model = sampleModel();
        withWindow(model, (window, root, storage) -> {
            enterCommand(root, "list");
            enterCommand(root, "find Nobody");
            assertEquals("0 contact(s) found.", output(root));
            enterCommand(root, "find Alex");
            String before = Files.readString(dataPath());
            enterCommand(root, "find n/");
            assertEquals("find n/", input(root).getText());
            assertTrue(root.lookup("#resultDisplay").getStyleClass().contains("error"));
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals(before, Files.readString(dataPath()));
            assertNoContactPanels(root);
            enterCommand(root, "clear");
            assertTrue(output(root).contains("This will delete all contacts! Type 'clear confirm' to proceed."));
            assertEquals(1, model.getFilteredPersonList().size());
            enterCommand(root, "clear confirm");
            enterCommand(root, "list");
            assertTrue(output(root).startsWith("No contacts found."));
            assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        });
    }

    @Test
    public void view_numberedChoicesAndCancellation_workEntirelyInText() throws Exception {
        ModelManager model = new ModelManager();
        withWindow(model, (window, root, storage) -> {
            enterCommand(root, "add n/Alicia Tan c/TechNova e/first@example.com p/91234567");
            enterCommand(root, "add n/Alicia Tan c/Other Ltd e/second@example.com p/87654321");
            String before = Files.readString(dataPath());
            enterCommand(root, "view n/Alicia Tan");
            assertTrue(output(root).contains("1. Alicia Tan at TechNova"));
            assertTrue(output(root).contains("2. Alicia Tan at Other Ltd"));
            enterCommand(root, "3");
            assertEquals("Please enter a number between 1 and 2.", output(root));
            enterCommand(root, "2");
            assertTrue(output(root).startsWith("Exhibitor contact found:\nAlicia Tan at Other Ltd"));
            assertTrue(output(root).contains("Email: second@example.com"));
            assertEquals(before, Files.readString(dataPath()));
            assertNoContactPanels(root);
            savePreview(root, 1180, 820, "cli-view.png");
            enterCommand(root, "view n/Alicia Tan");
            enterCommand(root, "find");
            enterCommand(root, "1");
            assertTrue(output(root).contains("Alicia Tan at TechNova"));
            enterCommand(root, "view n/Alicia Tan");
            enterCommand(root, "delete 99");
            enterCommand(root, "1");
            assertEquals("Unknown command.", output(root));
            enterCommand(root, "view n/Alicia Tan");
            enterCommand(root, "list");
            enterCommand(root, "2");
            assertEquals("Unknown command.", output(root));
        });
    }

    @Test
    public void add_persistsNormalisedValuesAndKeepsRejectedInputForCorrection() throws Exception {
        ModelManager model = sampleModel();
        withWindow(model, (window, root, storage) -> {
            enterCommand(root, "find Nobody");
            String add = "add n/Alicia Tan c/TechNova e/ALICIA@example.com p/+65 9123-4567 m/email"
                    + " t/technology t/high-priority";
            enterCommand(root, add);
            assertEquals("Added Alicia Tan at TechNova.", output(root));
            assertEquals("", input(root).getText());
            assertEquals(input(root), root.getScene().getFocusOwner());
            enterCommand(root, add);
            assertEquals(add, input(root).getText());
            assertTrue(root.lookup("#resultDisplay").getStyleClass().contains("error"));
            assertEquals(7, storage.readAddressBook().orElseThrow().getPersonList().size());
            enterCommand(root, "view n/Alicia");
            assertTrue(output(root).contains("Email: alicia@example.com"));
            assertTrue(output(root).contains("Phone: +6591234567"));
            assertTrue(output(root).contains("Tags: technology, high-priority"));
            assertFalse(root.lookup("#resultDisplay").getStyleClass().contains("error"));
            assertNoContactPanels(root);
        });
    }

    @Test
    public void keyboardFocusHelpAndExit_workWithoutMenus() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            assertEquals(input(root), root.getScene().getFocusOwner());
            root.lookup("#resultDisplay").requestFocus();
            boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
            window.getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.L,
                    false, !isMac, false, isMac));
            assertEquals(input(root), root.getScene().getFocusOwner());
            input(root).fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                    false, false, false, false));
            assertEquals(1, Window.getWindows().size());
            assertTrue(output(root).contains("BoothManagerPro commands"));
            assertTrue(output(root).contains("find"));
            assertTrue(output(root).contains("User Guide:"));
            assertEquals(input(root), root.getScene().getFocusOwner());
            enterCommand(root, "help");
            assertEquals(1, Window.getWindows().size());
            assertTrue(output(root).contains("BoothManagerPro commands"));
            assertTrue(output(root).contains("find"));
            assertTrue(output(root).contains("User Guide:"));
            assertEquals(input(root), root.getScene().getFocusOwner());
            enterCommand(root, "exit");
            assertFalse(window.getRoot().isShowing());
            assertTrue(Window.getWindows().isEmpty());
        });
    }

    @Test
    public void tabAutocomplete_completesCommandsAndRequiredPrefixes() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            TextField commandInput = input(root);

            commandInput.setText("del");
            pressKey(commandInput, KeyCode.A);
            assertEquals("del", commandInput.getText());

            pressKey(commandInput, KeyCode.TAB);
            assertEquals("delete ", commandInput.getText());
            assertEquals(commandInput.getText().length(), commandInput.getCaretPosition());

            commandInput.setText("ta");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("tag ", commandInput.getText());

            commandInput.setText("e");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("exit ", commandInput.getText());

            commandInput.setText("not-a-command");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("not-a-command", commandInput.getText());

            commandInput.setText("add unexpected-text");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add unexpected-text", commandInput.getText());

            commandInput.setText("add");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/", commandInput.getText());

            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/", commandInput.getText());

            commandInput.appendText("Alicia Tan");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/Alicia Tan c/", commandInput.getText());

            commandInput.appendText("TechNova");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/Alicia Tan c/TechNova e/", commandInput.getText());
            assertEquals(commandInput.getText().length(), commandInput.getCaretPosition());

            commandInput.setText("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567",
                    commandInput.getText());

            commandInput.setText("view");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("view n/", commandInput.getText());

            commandInput.appendText("Alicia Tan");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("view n/Alicia Tan", commandInput.getText());
            assertEquals(root.lookup("#resultDisplay"), root.getScene().getFocusOwner());
        });
    }

    @Test
    public void tabAutocomplete_completesNewCommandFieldsAndFixedValues() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            TextField commandInput = input(root);

            commandInput.setText("li");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("list ", commandInput.getText());
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("list o/", commandInput.getText());
            commandInput.appendText("st");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("list o/status", commandInput.getText());

            commandInput.setText("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 m");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 m/",
                    commandInput.getText());
            commandInput.appendText("e");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 m/email",
                    commandInput.getText());

            commandInput.setText("update Alicia Tan s");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("update Alicia Tan s/", commandInput.getText());
            commandInput.appendText("In");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("update Alicia Tan s/In progress", commandInput.getText());

            commandInput.setText("tag Alicia Tan");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("tag Alicia Tan t/", commandInput.getText());

            commandInput.setText("find f");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("find f/", commandInput.getText());

            commandInput.setText("fi");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("find ", commandInput.getText());
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("find n/", commandInput.getText());

            commandInput.setText("update 1 c");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("update 1 c/", commandInput.getText());

            commandInput.setText("view n");
            pressKey(commandInput, KeyCode.TAB);
            assertEquals("view n/", commandInput.getText());
            assertEquals(commandInput.getText().length(), commandInput.getCaretPosition());
        });
    }

    @Test
    public void commandBox_blankAndRepeatedInvalidInput_areHandledSafely() throws Exception {
        withWindow(new ModelManager(), (window, root, storage) -> {
            TextField commandInput = input(root);
            String initialOutput = output(root);

            commandInput.setText("   ");
            commandInput.fireEvent(new ActionEvent());
            assertEquals("   ", commandInput.getText());
            assertEquals(initialOutput, output(root));

            commandInput.setText("not-a-command");
            commandInput.fireEvent(new ActionEvent());
            assertTrue(commandInput.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            commandInput.fireEvent(new ActionEvent());
            assertTrue(commandInput.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void tabFocusReturn_keepsCommandInputUnselected() throws Exception {
        Stage stage = runOnFxThread(() -> {
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath()),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
            Stage newStage = new Stage();
            newStage.setOpacity(0);
            MainWindow window = new MainWindow(newStage, new LogicManager(new ModelManager(), storage), dataPath());
            window.fillInnerParts();
            window.show();
            Parent root = newStage.getScene().getRoot();
            input(root).setText("find n/Alicia");
            pressKey(input(root), KeyCode.TAB);
            assertEquals(root.lookup("#resultDisplay"), root.getScene().getFocusOwner());
            pressKey(root.getScene().getFocusOwner(), KeyCode.TAB);
            assertEquals(input(root), root.getScene().getFocusOwner());
            return newStage;
        });
        try {
            // Runs after the deferred caret update queued when the command input regained focus.
            runOnFxThread(() -> {
                TextField commandInput = input(stage.getScene().getRoot());
                assertEquals("", commandInput.getSelectedText());
                assertEquals(commandInput.getLength(), commandInput.getCaretPosition());
                return null;
            });
        } finally {
            runOnFxThread(() -> {
                for (Window open : List.copyOf(Window.getWindows())) {
                    open.hide();
                }
                return null;
            });
        }
    }

    private ModelManager sampleModel() {
        return new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
    }

    private Path dataPath() {
        return testFolder.resolve("contacts.json");
    }

    private void withWindow(ModelManager model, UiAction action) throws Exception {
        runOnFxThread(() -> {
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath()),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
            Stage stage = new Stage();
            stage.setOpacity(0);
            MainWindow window = new MainWindow(stage, new LogicManager(model, storage), dataPath());
            window.fillInnerParts();
            window.show();
            try {
                Parent root = stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                action.run(window, root, storage);
            } finally {
                for (Window open : List.copyOf(Window.getWindows())) {
                    open.hide();
                }
            }
            return null;
        });
    }

    @FunctionalInterface
    private interface UiAction {
        void run(MainWindow window, Parent root, StorageManager storage) throws Exception;
    }

    private static void assertNoContactPanels(Parent root) {
        assertTrue(root.lookupAll(".list-view").isEmpty());
        assertTrue(root.lookupAll(".split-pane").isEmpty());
        assertNull(root.lookup("#contactDetailsPlaceholder"));
        assertNull(root.lookup("#personListPanelPlaceholder"));
        assertNull(root.lookup("#recordCount"));
    }

    private static TextField input(Parent root) {
        return (TextField) root.lookup("#commandTextField");
    }

    private static void pressKey(Node node, KeyCode keyCode) {
        node.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false));
    }

    private static String output(Parent root) {
        return ((TextArea) root.lookup("#resultDisplay")).getText();
    }

    private static void enterCommand(Parent root, String text) {
        input(root).setText(text);
        input(root).fireEvent(new ActionEvent());
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(30, TimeUnit.SECONDS);
    }

    private static void savePreview(Parent root, int width, int height, String fileName) throws Exception {
        // A detached scene gives repeatable snapshots without opening a desktop window.
        Scene oldScene = root.getScene();
        oldScene.setRoot(new javafx.scene.layout.StackPane());
        Scene previewScene = new Scene(root, width, height);
        previewScene.getStylesheets().setAll(oldScene.getStylesheets());
        root.applyCss();
        root.layout();
        Node output = root.lookup("#resultDisplay").isVisible()
                ? root.lookup("#resultDisplay") : root.lookup("#contactScroll");
        assertTrue(output.getLayoutBounds().getHeight() > height * 0.6, "Output should fill the remaining workspace");
        WritableImage image = root.snapshot(null, null);
        BufferedImage bufferedImage = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                bufferedImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        Path directory = Path.of("build", "reports", "ui");
        Files.createDirectories(directory);
        ImageIO.write(bufferedImage, "png", directory.resolve(fileName).toFile());
        previewScene.setRoot(new javafx.scene.layout.StackPane());
        oldScene.setRoot(root);
    }
}
