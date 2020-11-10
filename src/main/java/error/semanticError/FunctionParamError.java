package error.semanticError;

public class FunctionParamError extends SemanticError {

  public FunctionParamError(String offendingToken, int line, int position) {
    super(offendingToken, line, position);
  }
}
