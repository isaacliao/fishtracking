// For parsing green sturgeon dataset from Arnold Ammann. 

package fishtracking;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.HashMap;

import com.google.gson.Gson;

import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.core.PApplet;

// Loads a csv file and saves it as a JSon.
public class SturgeonParser {

    public static final String FISHINFO_FILENAME = "data/sturgeonfishinfo.csv";
    public static final String DETECTIONS_FILENAME = "data/sturgeondetections.csv";
    public static final String OUTPUT_FILENAME = "data/sturgeon.json";

    // Zero-based column numbers
    public static final int FISHINFO_CODESPACE = 4;
    public static final int FISHINFO_ID = 3;
    public static final int FISHINFO_WEIGHT = 6;
    public static final int FISHINFO_LENGTH = 7;
    public static final int DETECTIONS_CODESPACE = 1;
    public static final int DETECTIONS_ID = 0;
    public static final int DETECTIONS_LAT = 7;
    public static final int DETECTIONS_LON = 8;
    public static final int DETECTIONS_DATETIME = 2;

    public static final String ID_HEADER = "Codespace-TagID"; // used to identify and skip header row
    public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";

    private static PApplet pApplet = new PApplet();

    public static JSONObject mergeDetections(String filename, JSONObject jData) {
	// for importing csv files into a 2d array
	// by che-wei wang

	String lines[] = pApplet.loadStrings(filename);
	int csvWidth = 0;

	// calculate max width of csv file
	for (int i = 0; i < lines.length; i++) {
	    String[] chars = lines[i].split(",");
	    if (chars.length > csvWidth) {
		csvWidth = chars.length;
	    }
	}

	// Load a certain number of lines from the CSV and merge into fish info
	int numLines = 10000;

	// parse values into 2d array
	int i = 0;
	String[][] csv = new String[numLines][csvWidth];
	int csvPointer = 0;
	while (i < lines.length) {
	    if (i % numLines == 0 && i > 0) {
		System.out.println("Parsing detections, %" + (float) i / (float) lines.length * 100);
		jData = addDetectionsToJSON(jData, csv);
		// Reset csv pointer and clear array
		csvPointer = 0;
		csv = new String[numLines][csvWidth];
	    }
	    String[] temp = new String[lines.length];
	    temp = lines[i].split(",");
	    for (int j = 0; j < temp.length; j++) {
		csv[csvPointer][j] = temp[j];
	    }
	    i++;
	    csvPointer++;
	}
	return jData;
    }

    /*
     * Converts a 2D string array from a CSV file into a JSON object. Assumptions: First row is header row, first column
     * is ID's, second column is datetimes, third column is latitude, fourth column is longitude, row 11 is weight, row
     * 13 is length, rows are sorted by time
     */
    public static JSONObject infoToJSON(String[][] csv) {
	// float minLat = Float.POSITIVE_INFINITY;
	// float minLon = Float.POSITIVE_INFINITY;
	// float maxLat = Float.NEGATIVE_INFINITY;
	// float maxLon = Float.NEGATIVE_INFINITY;
	// long minTime = Long.MAX_VALUE;
	// long maxTime = Long.MIN_VALUE;
	// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	JSONObject fishDictionary = new JSONObject();
	fishDictionary.setJSONObject("fishes", new JSONObject());
	float rowCounter = 0f;
	for (String[] row : csv) {
	    rowCounter++;
	    float percent = rowCounter / (float) csv.length * 100;
	    System.out.println("Adding fish, %" + percent);
	    JSONObject fish;
	    String id = row[FISHINFO_ID];
	    if (id.equals(ID_HEADER)) { // skip the header row
		continue;
	    }
	    if (fishDictionary.getJSONObject("fishes").hasKey(id)) {
		fish = fishDictionary.getJSONObject("fishes").getJSONObject(id);
	    } else {
		// New ID, create a new fish
		fish = new JSONObject();
		fish.setJSONArray("pings", new JSONArray()); // An array of times and locations that this fish was seen
		// Add other fish-related data here
		try {
		    fish.setFloat("weight", Float.parseFloat(row[FISHINFO_WEIGHT]));
		} catch (NumberFormatException e) {
		    System.out.println("Error parsing fish weight, id=" + id + ", weight=" + row[FISHINFO_WEIGHT]);
		}
		try {
		    fish.setFloat("length", Float.parseFloat(row[FISHINFO_LENGTH]));
		} catch (NumberFormatException e) {
		    System.out.println("Error parsing fish length, id=" + id + ", length=" + row[FISHINFO_LENGTH]);
		}
	    }

	    // Parse values
	    JSONObject ping = new JSONObject();
	    // long curTime;
	    // try {
	    // curTime = format.parse(row[1]).getTime();
	    // } catch (ParseException e) {
	    // System.out.println(e.getMessage());
	    // continue;
	    // }
	    // float curLat = Float.parseFloat(row[2]);
	    // float curLon = Float.parseFloat(row[3]);
	    //
	    // // Keep track of mins and maxes
	    // if (curLat < minLat) {
	    // minLat = curLat;
	    // } else if (curLat > maxLat) {
	    // maxLat = curLat;
	    // }
	    // if (curLon < minLon) {
	    // minLon = curLon;
	    // } else if (curLon > maxLon) {
	    // maxLon = curLon;
	    // }
	    // if (curTime < minTime) {
	    // minTime = curTime;
	    // } else if (curTime > maxTime) {
	    // maxTime = curTime;
	    // }
	    //
	    // // Add locations and times to fish's pings array
	    // ping.setFloat("latitude", curLat);
	    // ping.setFloat("longitude", curLon);
	    // ping.setLong("dateTime", curTime);
	    //
	    // fish.getJSONArray("pings").append(ping);

	    fishDictionary.getJSONObject("fishes").setJSONObject(id, fish);
	}
	// Add mins and maxes
	// fishDictionary.setLong("startTime", minTime);
	// fishDictionary.setLong("endTime", maxTime);
	// fishDictionary.setFloat("minLongitude", minLon);
	// fishDictionary.setFloat("maxLongitude", maxLon);
	// fishDictionary.setFloat("minLatitude", minLat);
	// fishDictionary.setFloat("maxLatitude", maxLat);
	return fishDictionary;
    }

    /*
     * Loads data from a 2D string array from a CSV file into a FishCollection.
     */
    public static FishCollection infoCSVToFishCollection(String[][] csv) {
	// float minLat = Float.POSITIVE_INFINITY;
	// float minLon = Float.POSITIVE_INFINITY;
	// float maxLat = Float.NEGATIVE_INFINITY;
	// float maxLon = Float.NEGATIVE_INFINITY;
	// long minTime = Long.MAX_VALUE;
	// long maxTime = Long.MIN_VALUE;
	// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	FishCollection fishCollection = new FishCollection();
	HashMap<String, Fish> fishes = new HashMap<String, Fish>();
	float rowCounter = 0f;
	for (String[] row : csv) {
	    rowCounter++;
	    float percent = rowCounter / (float) csv.length * 100;
	    System.out.println("Adding fish, %" + percent);
	    Fish fish;
	    String id = row[FISHINFO_CODESPACE]+"-"+row[FISHINFO_ID];
	    if (id.equals(ID_HEADER)) { // skip the header row
		continue;
	    }
	    if (fishes.containsKey(id)) {
		// Already added, continue
		continue;
	    } else {
		// New ID, create a new fish
		fish = new Fish();
		// Add other fish-related data here
		try {
		    fish.weight = Float.parseFloat(row[FISHINFO_WEIGHT]);
		} catch (NumberFormatException e) {
		    System.out.println("Error parsing fish weight, id=" + id + ", weight=" + row[FISHINFO_WEIGHT]);
		}
		try {
		    fish.length = Float.parseFloat(row[FISHINFO_LENGTH]);
		} catch (NumberFormatException e) {
		    System.out.println("Error parsing fish length, id=" + id + ", length=" + row[FISHINFO_LENGTH]);
		}
	    }

	    fishes.put(id, fish);
	}
	// Add mins and maxes
	// fishCollection.startTime = minTime;
	// fishCollection.endTime = maxTime;
	fishCollection.fishes = fishes;
	return fishCollection;
    }

    /*
     * Adds data from a 2D string array from a CSV file into the provided FishCollection. Assumptions: rows are sorted
     * by time
     */
    public static FishCollection addPingsToFishCollection(FishCollection fishCollection, String[][] csv) {
	float minLat = Float.POSITIVE_INFINITY;
	float minLon = Float.POSITIVE_INFINITY;
	float maxLat = Float.NEGATIVE_INFINITY;
	float maxLon = Float.NEGATIVE_INFINITY;
	long minTime = Long.MAX_VALUE;
	long maxTime = Long.MIN_VALUE;
	if (fishCollection.startTime == Long.MAX_VALUE)
	    minTime = fishCollection.startTime;
	if (fishCollection.endTime == Long.MIN_VALUE)
	    maxTime = fishCollection.endTime;
	SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
	float rowCounter = 0f;
	// fishDictionary.setJSONObject("fishes", new JSONObject());
	for (String[] row : csv) {
	    rowCounter++;
	    float percent = rowCounter / (float) csv.length * 100;
	    // System.out.println("Adding detections, %" + percent);
	    Fish fish;
	    String id = row[DETECTIONS_CODESPACE]+"-"+row[DETECTIONS_ID];
	    if (id.equals(ID_HEADER)) { // skip the header row
		continue;
	    }
	    if (fishCollection.fishes.containsKey(id)) {
		fish = fishCollection.fishes.get(id);
	    } else {
		// New ID, create a new fish
		fish = new Fish();
		System.out.println("New fish in detections, no length and weight data");
	    }

	    // Parse values
	    Ping ping = new Ping();
	    long curTime;
	    try {
		curTime = format.parse(row[DETECTIONS_DATETIME]).getTime();
	    } catch (ParseException e) {
		System.out.println(e.getMessage());
		continue;
	    }
	    float curLat = Float.parseFloat(row[DETECTIONS_LAT]);
	    float curLon = Float.parseFloat(row[DETECTIONS_LON]);

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
	    ping.latitude = curLat;
	    ping.longitude = curLon;
	    ping.dateTime = curTime;

	    fish.pings.add(ping);

	    fishCollection.fishes.put(id, fish);
	}
	// Add mins and maxes
	fishCollection.startTime = minTime;
	fishCollection.endTime = maxTime;
	// fishDictionary.setFloat("minLongitude", minLon);
	// fishDictionary.setFloat("maxLongitude", maxLon);
	// fishDictionary.setFloat("minLatitude", minLat);
	// fishDictionary.setFloat("maxLatitude", maxLat);
	return fishCollection;
    }

    /*
     * Adds data from a 2D string array from a CSV file into the provided JSON object. Assumptions: rows are sorted by
     * time
     */
    public static JSONObject addDetectionsToJSON(JSONObject fishDictionary, String[][] csv) {
	float minLat = Float.POSITIVE_INFINITY;
	float minLon = Float.POSITIVE_INFINITY;
	float maxLat = Float.NEGATIVE_INFINITY;
	float maxLon = Float.NEGATIVE_INFINITY;
	long minTime = Long.MAX_VALUE;
	long maxTime = Long.MIN_VALUE;
	if (fishDictionary.hasKey("startTime"))
	    minTime = fishDictionary.getLong("startTime");
	if (fishDictionary.hasKey("endTime"))
	    maxTime = fishDictionary.getLong("endTime");
	SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
	float rowCounter = 0f;
	// fishDictionary.setJSONObject("fishes", new JSONObject());
	for (String[] row : csv) {
	    rowCounter++;
	    float percent = rowCounter / (float) csv.length * 100;
	    // System.out.println("Adding detections, %" + percent);
	    JSONObject fish;
	    String id = row[DETECTIONS_ID];
	    if (id.equals(ID_HEADER)) { // skip the header row
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
		// fish.setFloat("weight", Float.parseFloat(row[11]));
		// fish.setFloat("length", Float.parseFloat(row[13]));
		System.out.println("New fish in detections, no length and weight data");
	    }

	    // Parse values
	    JSONObject ping = new JSONObject();
	    long curTime;
	    try {
		curTime = format.parse(row[DETECTIONS_DATETIME]).getTime();
	    } catch (ParseException e) {
		System.out.println(e.getMessage());
		continue;
	    }
	    float curLat = Float.parseFloat(row[DETECTIONS_LAT]);
	    float curLon = Float.parseFloat(row[DETECTIONS_LON]);

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

    public static String[][] loadCSV(String filename) {
	// for importing csv files into a 2d array
	// by che-wei wang

	String lines[] = pApplet.loadStrings(filename);
	String[][] csv;
	int csvWidth = 0;

	// calculate max width of csv file
	for (int i = 0; i < lines.length; i++) {
	    String[] chars = lines[i].split(",");
	    if (chars.length > csvWidth) {
		csvWidth = chars.length;
	    }
	}

	// create csv array based on # of rows and columns in csv file
	csv = new String[lines.length][csvWidth];

	// parse values into 2d array
	for (int i = 0; i < lines.length; i++) {
	    String[] temp = new String[csvWidth];
	    temp = lines[i].split(",");
	    for (int j = 0; j < temp.length; j++) {
		csv[i][j] = temp[j];
	    }
	}

	return csv;
    }

    /*
     * Loads part of a CSV file into a 2D string array. Includes startRow, ends just before endRow.
     */
    public static String[][] loadPartOfCSV(String filename, int startRow, int endRow) {
	// for importing csv files into a 2d array
	// by che-wei wang

	int numRows = endRow - startRow;
	String lines[] = pApplet.loadStrings(filename);
	String[][] csv;
	int csvWidth = 0;

	// calculate max width of csv file
	for (int i = 0; i < lines.length; i++) {
	    String[] chars = lines[i].split(",");
	    if (chars.length > csvWidth) {
		csvWidth = chars.length;
	    }
	}

	// create csv array based on # of rows and columns in csv file
	csv = new String[numRows][csvWidth];

	// parse values into 2d array
	for (int i = 0; i < numRows; i++) {
	    String[] temp = new String[csvWidth];
	    temp = lines[i].split(",");
	    for (int j = 0; j < temp.length; j++) {
		csv[i][j] = temp[j];
	    }
	}

	return csv;
    }

    public static int lengthOf(String filename) {
	String lines[] = pApplet.loadStrings(filename);
	return lines.length;
    }

    public static void main(String _args[]) {
	// String[][] info = loadCSV(FISHINFO_FILENAME);
	// System.out.println("Converting fish info to JSON...");
	// JSONObject jData = infoToJSON(info);
	// System.out.println("Loading detections file...");
	// jData = mergeDetections(DETECTIONS_FILENAME, jData);
	// System.out.println("Saving output file...");
	// boolean successfullySaved = pApplet.saveJSONObject(jData, OUTPUT_FILENAME);
	// if (successfullySaved) {
	// System.out.println("Successfully saved!");
	// } else {
	// System.out.println("Save failed!");
	// }
	// System.out.println("Done!");

	Gson gson = new Gson();
	Path path = FileSystems.getDefault().getPath(OUTPUT_FILENAME);
	BufferedWriter file = null;
	try 
	{file = Files.newBufferedWriter(path, Charset.defaultCharset());}
	catch (Exception e) {
	    System.out.println("Exception when trying to open output file");
	}
	
	System.out.println("Loading info file...");
	String[][] infoCSV = loadCSV(FISHINFO_FILENAME);
	System.out.println("Converting to FishCollection...");
	FishCollection fishCollection = infoCSVToFishCollection(infoCSV);

	int length = lengthOf(DETECTIONS_FILENAME);
	//length = 10000; // DEBUG: try to load first 10000 pings
	int interval = 10000; // number of rows to load at once

	for (int i = 0; i < length; i += interval) {
	    System.out.println("Loading row "+i+"/"+length);
	    int endRow;
	    if (i + interval > length)
		endRow = length;
	    else
		endRow = i + interval;
	    String[][] detectionsCSV = loadPartOfCSV(DETECTIONS_FILENAME, i, endRow);
	    fishCollection = addPingsToFishCollection(fishCollection, detectionsCSV);
	}
	System.out.println("Converting FishCollection to JSON...");	
	String json = gson.toJson(fishCollection);
	System.out.println("Writing to output file...");
	try {
	file.write(json);
	file.close();
	}
	catch (Exception e) {
	    System.out.println("Exception when trying to write to output file");
	}
	System.out.println("Done!");
    }
}
