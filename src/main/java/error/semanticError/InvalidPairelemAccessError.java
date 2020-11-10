package error.semanticError;

public class InvalidPairelemAccessError extends SemanticError {

  public InvalidPairelemAccessError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
