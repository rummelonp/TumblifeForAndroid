package jp.mitukiii.tumblife.exeption;

import java.io.IOException;

public class TLFailureException extends IOException
{
  private static final long serialVersionUID = 820045226246174242L;

  public TLFailureException() {}
  
  public TLFailureException(String message)
  {
    super(message);
  }
}
