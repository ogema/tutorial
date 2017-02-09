package com.example.snippet.ogema.gui;

import org.ogema.core.application.ApplicationManager;

import de.iwes.util.format.StringFormatHelper;
import de.iwes.widgets.api.widgets.OgemaWidget.SendValue;
import de.iwes.widgets.api.widgets.WidgetPage;
import de.iwes.widgets.api.widgets.sessionmanagement.OgemaHttpRequest;
import de.iwes.widgets.html.alert.Alert;
import de.iwes.widgets.html.calendar.datepicker.Datepicker;
import de.iwes.widgets.html.form.button.Button;
import de.iwes.widgets.html.html5.ProgressBar;

/**
 * Simple page providing a {@link Datepicker} that allows to choose a new framework time. Setting the framework time
 * can be activated via a Button. If the operation is not possible a message is shown in an {@link Alert}.
 * Progress of moving forward to the new framework time is shown in a {@link ProgressBar}.
 */
public class DatePickerAndProgressBar {
	
	public final long UPDATE_RATE = 5*1000;

	private final ProgressBar progress;
	private final Button sendButton;		
	long startTime;
	long offSet;
	private boolean frameworkTimeOperationInProgress = false;
	private boolean datePickerChoiceStarted = false;
	
	public DatePickerAndProgressBar(final WidgetPage<?> page, final ApplicationManager appMan) {


		final Alert alert = new Alert(page, "alert", "");

		/** This is a date picker that after loading the page polls the current time with standard UDPATE_RATE).
		 * When the user starts to edit the date picker, which is notified by a POST, the variable 
		 * datePickerChoiceStarted is set to true and polling is stopped as the date and time set by the user
		 * shall not be overwritten by polling.<br>
		 * It seems that setting SendValue.FALSE does not have any effect for the Datepicker. The datepicker
		 * sends a POST every time a new hour is chosen and maybe even when the time sub menu is opened, so
		 * a POST of the Datepicker should not trigger a real action, but this should be done via a separate
		 * button (sendFrameworkTime here).
		 */
		final Datepicker datepicker = new Datepicker(page, "datepickerFWTime", SendValue.FALSE) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onGET(OgemaHttpRequest req) {
				if(!frameworkTimeOperationInProgress) {
					enable(req);					
				}
				if(!datePickerChoiceStarted) {
					setPollingInterval(UPDATE_RATE, req);
				}
				if((!datePickerChoiceStarted)||frameworkTimeOperationInProgress)
					setDate(appMan.getFrameworkTime(), req);
			}
			
			@Override
			public void onPrePOST(String data, OgemaHttpRequest req) {
				datePickerChoiceStarted = true;
				setPollingInterval(-1, req);
				System.out.println("Received date: "+data +" / date value in onPrePOST:"+getDate(req));
			}
		};
		datepicker.setDefaultPollingInterval(UPDATE_RATE);
		
		/** The button triggers setting the framework time. It speeds up polling of the datepicker and the
		 * progress bar during the update process. Note that the timer task that runs in a thread outside
		 * the app GUI thread (e.g. the main application thread or a separate thread provided by a Java timer)
		 * cannot access the OgemaHttpRequest. So setting back the polling rate etc. has to be done in the
		 * onGET methods of the respective widgets.
		 */
		sendButton = new Button(page, "sendButton", "Send") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onPOSTComplete(String data, final OgemaHttpRequest req) {
				long timeSet = datepicker.getDateLong(req);
				startTime = appMan.getFrameworkTime();
				offSet = timeSet - startTime;
				if(offSet <= 10000) {
					alert.showAlert("New time must be at least 10 seconds ahead of current time!", false, req);
					return;
				}
				if(frameworkTimeOperationInProgress) {
					alert.showAlert("Other framework time operation in progress, try again later.", false, req);
					return;					
				}
				frameworkTimeOperationInProgress  = true;
				float simulationFactor = offSet / 10000;
				System.out.println("Setting simFactor to "+simulationFactor+" to jump over "+(offSet/60000)+" minutes");
				appMan.getAdministrationManager().getFrameworkClock().setSimulationFactor(simulationFactor);
				new java.util.Timer().schedule( 
				        new java.util.TimerTask() {
				            @Override
				            public void run() {
								appMan.getAdministrationManager().getFrameworkClock().setSimulationFactor(1.0f);
								frameworkTimeOperationInProgress = false;
								datePickerChoiceStarted = false;
								System.out.println("Setting simFactor to 1.0, reached "+StringFormatHelper.getTimeOfDayInLocalTimeZone(appMan.getFrameworkTime()));
				            }
				        }, 
				        10000 
				);
				datepicker.disable(req);
				progress.setPollingInterval(1000, req);
				datepicker.setPollingInterval(1000, req);
			}
		};
		
		/**Simple example for a progress bar. As explained with the button stopping the polling of the
		 * progress bar has to be made here whereas starting the polling is done by the button.*/
		progress = new ProgressBar(page, "progress") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onGET(OgemaHttpRequest req) {
				if(!frameworkTimeOperationInProgress) {
					setValue(1.0f, req);
					setPollingInterval(-1, req);
					return;
				}
				float state = (float) (((double)(appMan.getFrameworkTime() - startTime)) / offSet);
				System.out.println("Setting progress state to "+state);
				setValue(state, req);
			}
		};

		sendButton.registerDependentWidget(datepicker);
		sendButton.registerDependentWidget(progress);
		sendButton.registerDependentWidget(alert);
	}
}
