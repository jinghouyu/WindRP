package org.jinghouyu.windRP;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author Simsir.L
 * modified by <person></person> on <data></date>
 *
 * |  created date  | modified date  |  modified person |
 * |         |           |        |
 * @description
 * Resource entry includes the identifyCode, resource, partition.
 * identifyCode is an unique code that can be used to identify resource.
 * and it provide a way to release itself.
 * @since 1.0.0
 */
public final class ResourceEntry<T> {

	private String identifyCode;
	
	private T resource;
	
	private Partition<T> partition;
	
	private Date idleDate = new Date();
	
	private boolean hasDestroyed = false;
	
	public ResourceEntry(Partition<T> partition, T resource) {
		this.resource = resource;
		identifyCode = UUID.randomUUID().toString();
		this.partition = partition;
	}
	
	public String getIdentifyCode() {
		return this.identifyCode;
	}
	
	public T getResource() {
		return this.resource;
	}
	
	public Date getIdleDate() {
		return idleDate;
	}

	void setIdleDate(Date idleDate) {
		this.idleDate = idleDate;
	}

	Partition<T> getPartition() {
		return partition;
	}
	
	public void release() {
		getPartition().releaseResource(this);
	}
	
	public synchronized void destroy() {
		if(!hasDestroyed) {
			getPartition().destroyResource(this);
			hasDestroyed = true;
		}
	}
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o.getClass() != this.getClass()) return false;
		ResourceEntry<?> entry = (ResourceEntry<?>) o;
		return entry.getIdentifyCode().equals(this.getIdentifyCode());
	}
	
	public int hashCode() {
		return ("" + this.getIdentifyCode()).hashCode();
	}
}
