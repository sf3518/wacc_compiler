package ast.statement;

import java.util.List;

public final class CompositionStatAST implements StatAST {

  private final List<StatAST> statements;

  public CompositionStatAST(List<StatAST> statements) {
    this.statements = statements;
  }

  public List<StatAST> getStatements() {
    return statements;
  }

  @Override
  public StatToken getStatType() {
    return null;
  }

  @Override
  public int countStatement() {
    return 2;
  }

  @Override
  public int countChildren() {
    return 2;
  }

  @Override
  public String print() {
    StringBuilder builder = new StringBuilder();
    String prefix = "";
    for (StatAST statement : statements) {
      builder.append(prefix);
      prefix = ";\n";
      builder.append(statement.print());
    }
    return builder.toString();
  }
}
