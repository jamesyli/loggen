package org.jli.loggen;

public final class LoglineAndLogStartTime
{
  private final String logline;
  private final long logStartTime;
  
  public LoglineAndLogStartTime(String line, long start)
  {
    this.logline = line;
    this.logStartTime = start;
  }
  
  public String getLogline()
  {
    return this.logline;
  }
  
  public long getLogStartTime()
  {
    return this.logStartTime;
  }
}
