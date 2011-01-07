package jp.mitukiii.tumblife.model;

import jp.mitukiii.tumblife.util.TLExplorer;
import jp.mitukiii.tumblife.util.TLLog;

public class TLPost extends TLModel
{
  public static final String TYPE_QUOTE   = "quote";
  public static final String TYPE_PHOTO   = "photo";
  public static final String TYPE_REGULAR = "regular";
  public static final String TYPE_LINK    = "link";
  public static final String TYPE_VIDEO   = "video";
  public static final String TYPE_AUDIO   = "audio";
  public static final String TYPE_CHAT    = "conversation";

  protected long             id;
  protected String           url;
  protected String           urlWithSlug;
  protected String           type;
  protected String           dateGmt;
  protected String           date;
  protected int              unixTimestamp;
  protected String           format;
  protected String           reblogKey;
  protected String           slug;
  protected int              noteCount;
  protected String           tumblelogTitle;
  protected String           tumblelogName;
  protected String           tumblelogUrl;
  protected String           tumblelogTimezone;
  protected String           tag;
  protected String           quoteText;
  protected String           quoteSource;
  protected String           photoCaption;
  protected String           photoLinkUrl;
  protected String           photoUrlMaxWidth1280;
  protected String           photoUrlMaxWidth500;
  protected String           photoUrlMaxWidth400;
  protected String           photoUrlMaxWidth250;
  protected String           photoUrlMaxWidth100;
  protected String           photoUrlMaxWidth75;
  protected String           linkText;
  protected String           linkUrl;
  protected String           linkDescription;
  protected String           conversationTitle;
  protected String           conversationText;
  protected String           conversation;
  protected String           videoCaption;
  protected String           videoSource;
  protected String           videoPlayer;
  protected String           audioCaption;
  protected String           audioPlayer;
  protected String           downloadUrl;
  protected String           regularTitle;
  protected String           regularBody;

  protected int              index;
  protected String           fileName;
  protected String           fileUrl;
  protected boolean          isPhoto;
  protected String           imageFileName;
  protected String           imageFileUrl;
  protected String           html;

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrlWithSlug()
  {
    return urlWithSlug;
  }

  public void setUrlWithSlug(String urlWithSlug)
  {
    this.urlWithSlug = urlWithSlug;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    setPhoto(TYPE_PHOTO.equals(type));
    this.type = type;
  }

  public String getDateGmt()
  {
    return dateGmt;
  }

  public void setDateGmt(String dateGmt)
  {
    this.dateGmt = dateGmt;
  }

  public String getDate()
  {
    return date;
  }

  public void setDate(String date)
  {
    this.date = date;
  }

  public int getUnixTimestamp()
  {
    return unixTimestamp;
  }

  public void setUnixTimestamp(int unixTimestamp)
  {
    this.unixTimestamp = unixTimestamp;
  }

  public String getFormat()
  {
    return format;
  }

  public void setFormat(String format)
  {
    this.format = format;
  }

  public String getReblogKey()
  {
    return reblogKey;
  }

  public void setReblogKey(String reblogKey)
  {
    this.reblogKey = reblogKey;
  }

  public String getSlug()
  {
    return slug;
  }

  public void setSlug(String slug)
  {
    this.slug = slug;
  }

  public int getNoteCount()
  {
    return noteCount;
  }

  public void setNoteCount(int noteCount)
  {
    this.noteCount = noteCount;
  }

  public String getTumblelogTitle()
  {
    return tumblelogTitle;
  }

  public void setTumblelogTitle(String tumblelogTitle)
  {
    this.tumblelogTitle = tumblelogTitle;
  }

  public String getTumblelogName()
  {
    return tumblelogName;
  }

  public void setTumblelogName(String tumblelogName)
  {
    this.tumblelogName = tumblelogName;
  }

  public String getTumblelogUrl()
  {
    return tumblelogUrl;
  }

  public void setTumblelogUrl(String tumblelogUrl)
  {
    this.tumblelogUrl = tumblelogUrl;
  }

  public String getTumblelogTimezone()
  {
    return tumblelogTimezone;
  }

  public void setTumblelogTimezone(String tumblelogTimezone)
  {
    this.tumblelogTimezone = tumblelogTimezone;
  }

  public String getTag()
  {
    return tag;
  }

  public void setTag(String tag)
  {
    this.tag = tag;
  }

  public String getQuoteText()
  {
    return quoteText;
  }

  public void setQuoteText(String quoteText)
  {
    this.quoteText = quoteText;
  }

  public String getQuoteSource()
  {
    return quoteSource;
  }

  public void setQuoteSource(String quoteSource)
  {
    this.quoteSource = quoteSource;
  }

  public String getPhotoCaption()
  {
    return photoCaption;
  }

  public void setPhotoCaption(String photoCaption)
  {
    this.photoCaption = photoCaption;
  }

  public String getPhotoLinkUrl()
  {
    return photoLinkUrl;
  }

  public void setPhotoLinkUrl(String photoLinkUrl)
  {
    this.photoLinkUrl = photoLinkUrl;
  }

  public String getPhotoUrlMaxWidth1280()
  {
    return photoUrlMaxWidth1280;
  }

  public void setPhotoUrlMaxWidth1280(String photoUrlMaxWidth1280)
  {
    this.photoUrlMaxWidth1280 = photoUrlMaxWidth1280;
  }

  public String getPhotoUrlMaxWidth500()
  {
    return photoUrlMaxWidth500;
  }

  public void setPhotoUrlMaxWidth500(String photoUrlMaxWidth500)
  {
    this.photoUrlMaxWidth500 = photoUrlMaxWidth500;
  }

  public String getPhotoUrlMaxWidth400()
  {
    return photoUrlMaxWidth400;
  }

  public void setPhotoUrlMaxWidth400(String photoUrlMaxWidth400)
  {
    this.photoUrlMaxWidth400 = photoUrlMaxWidth400;
  }

  public String getPhotoUrlMaxWidth250()
  {
    return photoUrlMaxWidth250;
  }

  public void setPhotoUrlMaxWidth250(String photoUrlMaxWidth250)
  {
    this.photoUrlMaxWidth250 = photoUrlMaxWidth250;
  }

  public String getPhotoUrlMaxWidth100()
  {
    return photoUrlMaxWidth100;
  }

  public void setPhotoUrlMaxWidth100(String photoUrlMaxWidth100)
  {
    this.photoUrlMaxWidth100 = photoUrlMaxWidth100;
  }

  public String getPhotoUrlMaxWidth75()
  {
    return photoUrlMaxWidth75;
  }

  public void setPhotoUrlMaxWidth75(String photoUrlMaxWidth75)
  {
    this.photoUrlMaxWidth75 = photoUrlMaxWidth75;
  }

  public String getLinkText()
  {
    return linkText;
  }

  public void setLinkText(String linkText)
  {
    this.linkText = linkText;
  }

  public String getLinkUrl()
  {
    return linkUrl;
  }

  public void setLinkUrl(String linkUrl)
  {
    this.linkUrl = linkUrl;
  }

  public String getLinkDescription()
  {
    return linkDescription;
  }

  public void setLinkDescription(String linkDescription)
  {
    this.linkDescription = linkDescription;
  }

  public String getConversationTitle()
  {
    return conversationTitle;
  }

  public void setConversationTitle(String conversationTitle)
  {
    this.conversationTitle = conversationTitle;
  }

  public String getConversationText()
  {
    return conversationText;
  }

  public void setConversationText(String conversationText)
  {
    this.conversationText = conversationText;
  }

  public String getConversation()
  {
    return conversation;
  }

  public void setConversation(String conversation)
  {
    this.conversation = conversation;
  }

  public String getVideoCaption()
  {
    return videoCaption;
  }

  public void setVideoCaption(String videoCaption)
  {
    this.videoCaption = videoCaption;
  }

  public String getVideoSource()
  {
    return videoSource;
  }

  public void setVideoSource(String videoSource)
  {
    this.videoSource = videoSource;
  }

  public String getVideoPlayer()
  {
    return videoPlayer;
  }

  public void setVideoPlayer(String videoPlayer)
  {
    this.videoPlayer = videoPlayer;
  }

  public String getAudioCaption()
  {
    return audioCaption;
  }

  public void setAudioCaption(String audioCaption)
  {
    this.audioCaption = audioCaption;
  }

  public String getAudioPlayer()
  {
    return audioPlayer;
  }

  public void setAudioPlayer(String audioPlayer)
  {
    this.audioPlayer = audioPlayer;
  }

  public String getDownloadUrl()
  {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl)
  {
    this.downloadUrl = downloadUrl;
  }
  public String getRegularTitle()
  {
    return regularTitle;
  }

  public void setRegularTitle(String regularTitle)
  {
    this.regularTitle = regularTitle;
  }

  public String getRegularBody()
  {
    return regularBody;
  }

  public void setRegularBody(String regularBody)
  {
    this.regularBody = regularBody;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public String getFileName()
  {
    if (fileName == null) {
      fileName = getId() + "." + TLExplorer.HTML_EXTENSION;
    }
    return fileName;
  }

  public String getFileUrl()
  {
    return fileUrl;
  }

  public void setFileUrl(String fileUrl)
  {
    this.fileUrl = fileUrl;
  }

  public boolean isPhoto()
  {
    return isPhoto;
  }

  public void setPhoto(boolean isPhoto)
  {
    this.isPhoto = isPhoto;
  }

  public String getImageFileUrl()
  {
    return imageFileUrl;
  }

  public void setImageFileUrl(String imageFileUrl)
  {
    this.imageFileUrl = imageFileUrl;
  }

  public String getHtml(String header)
  {
    if (html != null && !isPhoto()) {
      return html;
    }
    StringBuffer sb = new StringBuffer();
    sb.append("<!DOCTYPE html>\n" +
              "<html>\n" +
              "<head>\n" +
              "<meta charset=\"UTF-8\">\n" +
              header +
              "</head>\n" +
              "<body>\n" +
              "<div id=\"post\">\n" +
              "<div id=\"meta\">\n" +
              "<h2 id=\"tumblelog\">" + getTumblelogName() + "</h2>\n" +
              "<h2 id=\"note-count\">" + getNoteCount() + " notes</h2>\n" +
              "</div>\n" +
              "<div id=\"content\" class=\"" + getType() + "\">\n");
    if (TYPE_QUOTE.equals(getType())) {
      if (getQuoteText() != null) {
        sb.append(getQuoteText() + "\n");
      }
      if (getQuoteSource() != null) {
        sb.append(getQuoteSource() + "\n");
      }
    } else if (TYPE_PHOTO.equals(getType())) {
      if (getPhotoLinkUrl() != null) {
        sb.append("<a href=\"" + getPhotoLinkUrl() + "\">\n");
      } else {
        sb.append("<a href=\"" + getPhotoUrlMaxWidth1280() + "\">\n");
      }
      if (getImageFileUrl() != null) {
        TLLog.v("TLPost / makeHTML : Type photo. / Image exists.");
        sb.append("<img src=\"" + getImageFileUrl() + "\" width=\"94%\" />\n");
      } else {
        TLLog.v("TLPost / makeHTML : Type photo. / Image not found.");
        sb.append("<img src=\"" + getPhotoUrlMaxWidth400() + "\" width=\"94%\" />\n");
      }
      sb.append("</a>\n");
      if (getPhotoCaption() != null) {
        sb.append(getPhotoCaption() + "\n");
      }
    } else if (TYPE_REGULAR.equals(getType())) {
      if (getRegularTitle() != null) {
        sb.append(getRegularTitle() + "\n");
      }
      if (getRegularBody() != null) {
        sb.append(getRegularBody() + "\n");
      }
    } else if (TYPE_LINK.equals(getType())) {
      if (getLinkUrl() != null) {
        sb.append("<a href=\"" + getLinkUrl() + "\">" + getLinkText() + "</a>\n");
      }
      if (getLinkDescription() != null) {
        sb.append(getLinkDescription() + "\n");
      }
    } else if (TYPE_VIDEO.equals(getType())) {
      if (getVideoSource() != null && getVideoSource().matches("^http.*?")) {
        sb.append("<a href=\"" + getVideoSource() + "\">Video</a>\n");
      }
      if (getVideoCaption() != null) {
        sb.append(getVideoCaption() + "\n");
      }
    } else if (TYPE_AUDIO.equals(getType())) {
      if (getDownloadUrl() != null && getDownloadUrl().matches("^http.*?")) {
        sb.append("<a href=\"" + getDownloadUrl() + "\">Audio</a>\n");
      }
      if (getAudioCaption() != null) {
        sb.append(getAudioCaption() + "\n");
      }
    } else if (TYPE_CHAT.equals(getType())) {
      if (getConversationTitle() != null) {
        sb.append("<h3>" + getConversationTitle() + "</h3>\n");
      }
      if (getConversation() != null) {
        sb.append(getConversation() + "\n");
      }
    }
    sb.append("</div>\n" +
              "</div>\n" +
              "</body>\n" +
              "</html>\n");
    html = sb.toString();
    return html;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TLPost other = (TLPost) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }
}