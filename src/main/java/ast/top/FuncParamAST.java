package ast.top;

import ast.AST;
import ast.mixed.IdentifierLeaf;
import types.Type;

public final class FuncParamAST implements AST {

  private final Type type;
  private final IdentifierLeaf identifier;

  public FuncParamAST(Type type, IdentifierLeaf identifier) {
    this.type = type;
    this.identifier = identifier;
  }

  public Type getType() {
    return type;
  }

  public IdentifierLeaf getIdentifier() {
    return identifier;
  }

  @Override
  public int countChildren() {
    return 2;
  }

  @Override
  public String print() {
    return type.print() + " " + identifier.print();
  }
}
