package jp.mitukiii.tumblife.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jp.mitukiii.tumblife.model.TLTumblelog;
import jp.mitukiii.tumblife.model.TLUser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TLUserParser extends TLParser
{
  public TLUserParser(InputStream input)
    throws XmlPullParserException
  {
    super(input);
  }
  
  public TLUser parse()
    throws XmlPullParserException, IOException
  {
    TLUser user = new TLUser();
    List<TLTumblelog> tumblelogs = new ArrayList<TLTumblelog>();
    for (int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT; e = parser.next()) {
      if (e == XmlPullParser.START_TAG) {
        String tag = parser.getName();
        if ("user".equals(tag)) {
          user.setDefaultPostFormat(parser.getAttributeValue(NAME_SPACE, "default-post-format"));
          user.setCanUploadAudio("1".equals(parser.getAttributeValue(NAME_SPACE, "can-upload-audio")));
          user.setCanUploadAiff("1".equals(parser.getAttributeValue(NAME_SPACE, "can-upload-aiff")));
          user.setCanAskQuestion("1".equals(parser.getAttributeValue(NAME_SPACE, "can-ask-question")));
          user.setCanUploadVideo("1".equals(parser.getAttributeValue(NAME_SPACE, "can-upload-video")));
          user.setMaxVideoBytesUploaded(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "max-video-bytes-uploaded")));
          user.setLikedPostCount(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "liked-post-count")));
        } else if ("tumblelog".equals(tag)) {
          TLTumblelog tumblelog = new TLTumblelog();
          tumblelog.setTitle(parser.getAttributeValue(NAME_SPACE, "title"));
          tumblelog.setAdmin("1".equals(parser.getAttributeValue(NAME_SPACE, "is-admin")));
          tumblelog.setPosts(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "posts")));
          tumblelog.setTwitterEnabled("1".equals(parser.getAttributeValue(NAME_SPACE, "twitter-enabled")));
          tumblelog.setDraftCount(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "draft-count")));
          tumblelog.setMessagesCount(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "messages-count")));
          tumblelog.setQueueCount(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "queue-count")));
          tumblelog.setName(parser.getAttributeValue(NAME_SPACE, "name"));
          tumblelog.setUrl(parser.getAttributeValue(NAME_SPACE, "url"));
          tumblelog.setType(parser.getAttributeValue(NAME_SPACE, "type"));
          tumblelog.setFollowers(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "followers")));
          tumblelog.setAvatarUrl(parser.getAttributeValue(NAME_SPACE, "avatar-url"));
          tumblelog.setPrimary("yes".equals(parser.getAttributeValue(NAME_SPACE, "is-primary")));
          tumblelog.setBackupPostLimit(Integer.valueOf(parser.getAttributeValue(NAME_SPACE, "backup-post-limit")));
          tumblelogs.add(tumblelog);
        }
      }
    }
    user.setTumblelogs(tumblelogs);
    return user;
  }
}
