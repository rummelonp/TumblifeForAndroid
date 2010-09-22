package jp.mitukiii.tumblife2.parser;

import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

abstract public class TLParser
{
  protected XmlPullParser parser;
  
  public static final String NAME_SPACE = null;
  
  public TLParser(InputStream input)
    throws XmlPullParserException
  {
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    factory.setValidating(false);
    parser = factory.newPullParser();
    parser.setInput(input, null);
  }
}
