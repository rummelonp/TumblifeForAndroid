package jp.mitukiii.tumblife.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TLBrowser
{
  protected String cookie;

  public HttpURLConnection request(String _url, String method, Map<String, String> parameters, Map<String, String> headers)
    throws MalformedURLException, IOException
  {
    HttpURLConnection con = null;

    headers.put("Cookie", cookie);

    con = TLConnection.request(_url, method, parameters, headers);

    Map<String, List<String>> _headers = con.getHeaderFields(); 
    List<String> cookieValues = _headers.get("set-cookie");
    if (cookieValues != null) {
      cookie = null;
      Iterator<String> iterator = cookieValues.iterator();
      while (iterator.hasNext()) {
        String cookieValue = iterator.next();
        if (cookie == null) {
          cookie = cookieValue;
        } else {
          cookie += ";" + cookieValue;
        }
      }
    }

    return con;
  }

  public HttpURLConnection request(String _url, String method, Map<String, String> parameters)
    throws MalformedURLException, IOException
  {
    return request(_url, method, parameters, new HashMap<String, String>());
  }

  public HttpURLConnection request(String _url, String method)
    throws MalformedURLException, IOException
  {
    return request(_url, method, new HashMap<String, String>(), new HashMap<String, String>());
  }

  public HttpURLConnection post(String _url, Map<String, String> parameters, Map<String, String> headers)
    throws MalformedURLException, IOException
  {
    return request(_url, TLConnection.POST, parameters, headers);
  }

  public HttpURLConnection post(String _url, Map<String, String> parameters)
    throws MalformedURLException, IOException
  {
    return request(_url, TLConnection.POST, parameters, new HashMap<String, String>());
  }

  public HttpURLConnection post(String _url)
    throws MalformedURLException, IOException
  {
    return request(_url, TLConnection.POST, new HashMap<String, String>(), new HashMap<String, String>());
  }

  public HttpURLConnection get(String _url, Map<String, String> parameters, Map<String, String> headers)
    throws MalformedURLException, IOException
  {
    return request(_url, TLConnection.GET, parameters, headers);
  }

  public HttpURLConnection get(String _url, Map<String, String> parameters)
    throws MalformedURLException, IOException
  {
    return request(_url, TLConnection.GET, parameters, new HashMap<String, String>());
  }

  public HttpURLConnection get(String _url)
    throws MalformedURLException, IOException
  {
    return request(_url, TLConnection.GET, new HashMap<String, String>(), new HashMap<String, String>());
  }
}
