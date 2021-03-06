package ast.statement;

import ast.expression.ExprAST;
import symbolTable.SymbolTable;

import java.util.List;

public final class LoopStatAST implements StatAST {

  private final ExprAST condition;
  private final List<StatAST> statements;
  private SymbolTable symbolTable;

  public LoopStatAST(ExprAST condition, List<StatAST> statements, SymbolTable symbolTable) {
    this.condition = condition;
    this.statements = statements;
    this.symbolTable = symbolTable;
  }

  public ExprAST getCondition() {
    return condition;
  }

  public List<StatAST> getStatements() {
    return statements;
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  @Override
  public StatToken getStatType() {
    return StatToken.LOOP;
  }

  @Override
  public int countChildren() {
    return 2;
  }

  @Override
  public String print() {
    StringBuilder builder = new StringBuilder();
    builder.append("while (");
    builder.append(condition.print());
    builder.append(") {\n");
    String prefix = "";
    for (StatAST statement : statements) {
      builder.append(prefix);
      prefix = ";\n";
      builder.append(statement.print());
    }
    builder.append("\n}");
    return builder.toString();
  }
}
