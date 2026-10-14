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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
            savePreview(root, 1180, 820, "cli-find.png");
            savePreview(root, 860, 640, "cli-compact.png");
            enterCommand(root, "edit 2 p/91234567");
            assertTrue(output(root).startsWith("Edited contact: David Li"));
            enterCommand(root, "find Alex David");
            assertTrue(output(root).contains("Phone: 91234567"));
            enterCommand(root, "delete 2");
            assertTrue(output(root).startsWith("Deleted contact: David Li"));
            assertEquals(5, storage.readAddressBook().orElseThrow().getPersonList().size());
            enterCommand(root, "list");
            assertTrue(output(root).contains("Total contacts: 5"));
            assertFalse(output(root).contains("David Li"));
            savePreview(root, 1180, 820, "cli-list.png");
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
        TextArea result = (TextArea) root.lookup("#resultDisplay");
        assertTrue(result.getHeight() > height * 0.6, "Text output should fill the remaining workspace");
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
