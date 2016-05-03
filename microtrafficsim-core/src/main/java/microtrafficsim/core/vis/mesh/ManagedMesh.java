package microtrafficsim.core.vis.mesh;

import java.util.List;
import java.util.Set;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;


public class ManagedMesh implements Mesh {
	
	private Mesh mesh;
	private int refcount;
	
	public ManagedMesh(Mesh mesh) {
		this.mesh = mesh;
		this.refcount = 1;
	}


	public int getReferenceCount() {
		return refcount;
	}

	@Override
	public State getState() {
		return mesh.getState();
	}

	
	public ManagedMesh require() {
		refcount++;
		return this;
	}

	@Override
	public boolean initialize(RenderContext context) {
		return mesh.initialize(context);
	}
	
	@Override
	public boolean initialize(RenderContext context, boolean force) {
		return mesh.initialize(context, force);
	}

	@Override
	public boolean load(RenderContext context) {
		return mesh.load(context);
	}
	
	@Override
	public boolean load(RenderContext context, boolean force) {
		return mesh.load(context, force);
	}

	@Override
	public void display(RenderContext context, ShaderProgram shader) {
		mesh.display(context, shader);
	}

	@Override
	public void display(RenderContext context, VertexArrayObject vao) {
		mesh.display(context, vao);
	}

	@Override
	public boolean dispose(RenderContext context) {
		if (mesh.getState() == State.DISPOSED || mesh.getState() == State.UNINITIALIZED) return false;

		if (refcount == 1) {
			refcount = 0;
			return mesh.dispose(context);
		} else {
			refcount--;
			return true;
		}
	}
	
	public boolean dispose(RenderContext context, boolean force) {
		if (force)
			return mesh.dispose(context);
		else
			return this.dispose(context);
	}


	@Override
	public VertexArrayObject createVAO(RenderContext context, ShaderProgram program) {
		return mesh.createVAO(context, program);
	}


	@Override
	public List<? extends MeshBucket> getBuckets() {
		return mesh.getBuckets();
	}

	
	@Override
	public void addLifeTimeObserver(LifeTimeObserver<Mesh> lto) {
		mesh.addLifeTimeObserver(lto);
	}
	
	public void removeLifeTimeObserver(LifeTimeObserver<Mesh> lto) {
		mesh.removeLifeTimeObserver(lto);
	}
	
	public Set<LifeTimeObserver<Mesh>> getLifeTimeObservers() {
		return mesh.getLifeTimeObservers();
	}
}
