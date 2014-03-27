package fishtracking;

import edu.exploratorium.rotary.RotaryInterface;
import processing.core.PApplet;

/*******************************************************************************************
 * Physical Interface
 * 
 * @author skildall
 *
 ******************************************************************************************
 *
 *	Handles rotary control, and in the future, the button presses.
 *	Relies on the rotary.jar class, created by Eric Socolofsky
 *	The rotary control is a custom keyboard emulator; the rotary interface translates the
 *	keystrokes to speed values; this class will manage these variables and will translate
 *	the speed control to a position values, since we are on left-to-right timeline (for now)
 *
 *	Caller is responsible for calling update() during every draw loop. This will update the
 *	speed value, aggregating it and we will store this as a position — this position is
 *	like a distance value: distance = rate * time; we run an internal millisecond timer to
 *	ensure accuracy in the movement
 *
 *	Button presses use the "!" key
 ********************************************************************************************
 */


public class PhysicalInterface {
	public static int RATE_TIMER_LENGTH = 25;
	public static float SPEED_MULTIPLIER = 20.0f;	// how much we adjust the speed value, i.e. how accurate is the controller
	
	//-- PUBLIC FUNCTIONS
	PhysicalInterface( PApplet _p, int _leftEdge, int _rightEdge ) {
		p = _p;
		leftEdge = _leftEdge;		
		rightEdge = _rightEdge;
		leftRotaryPosition = -(rightEdge-leftEdge)/2;		// for internal tracking of the rotary controller, we go from -width/2 to +width/2
		rightRotaryPosition = (rightEdge-leftEdge)/2;		// e.g. -120 to 120
		
		rotaryInterface = new RotaryInterface(p);
		rateTimer = new Timer(p, RATE_TIMER_LENGTH);
	}
	
	void update() {
		if( rateTimer.expired()) {
			float speed = rotaryInterface.getSpeed();
			if( !outOfBounds(speed*SPEED_MULTIPLIER) )
				adjustCurrentPosition(speed*SPEED_MULTIPLIER);
			
			rateTimer.start();
		}	
	}
	
	//-- position is an integer range from 0 to max range 
	float getPosition() {
		return (PApplet.map(currentPosition, leftRotaryPosition, rightRotaryPosition, leftEdge, rightEdge));
	}
	
	//-- PRIVATE FUNCTIONS
	
	//-- return true if the speed range is out of bounds, i.e. if we spin to the left and the current position is already maxxed out
	private boolean outOfBounds(float speed) {
		if( speed < 0  && currentPosition == leftRotaryPosition )
			return true;
		else if( speed > 0  && currentPosition == rightRotaryPosition )
			return true;
		else
			return false;
	}
	
	//-- adds speed value to the current position value, maxxing out on either side
	private void adjustCurrentPosition(float speed) {
		currentPosition += speed;
		if( currentPosition < leftRotaryPosition )
			currentPosition = leftRotaryPosition; 
		else if( currentPosition > rightRotaryPosition )
			currentPosition = rightRotaryPosition;  
		
	}
	
	//-- PRIVATE VARIABLES
	private	PApplet p;
	private RotaryInterface rotaryInterface;
	private Timer rateTimer;
	private int leftEdge, rightEdge;			// pixel values on the left and right side, reported back to the caller
	float leftRotaryPosition, rightRotaryPosition;
	float aggregateSpeed = 0;
	float currentPosition = 0;
}
