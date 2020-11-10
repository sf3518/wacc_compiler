package ast.statement;

import ast.assign.assignLhs.AssignLhsAST;
import ast.assign.assignRhs.AssignRhsAST;

public final class AssignStatAST implements StatAST {

  private final AssignLhsAST left;
  private final AssignRhsAST right;

  public AssignStatAST(AssignLhsAST left, AssignRhsAST right) {
    this.left = left;
    this.right = right;
  }

  public AssignLhsAST getLeft() {
    return left;
  }

  public AssignRhsAST getRight() {
    return right;
  }

  @Override
  public StatToken getStatType() {
    return StatToken.ASSIGN;
  }

  @Override
  public int countChildren() {
    return 2;
  }

  @Override
  public String print() {
    return left.print() + " = " + right.print();
  }
}
