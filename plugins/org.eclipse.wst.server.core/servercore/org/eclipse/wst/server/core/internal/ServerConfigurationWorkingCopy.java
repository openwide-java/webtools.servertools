/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.wst.server.core.internal;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.*;
import org.eclipse.wst.server.core.model.ServerConfigurationDelegate;
/**
 * 
 */
public class ServerConfigurationWorkingCopy extends ServerConfiguration implements IServerConfigurationWorkingCopy {
	protected ServerConfiguration config;
	protected WorkingCopyHelper wch;
	
	protected ServerConfigurationDelegate workingCopyDelegate;
	
	// working copy
	public ServerConfigurationWorkingCopy(ServerConfiguration config) {
		super(config.getFile());
		this.config = config;
		this.configurationType = config.configurationType;
		
		map = new HashMap(config.map);
		wch = new WorkingCopyHelper(this);
	}

	// creation
	public ServerConfigurationWorkingCopy(String id, IFile file, IServerConfigurationType configType) {
		super(id, file, configType);
		//this.config = this;
		wch = new WorkingCopyHelper(this);
		wch.setDirty(true);
	}

	public boolean isWorkingCopy() {
		return true;
	}
	
	public IServerConfiguration getOriginal() {
		return config;
	}
	
	public IServerConfigurationWorkingCopy createWorkingCopy() {
		return this;
	}

	public void setAttribute(String attributeName, int value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, boolean value) {
		wch.setAttribute(attributeName, value);
	}
	
	public void setAttribute(String attributeName, String value) {
		wch.setAttribute(attributeName, value);
	}
	
	public void setAttribute(String attributeName, List value) {
		wch.setAttribute(attributeName, value);
	}
	
	public void setAttribute(String attributeName, Map value) {
		wch.setAttribute(attributeName, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerWorkingCopy#setName(java.lang.String)
	 */
	public void setName(String name) {
		wch.setName(name);
	}
	
	public void setLocked(boolean b) {
		wch.setLocked(b);
	}

	public void setPrivate(boolean b) {
		wch.setPrivate(b);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.server.core.IServerWorkingCopy#isDirty()
	 */
	public boolean isDirty() {
		return wch.isDirty();
	}
	
	public IServerConfiguration save(boolean force, IProgressMonitor monitor) throws CoreException {
		monitor = ProgressUtil.getMonitorFor(monitor);
		monitor.subTask(ServerPlugin.getResource("%savingTask", getName()));
		
		if (!force)
			wch.validateTimestamp(getOriginal());
		
		if (config == null)
			config = new ServerConfiguration(file);
		
		config.setInternal(this);
		config.doSave(monitor);
		saveData(true, monitor);
		wch.setDirty(false);
		
		return config;
	}

	public ServerConfigurationDelegate getWorkingCopyDelegate(boolean load, IProgressMonitor monitor) {
		if (workingCopyDelegate != null)
			return workingCopyDelegate;
		
		synchronized (this) {
			if (workingCopyDelegate == null) {
				try {
					long time = System.currentTimeMillis();
					ServerConfigurationType configType = (ServerConfigurationType) configurationType;
					workingCopyDelegate = (ServerConfigurationDelegate) configType.getElement().createExecutableExtension("class");
					workingCopyDelegate.initialize(this);
					if (load)
						loadData(monitor);
					Trace.trace(Trace.PERFORMANCE, "ServerConfigurationWorkingCopy.getWorkingCopyDelegate(): <" + (System.currentTimeMillis() - time) + "> " + getServerConfigurationType().getId());
				} catch (Exception e) {
					Trace.trace(Trace.SEVERE, "Could not create delegate " + toString(), e);
				}
			}
		}
		return workingCopyDelegate;
	}
	
	public void dispose() {
		super.dispose();
		if (workingCopyDelegate != null)
			workingCopyDelegate.dispose();
	}
	
	/**
	 * Add a property change listener to this server.
	 *
	 * @param listener java.beans.PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		wch.addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove a property change listener from this server.
	 *
	 * @param listener java.beans.PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		wch.removePropertyChangeListener(listener);
	}
	
	/**
	 * Fire a property change event.
	 */
	public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		wch.firePropertyChangeEvent(propertyName, oldValue, newValue);
	}
	
	public void setDefaults(IProgressMonitor monitor) {
		try {
			getWorkingCopyDelegate(false, monitor).setDefaults();
			isDataLoaded = true;
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error calling delegate setDefaults() " + toString(), e);
		}
	}
	
	public void importFromPath(IPath path, IProgressMonitor monitor) throws CoreException {
		try {
			getWorkingCopyDelegate(false, monitor).importFromPath(path, monitor);
			isDataLoaded = true;
		} catch (CoreException ce) {
			throw ce;
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error calling delegate importFromPath() " + toString(), e);
		}
	}
	
	public void importFromRuntime(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		try {
			getWorkingCopyDelegate(false, monitor).importFromRuntime(runtime, monitor);
			isDataLoaded = true;
		} catch (CoreException ce) {
			throw ce;
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error calling delegate importFromRuntime() " + toString(), e);
		}
	}
	
	public String toString() {
		return "ServerConfigurationWorkingCopy " + getId();
	}
}
