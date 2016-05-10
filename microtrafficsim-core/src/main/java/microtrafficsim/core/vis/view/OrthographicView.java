package microtrafficsim.core.vis.view;

import microtrafficsim.math.*;


public class OrthographicView implements View {
	
	private int width;
	private int height;
	
	private float zNear;
	private float zFar;
	
	private double zoom;
	private double zoomMax;
	private double zoomMin;
	
	private Vec3d position;
	
	private Mat4f projection;
	private Mat4f view;
	private Mat4f viewprojection;
	
	
	public OrthographicView(int width, int height, float zNear, float zFar, double zoomMin, double zoomMax) {
		this.width = width;
		this.height = height;
		this.zNear = zNear;
		this.zFar = zFar;
		
		this.projection = Mat4f.identity();
		this.view = Mat4f.identity();
		this.viewprojection = Mat4f.identity();
		
		this.position = new Vec3d(0.0, 0.0, 10.0);
		
		this.zoom = 0.0f;
		this.zoomMin = zoomMin;
		this.zoomMax = zoomMax;
		
		updateProjection();
		updateView();
	}
	
	
	@Override
	public Vec2i getSize() {
		return new Vec2i(width, height);
	}
	
	@Override
	public void resize(Vec2i size) {
		this.width = size.x;
		this.height = size.y;
		updateProjection();
	}
	
	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		updateProjection();
	}

	
	@Override
	public Vec3d getPosition() {
		return position;
	}

	public void setPosition(Vec2d position) {
		this.position.x = position.x;
		this.position.y = position.y;
		updateView();
	}
	
	public void setPosition(double x, double y) {
		this.position.x = x;
		this.position.y = y;
		updateView();
	}
	
	@Override
	public void setPosition(Vec3d position) {
		this.position.set(position);
		updateView();
	}

	@Override
	public void setPosition(double x, double y, double z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
		updateView();
	}
	
	
	public void setZoomLevel(double zoom) {
		this.zoom = zoom;
		
		if (this.zoom > zoomMax)
			this.zoom = zoomMax;
		else if (this.zoom < zoomMin)
			this.zoom = zoomMin;
			
		updateProjection();
	}
	
	public double getZoomLevel() {
		return this.zoom;
	}
	
	public void setScale(double scale) {
		setZoomLevel(Math.log(scale) / Math.log(2));
	}
	
	public double getScale() {
		return Math.pow(2.0, zoom);
	}


	public Rect2d getViewportBounds() {
		double scale = getScale();

		return new Rect2d(
				position.x - width  / (scale * 2.0),
				position.y - height / (scale * 2.0),
				position.x + width  / (scale * 2.0),
				position.y + height / (scale * 2.0)
		);
	}


	@Override
	public Mat4f getViewProjection() {
		return viewprojection;
	}

	@Override
	public Mat4f getViewMatrix() {
		return view;
	}

	@Override
	public Mat4f getProjectionMatrix() {
		return projection;
	}

	
	private void updateProjection() {
		/*
		 * Note: no need to zero-check the size here, JOGL crashes before the reshape
		 * call if height/width is zero. Set the Window minimum-size instead.
		 */
		
		float left = -width / 2.0f;
		float right = width / 2.0f;
		float top = height / 2.0f;
		float bottom = -height / 2.0f;

		float scale = (float) getScale();
		projection.makeOrtho(left, right, top, bottom, zNear, zFar).scale(scale, scale, 1);
		viewprojection.set(projection).mul(view);
	}
	
	private void updateView() {
		view.makeLookInDirection(new Vec3f(position), new Vec3f(0, 0, -1), new Vec3f(0, 1, 0));
		viewprojection.set(projection).mul(view);
	}
}
