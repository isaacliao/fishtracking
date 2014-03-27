package fishtracking;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

/**
 * 	Class Debug
 * 
 *  Simple Debug class. It stores debug strings and will also draw all of them on screen.
 * 
 *  Caller can swap between a debug screen display and standard mode with a keypress in their draw() function, i.e.
 *  	if( bDebug)
 *  		 debug.draw();
 *  	else
 *  		 screen.draw();
 * 
 */

public class Debug {
	//-- this should be defined to fit the screen size. 44 strings will fit in a screen height of 768 px
		public int kNumDebugStrings = 68;
	    
		// file-output using classes: LogManager, LogWriter and SimpleLogger
	    public LogWriter logWriter;
	    
	    Debug(PApplet _p, boolean logFile ) {
	      p = _p;
	      
	      outStrings = new String[kNumDebugStrings];
	      
	      if( logFile ) {
	    	  logWriter = new LogWriter("fishtracker_log" + ".txt", true);
	    	  logWriter.setTimestampDisplay(false);
	      }
	      
	      debugFont = p.createFont("Arial", 12);
	      clear();
	    }
	    
	    //-- returns true if we got a fatal error
	    Boolean fatal() {
	    	return bFatal;
	    }
	    
	    //-- does println and stoes internal strings for drawing
	    void out(String s) {
	      System.out.println(s);
	      outStrings[currentIndex] = s;
	      currentIndex = getNextStringIndex(currentIndex);
	      
	      if(logWriter != null )
	    	  log(s);
	    }
	    
	    void log(String s) {
	    	if( logWriter != null )
	    		logWriter.writeToLog(s);
	    }
	    
	    void fatal(String s) {
	    	bFatal = true;
	    	out(s);
	    }
	    
	    //-- clears all debug strings, also serves as an allocator
	    public void clear() {
	      for( int i = 0; i < outStrings.length; i++ )
	        outStrings[i] = "";
	    }
	      
	  //-- could be used in other contexts, which is why it is a public function
	  public String getString(int index) {
	    return outStrings[index];
	   }
	   
	    //-- draws strings on the screen from first to last
	    public void draw() {
	      p.background(50);  // light gray
	      p.fill( 0,255,0);
	      p.textAlign( PConstants.LEFT );
	      
	      if( debugFont != null )
	    	  p.textFont(debugFont);
	      
	      String s = null;
	      int count = 0;
	      int drawCount = 0;
	      int drawIndex = currentIndex;
	      while(true) {
	        count++;
	        if( count > kNumDebugStrings )
	          break;
	        
	        s = getString(drawIndex);  
	         if( s == null )
	           break;
	         else if( s.equals("") == false ) {
	            p.text( s, 30, 200+ (15*drawCount));
	           drawCount++;  
	           
	         }
	           
	        drawIndex = getNextStringIndex(drawIndex);
	      }
	  }
	  
	    private int getNextStringIndex(int index) {
	      if( index+1 == kNumDebugStrings )
	        return 0;
	      else
	        return index+1;
	    }
	    
	    //----------------- PRIVATE VARIABLES -----------------//
	    private PApplet p;
	    private PFont debugFont;
	    private Boolean bFatal = false;
	    private String [] outStrings;
	    private int currentIndex = 0;   
	}

