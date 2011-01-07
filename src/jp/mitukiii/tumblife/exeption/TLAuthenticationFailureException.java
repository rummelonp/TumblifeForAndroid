package jp.mitukiii.tumblife.exeption;

public class TLAuthenticationFailureException extends TLFailureException
{
  private static final long serialVersionUID = -2760519504496168047L;

  public TLAuthenticationFailureException()
  {
    super("Authentication failed.");
  }

  public TLAuthenticationFailureException(String message)
  {
    super(message);
  }
}
