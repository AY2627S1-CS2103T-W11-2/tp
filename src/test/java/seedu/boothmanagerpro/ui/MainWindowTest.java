package seedu.boothmanagerpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.util.SampleDataUtil;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;

/** Exercises the actual FXML, selection and command flow on the JavaFX thread. */
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
    public void uiManager_startFocusHelpAndExit() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            ModelManager model = new ModelManager();
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(testFolder.resolve("data.json")),
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
            UiManager manager = new UiManager(new LogicManager(model, storage), testFolder.resolve("data.json")) {
                @Override
                void showFatalErrorDialogAndShutdown(String title, Throwable error) {
                    throw new AssertionError(title, error);
                }
            };
            Stage stage = new Stage();
            stage.setOpacity(0);
            try {
                manager.start(stage);
                assertTrue(stage.isShowing());
                Parent root = stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                TextField command = (TextField) root.lookup("#commandTextField");
                assertEquals(command, stage.getScene().getFocusOwner());
                assertEquals("0 records", ((Label) root.lookup("#recordCount")).getText());
                assertEquals("Select a contact", ((Label) root.lookup("#contactName")).getText());
                root.lookup("#resultDisplay").requestFocus();
                boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
                stage.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.L,
                        false, !isMac, false, isMac));
                assertEquals(command, stage.getScene().getFocusOwner());
                command.setText("help");
                command.fireEvent(new ActionEvent());
                assertEquals(2, Window.getWindows().size());
                command.setText("exit");
                command.fireEvent(new ActionEvent());
                assertFalse(stage.isShowing());
                assertTrue(Window.getWindows().isEmpty());
            } finally {
                for (Window window : java.util.List.copyOf(Window.getWindows())) {
                    window.hide();
                }
            }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    @Test
    public void mainWindow_selectionCommandsAndResponsiveLayout() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(testFolder.resolve("data.json")),
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    testFolder.resolve("data.json"));
            window.fillInnerParts();
            Parent root = window.getRoot().getScene().getRoot();
            root.applyCss();
            root.layout();
            @SuppressWarnings("unchecked")
            ListView<Person> list = (ListView<Person>) root.lookup("#personListView");
            Label name = (Label) root.lookup("#contactName");
            Label count = (Label) root.lookup("#recordCount");
            list.getSelectionModel().select(1);
            assertEquals(list.getItems().get(1).getName().fullName, name.getText());
            TextField command = (TextField) root.lookup("#commandTextField");
            TextArea result = (TextArea) root.lookup("#resultDisplay");
            command.setText("not-a-command");
            command.fireEvent(new ActionEvent());
            assertTrue(result.getStyleClass().contains("error"));
            assertEquals("not-a-command", command.getText());
            command.setText("list");
            command.fireEvent(new ActionEvent());
            assertFalse(result.getStyleClass().contains("error"));
            assertEquals("", command.getText());
            command.setText("find NobodyMatchesThisName");
            command.fireEvent(new ActionEvent());
            assertEquals("0 records", count.getText());
            assertEquals("Select a contact", name.getText());
            command.setText("list");
            command.fireEvent(new ActionEvent());
            window.getPersonListPanel().ensureSelection();
            assertEquals(list.getItems().get(0).getName().fullName, name.getText());
            command.setText("edit 1 n/Updated Representative");
            command.fireEvent(new ActionEvent());
            window.getPersonListPanel().ensureSelection();
            assertEquals("Updated Representative", name.getText());
            command.setText("delete 1");
            command.fireEvent(new ActionEvent());
            window.getPersonListPanel().ensureSelection();
            assertEquals("5 records", count.getText());
            assertEquals(list.getSelectionModel().getSelectedItem().getName().fullName, name.getText());
            savePreview(root, 1180, 820, "main-window.png");
            savePreview(root, 860, 640, "main-window-compact.png");
            command.setText("clear");
            command.fireEvent(new ActionEvent());
            assertEquals("0 records", count.getText());
            assertEquals("Select a contact", name.getText());
            assertTrue(list.getItems().isEmpty());
            window.getRoot().close();
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    private static void savePreview(Parent root, int width, int height, String fileName) throws Exception {
        // A detached scene gives repeatable snapshots without opening a desktop window.
        Scene oldScene = root.getScene();
        oldScene.setRoot(new javafx.scene.layout.StackPane());
        Scene previewScene = new Scene(root, width, height);
        previewScene.getStylesheets().setAll(oldScene.getStylesheets());
        root.applyCss();
        root.layout();
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
