package org.jli.loggen;

import java.util.logging.Logger;

public class SummaryKey
  implements Comparable<SummaryKey>
{
  private static final Logger logger = Logger.getLogger(SummaryKey.class.getName());
  private final long windowSeq;
  private final int windowSize;
  private final String acctID;
  private final String eventID;
  private final int bitrate;
  private final String ip;
  
  public SummaryKey(long newWindowSeq, int newWindowSize, String newAcctID, String newEventID, int newBitRate, String newIP)
  {
    this.windowSeq = newWindowSeq;
    this.windowSize = newWindowSize;
    this.acctID = newAcctID;
    this.eventID = newEventID;
    this.bitrate = newBitRate;
    this.ip = newIP;
  }
  
  public SummaryKey(long newWindowSeq, int newWindowSize, String newAcctID, String newEventID, int newBitRate)
  {
    this(newWindowSeq, newWindowSize, newAcctID, newEventID, newBitRate, null);
  }
  
  public int compareTo(SummaryKey rvalue)
  {
    int result = 0;
    if (this.windowSize < rvalue.windowSize)
    {
      result = -1;
    }
    else if (this.windowSize > rvalue.windowSize)
    {
      result = 1;
    }
    else if (this.windowSeq < rvalue.windowSeq)
    {
      result = -1;
    }
    else if (this.windowSeq > rvalue.windowSeq)
    {
      result = 1;
    }
    else
    {
      result = this.acctID.compareTo(rvalue.acctID);
      if (result == 0)
      {
        result = this.eventID.compareTo(rvalue.eventID);
        if (result == 0) {
          if (this.bitrate < rvalue.bitrate) {
            result = -1;
          } else if (this.bitrate > rvalue.bitrate) {
            result = 1;
          } else if ((this.ip != null) && (rvalue.ip != null)) {
            result = this.ip.compareTo(rvalue.ip);
          } else if ((this.ip == null) && (rvalue.ip == null)) {
            result = 0;
          } else if ((this.ip == null) && (rvalue.ip != null)) {
            result = -1;
          } else if ((this.ip != null) && (rvalue.ip == null)) {
            result = 1;
          }
        }
      }
    }
    return result;
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder(256);
    builder.append(this.windowSeq).append(",");
    builder.append(this.windowSize).append(",");
    builder.append(this.acctID).append(",");
    builder.append(this.eventID).append(",");
    builder.append(this.bitrate);
    if (this.ip != null)
    {
      builder.append(",");
      builder.append(this.ip);
    }
    return builder.toString();
  }
}
