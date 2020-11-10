package error.semanticError;

public class UndeclaredVarError extends SemanticError {

  public UndeclaredVarError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
