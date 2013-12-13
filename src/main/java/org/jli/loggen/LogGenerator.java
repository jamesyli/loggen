package org.jli.loggen;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class LogGenerator
{
  private static final Random generator = new Random();
  private static final Random timeGenerator = new Random();
  private static final int[] WINDOWS = { 300, 3600 };
  private static final int SESSION_SET_SIZE = 5000;
  private static final int summary_max_size = 8000000;
  private static Map<SummaryKey, Integer> summary = new TreeMap();
  private static List<Session> sessionList = new ArrayList();
  
  private static void putSummaryEntry(Map<SummaryKey, Integer> map, Session session, long logStartTime)
  {
    for (int win : WINDOWS)
    {
      long winSeq = logStartTime / 1000L - logStartTime / 1000L % win;
      SummaryKey[] keys = { new SummaryKey(winSeq, win, session.getAcctID(), session.getEventUUID(), session.getBitRate(), session.getIP()), new SummaryKey(winSeq, win, session.getAcctID(), session.getEventUUID(), session.getBitRate()) };
      for (SummaryKey k : keys)
      {
        Integer value = (Integer)map.get(k);
        if (value == null)
        {
          map.put(k, Integer.valueOf(1));
        }
        else
        {
          Integer newValue = Integer.valueOf(value.intValue() + 1);
          map.put(k, newValue);
        }
      }
    }
  }
  
  private static void printSummary(Writer writer)
    throws IOException
  {
    for (SummaryKey key : summary.keySet())
    {
      StringBuilder builder = new StringBuilder(128);
      builder.append(key).append(",");
      builder.append(summary.get(key));
      writer.write(builder.toString());
      writer.write("\n");
    }
    writer.flush();
  }
  
  public static void main(String[] args)
    throws IOException
  {
    if (Database.init() < 0) {
      System.exit(-1);
    }
    Long sessionCount = Long.valueOf(1L);
    for (int i = 0; i < Database.getNumSessions(); i++)
    {
      String rawSessionID = "session" + sessionCount.toString();
      String sessionID = Database.getSessionID(rawSessionID);
      Long localLong1 = sessionCount;Long localLong2 = sessionCount = Long.valueOf(sessionCount.longValue() + 1L);
      
      String acctID = Database.getAcctID();
      if (acctID == null)
      {
        System.out.println("account ID is null");
        System.exit(-1);
      }
      String shortname = Database.getShortName(acctID);
      if (shortname == null)
      {
        System.out.println("short name is null");
        System.exit(-1);
      }
      Database.Event event = Database.getEvent(shortname);
      if (event == null)
      {
        System.out.println("event is null");
        System.exit(-1);
      }
      String eventName = event.getEventName();
      String eventUUID = event.getEventUUID();
      int totalNumChunks = Database.getNumChunks();
      String ip = Database.getIP();
      if (ip == null)
      {
        System.out.println("ip is null");
        System.exit(-1);
      }
      sessionList.add(new Session(shortname, acctID, eventName, eventUUID, totalNumChunks, ip, sessionID));
    }
    double time = Database.getLogStartTime() / 1000.0D;
    double avgNumChunks = Database.getBaseNumChunks() * 1.1D;
    double mean = Database.getTimeRange() / (avgNumChunks * Database.getNumSessions());
    Writer writer = Database.getLogFileWriter();
    int lineCount = 0;
    int numSessionDone = 0;
    Writer summaryWriter = Database.getSumFileWriter();
    while (sessionList.size() > numSessionDone)
    {
      Collections.shuffle(sessionList);
      numSessionDone = 0;
      for (Session session : sessionList) {
        if (session != null) {
          if (session.isDone())
          {
            numSessionDone++;
          }
          else
          {
            if (generator.nextDouble() < 0.05D)
            {
              writer.write(session.printManifestLogLineForBitRateChange(time));
            }
            else
            {
              LoglineAndLogStartTime result = session.printLogLine(time);
              writer.write(result.getLogline());
              putSummaryEntry(summary, session, result.getLogStartTime());
              if (summary.size() > 8000000)
              {
                printSummary(summaryWriter);
                summary = new TreeMap();
              }
            }
            writer.write("\n");
            lineCount++;
            if (lineCount >= 20000000)
            {
              writer.flush();
              writer.close();
              writer = Database.getLogFileWriter();
              lineCount = 0;
            }
            time += -1.0D * mean * Math.log(timeGenerator.nextDouble());
          }
        }
      }
    }
    writer.flush();
    writer.close();
    printSummary(summaryWriter);
    summaryWriter.close();
  }
}
