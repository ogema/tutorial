package com.example.driver.schedule.csv.importer.patternlistener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.driver.schedule.csv.importer.FolderWatcher;
import com.example.driver.schedule.csv.importer.ScheduleCsvImporter;
import com.example.driver.schedule.csv.importer.pattern.FolderConfigPattern;

/**
 * A pattern listener for the TemplatePattern. It is informed by the framework 
 * about new pattern matches and patterns that no longer match.
 */
public class FolderConfigListener implements PatternListener<FolderConfigPattern>, PatternChangeListener<FolderConfigPattern> {
	
	private final ResourcePatternAccess patternAccess;
	private final Logger logger;
	// Map<model path, pattern>
	private Map<String, FolderWatcher> controllers = new HashMap<>();
	
	public FolderConfigListener(ResourcePatternAccess patternAccess, Logger logger) {
		this.patternAccess = patternAccess;
		this.logger = logger;
	}
	
	@Override
	public void patternAvailable(final FolderConfigPattern pattern) {
		newPattern(pattern);
		patternAccess.addPatternChangeListener(pattern, this, FolderConfigPattern.class);
		logger.info("New folder supervision config: {}",pattern.model.getPath());
	}
	
	@Override
	public void patternUnavailable(FolderConfigPattern pattern) {
		patternGone(pattern);
		patternAccess.removePatternChangeListener(pattern, this);
		logger.info("Folder supervision config gone: {}",pattern.model.getPath());
	}
	
	@Override
	public void patternChanged(FolderConfigPattern pattern, List<CompoundResourceEvent<?>> changes) {
		patternGone(pattern);
		newPattern(pattern);
		logger.info("Folder supervision config changed: {}",pattern.model.getPath());
	}
	
	private void newPattern(FolderConfigPattern pattern) {
		try {
			controllers.put(pattern.model.getPath(), new FolderWatcher(pattern));
		} catch (IOException e) {
			LoggerFactory.getLogger(ScheduleCsvImporter.class).error("Could not register a folder watcher for {}", pattern.model,e);
		}
	}
	
	private void patternGone(FolderConfigPattern pattern) {
		FolderWatcher watcher = controllers.remove(pattern.model.getPath());
		if (watcher != null) 
			watcher.close();
	}
	
	/**
	 * Regarding synchronization: we assume here that this method is only called from this driver's app thread
	 * (e.g. in a Resource listener or timer callback). If it was called from different threads as well (e.g. from another app, 
	 * a user interface thread, etc), access to the <tt>controllers</tt> field would have to be synchronized, 
	 * or a ConcurrentMap could be used. 
	 * @return
	 */
	public Map<String, FolderConfigPattern> getPatterns() {
		Map<String, FolderConfigPattern> patterns = new HashMap<>();
		for (Map.Entry<String, FolderWatcher> entry: controllers.entrySet()) {
			patterns.put(entry.getKey(), entry.getValue().getPattern());
		}
		return patterns;
	}
	
	public void close() {
		for (FolderWatcher watcher : controllers.values()) {
			watcher.close();
			patternAccess.removePatternChangeListener(watcher.getPattern(), this);
		}
		controllers.clear();
	}
	
	
}
