package microtrafficsim.utils.collections;


public class Grid<T> {
    private int sizeX;
    private int sizeY;
    private Object[] data;

    public Grid(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.data = new Object[sizeX * sizeY];
    }


    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }


    public void set(int x, int y, T element) {
        data[index(x, y)] = element;
    }

    @SuppressWarnings("unchecked")
    public T get(int x, int y) {
        return (T) data[index(x, y)];
    }


    private int index(int x, int y) {
        return y * sizeX + x;
    }
}
