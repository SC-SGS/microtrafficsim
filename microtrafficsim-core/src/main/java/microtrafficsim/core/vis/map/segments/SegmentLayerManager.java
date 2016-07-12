package microtrafficsim.core.vis.map.segments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.exceptions.AsyncOperationError;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider.LayerChangeListener;
import microtrafficsim.math.Rect2d;


public class SegmentLayerManager {
	
	private static final Comparator<SegmentLayerBucket> CMP_BUCKET = new BucketComparator();
	
	
	private SegmentLayerProvider provider;

	private ExecutorService worker;
	
	private ArrayList<SegmentLayerBucket> prebuilt;
	private HashMap<String, SegmentLayer> loaded;
	private HashMap<String, Future<SegmentLayer>> loading;
	
	private AtomicBoolean reload;
	private ConcurrentLinkedQueue<String> changed;
	
	
	public SegmentLayerManager(SegmentLayerProvider provider, int nWorkerThreads) {
		this.provider = provider;
		this.provider.addLayerChangeListener(new SegmentChangeListenerImpl());
		
		this.worker = Executors.newFixedThreadPool(nWorkerThreads);
		
		this.prebuilt = new ArrayList<>();
		this.loaded = new HashMap<>();
		this.loading = new HashMap<>();
		
		this.reload = new AtomicBoolean(true);
		this.changed = new ConcurrentLinkedQueue<>();
	}
	
	
	public void update(RenderContext context) {
		boolean rebuild;
		
		// apply changes to source
		if (reload.getAndSet(false)) {
			rebuild = true;
			reloadAll(context);
		} else {
			rebuild = !changed.isEmpty();
			while (!changed.isEmpty())
				reloadLayer(context, changed.poll());
		}
		
		// initialize directly available layers
		for (Iterator<Map.Entry<String, Future<SegmentLayer>>> it = loading.entrySet().iterator(); it.hasNext();) {
			Future<SegmentLayer> future = it.next().getValue();
			
			if (future.isDone() && !future.isCancelled()) {
				SegmentLayer layer;
				
				try {
					layer = future.get();
				} catch (InterruptedException | ExecutionException e) {
					/* Note that we already checked if the task was cancelled */
					throw new AsyncOperationError(e);
				}
				
				if (layer != null) {
					layer.initialize(context);
		
					SegmentLayer old = loaded.put(layer.getName(), layer);
					if (old != null) {
						old.dispose(context);
						provider.release(old);
					}
					
					rebuild = true;
				}
			}
			
			if (future.isDone())
				it.remove();
		}
		
		// rebuild if necessary
		if (rebuild) {
			prebuilt.clear();

			for (SegmentLayer layer : loaded.values())
				prebuilt.addAll(layer.getBuckets());

			prebuilt.sort(CMP_BUCKET);
		}
	}
	
	private void reloadAll(RenderContext context) {
        cancelAndDisposeAll(context);

		// load all available layers
		for (String layer : provider.getAvailableLayers())
			loading.put(layer, queryLayer(context, layer));
	}
	
	private void reloadLayer(RenderContext context, String name) {
		// cancel if layer is currently being loaded
		Future<SegmentLayer> future = loading.remove(name);
		if (future != null)
			future.cancel(true);
		
		// remove and dispose layer if it is loaded
		SegmentLayer layer = loaded.remove(name);
		if (layer != null) {
			layer.dispose(context);
			provider.release(layer);
		}
		
		// if layer is available, load it
		if (provider.getAvailableLayers().contains(name))
			loading.put(name, queryLayer(context, name));
	}

    private void cancelAndDisposeAll(RenderContext context) {
        // cancel layers which are currently being loaded
        for (Future<SegmentLayer> future : loading.values())
            future.cancel(true);

        // make sure everything is released correctly
        for (Future<SegmentLayer> future : loading.values()) {
            try {
                provider.release(future.get());
            } catch (InterruptedException | ExecutionException e) {
                // provider should make sure that there is a clean exit on interrupts/exceptions
            }
        }

        // dispose layers that have already been loaded
        for (SegmentLayer layer : loaded.values()) {
            layer.dispose(context);
            provider.release(layer);
        }

        loading.clear();
        changed.clear();
        loaded.clear();
        prebuilt.clear();
    }
	
	
	public void display(RenderContext context) {
		for (SegmentLayerBucket bucket : prebuilt)
			if (bucket.layer.isEnabled())
				bucket.display(context);
	}

	public void dispose(RenderContext context) {
        cancelAndDisposeAll(context);
	}
	

	public Bounds getBounds() {
		return provider.getBounds();
	}
	
	public Rect2d getProjectedBounds() {
		return provider.getProjectedBounds();
	}
	
	
	private Future<SegmentLayer> queryLayer(RenderContext context, String layer) {
		return worker.submit(new Loader(context, provider, layer));
	}
	
	
	private class SegmentChangeListenerImpl implements LayerChangeListener {

		@Override
		public void segmentChanged() {
			reload.set(true);
		}

		@Override
		public void layerChanged(String layer) {
			changed.add(layer);
		}
	}
	
	private static class Loader implements Callable<SegmentLayer> {
		
		private RenderContext context;
		private String layer;
		private SegmentLayerProvider provider;
		
		public Loader(RenderContext context, SegmentLayerProvider provider, String layer) {
			this.context = context;
			this.provider = provider;
			this.layer = layer;
		}

		@Override
		public SegmentLayer call() throws Exception {
			return provider.require(context, layer);
		}
	}
	
	private static class BucketComparator implements Comparator<SegmentLayerBucket> {

		@Override
		public int compare(SegmentLayerBucket a, SegmentLayerBucket b) {
			if (a.zIndex > b.zIndex)
				return 1;
			else if (a.zIndex < b.zIndex)
				return -1;

			if (a.layer.getIndex() > b.layer.getIndex())
				return 1;
			else if (a.layer.getIndex() < b.layer.getIndex())
				return -1;
			else
				return 0;
		}
	}

}
