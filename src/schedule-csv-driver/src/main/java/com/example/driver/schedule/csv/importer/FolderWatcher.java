package com.example.driver.schedule.csv.importer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.driver.schedule.csv.importer.pattern.FolderConfigPattern;

/**
 * Supervises one folder, and whenever a new CSV file appears, it tries to parse it
 * and write the contained timeseries to a schedule resource.
 * 
 * @author cnoelle
 */
public class FolderWatcher implements Runnable {
	
	private final static Logger logger = LoggerFactory.getLogger(ScheduleCsvImporter.class);
	private final FolderConfigPattern pattern;
	private final Path directory;
	private final Thread thread;
	private final WatchService watcher;

	public FolderWatcher(FolderConfigPattern pattern) throws IOException {
		this.pattern = pattern;
		String directory = pattern.directory.getValue();
		this.directory = Paths.get(directory);
		Files.createDirectories(this.directory);
		this.watcher = FileSystems.getDefault().newWatchService();
//		this.directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		this.directory.register(watcher, ENTRY_CREATE);
		thread = new Thread(this, "folder-watcher-" + pattern.model.getPath());
		thread.start();
	}
	
	// the main loop
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				WatchKey key = watcher.take(); // blocks
				List<WatchEvent<?>> events = key.pollEvents();
				processEvents(events);
				if (!key.reset())
					break;
			} catch (InterruptedException | ClosedWatchServiceException e) {
				break; // stopped
			}
		}
	}
	
	private void processEvents(List<WatchEvent<?>> events) {
		Path file;
		for (WatchEvent<?> event : events) {
			if (event.kind() == ENTRY_CREATE) {
				try {
					file = directory.resolve((Path) event.context());
					logger.debug("New file available",file);
					String targetSchedule = file.getFileName().toString();
					if (!Files.isRegularFile(file) || !targetSchedule.toLowerCase().endsWith(".csv")) {
						continue;
					}
					targetSchedule = targetSchedule.substring(0, targetSchedule.length()-4);
					if (!waitForFileCompletion(file)) { // XXX could be moved to separate thread, but for simplicity we keep waiting in the main loop
						logger.warn("File size did not stabilize within 30s: {}", file);
						continue;
					}
					writeValues(targetSchedule, file);
				} catch (Exception e) {
					logger.error("Writing schedule value failed",e);
				}
			}
		}
	}
	
	public void close() {
		// this interrupts watcher.take() in the main loop
		thread.interrupt();
		try {
			watcher.close();		
		} catch (IOException ignore) {}
	}
	
	public Path getPath() {
		return directory;
	}
	
	public FolderConfigPattern getPattern() {
		return pattern;
	}
	
	// parse the csv file and copy values to schedule
	private void writeValues(String targetSchedule, Path file) throws IOException, ResourceAlreadyExistsException {
		FloatResource base = pattern.target.getLocationResource();
		// either accesses existing schedule, or creates a new one
		// if a subresource with the requested name exists, a ResourceAlreadyExistsException is thrown
		AbsoluteSchedule schedule = base.getSubResource(targetSchedule, AbsoluteSchedule.class).create();
		writeValues(schedule, file);
	}
	
	private static void writeValues(final Schedule schedule, final Path file) throws IOException, ResourceAlreadyExistsException {
		final CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
		final List<SampledValue> values = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), "UTF-8"))) {
			try (CSVParser parser = new CSVParser(reader, format)) {
				long timestamp;
				float value;
				for (CSVRecord record : parser) {
					try {
						timestamp = Long.parseLong(record.get(0));
						// we could add a filter to the configuration resource
//						if (timestamp < start || timestamp > end)
//							continue;
					} catch (NumberFormatException e) {
						continue;
					}
					value = Float.parseFloat(record.get(1));
					values.add(new SampledValue(new FloatValue(value), timestamp, Quality.GOOD));	
				}
			}
		}
		schedule.addValues(values);
		schedule.activate(false);
		logger.debug("{} values written to schedule {}",values.size(),schedule.getPath());
	}
	
	/**
	 * Callbacks about new files are often issued before the new file's content is complete.
	 * Wait for at most several seconds until the file size does not change any longer.
	 * @param file
	 * @return
	 * @throws InterruptedException
	 */
	private static final boolean waitForFileCompletion(Path file) throws InterruptedException {
		long lastSize = -1;
		int cnt = 1;
		while (true) {
			if (cnt > 30)
				return false;
			Thread.sleep(100*cnt++);
			long localSize;
			try {
				localSize = Files.size(file);
			} catch (IOException e) { 
				lastSize = -1;
				continue;
			}
			if (localSize == lastSize) 
				return true;
			lastSize = localSize;
		}
		
	}
	
}
