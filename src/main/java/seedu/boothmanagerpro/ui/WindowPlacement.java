package seedu.boothmanagerpro.ui;

import java.awt.Point;
import java.util.List;

import javafx.geometry.Rectangle2D;
import seedu.boothmanagerpro.commons.core.GuiSettings;

/** Computes visible window bounds without creating native windows or requiring a JavaFX application thread. */
final class WindowPlacement {
    private WindowPlacement() {
    }

    /**
     * Fits saved bounds inside an available screen's visual area, keeping the title bar reachable.
     * The first screen is the primary fallback. The screen with greatest overlap retains a saved position;
     * a disconnected-screen position or first launch is centred on the primary screen instead.
     * Minimum dimensions yield to screen size on small displays. Screen bounds must be non-empty and positive.
     */
    static Rectangle2D fit(GuiSettings settings, List<Rectangle2D> screens, double minWidth, double minHeight) {
        GuiSettings defaults = new GuiSettings();
        double width = validSize(settings.getWindowWidth(), defaults.getWindowWidth());
        double height = validSize(settings.getWindowHeight(), defaults.getWindowHeight());
        Point position = settings.getWindowCoordinates();
        Rectangle2D screen = screens.getFirst();
        double largestOverlap = 0;
        if (position != null) {
            for (Rectangle2D candidate : screens) {
                double overlap = overlapArea(position, width, height, candidate);
                if (overlap > largestOverlap) {
                    screen = candidate;
                    largestOverlap = overlap;
                }
            }
        }
        width = Math.min(Math.max(width, minWidth), screen.getWidth());
        height = Math.min(Math.max(height, minHeight), screen.getHeight());
        double x = screen.getMinX() + (screen.getWidth() - width) / 2;
        double y = screen.getMinY() + (screen.getHeight() - height) / 2;
        if (position != null && largestOverlap > 0) {
            x = clamp(position.x, screen.getMinX(), screen.getMaxX() - width);
            y = clamp(position.y, screen.getMinY(), screen.getMaxY() - height);
        }
        return new Rectangle2D(x, y, width, height);
    }

    private static double validSize(double value, double fallback) {
        return Double.isFinite(value) && value > 0 ? value : fallback;
    }

    private static double overlapArea(Point position, double width, double height, Rectangle2D screen) {
        double overlapWidth = Math.max(0, Math.min(position.x + width, screen.getMaxX())
                - Math.max(position.x, screen.getMinX()));
        double overlapHeight = Math.max(0, Math.min(position.y + height, screen.getMaxY())
                - Math.max(position.y, screen.getMinY()));
        return overlapWidth * overlapHeight;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
