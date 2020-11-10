package error.semanticError;

public class ReturnFromGlobalScopeError extends SemanticError {

  public ReturnFromGlobalScopeError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
