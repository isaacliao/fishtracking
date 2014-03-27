package fishtracking;

import java.io.File;
import java.util.HashMap;

public class LogManager {
	private static String logFolderPath = "";
	
	private static HashMap<String, LogWriter> logWriters = new HashMap<String, LogWriter>();
	
	public static void setLogFolder (String logFolderPath) {
		File logFolder = new File(logFolderPath);
		if (!logFolder.exists()) {
			try {
				logFolder.mkdirs();
			} catch (Exception e) {
				System.err.println("Log folder not present at "+ logFolderPath +", and creation of folder failed.  Logs will be written to application directory.");
				e.printStackTrace();
				return;
			}
		}
		
		LogManager.logFolderPath = logFolderPath;
	}
	
	public static LogWriter addLogWriter (String logName, String logfilePath, boolean appendDateStamp) {
		LogWriter logWriter = new LogWriter(logFolderPath + logfilePath, appendDateStamp);
		logWriters.put(logName, logWriter);
		return logWriter;
	}
	
	public static LogWriter getLogWriter (String logName) {
		return logWriters.get(logName);
	}
	
	public static void removeLogWriter (String logName) {
		logWriters.remove(logName);
	}
	
	public static void writeToLog (String logName, String msg) {
		LogWriter log = logWriters.get(logName);
		if (log == null) {
			System.err.println("No LogWriter exists by the name of "+ logName);
			return;
		}
		
		log.writeToLog(msg);
	}
	
	public static void writeExceptionToLog (String logName, Exception e) {
		LogWriter log = logWriters.get(logName);
		if (log == null) {
			System.err.println("No LogWriter exists by the name of "+ logName);
			return;
		}
		
		log.writeExceptionToLog(e, 5, "");
	}
	
	public static void writeExceptionToLog (String logName, Exception e, int depth) {
		LogWriter log = logWriters.get(logName);
		if (log == null) {
			System.err.println("No LogWriter exists by the name of "+ logName);
			return;
		}
		
		log.writeExceptionToLog(e, depth, "");
	}
	
	public static void writeExceptionToLog (String logName, Exception e, int depth, String header) {
		LogWriter log = logWriters.get(logName);
		if (log == null) {
			System.err.println("No LogWriter exists by the name of "+ logName);
			return;
		}
		
		log.writeExceptionToLog(e, depth, header);
	}
	
	public static void writeEmptyLineToLog (String logName) {
		LogWriter log = logWriters.get(logName);
		if (log == null) {
			System.err.println("No LogWriter exists by the name of "+ logName);
			return;
		}
		
		boolean displayingTimestamp = log.getTimestampDisplay();
		log.setTimestampDisplay(false);
		log.writeToLog("");
		log.setTimestampDisplay(displayingTimestamp);
	}
	
	
	private LogManager () {}
}