package ast.expression;

import ast.AST;
import ast.assign.assignRhs.AssignRhsAST;
import ast.assign.assignRhs.AssignRhsToken;
import error.ErrorHandler;
import symbolTable.SymbolTable;
import types.Type;

public interface ExprAST extends AST, AssignRhsAST {

  ExprToken getExprToken();

  Type getExprType();

  Type exprToType(ErrorHandler errorHandler, SymbolTable symbolTable);

  @Override
  default AssignRhsToken getRhsToken() {
    return AssignRhsToken.EXPR;
  }

  @Override
  default Type getRhsType() {
    return getExprType();
  }
}
