package types;

import ast.AST;
import symbolTable.Node;

public interface Type extends AST, Node {

    int getOffset();
}
