package jp.mitukiii.tumblife.exeption;

public class TLParserException extends TLFailureException
{
  private static final long serialVersionUID = -5127186827932785079L;

  public TLParserException()
  {
    super("Parsing failed.");
  }
  
  public TLParserException(String message)
  {
    super(message);
  }
}
