package jp.mitukiii.tumblife2;

public interface TLDashboardDelegate
{
  public void noInternet();
  
  public void noAccount();
  
  public void noSDCard();
  
  public void loginSuccess();
  
  public void loginFailure();
  
  public void firstLoading();
  
  public void loading();
  
  public void loadSuccess();
  
  public void loadFailure();
  
  public void showToast(String string);
}
