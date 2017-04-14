package microtrafficsim.ui.gui;

import microtrafficsim.math.MathUtils;

import javax.swing.*;
import java.awt.*;

public class ScrollablePanel extends JPanel implements Scrollable {

    private boolean scrollableTracksViewportWidth = false;
    private boolean scrollableTracksViewportHeight = false;

    public ScrollablePanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public ScrollablePanel(LayoutManager layout) {
        super(layout);
    }

    public ScrollablePanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public ScrollablePanel() {
        super();
    }


    /**
     * @see #getScrollableTracksViewportWidth()
     */
    public void setScrollableTracksViewportWidth(boolean value) {
        scrollableTracksViewportWidth = value;
    }

    /**
     * @see #getScrollableTracksViewportHeight()
     */
    public void setScrollableTracksViewportHeight(boolean value) {
        scrollableTracksViewportHeight = value;
    }


    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(
                MathUtils.clamp(
                        (int) getPreferredSize().getWidth(),
                        (int) getMinimumSize().getWidth(),
                        (int) getMaximumSize().getWidth()
                ),
                MathUtils.clamp(
                        (int) getPreferredSize().getHeight(),
                        (int) getMinimumSize().getHeight(),
                        (int) getMaximumSize().getHeight()
                )
        );
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1; // todo play around
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1; // todo play around
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return scrollableTracksViewportWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return scrollableTracksViewportHeight;
    }
}