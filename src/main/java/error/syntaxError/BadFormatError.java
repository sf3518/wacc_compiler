package error.syntaxError;

public final class BadFormatError extends SyntaxError {

  public BadFormatError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
