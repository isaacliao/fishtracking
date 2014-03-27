package fishtracking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogWriter {
	private String logfilePath;
	private File logfile;
	private boolean displayTimestamp = true;
	
	/**
	 * 
	 * @param logfilePath
	 * @param appendDateStamp
	 */
	public LogWriter (String logfilePath, boolean appendDateStamp) {
		if (appendDateStamp) {
			this.logfilePath = appendDateStampToFilename(logfilePath);
		} else {
			this.logfilePath = logfilePath;
		}
		
		init();
	}
	
	public boolean getTimestampDisplay () {
		return displayTimestamp;
	}
	public void setTimestampDisplay (boolean val) {
		displayTimestamp = val;
	}
	
	public void writeToLog (String msg) {
		try {
			FileWriter logWriter = new FileWriter(logfile, true);
			if (displayTimestamp) {
				//logWriter.write("["+ getTimeStamp(" ") +"] "+ msg +"\n");
				logWriter.write(getTimeStamp("\t") + msg +"\n");
			} else {
				logWriter.write(msg +"\n");
			}
			logWriter.flush();
			logWriter.close();
		} catch (IOException e) {
			System.err.println("logfile not accessible or not found at "+ logfilePath +".");
		} 
	}
	
	public void writeExceptionToLog (Exception e) {
		writeExceptionToLog(e, 5, "");
	}
	
	public void writeExceptionToLog (Exception e, int depth) {
		writeExceptionToLog(e, depth, "");
	}
	
	public void writeExceptionToLog (Exception e, int depth, String header) {
		String msg = header +"\n"+ e.toString() +"\n";
		StackTraceElement[] stackTrace = e.getStackTrace();
		depth = Math.min(depth, stackTrace.length);
		for (int i=0; i<depth; i++) {
			msg += "    at "+ stackTrace[i].toString() +"\n";
		}
		
		System.err.println(msg);
		writeToLog(msg);
	}
	
	public static String getTimeStamp (String dateTimeDelimiter) {
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd"+ dateTimeDelimiter +"HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(now);
	}
	
	private static String getDateStamp () {
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(now);
	}
	
	private void init () {
		logfile = new File(logfilePath);
		try {
			logfile.createNewFile();
		} catch (Exception e) {
			System.err.println("logfile not created at "+ logfilePath +".");
			e.printStackTrace();
		}
	}
	
	private String appendDateStampToFilename (String filename) {
		int extensionIndex = filename.indexOf('.');
		if (extensionIndex == -1) {
			return filename +'_'+ getDateStamp();
		}
		
		String stampedName = filename.substring(0, extensionIndex);
		String extension = filename.substring(extensionIndex, filename.length());
		stampedName = stampedName +'_'+ getDateStamp() + extension;
		return stampedName;
	}
}