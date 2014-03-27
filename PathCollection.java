package fishtracking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class PathCollection implements Serializable {
    public HashMap<String, ArrayList<Ping>> paths;
    
    PathCollection() {
	paths = new HashMap<String, ArrayList<Ping>>();
    }
}
