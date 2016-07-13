package microtrafficsim.core.vis.opengl.utils;


public interface LifeTimeObserver<T> {
    void disposed(T obj);
}
