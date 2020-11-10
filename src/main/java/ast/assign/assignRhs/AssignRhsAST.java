package ast.assign.assignRhs;

import ast.AST;
import error.ErrorHandler;
import symbolTable.SymbolTable;
import types.Type;

public interface AssignRhsAST extends AST {

  AssignRhsToken getRhsToken();

  Type getRhsType();

  void checkType(ErrorHandler errorHandler, SymbolTable symbolTable, Type type);

  int getLine();

  int getCharPosition();
}
