package jp.mitukiii.tumblife2.exeption;

public class TLAuthenticationFailureException extends TLFailureException
{
  private static final long serialVersionUID = -2760519504496168047L;

  public TLAuthenticationFailureException() {}
  
  public TLAuthenticationFailureException(String message)
  {
    super(message);
  }
}
