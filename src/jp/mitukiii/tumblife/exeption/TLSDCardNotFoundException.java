package jp.mitukiii.tumblife.exeption;

public class TLSDCardNotFoundException extends TLFailureException
{
  private static final long serialVersionUID = 7263100142532892304L;

  public TLSDCardNotFoundException()
  {
    super("SDCard not found.");
  }
  
  public TLSDCardNotFoundException(String message)
  {
    super(message);
  }
}
