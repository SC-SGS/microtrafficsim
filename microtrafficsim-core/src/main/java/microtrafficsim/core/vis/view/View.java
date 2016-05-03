package microtrafficsim.core.vis.view;

import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Vec2i;
import microtrafficsim.math.Vec3d;
import microtrafficsim.math.Vec3f;


public interface View {
	
	Vec2i getSize();
	void resize(Vec2i size);
	void resize(int x, int y);
	
	Vec3d getPosition();
	void setPosition(Vec3d position);
	void setPosition(double x, double y, double z);
	
	Mat4f getViewProjection();
	Mat4f getViewMatrix();
	Mat4f getProjectionMatrix();
}
