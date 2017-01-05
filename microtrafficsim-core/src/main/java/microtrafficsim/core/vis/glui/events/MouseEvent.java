package microtrafficsim.core.vis.glui.events;


import microtrafficsim.math.Vec2d;

public class MouseEvent {

    public static final short BUTTON1 = com.jogamp.newt.event.MouseEvent.BUTTON1;
    public static final short BUTTON2 = com.jogamp.newt.event.MouseEvent.BUTTON2;
    public static final short BUTTON3 = com.jogamp.newt.event.MouseEvent.BUTTON3;
    public static final short BUTTON4 = com.jogamp.newt.event.MouseEvent.BUTTON4;
    public static final short BUTTON5 = com.jogamp.newt.event.MouseEvent.BUTTON5;
    public static final short BUTTON6 = com.jogamp.newt.event.MouseEvent.BUTTON6;
    public static final short BUTTON7 = com.jogamp.newt.event.MouseEvent.BUTTON7;
    public static final short BUTTON8 = com.jogamp.newt.event.MouseEvent.BUTTON8;
    public static final short BUTTON9 = com.jogamp.newt.event.MouseEvent.BUTTON9;

    public static final short BUTTON_COUNT = com.jogamp.newt.event.MouseEvent.BUTTON_COUNT;

    public static final short EVENT_MOUSE_CLICKED  = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_CLICKED;
    public static final short EVENT_MOUSE_ENTERED  = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_ENTERED;
    public static final short EVENT_MOUSE_EXITED   = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_EXITED;
    public static final short EVENT_MOUSE_PRESSED  = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_PRESSED;
    public static final short EVENT_MOUSE_RELEASED = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_RELEASED;
    public static final short EVENT_MOUSE_MOVED    = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_MOVED;
    public static final short EVENT_MOUSE_DRAGGED  = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_DRAGGED;
    public static final short EVENT_MOUSE_WHEEL_MOVED = com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_WHEEL_MOVED;


    private final Vec2d[] pointer;
    private final com.jogamp.newt.event.MouseEvent base;


    public MouseEvent(Vec2d[] pointer, com.jogamp.newt.event.MouseEvent base) {
        this.pointer = pointer;
        this.base = base;
    }

    public MouseEvent(Vec2d pointer, com.jogamp.newt.event.MouseEvent base) {
        this.pointer = new Vec2d[]{new Vec2d(pointer)};
        this.base = base;
    }

    public final MouseEvent createVariant(final short newEventType) {
        return new MouseEvent(getPointer(), base.createVariant(newEventType));
    }


    public final com.jogamp.newt.event.MouseEvent getBaseEvent() {
        return base;
    }


    public final short getButton() {
        return base.getButton();
    }


    public final double getX() {
        return pointer[0].x;
    }

    public final double getY() {
        return pointer[0].y;
    }

    public final Vec2d getPointer() {
        return pointer[0];
    }

    public final Vec2d[] getAllPointers() {
        return pointer;
    }



    public void setConsumed(boolean consumed) {
        base.setConsumed(consumed);
    }

    public boolean isConsumed() {
        return base.isConsumed();
    }

    public int getPointerCount() {
        return base.getPointerCount();
    }


    public boolean isControlDown() {
        return base.isControlDown();
    }

    public boolean isShiftDown() {
        return base.isShiftDown();
    }
}
