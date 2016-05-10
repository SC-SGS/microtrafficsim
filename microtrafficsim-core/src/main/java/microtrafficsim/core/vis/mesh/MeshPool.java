package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.HashMap;


public class MeshPool<K> {
	private HashMap<K, ManagedMesh> pool;
	private final LifeTimeObserver<Mesh> lto = m -> pool.values().remove(m);

	public MeshPool() {
		this.pool = new HashMap<>();
	}

	public ManagedMesh get(K key) {
		return pool.get(key);
	}

	public ManagedMesh put(K key, ManagedMesh mesh) {
		mesh.addLifeTimeObserver(lto);

		ManagedMesh old = pool.put(key, mesh);
		if (old != null)
			old.removeLifeTimeObserver(lto);

		return old;
	}

	public ManagedMesh remove(K key) {
		ManagedMesh mesh = pool.remove(key);

		if (mesh != null)
			mesh.removeLifeTimeObserver(lto);

		return mesh;
	}

	public boolean contains(K key) {
		return pool.containsKey(key);
	}
}
