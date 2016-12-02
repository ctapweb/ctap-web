package com.ctapweb.web.server.logging;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class LogMarker {
 public static Marker CTAP_SERVER_MARKER = MarkerManager.getMarker("CTAP_SERVER");
 public static Marker CTAP_CLIENT_MARKER = MarkerManager.getMarker("CTAP_CLIENT");
}
