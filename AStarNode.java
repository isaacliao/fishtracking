package fishtracking;

import java.util.HashSet;

import processing.core.PApplet;

public class AStarNode implements Comparable<AStarNode> {
	public int x, y, g, f;
	public AStarNode parent;
	public boolean closed = false;

	public AStarNode() {
	}

	public AStarNode (Ping ping, int maxX, int maxY) {
		this.x = (int) (FishTracking.lonToX(ping.longitude, maxX));
		this.y = (int) (FishTracking.latToY(ping.latitude, maxY));
		// Cap negative coordinates at 0
		if (this.x < 0)
		    this.x = 0;
		if (this.y < 0)
		    this.y = 0;
		// Cap positive coordinates at xMax and yMax
		if (this.x > maxX)
		    this.x = maxX;
		if (this.y > maxY)
		    this.y = maxY;
	}

	@Override
	public int compareTo(AStarNode arg0) {
		if (this.f < arg0.f)
			return -1;
		else if (this.f > arg0.f)
			return 1;
		else
			return 0;
	}

	public int ManhattanDistanceTo(AStarNode end) {
		return 10*Math.abs(this.x - end.x) + 10*Math.abs(this.y - end.y); 
	}

	public int DistanceToNeighbor(AStarNode neighbor) {
		if (this.x == neighbor.x || this.y == neighbor.y)
			return 10;
		else
			return 14; // diagonal distance to neighbor		
	}

	
	public HashSet<IntPair> getNeighborCoords(int maxX, int maxY) {
		HashSet<IntPair> neighborCoords = new HashSet<IntPair>();
		if (this.x - 1 >= 0) {
			neighborCoords.add(new IntPair(this.x-1, this.y));
			if (this.y-1 >=0)
				neighborCoords.add(new IntPair(this.x-1, this.y-1));
			if (this.y+1 <= maxY)
				neighborCoords.add(new IntPair(this.x-1, this.y+1));				
		}
		if (this.x + 1 <= maxX) {
			neighborCoords.add(new IntPair(this.x+1, this.y));
			if (this.y-1 >=0)
				neighborCoords.add(new IntPair(this.x+1, this.y-1));
			if (this.y+1 <= maxY)
				neighborCoords.add(new IntPair(this.x+1, this.y+1));							
		}
		if (this.y-1 >=0)
			neighborCoords.add(new IntPair(this.x, this.y-1));
		if (this.y+1 <= maxY)
			neighborCoords.add(new IntPair(this.x, this.y+1));				

		return neighborCoords;
	}
	
	public Ping toPing(int maxX, int maxY, long startTime, long endTime, long maxG) {
	    Ping ping = new Ping();
	    ping.longitude = PApplet.map(this.x, 0, maxX, FishTracking.BAY_MODEL_UPPER_LEFT.getLon(), FishTracking.BAY_MODEL_LOWER_RIGHT.getLon());
	    ping.latitude = PApplet.map(this.y, 0, maxY, FishTracking.BAY_MODEL_UPPER_LEFT.getLat(), FishTracking.BAY_MODEL_LOWER_RIGHT.getLat());
	    ping.dateTime = (long) PApplet.map(this.g, 0, maxG, startTime, endTime);
	    return ping;
	}
}