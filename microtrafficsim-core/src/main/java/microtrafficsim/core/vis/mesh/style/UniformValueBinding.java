package microtrafficsim.core.vis.mesh.style;

import microtrafficsim.core.vis.opengl.shader.Uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


// TODO: check style uniform suppliers (currently unchecked cast)

/**
 * Binds a uniform to a value provided by a {@code Supplier}.
 *
 * @param <T> the type of the uniform/value.
 * @author Maximilian Luz
 */
public class UniformValueBinding<T> {

    /**
     * Creates a list of {@code UniformValueBinding}s from the given style.
     *
     * @param style    the style to create the bindings for.
     * @param uniforms the map of uniforms to which the values should be bound.
     * @return the created {@code UniformValueBinding}s.
     */
    public static List<UniformValueBinding<?>> create(Style style, Map<String, Uniform<?>> uniforms) {
        List<UniformValueBinding<?>> bindings = new ArrayList<>();

        for (Map.Entry<String, Supplier<?>> e : style.getUniformSuppliers().entrySet()) {
            Uniform<?> uniform = uniforms.get(e.getKey());
            if (uniform != null)
                bindings.add(createInternal(uniform, e.getValue()));
        }

        return bindings;
    }

    /**
     * Internal method to create a uniform value binding for the given uniform and supplier. The caller must ensure
     * that the supplier provides a type convertible to the type accepted by the uniform.
     *
     * @param uniform  the uniform to which the given supplier should be bound.
     * @param supplier the supplier that should be bound to the given uniform.
     * @param <T>      the type of the uniform.
     * @return the created {@code UniformValueBinding}s.
     */
    @SuppressWarnings("unchecked")
    private static <T> UniformValueBinding<T> createInternal(Uniform<T> uniform, Supplier<?> supplier) {
        return create(uniform, (Supplier<? extends T>) supplier);
    }

    /**
     * Creates a uniform value binding for the given uniform and supplier.
     *
     * @param uniform  the uniform to which the given supplier should be bound.
     * @param supplier the supplier that should be bound to the given uniform.
     * @param <T>      the type of the uniform.
     * @return the created {@code UniformValueBinding}s.
     */
    public static <T> UniformValueBinding<T> create(Uniform<T> uniform, Supplier<? extends T> supplier) {
        return new UniformValueBinding<>(uniform, supplier);
    }


    public final Uniform<T> uniform;
    public final Supplier<? extends T> supplier;

    /**
     * Constructs a new uniform value binding for the given uniform and supplier.
     *
     * @param uniform  the uniform to which the given supplier should be bound.
     * @param supplier the supplier that should be bound to the given uniform.
     */
    public UniformValueBinding(Uniform<T> uniform, Supplier<? extends T> supplier) {
        this.uniform  = uniform;
        this.supplier = supplier;
    }

    /**
     * Updates the uniform with the value provided by the supplier.
     */
    public void update() {
        uniform.set(supplier.get());
    }
}