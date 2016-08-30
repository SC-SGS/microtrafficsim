package microtrafficsim.core.vis.opengl.utils;


/**
 * Callback-interface to receive notifications about the life-time of an (OpenGL) object.
 *
 * @param <T> the type of the object that should be observed.
 * @author Maximilian Luz
 */
public interface LifeTimeObserver<T> {

    /**
     * Called when {@code obj} is beeing disposed.
     *
     * @param obj the object beeing disposed.
     */
    void disposed(T obj);
}
