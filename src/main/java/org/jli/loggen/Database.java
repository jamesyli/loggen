package org.jli.loggen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Database
{
  private static final int DEFAULT_NUM_ACCOUNT = 1;
  private static final int DEFAULT_NUM_EVENT = 1;
  private static final int DEFAULT_NUM_SESSIONS = 5;
  private static final int DEFAULT_BASE_NUM_CHUNKS = 25;
  private static final String DEFAULT_START_TIME = "20120101T000000.000Z";
  private static final String DEFAULT_DURATION_TIME = "1h";
  private static final String DEFAULT_OUTPUT_LOGFILE = "hls-gen.log";
  private static final String DEFAULT_OUTPUT_SUMMARY = "hls-gen.sum";
  public static final int DENOMINATOR = 5;
  private static final Random generator = new Random();
  private static final Map<String, String> acctMap = new HashMap();
  private static final Map<String, ArrayList<Event>> eventMap = new HashMap();
  private static final DateTimeFormatter isoFormatter = ISODateTimeFormat.basicDateTime().withZoneUTC();
  private static String[] acctIDs;
  private static int baseNumChunks = 25;
  private static int logfileCount = 1;
  private static boolean getPrivateIP = true;
  private static final String[] static_addrs = { "208.48.140.16", "198.103.238.30", "125.76.253.168", "129.206.13.27", "163.1.60.42", "130.102.6.192", "200.234.196.157" };
  private static final List<String> ipaddrs = new ArrayList();
  private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  
  public static int init()
  {
    String configFile = System.getProperty("llnw.loggen.conf", "lg.conf");
    if (Properties.init(configFile) < 0) {
      return -1;
    }
    int numAccounts = 1;
    if (Properties.getProperty("lg.num.account") != null) {
      numAccounts = Integer.parseInt((String)Properties.getProperty("lg.num.account"));
    }
    List<Integer> numEvents = new ArrayList();
    if (Properties.getProperty("lg.num.event") != null)
    {
      Object rawNumEvents = Properties.getProperty("lg.num.event");
      if ((rawNumEvents instanceof String)) {
        numEvents.add(Integer.valueOf(Integer.parseInt((String)rawNumEvents)));
      } else if ((rawNumEvents instanceof List)) {
        for (String s : (List<String>)rawNumEvents) {
          numEvents.add(Integer.valueOf(Integer.parseInt(s)));
        }
      }
    }
    else
    {
      numEvents.add(Integer.valueOf(1));
    }
    for (int i = 0; i < numAccounts; i++)
    {
      String shortName = "account" + i;
      acctMap.put(Integer.valueOf(i).toString(), shortName);
      ArrayList<Event> events = new ArrayList();
      int n = (i < numEvents.size() ? (Integer)numEvents.get(i) : (Integer)numEvents.get(numEvents.size() - 1)).intValue();
      for (int j = 0; j < n; j++)
      {
        String name = "event" + j;
        events.add(new Event(name, getEventUUID(name)));
      }
      eventMap.put(shortName, events);
    }
    acctIDs = (String[])acctMap.keySet().toArray(new String[acctMap.size()]);
    
    Object rawNumChunks = Properties.getProperty("lg.basenum.chunk");
    if (rawNumChunks != null) {
      baseNumChunks = Integer.parseInt((String)rawNumChunks);
    }
    int numIP = getNumSessions() / 20;
    for (String ip : static_addrs)
    {
      ipaddrs.add(ip);
      if (ipaddrs.size() >= numIP) {
        break;
      }
    }
    BufferedReader reader = getFileReader();
    if (reader != null) {
      while (ipaddrs.size() < numIP) {
        try
        {
          String line = reader.readLine();
          if (line != null)
          {
            ipaddrs.add(line);
          }
          else
          {
            reader.close();
            break;
          }
        }
        catch (IOException e) {}
      }
    }
    System.out.println(acctMap.toString());
    System.out.println(eventMap.toString());
    return 0;
  }
  
  public static long getLogStartTime()
  {
    String startTime = "20120101T000000.000Z";
    Object rawStartTime = Properties.getProperty("lg.time.start");
    if (rawStartTime != null) {
      startTime = (String)rawStartTime;
    }
    return isoFormatter.parseMillis(startTime);
  }
  
  public static double getTimeRange()
  {
    String timeRange = "1h";
    Object rawRange = Properties.getProperty("lg.time.duration");
    if (rawRange != null) {
      timeRange = (String)rawRange;
    }
    double result = 0.0D;
    if (timeRange.indexOf("h") > 0) {
      result += 3600.0D * computeDayOrHourValue(timeRange.substring(0, timeRange.indexOf("h")));
    }
    if (timeRange.indexOf("d") > 0) {
      result += 86400.0D * computeDayOrHourValue(timeRange.substring(0, timeRange.indexOf("d")));
    }
    System.out.format("get time range <%f>%n", new Object[] { Double.valueOf(result) });
    return result;
  }
  
  private static double computeDayOrHourValue(String timeString)
  {
    int p = 0;
    double result = 0.0D;
    int index = timeString.length() - 1;
    while ((index >= 0) && (Character.isDigit(timeString.charAt(index))))
    {
      result += Math.pow(10.0D, p) * Character.getNumericValue(timeString.charAt(index));
      p++;
      index--;
    }
    return result;
  }
  
  public static int getBaseNumChunks()
  {
    return baseNumChunks;
  }
  
  public static int getNumChunks()
  {
    return baseNumChunks + generator.nextInt(baseNumChunks / 5);
  }
  
  public static int getNumSessions()
  {
    Object rawNumSessions = Properties.getProperty("lg.num.session");
    int numSessions = 5;
    if (rawNumSessions != null) {
      numSessions = Integer.parseInt((String)rawNumSessions);
    }
    return numSessions;
  }
  
  public static Writer getLogFileWriter()
    throws IOException
  {
    Object rawName = Properties.getProperty("lg.output.logfile");
    String name = rawName == null ? "hls-gen.log" : (String)rawName;
    FileWriter file = new FileWriter(name + "." + logfileCount);
    logfileCount += 1;
    return new BufferedWriter(file);
  }
  
  public static Writer getSumFileWriter()
    throws IOException
  {
    Object rawName = Properties.getProperty("lg.output.summary");
    String name = rawName == null ? "hls-gen.sum" : (String)rawName;
    FileWriter file = new FileWriter(name);
    return new BufferedWriter(file);
  }
  
  private static BufferedReader getFileReader()
  {
    BufferedReader result = null;
    String name = getIpDb();
    if (name != null) {
      try
      {
        FileReader file = new FileReader(name);
        result = new BufferedReader(file);
      }
      catch (FileNotFoundException e) {}
    }
    return result;
  }
  
  private static String getIpDb()
  {
    String name = null;
    Object rawName = Properties.getProperty("lg.ipdb.file");
    if (rawName != null) {
      name = (String)rawName;
    }
    return name;
  }
  
  private static MessageDigest getMD()
  {
    MessageDigest md = null;
    try
    {
      md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e)
    {
      System.out.println("No MD5 hash algo.");
      System.exit(-1);
    }
    return md;
  }
  
  private static String toHexString(byte[] hash)
  {
    char[] buf = new char[hash.length * 2];
    int i = 0;
    for (int x = 0; i < hash.length; i++)
    {
      buf[(x++)] = HEX_CHARS[(hash[i] >>> 4 & 0xF)];
      buf[(x++)] = HEX_CHARS[(hash[i] & 0xF)];
    }
    return new String(buf);
  }
  
  private static String getEventUUID(String name)
  {
    StringBuilder builder = new StringBuilder(36);
    MessageDigest md = getMD();
    String s = toHexString(md.digest(name.getBytes()));
    builder.append(s.substring(0, 8)).append("-");
    builder.append(s.substring(8, 12)).append("-");
    builder.append(s.substring(12, 16)).append("-");
    builder.append(s.substring(16, 20)).append("-");
    builder.append(s.substring(20));
    return builder.toString();
  }
  
  public static String getSessionID(String raw)
  {
    MessageDigest md = getMD();
    return toHexString(md.digest(raw.getBytes()));
  }
  
  public static String getIP()
  {
    if (getPrivateIP)
    {
      getPrivateIP = false;
      return "10.0.0.1";
    }
    return (String)ipaddrs.get(generator.nextInt(ipaddrs.size()));
  }
  
  public static String getAcctID()
  {
    return acctIDs[generator.nextInt(acctIDs.length)];
  }
  
  public static String getShortName(String acctID)
  {
    return (String)acctMap.get(acctID);
  }
  
  public static Event getEvent(String shortname)
  {
    ArrayList<Event> events = (ArrayList)eventMap.get(shortname);
    return (Event)events.get(generator.nextInt(events.size()));
  }
  
  public static final class Event
  {
    private final String eventName;
    private final String eventUUID;
    
    public Event(String newEventName, String newEventUUID)
    {
      this.eventName = newEventName;
      this.eventUUID = newEventUUID;
    }
    
    public String getEventName()
    {
      return this.eventName;
    }
    
    public String getEventUUID()
    {
      return this.eventUUID;
    }
    
    public String toString()
    {
      return this.eventName;
    }
  }
}
