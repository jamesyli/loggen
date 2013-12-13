package org.jli.loggen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Properties
{
  private static final Logger classLogger = Logger.getLogger(Properties.class.getName());
  private static final String COMMA = ",";
  private static Map<String, Object> properties = new HashMap();
  
  public static int init(String config)
  {
    LineNumberReader reader;
    try
    {
      reader = new LineNumberReader(new InputStreamReader(new FileInputStream(config)));
    }
    catch (FileNotFoundException e)
    {
      classLogger.log(Level.SEVERE, "Cannot find config file");
      return -1;
    }
    String line = null;
    try
    {
      while ((line = reader.readLine()) != null)
      {
        line = line.trim();
        if ((line.length() != 0) && (line.charAt(0) != '#'))
        {
          int equalSign = line.indexOf('=');
          if (equalSign > 0)
          {
            String key = line.substring(0, equalSign).trim();
            String value = line.substring(equalSign + 1).trim();
            store(key, value);
          }
        }
      }
    }
    catch (IOException e)
    {
      classLogger.log(Level.SEVERE, "IO error when reading config file");
      return -1;
    }
    return 0;
  }
  
  private static void store(String k, String v)
  {
    List<String> newValue = new ArrayList();
    boolean hasComma = false;
    if (v.indexOf(",") > 0)
    {
      hasComma = true;
      for (String s : v.split(",")) {
        newValue.add(s);
      }
    }
    if (properties.containsKey(k))
    {
      Object oldValue = properties.get(k);
      if (!hasComma) {
        newValue.add(v);
      }
      if ((oldValue instanceof String)) {
        newValue.add((String)oldValue);
      } else if ((oldValue instanceof List)) {
        newValue.addAll((List)oldValue);
      }
      properties.put(k, newValue);
    }
    else if (!hasComma)
    {
      properties.put(k, v);
    }
    else
    {
      properties.put(k, newValue);
    }
  }
  
  public static Object getProperty(String k)
  {
    return properties.get(k);
  }
}
