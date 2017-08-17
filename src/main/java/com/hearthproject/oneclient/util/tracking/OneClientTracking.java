package com.hearthproject.oneclient.util.tracking;

import com.hearthproject.oneclient.util.launcher.SettingsUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import org.apache.http.HttpResponse;
import org.piwik.java.tracking.PiwikRequest;
import org.piwik.java.tracking.PiwikTracker;

import java.io.IOException;
import java.net.URL;

public class OneClientTracking {

	static PiwikTracker tracker = new PiwikTracker("http://analytics.hearthproject.uk/piwik.php");

	private static final int siteId = 2;

	public static void sendRequest(String action){
		if(!SettingsUtil.settings.tracking){
			return;
		}
		try {
			PiwikRequest request = new PiwikRequest(siteId, new URL("http://oneclient.analytics.hearthproject.uk/action/" + action));
			HttpResponse response = tracker.sendRequest(request);
		} catch (IOException e) {
			OneClientLogging.log(e);
		}
	}

}
