package jp.mitukiii.tumblife.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jp.mitukiii.tumblife.model.TLPost;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TLPostParser extends TLParser
{
  public TLPostParser(InputStream input)
    throws XmlPullParserException
  {
    super(input);
  }

  public List<TLPost> parse()
    throws NumberFormatException, XmlPullParserException, IOException
  {
    List<TLPost> posts = new ArrayList<TLPost>(50);
    TLPost post = null;
    for (int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT; e = parser.next()) {
      String tag = parser.getName();
      if (e == XmlPullParser.START_TAG) {
        if ("post".equals(tag)) {
          post = new TLPost();
          post.setId(Long.valueOf(parser.getAttributeValue(NAME_SPACE, "id")));
          post.setUrl(parser.getAttributeValue(NAME_SPACE, "url"));
          post.setUrlWithSlug(parser.getAttributeValue(NAME_SPACE, "url-with-slug"));
          post.setType(parser.getAttributeValue(NAME_SPACE, "type"));
          post.setDateGmt(parser.getAttributeValue(NAME_SPACE, "date-gmt"));
          post.setDate(parser.getAttributeValue(NAME_SPACE, "date"));
          post.setUnixTimestamp(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "unix-timestamp")));
          post.setFormat(parser.getAttributeValue(NAME_SPACE, "format"));
          post.setReblogKey(parser.getAttributeValue(NAME_SPACE, "reblog-key"));
          post.setSlug(parser.getAttributeValue(NAME_SPACE, "slug"));
          String noteCount = parser.getAttributeValue(NAME_SPACE, "note-count");
          if (noteCount == null || noteCount.length() == 0) {
            post.setNoteCount(0);
          } else {
            post.setNoteCount(Integer.valueOf(noteCount));
          }
          post.setRebloggedFromUrl(parser.getAttributeValue(NAME_SPACE, "reblogged-from-url"));
          post.setRebloggedFromName(parser.getAttributeValue(NAME_SPACE, "reblogged-from-name"));
          post.setRebloggedFromTitle(parser.getAttributeValue(NAME_SPACE, "reblogged-from-title"));
        } else if ("tumblelog".equals(tag)) {
          post.setTumblelogTitle(parser.getAttributeValue(NAME_SPACE, "title"));
          post.setTumblelogName(parser.getAttributeValue(NAME_SPACE, "name"));
          post.setTumblelogUrl(parser.getAttributeValue(NAME_SPACE, "url"));
          post.setTumblelogTimezone(parser.getAttributeValue(NAME_SPACE, "timezone"));
        } else if ("tag".equals(tag)) {
          post.setTag(parser.nextText());
        } else if ("quote-text".equals(tag)) {
          post.setQuoteText(parser.nextText());
        } else if ("quote-source".equals(tag)) {
          post.setQuoteSource(parser.nextText());
        } else if ("photo-caption".equals(tag)) {
          post.setPhotoCaption(parser.nextText());
        } else if ("photo-link-url".equals(tag)) {
          post.setPhotoLinkUrl(parser.nextText());
        } else if ("photo-url".equals(tag)) {
          String maxWidth = parser.getAttributeValue(NAME_SPACE, "max-width");
          if ("1280".equals(maxWidth)) {
            post.setPhotoUrlMaxWidth1280(parser.nextText());
          } else if ("500".equals(maxWidth)) {
            post.setPhotoUrlMaxWidth500(parser.nextText());
          } else if ("400".equals(maxWidth)) {
            post.setPhotoUrlMaxWidth400(parser.nextText());
          } else if ("250".equals(maxWidth)) {
            post.setPhotoUrlMaxWidth250(parser.nextText());
          } else if ("100".equals(maxWidth)) {
            post.setPhotoUrlMaxWidth100(parser.nextText());
          } else if ("75".equals(maxWidth)) {
            post.setPhotoUrlMaxWidth75(parser.nextText());
          } 
        } else if ("link-text".equals(tag)) {
          post.setLinkText(parser.nextText());
        } else if ("link-url".equals(tag)) {
          post.setLinkUrl(parser.nextText());
        } else if ("link-description".equals(tag)) {
          post.setLinkDescription(parser.nextText());
        } else if ("conversation-title".equals(tag)) {
          post.setConversationTitle(parser.nextText());
        } else if ("conversation-text".equals(tag)) {
          post.setConversationText(parser.nextText());
        } else if ("line".equals(tag)) {
          String beforeText = post.getConversation();
          if (beforeText == null) {
            beforeText = "";
          }
          post.setConversation(beforeText + "<p>" + parser.getAttributeValue(NAME_SPACE, "label") + parser.nextText() + "</p>");
        } else if ("video-caption".equals(tag)) {
          post.setVideoCaption(parser.nextText());
        } else if ("video-source".equals(tag)) {
          post.setVideoSource(parser.nextText());
        } else if ("video-player".equals(tag)) {
          post.setVideoPlayer(parser.nextText());
        } else if ("audio-caption".equals(tag)) {
          post.setAudioCaption(parser.nextText());
        } else if ("audio-player".equals(tag)) {
          post.setAudioPlayer(parser.nextText());
        } else if ("download-url".equals(tag)) {
          post.setDownloadUrl(parser.nextText());
        } else if ("regular-title".equals(tag)) {
          post.setRegularTitle(parser.nextText());
        } else if ("regular-body".equals(tag)) {
          post.setRegularBody(parser.nextText());
        }
      } else if (e == XmlPullParser.END_TAG) {
        if ("post".equals(tag)) {
          posts.add(post);
        }
      }
    }
    return posts;
  }

}
