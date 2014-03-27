package fishtracking;

import java.io.PrintStream;

/**
 * When all logging for an application will be written to one file,
 * SimpleLogger provides an easier interface for logging than maintaining logs through LogManager.
 * 
 * @author esocolofsky
 */
public class SimpleLogger {
	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int WARN = 2;
	public static final int ERROR = 3;
	public static final int FATAL = 4;
	public static final String[] LEVEL_NAMES = new String[] { "DEBUG", "INFO", "WARN", "ERROR", "FATAL" };
	
	private static int logLevel = DEBUG;
	private static LogWriter logWriter = null;
	private static boolean appendDateStamp;
	private static boolean localEcho = false;
	
	
	public static void init (String logFolder, String logName, boolean appendDateStamp) {
		LogManager.setLogFolder(logFolder);
		logWriter = LogManager.addLogWriter(logName, logName +".txt", true);
		
		// append date and log level stamps locally
		logWriter.setTimestampDisplay(false);
		SimpleLogger.appendDateStamp = appendDateStamp;
	}
	
	
	public static int logLevel () {
		return logLevel;
	}
	public static void logLevel (int val) {
		if (val < DEBUG || val > FATAL) { return; }
		logLevel = val;
	}
	
	public static boolean localEcho () {
		return localEcho;
	}
	public static void localEcho (boolean val) {
		localEcho = val;
	}

	
	public static void log (String message, int level) {
		if (logWriter == null) {
			System.err.println("Initialize SimpleLogger with init() before logging.");
		}
		
		if (level < 0 || level > LEVEL_NAMES.length-1) {
			System.err.println("Invalid value for log level; must be between 0 and "+ (LEVEL_NAMES.length-1) +", inclusive.");
		}
		
		if (level >= logLevel) {
			// append date and log level stamps
			String stamp = "";
			if (appendDateStamp) {
				stamp = "[ "+ LEVEL_NAMES[level] +" "+ LogWriter.getTimeStamp(" ") +" ]\t";
			}
			logWriter.writeToLog(stamp + message);
			
			if (localEcho) {
				PrintStream console = (level >= ERROR) ? System.err : System.out;
				console.println(stamp + message);
			}
		}
	}
	
	public static void log (String message) {
		log(message, 1);
	}
	
	public static void debug (String message) {
		log(message, DEBUG);
	}
	
	public static void info (String message) {
		log(message, INFO);
	}
	
	public static void warn (String message) {
		log(message, WARN);
	}
	
	public static void error (String message) {
		log(message, ERROR);
	}
	
	public static void fatal (String message) {
		log(message, FATAL);
	}
	
	public static void writeExceptionToLog (Exception e) {
		writeExceptionToLog(e, 5, "");
	}
	
	public static void writeExceptionToLog (Exception e, int depth) {
		writeExceptionToLog(e, depth, "");
	}
	
	public static void writeExceptionToLog (Exception e, int depth, String header) {
		if (header == null) {
			header = "";
		}
		header = "[ "+ LEVEL_NAMES[ERROR] +" "+ LogWriter.getTimeStamp(" ") +" ]\t"+ header;
		logWriter.writeExceptionToLog(e, depth, header);
	}
	
	public static void writeEmptyLineToLog () {
		logWriter.writeToLog("");
	}
	
}