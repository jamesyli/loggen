package org.jli.loggen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionSet
{
  private final Map<String, Session> sessions = new HashMap();
  private final List<String> sessionKeys = new ArrayList();
  private final int capacity;
  
  public SessionSet(int cap)
  {
    this.capacity = cap;
  }
  
  public boolean isFull()
  {
    return this.sessions.size() >= this.capacity;
  }
  
  public boolean isEmpty()
  {
    return this.sessions.size() == 0;
  }
  
  public int size()
  {
    return this.sessions.size();
  }
  
  public Session putSession(String k, Session v)
  {
    this.sessionKeys.add(k);
    return (Session)this.sessions.put(k, v);
  }
  
  public Session getSession(int index)
  {
    return (Session)this.sessions.get(this.sessionKeys.get(index));
  }
  
  public void removeSession(int index)
  {
    String k = (String)this.sessionKeys.get(index);
    this.sessionKeys.remove(index);
    this.sessions.remove(k);
  }
}
