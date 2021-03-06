package org.jinghouyu.windRP;

import java.util.ArrayList;
import java.util.List;

import org.jinghouyu.windRP.exception.ResourceCannotFoundException;
import org.jinghouyu.windRP.exception.ResourceException;
import org.jinghouyu.windRP.interf.NumberChooser;

/**
 *
 * @author Simsir.L
 * modified by <person></person> on <data></date>
 *
 * |  created date  | modified date  |  modified person |
 * |  2012-12-28       |           |        |
 * @description
 * it describe a resource pool, also the entrance of the pool;
 * it mainly provide the way to get and manage Resource in the pool;
 * @since 1.0.0
 */
public class WindRPool<T> {

	private ResourceHolder<T> resourceHolder;
	
	private RPConfig config;
	
	private NumberChooser numberChooser = Constant.defaultNumberChooser;
	
	private List<Partition<T>> partitions = null;
	
	public WindRPool(ResourceHolder<T> resourceHolder) {
		this.resourceHolder = resourceHolder;
	}

	public ResourceHolder<T> getResourceHolder() {
		return resourceHolder;
	}

	public void setResourceHolder(ResourceHolder<T> resourceHolder) {
		this.resourceHolder = resourceHolder;
	}

	public RPConfig getConfig() {
		return config;
	}

	public void setConfig(RPConfig config) throws ResourceException {
		this.config = config;
		partitions = new ArrayList<Partition<T>>(config.getPartitionCount());
		for(int i = 0; i < config.getPartitionCount() ; i++) {
			partitions.add(createPartition(config));
		}
	}
	
	/**
	 * release and then remove all resources
	 * if this method is called, we should shop client to re
	 */
	public void releaseAndRemoveAll() {
		for(Partition<T> partition : partitions) {
			partition.releaseAndRemoveAll();
		}
	}
	
	private Partition<T> createPartition(RPConfig config) throws ResourceException {
		return new Partition<T>(this,
								config.getInitResourceCountPerPartition(),
				                config.getMaxResourceCountPerPartition(),
				                this.resourceHolder,
				                config.getIdleMaxTime(),
				                config.getIdleMaxTimeUnit());
	}
	
	public NumberChooser getNumberChooser() {
		return numberChooser;
	}

	public void setNumberChooser(NumberChooser numberChooser) {
		this.numberChooser = numberChooser;
	}

	public ResourceEntry<T> getResourceEntry() throws ResourceException {
		int firstPartitionIndex = numberChooser.choose(config);
		int defaultIndex = (int)Thread.currentThread().getId() % config.getPartitionCount();
		if(firstPartitionIndex + 1 > config.getPartitionCount()) {
			//log with debug level;
			firstPartitionIndex = defaultIndex;
		}
		ResourceEntry<T> resourceEntry = partitions.get(firstPartitionIndex).getResource();
		if(resourceEntry == null) {
			int lastIndex = firstPartitionIndex;
			for(int i = 0; i < config.getRetryTime(); i++) {
				int index = numberChooser.next(config, lastIndex, firstPartitionIndex);
				if(index == firstPartitionIndex) {
					continue;
				}
				if(index > config.getPartitionCount()) {
					//log with debug level
					index = defaultIndex;
				}
				resourceEntry = partitions.get(index).getResource();
				if(resourceEntry != null) {
					return resourceEntry;
				}
				lastIndex = index;
			}
		}
		if(resourceEntry == null) {
			resourceEntry = partitions.get(firstPartitionIndex).getResource(config.getWaitTime(), config.getTimeUnit());
		}
		if(resourceEntry == null) {
			throw new ResourceCannotFoundException("cannot find availiable resource");
		}
		return resourceEntry;
	}
	
	public void releaseResourceEntry(ResourceEntry<T> resourceEntry) {
		resourceEntry.getPartition().releaseResource(resourceEntry);
	}
}