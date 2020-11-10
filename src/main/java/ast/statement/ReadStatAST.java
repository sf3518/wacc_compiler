package ast.statement;

import ast.assign.assignLhs.AssignLhsAST;

public final class ReadStatAST implements StatAST {

  private final AssignLhsAST content;

  public ReadStatAST(AssignLhsAST content) {
    this.content = content;
  }

  public AssignLhsAST getContent() {
    return content;
  }

  @Override
  public StatToken getStatType() {
    return StatToken.READ;
  }

  @Override
  public int countChildren() {
    return 1;
  }

  @Override
  public String print() {
    return "read " + content.print();
  }
}
