package fishtracking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

import com.google.gson.Gson;

import processing.core.PApplet;
import processing.core.PImage;

public class PathFinder {
    // private static final String[] FILENAMES = { "chinook", "chinook2", "sturgeon" };
  //  private static final String[] FILENAMES = { "chinook2" };
	  private static final String[] FILENAMES = { "seaturtles" };
    private static final String OBSTACLE_IMAGE_PATH = "data/obstacleMap.png";
    private static final int WIDTH = 1500;
    private static final int FISH_PER_BATCH = 1000;

    private static int BLACK_INT;

    /*
     * Calculates the entire path that a Fish will take and returns it. Assumes that the pings of the input Fish are
     * populated.
     * 
     * PImage obstacleImage: a bitmask showing where obstacles to be avoided are located. Height and width must be
     * integer factors of screen size
     */
    public static ArrayList<Ping> calculatePath(Fish fish, PImage obstacleImage) {
	ArrayList<Ping> pings = fish.pings;
	ArrayList<Ping> path = new ArrayList<Ping>();
	int maxX = obstacleImage.width - 1;
	int maxY = obstacleImage.height - 1;
	// Calculate additional path points between each ping
	for (int i = 0; i < pings.size() - 1; i++) {
	    Ping startPing = pings.get(i);
	    Ping endPing = pings.get(i + 1);

	    // Add starting location to path
	    // this.path.add(startPing);

	    // Use A* to calculate optimal path between Ping i and Ping i+1.
	    // Black pixels in obstacleImage denote obstacles.
	    // Credit to: http://www.policyalmanac.org/games/aStarTutorial.htm
	    // and http://en.wikipedia.org/wiki/A_star (roughly follows
	    // pseudocode)

	    // Create start and end nodes
	    AStarNode startNode = new AStarNode(startPing, maxX, maxY);
	    AStarNode endNode = new AStarNode(endPing, maxX, maxY);

	    // Check if start or end nodes are completely enclosed
	    if (obstacleImage.get(startNode.x+1, startNode.y) == BLACK_INT
		    && obstacleImage.get(startNode.x-1, startNode.y) == BLACK_INT
		    && obstacleImage.get(startNode.x, startNode.y+1) == BLACK_INT
		    && obstacleImage.get(startNode.x, startNode.y-1) == BLACK_INT
		    && obstacleImage.get(startNode.x+1, startNode.y+1) == BLACK_INT
		    && obstacleImage.get(startNode.x+1, startNode.y-1) == BLACK_INT
		    && obstacleImage.get(startNode.x-1, startNode.y+1) == BLACK_INT
		    && obstacleImage.get(startNode.x-1, startNode.y-1) == BLACK_INT)
		System.out.println("Warning: start node completely enclosed");

	    if (obstacleImage.get(endNode.x+1, endNode.y) == BLACK_INT
		    && obstacleImage.get(endNode.x-1, endNode.y) == BLACK_INT
		    && obstacleImage.get(endNode.x, endNode.y+1) == BLACK_INT
		    && obstacleImage.get(endNode.x, endNode.y-1) == BLACK_INT
		    && obstacleImage.get(endNode.x+1, endNode.y+1) == BLACK_INT
		    && obstacleImage.get(endNode.x+1, endNode.y-1) == BLACK_INT
		    && obstacleImage.get(endNode.x-1, endNode.y+1) == BLACK_INT
		    && obstacleImage.get(endNode.x-1, endNode.y-1) == BLACK_INT)
		System.out.println("Warning: end node completely enclosed");
	    
	    // If start and end nodes are adjacent, don't need to do pathfinding
	    if (Math.abs(startNode.x - endNode.x) <= 1 && Math.abs(startNode.y - endNode.y) <= 1) {
		path.add(startPing);
		path.add(endPing);
		continue;
	    }

	    PriorityQueue<AStarNode> openSet = new PriorityQueue<AStarNode>();
	    AStarNode grid[][] = new AStarNode[obstacleImage.width][obstacleImage.height];

	    // Start with start node in open set
	    startNode.g = 0;
	    startNode.f = startNode.ManhattanDistanceTo(endNode);
	    openSet.add(startNode);
	    grid[startNode.x][startNode.y] = startNode;

	    // Iterate over open set
	    boolean pathFound = false;
	    while (!openSet.isEmpty()) {
		AStarNode currentNode = openSet.poll();
		if (currentNode.x == endNode.x && currentNode.y == endNode.y) {
		    endNode = currentNode;
		    pathFound = true;
		    break; // We're done!
		}
		currentNode.closed = true;
		// Iterate over neighbors, creating if they don't exist
		for (IntPair neighborCoord : currentNode.getNeighborCoords(maxX, maxY)) {
		    if (obstacleImage.get(neighborCoord.x, neighborCoord.y) == BLACK_INT
			    && (neighborCoord.x != endNode.x || neighborCoord.y != endNode.y)) {
			continue; // skip this neighbor since it's an obstacle, and not the end node
		    }
		    AStarNode neighbor;
		    if (grid[neighborCoord.x][neighborCoord.y] == null) {
			neighbor = new AStarNode();
			neighbor.x = neighborCoord.x;
			neighbor.y = neighborCoord.y;
			neighbor.g = currentNode.g + currentNode.DistanceToNeighbor(neighbor);
			grid[neighborCoord.x][neighborCoord.y] = neighbor;
		    } else {
			neighbor = grid[neighborCoord.x][neighborCoord.y];
		    }
		    int tentative_g = currentNode.g + currentNode.DistanceToNeighbor(neighbor);
		    if (neighbor.closed && tentative_g >= neighbor.g) {
			continue; // We've already evaluated this neighbor and the path is not better
		    }
		    if (!neighbor.closed || tentative_g < neighbor.g) {
			// We haven't evaluated this neighbor yet, or we've found a better path
			neighbor.parent = currentNode;
			neighbor.g = tentative_g;
			neighbor.f = neighbor.g + neighbor.ManhattanDistanceTo(endNode);
			if (!openSet.contains(neighbor)) {
			    openSet.add(neighbor);
			}
		    }
		}
	    }
	    if (!pathFound) {
		path.add(startPing);
		path.add(endPing);
		continue; // No path found, just let fish move from start to end
	    }

	    // Reconstruct the path, following parents from the end node to the start node
	    Stack<AStarNode> sequence = new Stack<AStarNode>();
	    AStarNode currentNode = endNode;
	    while (currentNode != startNode) {
		sequence.push(currentNode);
		currentNode = currentNode.parent;
	    }
	    sequence.push(startNode);

	    // TODO: Smooth the path

	    // Convert path to pings, can use g-score to estimate timestamp
	    // Add pings to path
	    while (!sequence.isEmpty()) {
		currentNode = sequence.pop();
		Ping ping = currentNode.toPing(maxX, maxY, startPing.dateTime, endPing.dateTime, endNode.g);
		path.add(ping);
	    }
	}

	// Add last ping to end of path
	// this.path.add(this.pings.get(pings.size() - 1));
	return path;
    }

    public static void main(String _args[]) {
	for (String filename : FILENAMES) {
	    Gson gson = new Gson();
	    Path inFilePath = FileSystems.getDefault().getPath("data/" + filename + ".json");
	    BufferedReader inFile = null;
	    try {
		inFile = Files.newBufferedReader(inFilePath, Charset.defaultCharset());
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    FishCollection fishCollection = gson.fromJson(inFile, FishCollection.class);
	    // FishCollection fishCollection = FishTracking.loadFishData("data/" + INPUT_FILENAME + ".json", 0);
	    PApplet pApplet = new PApplet();

	    BLACK_INT = pApplet.color(0);
	    PImage obstacleImage = pApplet.loadImage(OBSTACLE_IMAGE_PATH);
	    obstacleImage.loadPixels();
	    PathCollection pathCollection = new PathCollection();
	    int fishCounter = 0;
	    int fileCounter = 0;
	    for (String id : fishCollection.fishes.keySet()) {
		fishCounter++;
		System.out.println("Calculating paths, " + (float) fishCounter / (float) fishCollection.fishes.size()
			* 100f + "%");
		Fish fish = fishCollection.fishes.get(id);
		ArrayList<Ping> path = calculatePath(fish, obstacleImage);
		pathCollection.paths.put(id, path);
		if (fishCounter % FISH_PER_BATCH == 0 && fishCounter > 0) {
		    fileCounter++;
		    writePaths(pathCollection, "data/" + filename + "paths" + fileCounter + ".ser");
		    pathCollection = new PathCollection();
		}
	    }
	    // String json = gson.toJson(pathCollection);\
	    fileCounter++;
	    writePaths(pathCollection, "data/" + filename + "paths" + fileCounter + ".ser");
	}
	System.out.println("Done!");
    }

    private static void writePaths(PathCollection pathCollection, String fileName) {
	try {
	    System.out.println("Writing to output file " + fileName + "...");

	    long start = System.currentTimeMillis();
	    FileOutputStream fos = new FileOutputStream(fileName);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(pathCollection);
	    oos.close();
	    long end = System.currentTimeMillis();
	    long seconds = (end - start) / 1000;
	    System.out.println("Writing to file took " + seconds + " seconds.");
	} catch (Exception e) {
	    System.out.println("Exception trying to write to output file: " + e.toString());
	}
    }

    private static void writePathsUsingGson(PathCollection pathCollection, String fileName) {
	Gson gson = new Gson();
	System.out.println("Writing to output file...");
	Path outFilePath = FileSystems.getDefault().getPath(fileName);
	try {
	    long start = System.currentTimeMillis();
	    BufferedWriter outFile = Files.newBufferedWriter(outFilePath, Charset.defaultCharset());
	    long end = System.currentTimeMillis();
	    long seconds = (end - start) / 1000;

	    System.out.println("Creating file took " + seconds + " seconds.");

	    start = System.currentTimeMillis();
	    String outJson = gson.toJson(pathCollection);
	    end = System.currentTimeMillis();
	    seconds = (end - start) / 1000;

	    System.out.println("Converting object to JSON string took " + seconds + " seconds.");

	    start = System.currentTimeMillis();
	    outFile.write(outJson, 0, outJson.length());
	    end = System.currentTimeMillis();
	    seconds = (end - start) / 1000;

	    System.out.println("Writing to file took " + seconds + " seconds.");

	    start = System.currentTimeMillis();
	    outFile.close();
	    end = System.currentTimeMillis();
	    seconds = (end - start) / 1000;

	    System.out.println("Closing file took " + seconds + " seconds.");
	} catch (Exception e) {
	    System.out.println("Exception trying to write to output file");
	}
    }
}
