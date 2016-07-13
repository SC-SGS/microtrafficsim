package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.HashMap;


public class MeshPool<K> {
    private HashMap<K, ManagedMesh> pool;

    private final LifeTimeObserver<Mesh> lto = m -> removeMesh((ManagedMesh) m);


    public MeshPool() {
        this.pool = new HashMap<>();
    }

    public synchronized ManagedMesh get(K key) {
        return pool.get(key);
    }

    public synchronized ManagedMesh put(K key, ManagedMesh mesh) {
        mesh.addLifeTimeObserver(lto);

        ManagedMesh old = pool.put(key, mesh);
        if (old != null)
            old.removeLifeTimeObserver(lto);

        return old;
    }

    public synchronized ManagedMesh remove(K key) {
        ManagedMesh mesh = pool.remove(key);

        if (mesh != null)
            mesh.removeLifeTimeObserver(lto);

        return mesh;
    }

    public synchronized boolean removeMesh(ManagedMesh mesh) {
        mesh.removeLifeTimeObserver(lto);
        return pool.values().remove(mesh);
    }

    public synchronized boolean contains(K key) {
        return pool.containsKey(key);
    }
}
