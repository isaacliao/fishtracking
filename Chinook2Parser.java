// For parsing Chinook salmon dataset from Arnold Ammann. 

package fishtracking;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

// Loads a csv file and saves it as a JSon.
public class Chinook2Parser extends PApplet {

    public String[][] loadCSV(String filename) {
	// for importing csv files into a 2d array
	// by che-wei wang

	String lines[] = loadStrings(filename);
	String[][] csv;
	int csvWidth = 0;

	// calculate max width of csv file
	for (int i = 0; i < lines.length; i++) {
	    String[] chars = split(lines[i], ',');
	    if (chars.length > csvWidth) {
		csvWidth = chars.length;
	    }
	}

	// create csv array based on # of rows and columns in csv file
	csv = new String[lines.length][csvWidth];

	// parse values into 2d array
	for (int i = 0; i < lines.length; i++) {
	    String[] temp = new String[lines.length];
	    temp = split(lines[i], ',');
	    for (int j = 0; j < temp.length; j++) {
		csv[i][j] = temp[j];
	    }
	}

	// test
	println(csv[2][2]);
	return csv;
    }

    /*
     * Converts a 2D string array from a CSV file into a JSON object.
     * Assumptions: First row is header row, first column is ID's, second column is datetimes,
     * third column is latitude, fourth column is longitude, row 11 is weight, row 13 is length,
     * rows are sorted by time
     */
    public JSONObject infoToJSON(String[][] csv) {
//	float minLat = Float.POSITIVE_INFINITY;
//	float minLon = Float.POSITIVE_INFINITY;
//	float maxLat = Float.NEGATIVE_INFINITY;
//	float maxLon = Float.NEGATIVE_INFINITY;
//	long minTime = Long.MAX_VALUE;
//	long maxTime = Long.MIN_VALUE;
//	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	JSONObject fishDictionary = new JSONObject();
	fishDictionary.setJSONObject("fishes", new JSONObject());
	for (String[] row : csv) {
	    JSONObject fish;
	    String id = row[6];
	    if (id.equals("TagID")) { // skip the header row
		continue;
	    }
	    if (fishDictionary.getJSONObject("fishes").hasKey(id)) {
		fish = fishDictionary.getJSONObject("fishes").getJSONObject(id);
	    } else {
		// New ID, create a new fish
		fish = new JSONObject();
		fish.setJSONArray("pings", new JSONArray()); // An array of times and locations that this fish was seen
		// Add other fish-related data here
		fish.setFloat("weight", Float.parseFloat(row[7]));
		fish.setFloat("length", Float.parseFloat(row[8]));
	    }

	    // Parse values
	    JSONObject ping = new JSONObject();
//	    long curTime;
//	    try {
//		curTime = format.parse(row[1]).getTime();
//	    } catch (ParseException e) {
//		System.out.println(e.getMessage());
//		continue;
//	    }
//	    float curLat = Float.parseFloat(row[2]);
//	    float curLon = Float.parseFloat(row[3]);
//
//	    // Keep track of mins and maxes
//	    if (curLat < minLat) {
//		minLat = curLat;
//	    } else if (curLat > maxLat) {
//		maxLat = curLat;
//	    }
//	    if (curLon < minLon) {
//		minLon = curLon;
//	    } else if (curLon > maxLon) {
//		maxLon = curLon;
//	    }
//	    if (curTime < minTime) {
//		minTime = curTime;
//	    } else if (curTime > maxTime) {
//		maxTime = curTime;
//	    }
//
//	    // Add locations and times to fish's pings array
//	    ping.setFloat("latitude", curLat);
//	    ping.setFloat("longitude", curLon);
//	    ping.setLong("dateTime", curTime);
//
//	    fish.getJSONArray("pings").append(ping);

	    fishDictionary.getJSONObject("fishes").setJSONObject(id, fish);
	}
	// Add mins and maxes
//	fishDictionary.setLong("startTime", minTime);
//	fishDictionary.setLong("endTime", maxTime);
	// fishDictionary.setFloat("minLongitude", minLon);
	// fishDictionary.setFloat("maxLongitude", maxLon);
	// fishDictionary.setFloat("minLatitude", minLat);
	// fishDictionary.setFloat("maxLatitude", maxLat);
	return fishDictionary;
    }

    /*
     * Adds data from a 2D string array from a CSV file into the provided JSON object. Assumptions: First row is header
     * row, first column is ID's, second column is datetimes, third column is latitude, fourth column is longitude, row
     * 11 is weight, row 13 is length, rows are sorted by time
     */
    public JSONObject addDetectionsToJSON(JSONObject fishDictionary, String[][] csv) {
	float minLat = Float.POSITIVE_INFINITY;
	float minLon = Float.POSITIVE_INFINITY;
	float maxLat = Float.NEGATIVE_INFINITY;
	float maxLon = Float.NEGATIVE_INFINITY;
	long minTime = Long.MAX_VALUE;
	long maxTime = Long.MIN_VALUE;
//	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//	fishDictionary.setJSONObject("fishes", new JSONObject());
	for (String[] row : csv) {
	    JSONObject fish;
	    String id = row[0];
	    if (id.equals("TagID")) { // skip the header row
		continue;
	    }
	    if (fishDictionary.getJSONObject("fishes").hasKey(id)) {
		fish = fishDictionary.getJSONObject("fishes").getJSONObject(id);
	    } else {
		// New ID, create a new fish
		fish = new JSONObject();
		fish.setJSONArray("pings", new JSONArray()); // An array of
							     // times and
							     // locations
							     // that this
							     // fish was seen
		// Add other fish-related data here
//		fish.setFloat("weight", Float.parseFloat(row[11]));
//		fish.setFloat("length", Float.parseFloat(row[13]));
		System.out.println("New fish in detections, no length and weight data");
	    }

	    // Parse values
	    JSONObject ping = new JSONObject();
	    long curTime;
	    try {
		curTime = format.parse(row[1]).getTime();
	    } catch (ParseException e) {
		System.out.println(e.getMessage());
		continue;
	    }
	    float curLat = Float.parseFloat(row[6]);
	    float curLon = Float.parseFloat(row[7]);

	    // Keep track of mins and maxes
	    if (curLat < minLat) {
		minLat = curLat;
	    } else if (curLat > maxLat) {
		maxLat = curLat;
	    }
	    if (curLon < minLon) {
		minLon = curLon;
	    } else if (curLon > maxLon) {
		maxLon = curLon;
	    }
	    if (curTime < minTime) {
		minTime = curTime;
	    } else if (curTime > maxTime) {
		maxTime = curTime;
	    }

	    // Add locations and times to fish's pings array
	    ping.setFloat("latitude", curLat);
	    ping.setFloat("longitude", curLon);
	    ping.setLong("dateTime", curTime);

	    fish.getJSONArray("pings").append(ping);

	    fishDictionary.getJSONObject("fishes").setJSONObject(id, fish);
	}
	// Add mins and maxes
	fishDictionary.setLong("startTime", minTime);
	fishDictionary.setLong("endTime", maxTime);
	// fishDictionary.setFloat("minLongitude", minLon);
	// fishDictionary.setFloat("maxLongitude", maxLon);
	// fishDictionary.setFloat("minLatitude", minLat);
	// fishDictionary.setFloat("maxLatitude", maxLat);
	return fishDictionary;
    }

    public void setup() {

	String[][] info = this.loadCSV("chinook2fishinfo.csv");
	JSONObject jData = this.infoToJSON(info);

	String[][] detections = this.loadCSV("chinook2detections.csv");
	jData = this.addDetectionsToJSON(jData, detections);
//	boolean successfullySaved = saveJSONObject(jData, "data/chinook2.json");
//	if (successfullySaved)
//	    background(0, 255, 0);
//	else
//	    background(255, 0, 0);
    }

    public void draw() {
    }

    public static void main(String _args[]) {
	PApplet.main(new String[] { fishtracking.Chinook2Parser.class.getName() });
    }
}
