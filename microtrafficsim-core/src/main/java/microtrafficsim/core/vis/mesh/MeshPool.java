package microtrafficsim.core.vis.mesh;

import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;

import java.util.HashMap;


/**
 * Thread-safe pool to store {@code ManagedMesh}es associated with a key.
 *
 * @param <K> the key-type.
 */
public class MeshPool<K> {
    private HashMap<K, ManagedMesh> pool;

    private final LifeTimeObserver<Mesh> lto = m -> removeMesh((ManagedMesh) m);


    /**
     * Constructs a new, empty {@code MeshPool}.
     */
    public MeshPool() {
        this.pool = new HashMap<>();
    }

    /**
     * Returns the {@code ManagedMesh} associated with the given key.
     *
     * @param key the key for which the {@code ManagedMesh} should be returned.
     * @return the {@code ManagedMesh} associated with the given key or {@code null} if no mesh is associated with the
     * key.
     */
    public synchronized ManagedMesh get(K key) {
        return pool.get(key);
    }

    /**
     * Associates the given key with the given {@code ManagedMesh}.
     *
     * @param key  the key.
     * @param mesh the {@code ManagedMesh} to be associated with the given key.
     * @return the {@code ManagedMesh} previously associated with the given key or {@code null} if none was associated
     * with the key.
     */
    public synchronized ManagedMesh put(K key, ManagedMesh mesh) {
        mesh.addLifeTimeObserver(lto);

        ManagedMesh old = pool.put(key, mesh);
        if (old != null)
            old.removeLifeTimeObserver(lto);

        return old;
    }

    /**
     * Removes the {@code ManagedMesh} associated with the given key.
     *
     * @param key the key for which the {@code ManagedMesh} should be removed.
     * @return the {@code ManagedMesh} that has been removed by this call.
     */
    public synchronized ManagedMesh remove(K key) {
        ManagedMesh mesh = pool.remove(key);

        if (mesh != null)
            mesh.removeLifeTimeObserver(lto);

        return mesh;
    }

    /**
     * Removes (a single instance of) the given mesh.
     *
     * @param mesh the mesh to remove.
     * @return {@¢ode true} if the underlying map changed.
     */
    public synchronized boolean removeMesh(ManagedMesh mesh) {
        mesh.removeLifeTimeObserver(lto);
        return pool.values().remove(mesh);
    }

    /**
     * Checks if this pool contains a mesh associated with the given key.
     *
     * @param key the key to check for.
     * @return {@code true} if this pool contains a mesh associated with the given key.
     */
    public synchronized boolean contains(K key) {
        return pool.containsKey(key);
    }
}
