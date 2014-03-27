package fishtracking;

import java.util.HashMap;

public class FishCollection {
    public HashMap<String, Fish> fishes;
    public long startTime, endTime;
    
    public FishCollection () {
	startTime = Long.MAX_VALUE;
	endTime = Long.MIN_VALUE;
	fishes = new HashMap<String, Fish>();
    }
    
    /*
     * Merges another FishCollection's data with this one's.
     */
    public void merge(FishCollection input) {
	if (this.startTime > input.startTime)
	    this.startTime = input.startTime;
	if (this.endTime < input.endTime)
	    this.endTime = input.endTime;
	fishes.putAll(input.fishes);
    }
    /*
     * Merges a PathCollection into the "paths" field of all fishes in this FishCollection.
     */
    public void merge(PathCollection input) {
	for (String id : input.paths.keySet()) {
	    if (this.fishes.containsKey(id)) {
		this.fishes.get(id).path = input.paths.get(id);
	    }
	}
    }
}
