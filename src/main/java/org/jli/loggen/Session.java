package org.jli.loggen;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Session
{
  private static final int PROCESS_RATE = 12500;
  private static final int HTTP_HEADER_SIZE = 200;
  private static final int manifestSize = 5000;
  private static final char DELIMITER = ' ';
  private static final String ORIGIN_IP = "87.248.208.215";
  private static final String CHUNK_TYPE = "video/MP2T";
  private static final String MANIFEST_TYPE = "application/vnd.apple.mpegurl";
  private static final String CAM_ID = "0";
  private static final String AGENT = "Apple%20Mac%20OS%20X%20v10.6.8%20CoreMedia%20v1.0.0.10K549";
  private static final String PERF_DATA = "r:19,s:19,rtt:179187,rttv:4937";
  private static final String BYTES_READ = "0";
  private static final Map<Integer, Integer> bitRateChunkSize = new HashMap();
  
  static
  {
    bitRateChunkSize.put(Integer.valueOf(200), Integer.valueOf(65536));
    bitRateChunkSize.put(Integer.valueOf(400), Integer.valueOf(131072));
    bitRateChunkSize.put(Integer.valueOf(800), Integer.valueOf(262144));
    bitRateChunkSize.put(Integer.valueOf(1600), Integer.valueOf(524288));
  }
  
  private static final Integer[] bitRates = (Integer[])bitRateChunkSize.keySet().toArray(new Integer[bitRateChunkSize.size()]);
  private static final Random generator = new Random();
  private final String shortName;
  private final String acctID;
  private final String eventName;
  private final String eventUUID;
  private final int totalNumChunks;
  private final String ip;
  private final String sessionID;
  private int currentBitRate;
  private int currentChunk;
  
  public Session(String newShortName, String newAcctID, String newEventName, String newEventUUID, int newTotalNumChunks, String newip, String newSessionID)
  {
    this.shortName = newShortName;
    this.acctID = newAcctID;
    this.eventName = newEventName;
    this.eventUUID = newEventUUID;
    this.totalNumChunks = newTotalNumChunks;
    this.ip = newip;
    this.sessionID = newSessionID;
    
    this.currentBitRate = 800;
    this.currentChunk = 1;
  }
  
  public String getAcctID()
  {
    return this.acctID;
  }
  
  public String getEventUUID()
  {
    return this.eventUUID;
  }
  
  public String getIP()
  {
    return this.ip;
  }
  
  public int getBitRate()
  {
    return this.currentBitRate;
  }
  
  private String getURL(boolean isManifest)
  {
    StringBuilder result = new StringBuilder(256);
    result.append("http://");
    result.append(this.eventName).append(".");
    result.append(this.shortName).append(".");
    if (isManifest)
    {
      result.append("cs.llnwd.net/llnw/demo/demo/");
      result.append("demo_").append(this.currentBitRate).append(".");
      result.append("m3u8");
    }
    else
    {
      result.append("cs.llnwd.net/llnw/llnw_demo/streams/demo/");
      result.append("demo_").append(this.currentBitRate);
      result.append("Num").append(this.currentChunk).append(".");
      result.append("ts");
    }
    return result.toString();
  }
  
  private int getNewBitRate()
  {
    int newBitRate;
    do
    {
      newBitRate = bitRates[generator.nextInt(bitRates.length)].intValue();
    } while (this.currentBitRate == newBitRate);
    return newBitRate;
  }
  
  public String printManifestLogLineForBitRateChange(double time)
  {
    StringBuilder logline = new StringBuilder(256);
    StringBuilder timeAndDuration = new StringBuilder(20);
    Formatter helper = new Formatter(timeAndDuration);
    



    this.currentBitRate = getNewBitRate();
    if (this.currentChunk >= 2) {
      this.currentChunk -= 1;
    } else {
      this.currentChunk = 1;
    }
    int size = 5200;
    int duration = 0;
    helper.format("%14.3f %6d", new Object[] { Double.valueOf(time), Integer.valueOf(duration) });
    String url = getURL(true);
    
    logline.append(timeAndDuration);
    logline.append(' ');
    logline.append(this.ip);
    logline.append(' ');
    logline.append("TCP_MISS/200");
    logline.append(' ');
    logline.append(size);
    logline.append(' ');
    logline.append("GET");
    logline.append(' ');
    logline.append(url);
    logline.append(' ');
    logline.append("-");
    logline.append(' ');
    logline.append("DIRECT/").append("87.248.208.215");
    logline.append(' ');
    logline.append("application/vnd.apple.mpegurl");
    logline.append(' ');
    logline.append("0");
    logline.append(' ');
    logline.append(this.acctID);
    logline.append(' ');
    logline.append("- - -");
    logline.append(' ');
    logline.append("Apple%20Mac%20OS%20X%20v10.6.8%20CoreMedia%20v1.0.0.10K549");
    logline.append(' ');
    logline.append("- -");
    logline.append(' ');
    logline.append(url);
    logline.append(' ');
    logline.append(5000);
    logline.append(' ');
    logline.append("- - - -");
    logline.append(' ');
    logline.append("r:19,s:19,rtt:179187,rttv:4937");
    logline.append(' ');
    logline.append("-");
    logline.append(' ');
    logline.append(size);
    logline.append(' ');
    logline.append("- - - -");
    logline.append(' ');
    logline.append("0");
    
    return logline.toString();
  }
  
  public LoglineAndLogStartTime printLogLine(double time)
  {
    StringBuilder logline = new StringBuilder(256);
    StringBuilder endTime = new StringBuilder(14);
    StringBuilder duration = new StringBuilder(6);
    Formatter endTimeFormatter = new Formatter(endTime);
    Formatter durationFormatter = new Formatter(duration);
    
    int contentLength = ((Integer)bitRateChunkSize.get(Integer.valueOf(this.currentBitRate))).intValue();
    int size = contentLength + 200;
    int dur = contentLength / 12500;
    endTimeFormatter.format("%14.3f", new Object[] { Double.valueOf(time) });
    durationFormatter.format("%6d", new Object[] { Integer.valueOf(dur) });
    String url = getURL(false);
    
    int dotpos = endTime.toString().indexOf(".");
    String logEndTimeString = endTime.toString().substring(0, dotpos).concat(endTime.toString().substring(dotpos + 1));
    long logStartTime = Long.parseLong(logEndTimeString) - dur;
    
    logline.append(endTime);
    logline.append(' ');
    logline.append(duration);
    logline.append(' ');
    logline.append(this.ip);
    logline.append(' ');
    logline.append("TCP_MISS/200");
    logline.append(' ');
    logline.append(size);
    logline.append(' ');
    logline.append("GET");
    logline.append(' ');
    logline.append(url);
    logline.append(' ');
    logline.append("-");
    logline.append(' ');
    logline.append("DIRECT/").append("87.248.208.215");
    logline.append(' ');
    logline.append("video/MP2T");
    logline.append(' ');
    logline.append("0");
    logline.append(' ');
    logline.append(this.acctID);
    logline.append(' ');
    logline.append("- - -");
    logline.append(' ');
    logline.append("Apple%20Mac%20OS%20X%20v10.6.8%20CoreMedia%20v1.0.0.10K549");
    logline.append(' ');
    logline.append("__llnwmfs=").append(this.sessionID);
    logline.append(' ');
    logline.append("-");
    logline.append(' ');
    logline.append(url);
    logline.append(' ');
    logline.append(contentLength);
    logline.append(' ');
    logline.append("- - - -");
    logline.append(' ');
    logline.append("r:19,s:19,rtt:179187,rttv:4937");
    logline.append(' ');
    logline.append("-");
    logline.append(' ');
    logline.append(size);
    logline.append(' ');
    logline.append("- - - -");
    logline.append(' ');
    logline.append("0");
    logline.append(' ');
    logline.append("live_").append(this.eventUUID);
    
    this.currentChunk += 1;
    
    return new LoglineAndLogStartTime(logline.toString(), logStartTime);
  }
  
  public boolean isDone()
  {
    return this.currentChunk >= this.totalNumChunks;
  }
}
