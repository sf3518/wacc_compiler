package ast.statement;

import ast.assign.assignRhs.AssignRhsAST;
import ast.mixed.IdentifierLeaf;
import types.Type;

public final class DeclStatAST implements StatAST {

  private final Type type;
  private final IdentifierLeaf identifier;
  private final AssignRhsAST right;

  public DeclStatAST(Type type, IdentifierLeaf identifier, AssignRhsAST right) {
    this.type = type;
    this.identifier = identifier;
    this.right = right;
  }

  public Type getType() {
    return type;
  }

  public IdentifierLeaf getIdentifier() {
    return identifier;
  }

  public AssignRhsAST getRight() {
    return right;
  }

  @Override
  public StatToken getStatType() {
    return StatToken.DECLARATION;
  }

  @Override
  public int countChildren() {
    return 3;
  }

  @Override
  public String print() {
    return type.print() + " " + identifier.print() + " = " + right.print();
  }
}
