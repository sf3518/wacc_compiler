package ast.expression;

import error.ErrorHandler;
import error.semanticError.IncompatibleTypeError;
import symbolTable.SymbolTable;
import types.NullType;
import types.Type;
import types.TypeToken;

public final class PairlitExprLeaf implements ExprAST {

  private final int line;
  private final int charPosition;

  public PairlitExprLeaf(int line, int charPosition) {
    this.line = line;
    this.charPosition = charPosition;
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
    return ExprToken.PAIRLIT;
  }

  @Override
  public Type getExprType() {
    return new NullType();
  }

  @Override
  public void checkType(ErrorHandler errorHandler, SymbolTable symbolTable, Type type) {
    if (!type.getTypeToken().equals(TypeToken.NULL)
        && !type.getTypeToken().equals(TypeToken.PAIR)
        && !type.getTypeToken().equals(TypeToken.ANY)) {
      errorHandler.report(
          new IncompatibleTypeError(
              "Pair Literal", line, charPosition, type.getTypeToken(), TypeToken.NULL));
    }
  }

  @Override
  public Type exprToType(ErrorHandler errorHandler, SymbolTable symbolTable) {
    return new NullType();
  }

  @Override
  public int countChildren() {
    return 0;
  }

  @Override
  public String print() {
    return "null";
  }
}
