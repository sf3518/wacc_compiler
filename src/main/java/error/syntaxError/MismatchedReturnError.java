package error.syntaxError;

public final class MismatchedReturnError extends SyntaxError {

  public MismatchedReturnError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
