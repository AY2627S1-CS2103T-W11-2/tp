package seedu.boothmanagerpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import seedu.boothmanagerpro.commons.core.GuiSettings;

public class WindowPlacementTest {
    private static final Rectangle2D PRIMARY = new Rectangle2D(0, 0, 1920, 1040);

    @Test
    public void disconnectedMonitor_centresWindowOnPrimary() {
        assertEquals(new Rectangle2D(460, 170, 1000, 700),
                fit(new GuiSettings(1000, 700, 2500, -300), PRIMARY));
    }

    @Test
    public void partiallyVisibleWindow_keepsTitleBarOnScreen() {
        assertEquals(new Rectangle2D(920, 0, 1000, 998),
                fit(new GuiSettings(1000, 998, 1454, -301), PRIMARY));
    }

    @Test
    public void validPosition_preservedOnSecondaryScreen() {
        Rectangle2D secondary = new Rectangle2D(-1600, 0, 1600, 900);
        assertEquals(new Rectangle2D(-1500, 50, 1000, 700),
                fit(new GuiSettings(1000, 700, -1500, 50), PRIMARY, secondary));
    }

    @Test
    public void oversizedWindow_shrinksToSmallScreenIncludingTaskbar() {
        Rectangle2D small = new Rectangle2D(0, 30, 800, 570);
        assertEquals(small, fit(new GuiSettings(2000, 1200, 100, 100), small));
    }

    @Test
    public void firstLaunch_centresDefaultSize() {
        assertEquals(new Rectangle2D(370, 110, 1180, 820), fit(new GuiSettings(), PRIMARY));
    }

    @Test
    public void invalidSizes_useDefaultsAndStayVisible() {
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, 0, -1}) {
            assertEquals(new Rectangle2D(0, 0, 1180, 820),
                    fit(new GuiSettings(invalid, invalid, 0, 0), PRIMARY));
        }
    }

    @Test
    public void spanningWindow_usesScreenWithLargestOverlap() {
        Rectangle2D secondary = new Rectangle2D(1920, 0, 1600, 900);
        Rectangle2D bounds = fit(new GuiSettings(1000, 700, 1800, 100), PRIMARY, secondary);
        assertEquals(1920, bounds.getMinX());
        assertTrue(secondary.contains(bounds));
    }

    private static Rectangle2D fit(GuiSettings settings, Rectangle2D... screens) {
        return WindowPlacement.fit(settings, List.of(screens), 860, 640);
    }
}
