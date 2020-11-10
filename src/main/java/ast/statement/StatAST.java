package ast.statement;

import ast.AST;

public interface StatAST extends AST {

  StatToken getStatType();

  default int countStatement() {
    return 1;
  }
}
