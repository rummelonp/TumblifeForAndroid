package jp.mitukiii.tumblife.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TLConnection
{
  public static String POST = "POST";
  public static String GET  = "GET";

  public static HttpURLConnection request(String _url, String method, Map<String, String> parameters, Map<String, String> headers)
    throws MalformedURLException, IOException
  {
    TLLog.i("TLConnection / request : url / " + _url);

    HttpURLConnection con = null;
    OutputStreamWriter writer = null;

    try {
      con = (HttpURLConnection) new URL(_url).openConnection();
      con.setDoInput(true);
      con.setDoOutput(true);
      con.setRequestMethod(method);

      Iterator<String> hit = headers.keySet().iterator();
      while (hit.hasNext()) {
        String field = (String)hit.next();
        String value = headers.get(field);
        con.setRequestProperty(field, value);
      }

      String parameterString = null;
      Iterator<String> pit = parameters.keySet().iterator();
      while (pit.hasNext()) {
        String parameterKey = (String)pit.next();
        String parameterValue = parameters.get(parameterKey);
        if (parameterString == null) {
          parameterString = "";
        } else {
          parameterString += "&"; 
        }
        parameterString += parameterKey + "=" + URLEncoder.encode(parameterValue);
      }
      if (parameterString != null) {
        writer = new OutputStreamWriter(con.getOutputStream());
        writer.write(parameterString);
        writer.flush();
      }

      con.connect();

      return con;
    } finally {
      try {
        if (writer != null) {
          writer.close();
        }
      } catch (IOException e) {
        TLLog.i("TLConnection / request", e);
      }
    }
  }

  public static HttpURLConnection request(String _url, String method, Map<String, String> parameters)
    throws MalformedURLException, IOException
  {
    return request(_url, method, parameters, new HashMap<String, String>());
  }

  public static HttpURLConnection request(String _url, String method)
    throws MalformedURLException, IOException
  {
    return request(_url, method, new HashMap<String, String>(), new HashMap<String, String>());
  }

  public static HttpURLConnection post(String _url, Map<String, String> parameters, Map<String, String> headers)
    throws MalformedURLException, IOException
  {
    return request(_url, POST, parameters, headers);
  }

  public static HttpURLConnection post(String _url, Map<String, String> parameters)
    throws MalformedURLException, IOException
  {
    return request(_url, POST, parameters, new HashMap<String, String>());
  }

  public static HttpURLConnection post(String _url)
    throws MalformedURLException, IOException
  {
    return request(_url, POST, new HashMap<String, String>(), new HashMap<String, String>());
  }

  public static HttpURLConnection get(String _url, Map<String, String> parameters, Map<String, String> headers)
    throws MalformedURLException, IOException
  {
    return request(_url, GET, parameters, headers);
  }

  public static HttpURLConnection get(String _url, Map<String, String> parameters)
    throws MalformedURLException, IOException
  {
    return request(_url, GET, parameters, new HashMap<String, String>());
  }

  public static HttpURLConnection get(String _url)
    throws MalformedURLException, IOException
  {
    return request(_url, GET, new HashMap<String, String>(), new HashMap<String, String>());
  }
}
