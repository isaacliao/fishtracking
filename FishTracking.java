package fishtracking;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import de.fhpotsdam.unfolding.geo.*;

import com.google.gson.*;

public class FishTracking extends PApplet {

    // assumes data will be in FILENAMES[i]+".json" and corresponding path data
    // will be in FILENAMES[i]+"paths.ser"
    // public static final String[] FILENAMES = { "chinook", "chinook2",
    // "sturgeon" };
	
	// CRASHES
	public static final String[] FILENAMES = { "chinook", "chinook2", "sturgeon", "seaturtles" };
	
	// NO CRASH
	//public static final String[] FILENAMES = { "chinook", "sturgeon", "seaturtles" };
    
	// corresponds to FILENAMES
    public int[] COLORS = { color(255, 204, 204, 127), color(255, 102, 102, 127), color(153, 255, 102, 127), color(255, 255, 102, 127) };
    public int[] TRACE_COLORS = { color(255, 204, 204, 10), color(255, 102, 102, 10), color(153, 255, 102, 10), color(255, 255, 102, 10) };
    // public int[] COLORS = { color(255, 204, 204, 127), color(255, 102, 102, 127) };
    // public int[] TRACE_COLORS = { color(255, 204, 204, 10), color(255, 102, 102, 10) };

    // public static final String[] FILENAMES = { "sturgeon" };
    // public int[] COLORS = { color(153, 255, 102, 127) };

    // Positioning on the screen
    public static  int PROJECTION_WIDTH = 1500;
    public static int PROJECTION_HEIGHT = 1200;
    public static int TIMELINE_LEFT_MARGIN = 25;
    public static int TIMELINE_RIGHT_MARGIN = 1100;
    public static int TIMELINE_BOTTOM_MARGIN = 200;
    
    public Debug debug;
    public Boolean bDebug;
    public Boolean bInitialized = false;
    public int initializationStage = 0;
    public int fishFileCounter = 0;	// used like a for(int i=0) loop on initialization
    
    // Background filename,
   // public static final String LANDBOUNDARY_FILENAME = "landWaterBoundary.png";		// the one with a transparent look to it
    public static final String LANDBOUNDARY_FILENAME = "data/landWaterBoundary_color.png";		// the one with a colors in it, especially blue water
    
    
    public static final Location BAY_MODEL_UPPER_LEFT = new Location(38.2268534751704f, -122.917099f);
    public static final Location BAY_MODEL_LOWER_RIGHT = new Location(37.37179426133591f, -121.56440734863281f);
    public static final int TIMELINE_PADDING = 100; // how much space (in
						    // pixels) between the edge
						    // of the screen and the
    // timeline
    public static final float SECONDS_PER_MONTH = 6 * 60 * 24 * 30;
    public static final float ANIMATION_RATE = SECONDS_PER_MONTH; // speedup
								  // multiplier
								  // of data
								  // time to
								  // real time

    public static long StartTime = Long.MAX_VALUE;
    public static long EndTime = Long.MIN_VALUE;
    public long CurrentFrameTime, LastFrameTime;
    public int TIMELINE_LEFT, TIMELINE_RIGHT;

    private int PROJECTION_LEFT = 0;
    private PImage boundary, backgroundImage;
    private FishCollection fishCollection = new FishCollection();
    private PFont smallFont = createFont("Explo", 12);
    private static Calendar cal = Calendar.getInstance();
    private long currentTime;
    private long secondTimer = 0;
    private int traceToDraw = 0;
    private int drawMode = 0;
    private PhysicalInterface physicalInterface;
    protected enum LocationType {
	PATH, PING
    };

    public void setup() {
    	size(1920, 1200, OPENGL);
    	PROJECTION_WIDTH  = 1500;
    	PROJECTION_HEIGHT  = 1200;
    	PROJECTION_LEFT = (width-PROJECTION_WIDTH)/2;		
    			
    	// Display the debug screen until we have loaded all the fish data
    	debug = new Debug(this,false);		// 2nd param = true if we are producing file logs
    	bDebug = true;
    	debug.log("Starting Fish Tracker");
    	
    	//** old positioning code **/
    	//	TIMELINE_LEFT = TIMELINE_PADDING;
    	//	TIMELINE_RIGHT = width - TIMELINE_PADDING;
    	TIMELINE_LEFT = TIMELINE_LEFT_MARGIN;
    	TIMELINE_RIGHT = PROJECTION_WIDTH - TIMELINE_RIGHT_MARGIN;
	
    }

    public void draw() {
    	if( !bInitialized )
    		initialize();
    	
    	// if we are in debug mode, draw the debug screen, exit draw
    	if( bDebug ) {
    		debug.draw();
    		return;
    	}
    	
    	// Save frame info for fps
    	LastFrameTime = CurrentFrameTime;
    	CurrentFrameTime = millis();
    	
    	// Get info from physical controller
    	 try {
    		 if( physicalInterface != null ) {
    	    		physicalInterface.update();
    	    		this.currentTime = (long) map(physicalInterface.getPosition(), TIMELINE_LEFT, TIMELINE_RIGHT, StartTime, EndTime);
    	    }
    	 } catch (Exception e) {
	    	debug.out("drawing error: " + e.getMessage());
    	 }
    	
    	// Regular screen-drawing
    	// background(backgroundImage);
    	
    	background(0);	// use for projector bleed-over
    	//image(backgroundImage, 0, 0, width, height);
    	
    	image(backgroundImage, PROJECTION_LEFT, 0, PROJECTION_WIDTH, PROJECTION_HEIGHT );
	
    	if (drawMode == 0)
    		drawAnimatedPositions(LocationType.PATH);
    		// drawAnimatedPositions(LocationType.PING);

    	else if (drawMode == 1)
    		drawTraces(LocationType.PATH);
    	// drawLocations(LocationType.PING);
	
    	else if (drawMode == 2)
    		drawPings(LocationType.PING);

    	drawfps();
    }

    public void keyPressed() {
    if( key == ' ' && key == SHIFT ) {	// space bar toggles debug screen
    	bDebug = !bDebug;
    }
    
	if (key == CODED) {
	    if (keyCode == RIGHT)
		this.traceToDraw++;
	    if (keyCode == LEFT)
		this.traceToDraw--;
	    if (keyCode == UP)
		this.drawMode++;
	    if (keyCode == DOWN)
		this.drawMode--;
	}
	if (this.traceToDraw > TRACE_COLORS.length)
	    traceToDraw = 0;
	if (this.traceToDraw < 0)
	    traceToDraw = TRACE_COLORS.length;
	if (this.drawMode > 2)
	    drawMode = 0;
	if (this.drawMode < 0)
	    drawMode = 2;
    }

    //-- we do a series of "stages" for initialization; the reason being is that in the runtime mode, we are failing somewhere
    //-- this will be used to show where we are at in the initialization code. After each stage, we increment the initializationStage
    //-- counter and initialize another object/portion
    private void initialize() {
    	switch( initializationStage ) {
    		case 0:
    			debug.out("Initializing FishTracking" );
    			initializationStage++;
    			break;
			
    		case 1:
    			debug.out("Initializing PhysicalInterface" );
    			
    			// UNCOMMENT when on the viz 
    			// physicalInterface = new PhysicalInterface(this,TIMELINE_LEFT, TIMELINE_RIGHT );
    			initializationStage++;
    			break;
    		
    		case 2:
    			debug.out("Fish data files:");
    			for (int i = 0; i < FILENAMES.length; i++) {
    				//debug.out("  " + dataPath(FILENAMES[i] + ".json"));
    				
    				debug.out("  " + "data/" + FILENAMES[i] + ".json");
    				
    	    	   // fishCollection.merge(loadFishData("data/" + FILENAMES[i] + ".json", i));
    	    	}
    			initializationStage++;
    			break;
    			
    		case 3:
    			debug.out("Fish .ser files:");
    			
    			for (int i = 0; i < FILENAMES.length; i++) {
    				//debug.out("  " + dataPath(FILENAMES[i] + "paths" + "1" + ".ser"));
    				debug.out("  " + "data/" + FILENAMES[i] + "paths" + "1" + ".ser");
    			}
    			
    			//---OLD
    			/**
    			for (int i = 0; i < FILENAMES.length; i++) {
    			 try {
//     	    		for (Path path : Files.newDirectoryStream(FileSystems.getDefault().getPath("data/"), FILENAMES[i]
//     	    			+ "paths?.ser")) // TODO: Support more than 9 path files per data set
     	    			for (Path path : Files.newDirectoryStream(FileSystems.getDefault().getPath(dataPath("")), FILENAMES[i]
     	    					+ "paths?.ser")) // TODO: Support more than 9 path files per data set
     	    					
     	    		{
     	    			debug.out("  " + path.toString());
     	    		   
     	    		}
     	    	    } catch (IOException e) {
     	    		// TODO Auto-generated catch block
     	    		e.printStackTrace();
     	    	    }
    			 
    			}**/
    			
    			initializationStage++;
    			fishFileCounter = 0;	// set for next stage
    			break;
    			
    		case 4:
    			// break out into separate fish data file initializers
    			debug.out("Loading Fish data from file: " +  FILENAMES[fishFileCounter]);
    			// Load fish data
    			fishCollection.merge(loadFishData("data/" + FILENAMES[fishFileCounter] + ".json", fishFileCounter));
    			debug.log("fishCollection merge is finished" );
    			//fishCollection.merge(loadFishData(dataPath(FILENAMES[fishFileCounter] + ".json"), fishFileCounter));
    			fishFileCounter++;
    			
    			/*
    	    	for (int i = 0; i < FILENAMES.length; i++) {
    	    		fishCollection.merge(loadFishData(dataPath(FILENAMES[i] + ".json"), i));
    	    	   // fishCollection.merge(loadFishData("data/" + FILENAMES[i] + ".json", i));
    	    	}*/
    	    	
    	    	if( fishFileCounter == FILENAMES.length ) {
    	    		// done loading, go to next stage
    	    		fishFileCounter = 0;
        	    	initializationStage++;
    	    	}

    	    	break;
    	    	
    		case 5:
    			debug.out("Loading calculated paths: " +  FILENAMES[fishFileCounter]);
    			// Load calculated paths
    			// PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:"+FILENAMES[i]+"paths?.ser");
    			
    			this.loadFishPaths("data/" + FILENAMES[fishFileCounter] + "paths" + "1" + ".ser");
    			//this.loadFishPaths(dataPath(FILENAMES[fishFileCounter] + "paths" + "1" + ".ser"));
    			
    			/**
    	    	   try {
//    	    		for (Path path : Files.newDirectoryStream(FileSystems.getDefault().getPath("data/"), FILENAMES[i]
//    	    			+ "paths?.ser")) // TODO: Support more than 9 path files per data set
    	    			for (Path path : Files.newDirectoryStream(FileSystems.getDefault().getPath(dataPath("")), FILENAMES[fishFileCounter]
    	    					+ "paths?.ser")) // TODO: Support more than 9 path files per data set
    	    					
    	    		{
    	    		    this.loadFishPaths(path.toString());
    	    		}
    	    	    } catch (IOException e) {
    	    		// TODO Auto-generated catch block
    	    		e.printStackTrace();
    	    	    }**/
    	    	   
    	    	   fishFileCounter++;
    	    
    			
    	    	/**for (int i = 0; i < FILENAMES.length; i++) {
    	    	    // PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:"+FILENAMES[i]+"paths?.ser");

    	    	    try {
//    	    		for (Path path : Files.newDirectoryStream(FileSystems.getDefault().getPath("data/"), FILENAMES[i]
//    	    			+ "paths?.ser")) // TODO: Support more than 9 path files per data set
    	    			for (Path path : Files.newDirectoryStream(FileSystems.getDefault().getPath(dataPath("")), FILENAMES[i]
    	    					+ "paths?.ser")) // TODO: Support more than 9 path files per data set
    	    					
    	    		{
    	    		    this.loadFishPaths(path.toString());
    	    		}
    	    	    } catch (IOException e) {
    	    		// TODO Auto-generated catch block
    	    		e.printStackTrace();
    	    	    }
    	    	}**/
    			
    	    	if( fishFileCounter == FILENAMES.length ) {
    	    		// done loading, go to next stage
    	    		fishFileCounter = 0;
        	    	initializationStage++;
    	    	}
    			break;
    			
    		case 6:
    			debug.out("Creating timeline image");
    			// Create background image, including boundary, timeline, and graph
    	    	this.backgroundImage = createTimelineImage();
    	    	currentTime = StartTime;
    	    	CurrentFrameTime = millis();
    	    	initializationStage++;
    	    	
    	    	debug.out("done creating timeline, we are done with initialization");
    	    	bInitialized = true;
    	    	bDebug = false;
    	    	break;
    	}
    }
    
    private int lastFrameCount = 0;
    private int fps = 0;

    private void drawfps() {
	if (CurrentFrameTime - secondTimer > 1000) {
	    secondTimer = CurrentFrameTime;
	    fps = frameCount - lastFrameCount;
	    lastFrameCount = frameCount;
	}
	textFont(smallFont);
	textAlign(LEFT, TOP);
	text(fps, 0, 0);
    }

    private int pathsCounter = 0;

    private void loadFishPathsOld(String filename) {
    	
	JSONObject jPaths = loadJSONObject(filename);
	for (Object id : jPaths.keys()) {
	    JSONArray jPath = jPaths.getJSONArray((String) id);
	    ArrayList<Ping> path = new ArrayList<Ping>();
	    for (int i = 0; i < jPath.size(); i++) {
		JSONObject jPing = jPath.getJSONObject(i);
		Ping ping = new Ping();
		ping.latitude = jPing.getFloat("latitude");
		ping.longitude = jPing.getFloat("longitude");
		ping.dateTime = jPing.getLong("dateTime");
		if (ping.dateTime < StartTime)
		    System.out.println("Error: ping time of " + ping.dateTime + " is before StartTime of " + StartTime);

		path.add(ping);
	    }
	    pathsCounter++;
	    System.out.println("Loaded " + pathsCounter + " paths");
	    debug.log("Loaded " + pathsCounter + " paths");
	    fishCollection.fishes.get(id).path = path;
	}
    }

    private void loadFishPathsUsingGson(String filename) {
	Gson gson = new Gson();
	Path inFilePath = FileSystems.getDefault().getPath(filename);
	BufferedReader inFile = null;
	try {
	    inFile = Files.newBufferedReader(inFilePath, Charset.defaultCharset());
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	PathCollection pathCollection = gson.fromJson(inFile, PathCollection.class);
	this.fishCollection.merge(pathCollection);

	// JSONObject jPaths = loadJSONObject(filename);
	// for (Object id : jPaths.keys()) {
	// JSONArray jPath = jPaths.getJSONArray((String) id);
	// ArrayList<Ping> path = new ArrayList<Ping>();
	// for (int i = 0; i < jPath.size(); i++) {
	// JSONObject jPing = jPath.getJSONObject(i);
	// Ping ping = new Ping();
	// ping.latitude = jPing.getFloat("latitude");
	// ping.longitude = jPing.getFloat("longitude");
	// ping.dateTime = jPing.getLong("dateTime");
	// if (ping.dateTime < StartTime)
	// System.out.println("Error: ping time of " + ping.dateTime +
	// " is before StartTime of " + StartTime);
	//
	// path.add(ping);
	// }
	// pathsCounter++;
	// System.out.println("Loaded " + pathsCounter + " paths");
	// fishCollection.fishes.get(id).path = path;
	// }
    }

    private void loadFishPaths(String filename) {
    	debug.log("load fish paths(), filename = " + filename );
	//Path inFilePath = FileSystems.getDefault().getPath(filename);
	//debug.log("inFilePath = " + inFilePath.toString() );
	
	
	PathCollection pathCollection = null;
	try {
	    	//FileInputStream fis = new FileInputStream(inFilePath.toString());
	    	FileInputStream fis = new FileInputStream(filename);
	    	ObjectInputStream ois = new ObjectInputStream(fis);
	    	pathCollection = (PathCollection) ois.readObject();
	    	ois.close();
		} catch (Exception e) {
			debug.log("exception " + e.getMessage() );
		e.printStackTrace();
		}
		debug.log("done loading fish paths()" );
		this.fishCollection.merge(pathCollection);
		debug.log("done merging fish paths()" );
    }

    private static int fishCounter = 0;

    public  FishCollection loadFishData(String filename, int dataset) {
    	debug.log("Entering loadFishData()");
    	debug.log("Filename = " + filename);
	FishCollection fishCollection = new FishCollection();
	debug.log("Allocated Fish Collection object");
	JSONObject fishData = loadJSONObject(filename);
	debug.log("loaded JSON Object");
	if( fishData == null )
		debug.log("fishData is NULL");
	
	if (fishData.getLong("startTime") < StartTime)
	    StartTime = fishData.getLong("startTime");
	if (fishData.getLong("endTime") > EndTime)
	    EndTime = fishData.getLong("endTime");

	JSONObject fishList = fishData.getJSONObject("fishes");
	debug.log("got fishList");
	if( fishList == null )
		debug.log("fishList = null");
	for (Object key : fishList.keys()) {
	    fishCounter++;
	    String id = key.toString();
	    // Fish fish = gson.fromJson(fishList.getJSONObject(id).toString(),
	    // Fish.class);
	    Fish fish = new Fish();
	    JSONObject jFish = fishList.getJSONObject(id);
	   
	    try {
	    	fish.length = jFish.getFloat("length");
	    	fish.weight = jFish.getFloat("weight");
	    }
	    catch(Exception e){
	    	System.out.println("Missing length or weight");
	    }
	    fish.dataset = dataset;
	    JSONArray jPings = jFish.getJSONArray("pings");
	    ArrayList<Ping> pings = new ArrayList<Ping>();
	    for (int i = 0; i < jPings.size(); i++) {
		// Ping ping = gson.fromJson(jPings.getString(i), Ping.class);

		Ping ping = new Ping();
		ping.latitude = jPings.getJSONObject(i).getFloat("latitude");
		ping.longitude = jPings.getJSONObject(i).getFloat("longitude");
		ping.dateTime = jPings.getJSONObject(i).getLong("dateTime");

		pings.add(ping);
	    }
	    fish.pings = pings;
	    fishCollection.fishes.put(id, fish);
	    System.out.println("Loaded " + fishCounter + " fish");
	    debug.log("Loaded " + fishCounter + " fish");
	    
		}
		debug.log("Total fish count = " + fishCounter );
		return fishCollection;
    }

    private PImage createTimelineImage() {
	PGraphics pg = createGraphics(PROJECTION_WIDTH, PROJECTION_HEIGHT);
	debug.log("done creating graphics");
	if( pg == null )
		debug.log("pg = null");
	
	pg.beginDraw();
	boundary = loadImage(LANDBOUNDARY_FILENAME);
	if( boundary == null )
		debug.log("loadImage: " + LANDBOUNDARY_FILENAME );
	
	if( boundary == null )
		debug.log("boundary = null");
	pg.background(0);
	pg.image(boundary, 0, 0);//, width, height);

	// draw a timeline
	float timeY = height - TIMELINE_BOTTOM_MARGIN;
	float timeLeftX = TIMELINE_LEFT;
	float timeRightX = TIMELINE_RIGHT;
	
	// OLD - remove
//	float timeLeftX = TIMELINE_PADDING;
//	float timeRightX = width - TIMELINE_PADDING;
	
	// float timeX = map(currentTime, StartTime, EndTime, timeLeftX,
	// timeRightX);
	pg.stroke(255, 127);
	pg.fill(255, 127);
	pg.line(timeLeftX, timeY, timeRightX, timeY);

	// Draw dashes for months
	PFont exploFont = createFont("Explo", 14);//24);
	pg.textFont(exploFont);
	pg.textAlign(CENTER, BOTTOM);

	Calendar cal2 = Calendar.getInstance();
	cal2.setTimeInMillis(StartTime);
	cal.clear();
	cal.set(cal2.get(Calendar.YEAR), cal2.get(Calendar.MONTH), 1); // Zero
								       // out
								       // days,
								       // hours,
								       // minutes,
								       // etc.
	cal.roll(Calendar.MONTH, true); // Add a month (assumes that first ping
					// time is not exactly at start of
					// month)
	if (cal.get(Calendar.MONTH) == Calendar.JANUARY) // Just rolled to
							 // January, so need
							 // to add a year
	    cal.roll(Calendar.YEAR, true); // Add a year
	while (cal.getTimeInMillis() < EndTime) {
	    float monthX = map(cal.getTimeInMillis(), StartTime, EndTime, timeLeftX, timeRightX);
	    if (cal.get(Calendar.MONTH) == Calendar.JANUARY) { // Make the first
							       // month of the
							       // year line
							       // longer
		// pg.strokeWeight(3f);
		pg.line(monthX, timeY - 10, monthX, timeY + 10);
		pg.text(cal.get(Calendar.YEAR), monthX, timeY - 10); // Label
								     // years
	    } else {
		pg.strokeWeight(1f);
		pg.line(monthX, timeY - 5, monthX, timeY + 5);
	    }
	    cal.roll(Calendar.MONTH, true); // Add a month
	    if (cal.get(Calendar.MONTH) == Calendar.JANUARY) // Just rolled to
							     // January, so
							     // need to add a
							     // year
		cal.roll(Calendar.YEAR, true); // Add a year
	    // System.out.println(cal.getTime().toString());
	}
	// Draw a graph of pings for each file
	for (int i = 0; i < FILENAMES.length; i++) {
	    // Count how many pings are in each month from the specified file
	    HashMap<Long, Integer> monthlyPings = new HashMap<Long, Integer>();
	    for (Fish fish : fishCollection.fishes.values()) {
		if (fish.dataset == i) {
		    for (Ping ping : fish.pings) {
			cal2.setTimeInMillis(ping.dateTime);
			cal.clear();
			cal.set(cal2.get(Calendar.YEAR), cal2.get(Calendar.MONTH), 1); // Reset to
										       // beginning of
										       // current
			// month
			long monthStart = cal.getTimeInMillis();

			if (monthlyPings.containsKey(monthStart)) {
			    monthlyPings.put(monthStart, monthlyPings.get(monthStart) + 1);
			} else {
			    monthlyPings.put(monthStart, 1);
			}
		    }
		}
	    }
	    // Find max value in monthlyPings
	    int maxPingsPerMonth = 0;
	    for (int numPings : monthlyPings.values()) {
		if (numPings > maxPingsPerMonth)
		    maxPingsPerMonth = numPings;
	    }
	    // Draw a graph of pings per month
	    pg.noStroke();
	    pg.fill(COLORS[i]);
	    pg.beginShape();
	    pg.vertex(timeLeftX, timeY);

	    cal2.setTimeInMillis(StartTime);
	    cal.clear();
	    cal.set(cal2.get(Calendar.YEAR), cal2.get(Calendar.MONTH), 1); // Zero
									   // out
									   // days,
									   // hours,
									   // minutes,
									   // etc.
	    cal.roll(Calendar.MONTH, true); // Add a month (assumes that first
					    // ping time is not exactly at start
					    // of
	    // month)
	    if (cal.get(Calendar.MONTH) == Calendar.JANUARY) // Just rolled to
							     // January, so
							     // need to add a
							     // year
		cal.roll(Calendar.YEAR, true); // Add a year
	    while (cal.getTimeInMillis() < EndTime) {
		long monthStartTime = cal.getTimeInMillis();
		cal.set(Calendar.DAY_OF_MONTH, 15);
		long monthHalfTime = cal.getTimeInMillis();
		cal.set(Calendar.DAY_OF_MONTH, 1);

		// float monthX = map(monthStartTime, StartTime, EndTime,
		// timeLeftX,
		// timeRightX);
		float monthHalfX = map(monthHalfTime, StartTime, EndTime, timeLeftX, timeRightX);

		float monthHalfY = timeY;
		if (monthlyPings.containsKey(monthStartTime))
		    monthHalfY = timeY - (float) monthlyPings.get(monthStartTime) / (float) maxPingsPerMonth * 50; // Normalize
		// height
		// of
		// graph
		// to
		// this
		// number
		// of
		// pixels

		pg.vertex(monthHalfX, monthHalfY);

		cal.roll(Calendar.MONTH, true); // Add a month
		if (cal.get(Calendar.MONTH) == Calendar.JANUARY) // Just rolled
								 // to
								 // January,
								 // so need
								 // to add a
								 // year
		    cal.roll(Calendar.YEAR, true); // Add a year
		// System.out.println(cal.getTime().toString());
	    }
	    pg.vertex(timeRightX, timeY);
	    pg.endShape();
	}

	pg.endDraw();
	return pg.get();
    }

    private void drawPings(LocationType locationType) {
	for (Fish fish : fishCollection.fishes.values()) {
	    if (this.traceToDraw < TRACE_COLORS.length && fish.dataset != this.traceToDraw)
		continue;
	    boolean init = true;
	    stroke(TRACE_COLORS[fish.dataset]);
	    fill(TRACE_COLORS[fish.dataset]);
	    strokeWeight(1);

	    ArrayList<Ping> locationData = null;
	    if (locationType == LocationType.PATH)
		locationData = fish.path;
	    else if (locationType == LocationType.PING)
		locationData = fish.pings;
	    else
		System.out.println("Error: no location data source specified");

	    for (Ping ping : locationData) {
		if (init) {
		    init = false;
		} else {
		    // Draw a circle at ping location
		    float pingX, pingY;
		    pingX = map(ping.longitude, BAY_MODEL_UPPER_LEFT.getLon(), BAY_MODEL_LOWER_RIGHT.getLon(), 0, width);
		    pingY = map(ping.latitude, BAY_MODEL_UPPER_LEFT.getLat(), BAY_MODEL_LOWER_RIGHT.getLat(), 0, height);

		    ellipse(pingX, pingY, 3, 3);
		}
	    }
	}
    }

    private void drawTraces(LocationType locationType) {
	for (Fish fish : fishCollection.fishes.values()) {
	    if (this.traceToDraw < TRACE_COLORS.length && fish.dataset != this.traceToDraw)
		continue;
	    boolean init = true;
	    Ping lastPing = null;
	    stroke(TRACE_COLORS[fish.dataset]);
	    strokeWeight(1);
	    noFill();

	    ArrayList<Ping> locationData = null;
	    if (locationType == LocationType.PATH)
		locationData = fish.path;
	    else if (locationType == LocationType.PING)
		locationData = fish.pings;
	    else
		System.out.println("Error: no location data source specified");

	    for (Ping ping : locationData) {
		if (init) {
		    init = false;
		    lastPing = ping;
		} else {
		    // Draw a line from lastPing to ping
		    float lastPingX, lastPingY, pingX, pingY;
		    lastPingX = map(lastPing.longitude, BAY_MODEL_UPPER_LEFT.getLon(), BAY_MODEL_LOWER_RIGHT.getLon(),
			    0, 1500);
		    lastPingY = map(lastPing.latitude, BAY_MODEL_UPPER_LEFT.getLat(), BAY_MODEL_LOWER_RIGHT.getLat(),
			    0, 1200);
		    pingX = map(ping.longitude, BAY_MODEL_UPPER_LEFT.getLon(), BAY_MODEL_LOWER_RIGHT.getLon(), 0, width);
		    pingY = map(ping.latitude, BAY_MODEL_UPPER_LEFT.getLat(), BAY_MODEL_LOWER_RIGHT.getLat(), 0, height);

		    line(lastPingX, lastPingY, pingX, pingY);
		    lastPing = ping;
		}
	    }
	}
    }

    private void drawLocations(LocationType locationType) {
	for (Fish fish : fishCollection.fishes.values()) {

	    ArrayList<Ping> locationData = null;
	    if (locationType == LocationType.PATH)
		locationData = fish.path;
	    else if (locationType == LocationType.PING)
		locationData = fish.pings;
	    else
		System.out.println("Error: no location data source specified");

	    for (Ping ping : locationData) {
		PVector drawPosition = new PVector();
		drawPosition.x = map(ping.longitude, BAY_MODEL_UPPER_LEFT.getLon(), BAY_MODEL_LOWER_RIGHT.getLon(), 0,
			1500);
		drawPosition.y = map(ping.latitude, BAY_MODEL_UPPER_LEFT.getLat(), BAY_MODEL_LOWER_RIGHT.getLat(), 0,
			1200);
		ellipse(drawPosition.x, drawPosition.y, 10, 10);
	    }
	}
    }

    private void drawAnimatedPositions(LocationType locationType) {
	if (currentTime > EndTime) {
	    currentTime = StartTime;
	}
	// update all fish
	for (String id : fishCollection.fishes.keySet()) {
	    fishCollection.fishes.get(id).update(currentTime, locationType);
	}
	// draw all fish
	noStroke();
	for (Fish fish : fishCollection.fishes.values()) {
	    if (fish.isVisible) {
		fill(COLORS[fish.dataset]);
		PVector drawPosition = new PVector();
		drawPosition.x = lonToX(fish.position.getLon(), width);
		drawPosition.y = latToY(fish.position.getLat(), height);
		ellipse(drawPosition.x, drawPosition.y, 10, 10);
		// break;
	    }
	}

	// draw current time indicator
	float timeY = height - TIMELINE_BOTTOM_MARGIN;	//TIMELINE_PADDING;
	float timeLeftX = TIMELINE_LEFT;	//TIMELINE_PADDING;
	float timeRightX = TIMELINE_RIGHT;	//width - TIMELINE_PADDING;
	float timeX = map(currentTime, StartTime, EndTime, timeLeftX, timeRightX);

	noStroke();
	fill(255, 127);
	
	float triangleX = timeX + (width-PROJECTION_WIDTH)/2; 
	triangle(triangleX, timeY, triangleX - 5, timeY + 10, triangleX + 5, timeY + 10);
//old-remove
// 	triangle(timeX, timeY, timeX - 5, timeY + 10, timeX + 5, timeY + 10);

	// currentTime = currentTime + 86400000; // one day per frame
	// currentTime = currentTime + 3600000; // one hour per frame
	// currentTime += 1440000; // one day per second at 60 fps
	// currentTime += 10080000; // one week per second at 60 fps
	if (!mouseLocked)
	    currentTime += ANIMATION_RATE * (CurrentFrameTime - LastFrameTime);
    }

    public static void main(String _args[]) {
	PApplet.main(new String[] { "--present", fishtracking.FishTracking.class.getName() });
	//PApplet.main(new String[] {  fishtracking.FishTracking.class.getName() });
    }

    /*
     * A utility function to convert from latitude to Y-coordinate.
     */
    public static float latToY(float latitude, float maxY) {
	return map(latitude, BAY_MODEL_UPPER_LEFT.getLat(), BAY_MODEL_LOWER_RIGHT.getLat(), 0, maxY);
    }

    /*
     * A utility function to convert from longitude to X-coordinate.
     */
    public static float lonToX(float longitude, float maxX) {
	return map(longitude, BAY_MODEL_UPPER_LEFT.getLon(), BAY_MODEL_LOWER_RIGHT.getLon(), 0, maxX);
    }

    private boolean mouseLocked = false;

    public void mousePressed() {
    int xAdjust = (width - PROJECTION_WIDTH)/2;
    
	if (mouseX <= (TIMELINE_RIGHT+xAdjust) && mouseX >= (TIMELINE_LEFT+xAdjust) ) {
	    this.currentTime = (long) map(mouseX, TIMELINE_LEFT+xAdjust, TIMELINE_RIGHT+xAdjust, StartTime, EndTime);
	    mouseLocked = true;
	}
    }

    public void mouseDragged() {
	if (mouseLocked) {
		 int xAdjust = (width - PROJECTION_WIDTH)/2;
		 
	    int cappedMouseX = mouseX;
	    if (cappedMouseX < TIMELINE_LEFT+xAdjust)
		cappedMouseX = TIMELINE_LEFT+xAdjust;
	    else if (cappedMouseX > TIMELINE_RIGHT+xAdjust)
		cappedMouseX = TIMELINE_RIGHT+xAdjust;
	    this.currentTime = (long) map(cappedMouseX, TIMELINE_LEFT+xAdjust, TIMELINE_RIGHT+xAdjust, StartTime, EndTime);
	}
    }

    public void mouseReleased() {
	mouseLocked = false;
    }
}
