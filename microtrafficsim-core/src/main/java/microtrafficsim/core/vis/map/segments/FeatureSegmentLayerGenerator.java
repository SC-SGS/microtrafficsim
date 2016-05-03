package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.mesh.FeatureMeshGenerator;
import microtrafficsim.core.vis.map.segments.mesh.StreetMeshGenerator;
import microtrafficsim.core.vis.mesh.ManagedMesh;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshPool;
import microtrafficsim.core.vis.mesh.style.FeatureStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;


public class FeatureSegmentLayerGenerator implements SegmentLayerGenerator {
	private static final Logger logger = LoggerFactory.getLogger(FeatureSegmentLayerGenerator.class);

	private MeshPool<FeatureMeshGenerator.FeatureMeshKey> pool;
	private HashSet<FeatureMeshGenerator.FeatureMeshKey> loading;
	private HashMap<Class<? extends FeaturePrimitive>, FeatureMeshGenerator> generators;
	
	
	public FeatureSegmentLayerGenerator() {
		this(true);
	}
	
	public FeatureSegmentLayerGenerator(boolean defaultInit) {
		this.pool = new MeshPool<>();
		this.loading = new HashSet<>();
		this.generators = new HashMap<>();
		
		if (defaultInit) {
            generators.put(Street.class, new StreetMeshGenerator());
		}
	}
	

	@Override
	public FeatureSegmentLayer generate(RenderContext context, LayerDefinition def, Projection projection) {
        if (!(def.getSource() instanceof FeatureSegmentLayerSource)) return null;

		FeatureSegmentLayerSource src = (FeatureSegmentLayerSource) def.getSource();
		if (!src.isAvailable()) return null;

		FeatureMeshGenerator generator = generators.get(src.getFeatureType());
		if (generator == null) return null;
		
		FeatureMeshGenerator.FeatureMeshKey key = generator.getKey(context, src, projection);
		ManagedMesh mesh;

		synchronized (this) {
			mesh = pool.get(key);

			if (mesh == null) {
				// if mesh is already being loaded, wait
				if (loading.contains(key)) {
					try {
						while (loading.contains(key))
							this.wait();
					} catch (InterruptedException e) {
						return null;
					}

					mesh = pool.get(key);
					mesh.require();

				} else {
					loading.add(key);
				}
			} else {
				mesh.require();
			}
		}

		if (mesh == null) {
			logger.debug("re-generating mesh for feature '" + src.getFeatureName() + "'");
            {
                Mesh m = generator.generate(context, src, projection);
                if (m == null) return null;

                mesh = new ManagedMesh(m);
            }

			synchronized (this) {
				pool.put(key, mesh);
				loading.remove(key);
				this.notifyAll();
			}
		}

		FeatureStyle style = new FeatureStyle(src.getStyle());
		return new FeatureSegmentLayer(def.getName(), def.getIndex(), def.getSource(), mesh, style);
	}
}
