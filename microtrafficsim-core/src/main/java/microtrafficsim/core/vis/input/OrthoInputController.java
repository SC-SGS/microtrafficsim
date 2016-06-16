package microtrafficsim.core.vis.input;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Vec3d;

import java.util.HashMap;


public class OrthoInputController implements MouseListener, KeyController {
	
	private OrthographicView view;
	
	private int prevX;
	private int prevY;
	
	private double zoomFactor;
	
	private HashMap<Short, KeyCommand> cmdPressed;
	private HashMap<Short, KeyCommand> cmdReleased;
	
	
	public OrthoInputController(OrthographicView view, double zoomMultiplier) {
		this.view = view;
		this.zoomFactor = zoomMultiplier;
		this.cmdPressed = new HashMap<>();
		this.cmdReleased = new HashMap<>();
	}
	
	
	public double getZoomFactor() {
		return zoomFactor;
	}
	
	public void setZoomFactor(double zoomFactor) {
		this.zoomFactor = zoomFactor;
	}
	

	@Override
	public void mousePressed(MouseEvent e) {
		prevX = e.getX();
		prevY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		prevX = e.getX();
		prevY = e.getY();
	}

	int i = 0;
	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		double scale = view.getScale();
		double diffX = (x - prevX) * (1.0 / scale);
		double diffY = (y - prevY) * (1.0 / scale);

		Vec3d pos = view.getPosition();
		view.setPosition(pos.x - diffX, pos.y + diffY);

		prevX = x;
		prevY = y;
	}

	@Override
	public void mouseWheelMoved(MouseEvent e) {
		double zoom = view.getZoomLevel();
		zoom += e.getRotation()[1] * e.getRotationScale() * zoomFactor;
		view.setZoomLevel(zoom);
	}
	

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		KeyCommand cmd = cmdPressed.get(e.getKeyCode());
		if (cmd != null) cmd.event(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		KeyCommand cmd = cmdReleased.get(e.getKeyCode());
		if (cmd != null) cmd.event(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}


	@Override
	public KeyCommand addKeyCommand(short event, short vk, KeyCommand command) {
		switch (event) {
		case KeyEvent.EVENT_KEY_PRESSED:
			return cmdPressed.put(vk, command);
			
		case KeyEvent.EVENT_KEY_RELEASED:
			return cmdReleased.put(vk, command);
		}
		
		return null;
	}

	@Override
	public KeyCommand removeKeyCommand(short event, short vk) {
		switch (event) {
		case KeyEvent.EVENT_KEY_PRESSED:
			return cmdPressed.remove(Short.valueOf(vk));
			
		case KeyEvent.EVENT_KEY_RELEASED:
			return cmdReleased.remove(Short.valueOf(vk));
		}
		
		return null;
	}
}
