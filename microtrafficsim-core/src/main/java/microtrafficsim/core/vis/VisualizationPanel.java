package microtrafficsim.core.vis;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;


public class VisualizationPanel extends JPanel {
	
	private static final long serialVersionUID = -3494671834324453286L;

	private FPSAnimator animator;
	
	private Visualization visualization;
	
	
	public VisualizationPanel(Visualization visualization) throws UnsupportedFeatureException {
		this(visualization, visualization.getDefaultConfig());
	}
	
	public VisualizationPanel(Visualization visualization, VisualizerConfig config) {
		this.visualization = visualization;
		
		GLWindow window = GLWindow.create(config.glcapabilities);
		window.addGLEventListener(visualization.getRenderContext());
		window.addMouseListener(visualization.getMouseController());
		window.addKeyListener(visualization.getKeyController());
		
		animator = new FPSAnimator(window, config.fps, true);
		visualization.getRenderContext().setAnimator(animator);
		
		NewtCanvasAWT canvas = new NewtCanvasAWT(window);
		
		this.setLayout(new BorderLayout());
		this.add(canvas, BorderLayout.CENTER);
	}
	
	
	public void start() {
		animator.start();
	}
	
	public void pause() {
		animator.pause();
	}
	
	public void stop() {
		animator.stop();
	}
	
	public boolean isStarted() {
		return animator.isStarted();
	}
	
	public boolean isPaused() {
		return animator.isPaused();
	}
	
	
	public Visualization getVisualization() {
		return visualization;
	}
}
