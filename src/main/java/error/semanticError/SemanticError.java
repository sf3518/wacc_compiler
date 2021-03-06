package error.semanticError;

import error.Error;

public abstract class SemanticError implements Error {

  private final String offendingToken;
  private final int line;
  private final int position;

  protected SemanticError(String offendingToken, int line, int position) {
    this.offendingToken = offendingToken;
    this.line = line;
    this.position = position;
  }

  @Override
  public String getOffendingToken() {
    return offendingToken;
  }

  @Override
  public int getLine() {
    return line;
  }

  @Override
  public int getPosition() {
    return position;
  }
}
