package jp.mitukiii.tumblife2;

public interface TLDashboardDelegate
{
  public void noInternet();
  
  public void noAccount();
  
  public void noSDCard();
  
  public void loginSuccess();
  
  public void loginFailure();
  
  public void loading();
  
  public void loadSuccess();
  
  public void loadAllSuccess();
  
  public void loadFailure();
  
  public void likeSuccess();
  
  public void likeFailure();
  
  public void likeAllSuccess();
  
  public void likeAllFailure();
  
  public void reblogSuccess();
  
  public void reblogFailure();
  
  public void reblogAllSuccess();
  
  public void reblogAllFailure();
  
  public void showNewPosts(String text);
}
