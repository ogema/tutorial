package com.example.driver.schedule.csv.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;

import com.example.driver.schedule.csv.importer.model.FolderConfiguration;
import com.example.driver.schedule.csv.importer.model.ScheduleCsvConfig;

public class FolderConfigManager {
	
	private final OgemaLogger log;
    private final ApplicationManager appMan;
	final ScheduleCsvConfig appConfigData;

    public FolderConfigManager(ApplicationManager appMan) {
		this.appMan = appMan;
		this.log = appMan.getLogger();
        this.appConfigData = initConfigurationResource();
     // development mode // property can be set in <RUNDIR>/config/ogema.properties
 		boolean createTestResource = Boolean.getBoolean("org.ogema.apps.createtestresources");
 		if (createTestResource && appConfigData.connections().size() == 0) {
 			createTestFolderConfig();
 		}
 	}

    /** 
     * Call this method when information on a new connection is available. This data can be provided
     * by some auto-detect / plug&play mechanism or from a GUI page where the user enters configuration
     * data or some other source of information. The new connection will be detected and processed by
     * the driver via the pattern listener registered in the TemplateDriver class.<br>
     * An alternative low-level method to create a new connection is to create the required resources directly. 
     * This way the GUI provided by this driver works. 
     * 
     * @param value the value to read/write to. If the connection reads/writes into more than one resource
     *  and/or the resources have a different type from FloatResource, adapt accordingly
     * @param configurationData adapt this to the information actually required for the driver's connections
     */
    public void createNewConnection(Path path, FloatResource target) {
		FolderConfiguration newConnection = appConfigData.connections().add();
		newConnection.directory().<StringResource> create().setValue(path.toString());
		newConnection.target().setAsReference(target);
		newConnection.activate(true);
	}

    /*
     * This app uses a central configuration resource, which is accessed here
     */
    private final ScheduleCsvConfig initConfigurationResource() {
    	ScheduleCsvConfig masterConfig;
		final String name = ScheduleCsvConfig.class.getSimpleName().substring(0, 1).toLowerCase()+ScheduleCsvConfig.class.getSimpleName().substring(1);
		masterConfig = appMan.getResourceAccess().getResource(name);
		if (masterConfig != null) { // resource already exists (appears in case of non-clean start)
			appMan.getLogger().debug("{} started with previously-existing config resource", getClass().getName());
		}
		else {
			masterConfig = (ScheduleCsvConfig) appMan.getResourceManagement().createResource(name, ScheduleCsvConfig.class);
			masterConfig.activate(true);
			appMan.getLogger().debug("{} started with new config resource", getClass().getName());
		}
		return masterConfig;
    }
    
    private final void createTestFolderConfig() {
    	// would require a permission when run with activated security; ok for development
    	Path baseDir = Paths.get("data/schedules");
    	try {
			Files.createDirectories(baseDir);
		} catch (IOException e) {
			log.error("Could not create test folder {}",baseDir,e);
			return;
		}
    	// would require a permission as well
    	FloatResource base = appMan.getResourceManagement().createResource("scheduleCsvImportDemo", FloatResource.class);
    	base.activate(false);
    	createNewConnection(baseDir, base);
    }
   
}
