package org.snippets.tutorial;

import java.util.concurrent.RejectedExecutionException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;

import de.iwes.widgets.api.OgemaGuiService;
import de.iwes.widgets.api.messaging.Message;
import de.iwes.widgets.api.messaging.MessagePriority;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class) 
public class SimpleMessagingApp implements Application {
	ApplicationManager appMan;

	@Reference
	private OgemaGuiService guiService;
	
	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
	}

	/*public void sendEmail(String subject, String body) {
		MessagePriority pr = MessagePriority.LOW;
		Message msg = new MessageImpl(subj, bod, pr);
		try {
			guiService.getMessagingService().sendMessage(appMan, msg);
		} catch (RejectedExecutionException e) {
			appMan.getLogger().warn("Email message could not be sent: "+ e);
		}		
	}*/

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub
		
	}
}
