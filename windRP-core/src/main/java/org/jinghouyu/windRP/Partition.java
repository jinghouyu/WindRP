package org.jinghouyu.windRP;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jinghouyu.windRP.exception.ResourceException;

/**
 *
 * @author Simsir.L
 * modified by <person></person> on <data></date>
 *
 * |  created date  | modified date  |  modified person |
 * |         |           |        |
 * @description
 *
 * @since 1.0.0
 */
public class Partition<T> {

	private int maxResourceCountPerPartition;
	
	private ResourceHolder<T> resourceHolder = null;
	
	private Long idleMaxTime;
	
	private LinkedBlockingDeque<ResourceEntry<T>> avails;
	
	private AtomicInteger currentSize = new AtomicInteger(0);
	
	private WindRPool<T> pool = null;
	
	Partition(
			WindRPool<T> pool,
			int initResourceCountPerPartition, 
			int maxResourceCountPerPartition, 
			ResourceHolder<T> resourceHolder,
			Long idleMaxTime,
			TimeUnit idleMaxTimeUnit) throws ResourceException {
		this.pool = pool;
		this.maxResourceCountPerPartition = maxResourceCountPerPartition;
		this.resourceHolder = resourceHolder;
		this.idleMaxTime = idleMaxTime < 0 ? -1 : TimeUnit.MILLISECONDS.convert(idleMaxTime, idleMaxTimeUnit);
		avails = new LinkedBlockingDeque<ResourceEntry<T>>(maxResourceCountPerPartition);
		for(int i = 0 ; i < initResourceCountPerPartition; i++) {
			ResourceEntry<T> resourceEntry = resourceHolder.buildResource(this);
			avails.add(resourceEntry);
			addCurrentSize(1);
		}
	}
	
	private ResourceEntry<T> getAvailOne(Date expiredDate) {
		ResourceEntry<T> resourceEntry = avails.poll();
		if(resourceEntry == null) {
			return null;
		}
		if(resourceEntry.getIdleDate().before(expiredDate)) {
			resourceEntry.destroy();
			return getAvailOne(expiredDate);
		} else {
			return resourceEntry;
		}
	}
	
	ResourceEntry<T> getResource() throws ResourceException {
		ResourceEntry<T> resourceEntry = null;
		if(avails.size() > 0) {   //if avail resource is exists
			synchronized(this) {
				if(avails.size() > 0) {
					if(this.idleMaxTime < 0) {
						resourceEntry = avails.poll();
					} else {
						Date expiredDate = new Date(System.currentTimeMillis() - this.idleMaxTime);
						resourceEntry = getAvailOne(expiredDate);
					}
				}
			}
		}
		if(resourceEntry == null) {
			if(currentSize.get() < maxResourceCountPerPartition) {  //if it has not reached up to the max count;
				synchronized(this) {
					if(currentSize.get() < maxResourceCountPerPartition) {
						resourceEntry = resourceHolder.buildResource(this);
						addCurrentSize(1);
					}
				}
			}
		}
		return resourceEntry;
	}
	
	ResourceEntry<T> getResource(Long waitTime, TimeUnit unit) throws ResourceException {
		ResourceEntry<T> resource = getResource();
		if(resource == null) {  //the event that resource is null means resource's count has reached up to the max count;
			try {
				if(waitTime < 0) {
					resource = avails.takeFirst();
				} else {
					resource = avails.pollFirst(waitTime, unit);
				}
			} catch (InterruptedException e) {
				resource = null;  //if it is interrupted, give up this Resource request;
			}
		}
		return resource;
	}
	
	
	public synchronized void releaseAndRemoveAll() {
		ResourceEntry<T> resourceEntry = null;
		while((resourceEntry = avails.poll()) != null) {
			resourceEntry.destroy();
		}
	}
	
	private void addCurrentSize(int size) {
		currentSize.addAndGet(size);
	}
	
	public int getAvailSize() {
		return avails.size();
	}
	
	public WindRPool<T> getPool() {
		return this.pool;
	}
	
	void destroyResource(ResourceEntry<T> resourceEntry) {
		addCurrentSize(-1);
		try {
			((Resource) resourceEntry.getResource()).releaseRealResource();
		} catch (ResourceException e) {
			// log with debug level
		}
	}
	
	void releaseResource(ResourceEntry<T> resourceEntry) {
		if(!avails.offer(resourceEntry)) {
			resourceEntry.destroy();
		} else {
			resourceEntry.setIdleDate(new Date(System.currentTimeMillis()));
		}
	}
}
