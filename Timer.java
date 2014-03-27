package fishtracking;

import processing.core.PApplet;

/**
 * 	Timer
 * 
 *  Timing class which let's you specify a time, in milliseconds. This is event-driven, i.e. needs to be called by the
 *  draw() function rather than an interrupt-driven function
 *  
 */

public class Timer {
	//-------- PUBLIC FUNCTIONS --------/
	public Timer(PApplet _p, long _duration ) { 
		p = _p;
		setTimer(_duration);
	}
	
	public void setTimer(long _duration) { duration = _duration; }
	
	public void start() { 
		startTime = p.millis();
	}
	
	public Boolean expired() {
		return ((startTime + duration) < p.millis());
	}
	
	public float percentage() {
		if( expired() )
			return 1.0f;
		else
			return (float)(p.millis()-startTime)/(float)duration; 
	}
	
	//-------- PRIVATE VARIABLES --------/
	protected PApplet p;
	private long duration;
	protected long startTime = 0;
}
