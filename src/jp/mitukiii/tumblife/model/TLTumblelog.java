package jp.mitukiii.tumblife.model;

import java.io.Serializable;

public class TLTumblelog extends TLModel implements Serializable
{
  private static final long serialVersionUID = 4700140126971213055L;

  protected String  title;
  protected boolean isAdmin;
  protected int     posts;
  protected boolean twitterEnabled;
  protected int     draftCount;
  protected int     messagesCount;
  protected int     queueCount;
  protected int     privateId;
  protected String  name;
  protected String  url;
  protected String  type;
  protected int     followers;
  protected String  avatarUrl;
  protected boolean isPrimary;
  protected int     backupPostLimit;

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public boolean isAdmin()
  {
    return isAdmin;
  }

  public void setAdmin(boolean isAdmin)
  {
    this.isAdmin = isAdmin;
  }

  public int getPosts()
  {
    return posts;
  }

  public void setPosts(int posts)
  {
    this.posts = posts;
  }

  public boolean getTwitterEnabled()
  {
    return twitterEnabled;
  }

  public void setTwitterEnabled(boolean twitterEnabled)
  {
    this.twitterEnabled = twitterEnabled;
  }

  public int getDraftCount()
  {
    return draftCount;
  }

  public void setDraftCount(int draftCount)
  {
    this.draftCount = draftCount;
  }

  public int getMessagesCount()
  {
    return messagesCount;
  }

  public void setMessagesCount(int messagesCount)
  {
    this.messagesCount = messagesCount;
  }

  public int getQueueCount()
  {
    return queueCount;
  }

  public void setQueueCount(int queueCount)
  {
    this.queueCount = queueCount;
  }

  public int getPrivateId()
  {
    return privateId;
  }

  public void setPrivateId(int privateId)
  {
    this.privateId = privateId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public int getFollowers()
  {
    return followers;
  }

  public void setFollowers(int followers)
  {
    this.followers = followers;
  }

  public String getAvatarUrl()
  {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl)
  {
    this.avatarUrl = avatarUrl;
  }

  public boolean isPrimary()
  {
    return isPrimary;
  }

  public void setPrimary(boolean isPrimary)
  {
    this.isPrimary = isPrimary;
  }

  public int getBackupPostLimit()
  {
    return backupPostLimit;
  }

  public void setBackupPostLimit(int backupPostLimit)
  {
    this.backupPostLimit = backupPostLimit;
  }
}