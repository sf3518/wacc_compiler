package error.semanticError;

public class DoubleDeclError extends SemanticError {

  public DoubleDeclError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
