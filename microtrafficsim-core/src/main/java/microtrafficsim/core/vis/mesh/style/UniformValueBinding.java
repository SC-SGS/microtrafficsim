package microtrafficsim.core.vis.mesh.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import microtrafficsim.core.vis.opengl.shader.Uniform;


public class UniformValueBinding<T> {
	
	public static List<UniformValueBinding<?>> create(Style style, Map<String, Uniform<?>> uniforms) {
		List<UniformValueBinding<?>> bindings = new ArrayList<>();
		
		for (Map.Entry<String, Supplier<?>> e : style.getUniformSuppliers().entrySet()) {
			Uniform<?> uniform = uniforms.get(e.getKey());

			if (uniform != null)
				bindings.add(create(uniform, e.getValue()));
		}
		
		return bindings;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> UniformValueBinding<T> create(Uniform<T> uniform, Supplier<?> supplier) {
		return new UniformValueBinding<>(uniform, (Supplier<? extends T>) supplier);
	}
	
	
	public final Uniform<T> uniform;
	public final Supplier<? extends T> supplier;
	
	public UniformValueBinding(Uniform<T> uniform) {
		this(uniform, null);
	}
	
	public UniformValueBinding(Uniform<T> uniform, Supplier<? extends T> supplier) {
		this.uniform = uniform;
		this.supplier = supplier;
	}
	
	public void update() {
		uniform.set(supplier.get());
	}
}