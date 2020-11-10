package ast.assign.assignLhs;

import ast.AST;
import error.ErrorHandler;
import symbolTable.SymbolTable;
import types.Type;

public interface AssignLhsAST extends AST {

  LhsToken getLhsToken();

  Type getLhsType();

  Type leftToType(ErrorHandler errorHandler, SymbolTable symbolTable);

  int getLine();

  int getCharPosition();
}
