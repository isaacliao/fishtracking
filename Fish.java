package fishtracking;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import de.fhpotsdam.unfolding.geo.Location;
import fishtracking.FishTracking.LocationType;

//import processing.core.PVector;

/* 
 * A data structure representing a single fish.
 */
public class Fish {
    public float length, weight;
    public ArrayList<Ping> pings;
    public Location position;
    public boolean isVisible;
    public int dataset; 		// used to identify which dataset this Fish is from

    private int nextPingIndex, lastPingIndex;
    public ArrayList<Ping> path;

    public Fish() {
	this.isVisible = false;
	this.pings = new ArrayList<Ping>();
	this.path = new ArrayList<Ping>();
	this.position = new Location(0, 0);
    }

    public void update(long currentTime, LocationType locationType) {
	ArrayList<Ping> drawPath = null;
	if (locationType == LocationType.PATH)
	    drawPath = this.path;
	else if (locationType == LocationType.PING)
	    drawPath = this.pings;
	else
	    System.out.println("Error: no location data source specified");

	// If empty path, don't draw
	if (drawPath.size() == 0) {
	    this.isVisible = false;
	    //System.out.println("Empty fish drawPath");
	}
	// If before or after all pings, don't draw
	else if (currentTime < drawPath.get(0).dateTime || currentTime > drawPath.get(drawPath.size() - 1).dateTime) {
	    this.isVisible = false;
	    // Reset nextPingIndex
	    // this.nextPingIndex = 0;

	} else {
	    // We're within the timespan covered by drawPath list
	    this.isVisible = true;

	    // // Seek to correct position in drawPath
	    // int i = 0;
	    // while (i < drawPath.size() && drawPath.get(i).dateTime < currentTime) {
	    // i++;
	    // }
	    // Ping nextPing = drawPath.get(i);

	    // Check if we're at or after nextPing

	    // Advance nextPing to the first ping at or after the current time
	    Ping nextPing = drawPath.get(0);
	    nextPingIndex = 0;
	    while (currentTime > nextPing.dateTime && nextPingIndex < drawPath.size()) {
		nextPingIndex++;
		nextPing = drawPath.get(nextPingIndex);
	    }

	    if (currentTime == nextPing.dateTime) {
		position.setLat(nextPing.latitude);
		position.setLon(nextPing.longitude);
	    } else {
		// We're between pings, so linearly interpolate position
		int lastPingIndex = nextPingIndex - 1;
		if (lastPingIndex < 0) {
		    lastPingIndex = 0; // this shouldn't happen, since the only
				       // time nextPingIndex = 0 is when we're
				       // exactly at that time
		    System.out.println("Warning: negative lastPingIndex");
		}
		Ping lastPing = drawPath.get(lastPingIndex);
		Location lastLocation = new Location(lastPing.latitude, lastPing.longitude);
		Location nextLocation = new Location(nextPing.latitude, nextPing.longitude);

		float proportion = (float) (currentTime - lastPing.dateTime)
			/ (float) (nextPing.dateTime - lastPing.dateTime);
		PVector p = PVector.lerp(lastLocation, nextLocation, proportion);
		position.setLat(p.x);
		position.setLon(p.y);
	    }
	}
    }
}
