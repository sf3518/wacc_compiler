package ast.statement;

public final class SkipStatLeaf implements StatAST {

  @Override
  public StatToken getStatType() {
    return StatToken.SKIP;
  }

  @Override
  public int countChildren() {
    return 0;
  }

  @Override
  public String print() {
    return "Skip";
  }
}
