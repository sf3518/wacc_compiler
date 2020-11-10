package ast.expression;

import error.ErrorHandler;
import error.semanticError.IncompatibleTypeError;
import symbolTable.SymbolTable;
import types.StringType;
import types.Type;
import types.TypeToken;

public final class StrlitExprLeaf implements ExprAST {

  private final String value;
  private final int line;
  private final int charPosition;

  public StrlitExprLeaf(String value, int line, int charPosition) {
    this.value = value;
    this.line = line;
    this.charPosition = charPosition;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int getLine() {
    return line;
  }

  @Override
  public int getCharPosition() {
    return charPosition;
  }

  @Override
  public ExprToken getExprToken() {
    return ExprToken.STRLIT;
  }

  @Override
  public Type getExprType() {
    return new StringType();
  }

  @Override
  public void checkType(ErrorHandler errorHandler, SymbolTable symbolTable, Type type) {
    if (!type.getTypeToken().equals(TypeToken.STRING)
        && !type.getTypeToken().equals(TypeToken.ANY)) {
      errorHandler.report(
          new IncompatibleTypeError(
              value, line, charPosition, type.getTypeToken(), TypeToken.STRING));
    }
  }

  @Override
  public Type exprToType(ErrorHandler errorHandler, SymbolTable symbolTable) {
    return new StringType();
  }

  @Override
  public int countChildren() {
    return 0;
  }

  @Override
  public String print() {
    return "\"" + value + "\"";
  }
}
