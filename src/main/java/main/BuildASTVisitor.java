package main;

import ast.AST;
import ast.assign.PairElemAST;
import ast.assign.PairelemFstAST;
import ast.assign.PairelemSndAST;
import ast.assign.assignLhs.AssignLhsAST;
import ast.assign.assignRhs.ArraylitRhsAST;
import ast.assign.assignRhs.AssignRhsAST;
import ast.assign.assignRhs.CallRhsAST;
import ast.assign.assignRhs.NewpairRhsAST;
import ast.expression.*;
import ast.mixed.ArrayElemAST;
import ast.mixed.IdentifierLeaf;
import ast.statement.*;
import ast.top.FuncAST;
import ast.top.FuncParamAST;
import ast.top.ProgAST;
import error.ErrorHandler;
import error.semanticError.DoubleDeclError;
import error.semanticError.IncompatibleTypeError;
import error.semanticError.ReadError;
import error.semanticError.ReturnFromGlobalScopeError;
import parser.BasicParser;
import parser.BasicParserBaseVisitor;
import symbolTable.Function;
import symbolTable.Node;
import symbolTable.SymbolTable;
import types.*;

import java.util.ArrayList;
import java.util.List;

public class BuildASTVisitor extends BasicParserBaseVisitor<AST> {

  private ErrorHandler semanticErrHandler = new ErrorHandler();
  private SymbolTable symbolTable = new SymbolTable(null);
  private Type returnType = null;

  @Override
  public ProgAST visitProg(BasicParser.ProgContext ctx) {
    // Remember symbol table:
    SymbolTable origin = this.symbolTable;
    // Add all functions in the 1st pass partially (Function statements not included):
    for (BasicParser.FuncContext funcContext : ctx.func()) {
      Function function = buildFunctionFromContext(funcContext);
      String id = function.getName();
      if (checkDuplicateId(id, true)) {
        semanticErrHandler.report(
            new DoubleDeclError(
                id, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine()));
      }
      origin.insert(id, function);
    }
    // Visit functions in the 2nd pass (Function statements included):
    List<FuncAST> functions = new ArrayList<>();
    for (BasicParser.FuncContext funcContext : ctx.func()) {
      // New symbol table for each function:
      this.symbolTable = new SymbolTable(origin);
      // Visit:
      FuncAST funcAST = visitFunc(funcContext);
      functions.add(funcAST);
    }
    // Visit statement in the 2nd pass:
    // Update symbol table and visit statement:
    this.symbolTable = new SymbolTable(origin);
    StatAST statAST = visitStat(ctx.stat());
    List<StatAST> statements = new ArrayList<>();
    if (statAST.countStatement() == 1) {
      statements.add(statAST);
    } else if (statAST.countStatement() == 2) {
      statements = ((CompositionStatAST) statAST).getStatements();
    }
    return new ProgAST(statements, functions, symbolTable);
  }

  @Override
  public FuncAST visitFunc(BasicParser.FuncContext ctx) {
    // Remember symbol table:
    SymbolTable origin = this.symbolTable;
    // Build AST:
    types.Type returnType = visitType(ctx.type());
    IdentifierLeaf functionName = visitIdent(ctx.ident());
    List<FuncParamAST> inputParams = new ArrayList<>();
    for (BasicParser.ParamContext paramContext : ctx.param()) {
      inputParams.add(visitParam(paramContext));
    }
    // Update symbol table with parameters:
    for (FuncParamAST funcParamAST : inputParams) {
      String name = funcParamAST.getIdentifier().getText();
      origin.insert(name, funcParamAST.getType());
    }
    // Set return type:
    this.returnType = returnType;
    // Visit statement:
    StatAST statAST = visitStat(ctx.stat());
    List<StatAST> statements = new ArrayList<>();
    if (statAST.countStatement() == 1) {
      statements.add(statAST);
    } else if (statAST.countStatement() == 2) {
      statements = ((CompositionStatAST) statAST).getStatements();
    }
    // Set return type field back to null:
    this.returnType = null;
    return new FuncAST(
        returnType,
        functionName,
        inputParams,
        statements,
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        symbolTable);
  }

  @Override
  public FuncParamAST visitParam(BasicParser.ParamContext ctx) {
    types.Type type = visitType(ctx.type());
    IdentifierLeaf identifier = visitIdent(ctx.ident());
    return new FuncParamAST(type, identifier);
  }

  @Override
  public StatAST visitStat(BasicParser.StatContext ctx) {
    if (ctx.statSkip() != null) {
      return visitStatSkip(ctx.statSkip());
    } else if (ctx.statInitVar() != null) {
      return visitStatInitVar(ctx.statInitVar());
    } else if (ctx.statAssignVar() != null) {
      return visitStatAssignVar(ctx.statAssignVar());
    } else if (ctx.statRead() != null) {
      return visitStatRead(ctx.statRead());
    } else if (ctx.statFree() != null) {
      return visitStatFree(ctx.statFree());
    } else if (ctx.statReturn() != null) {
      return visitStatReturn(ctx.statReturn());
    } else if (ctx.statExit() != null) {
      return visitStatExit(ctx.statExit());
    } else if (ctx.statPrint() != null) {
      return visitStatPrint(ctx.statPrint());
    } else if (ctx.statPrintln() != null) {
      return visitStatPrintln(ctx.statPrintln());
    } else if (ctx.statCond() != null) {
      return visitStatCond(ctx.statCond());
    } else if (ctx.statLoop() != null) {
      return visitStatLoop(ctx.statLoop());
    } else if (ctx.statBegin() != null) {
      return visitStatBegin(ctx.statBegin());
    } else if (ctx.SEMICOLON() != null) {
      List<StatAST> statements = new ArrayList<>();
      StatAST first = visitStat(ctx.stat(0));
      if (first.countStatement() == 1) {
        statements.add(first);
      } else if (first.countStatement() == 2) {
        statements = ((CompositionStatAST) first).getStatements();
      }
      StatAST second = visitStat(ctx.stat(1));
      statements.add(second);
      return new CompositionStatAST(statements);
    }
    return null;
  }

  @Override
  public SkipStatLeaf visitStatSkip(BasicParser.StatSkipContext ctx) {
    return new SkipStatLeaf();
  }

  @Override
  public DeclStatAST visitStatInitVar(BasicParser.StatInitVarContext ctx) {
    // Build AST for Type and Identifier:
    types.Type type = visitType(ctx.type());
    IdentifierLeaf identifier = visitIdent(ctx.ident());
    identifier.setType(type);
    // Update symbol table and semantic check:
    String id = identifier.getText();
    // Check double declaration:
    if (checkDuplicateId(identifier.getText(), false)) {
      this.semanticErrHandler.report(
          new DoubleDeclError(
              id, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine()));
    }
    this.symbolTable.insert(id, type);
    // Build AST Assign-rhs && check compatible type:
    AssignRhsAST right = visitAssignRhs(ctx.assignRhs());
    right.checkType(this.semanticErrHandler, this.symbolTable, type);
    return new DeclStatAST(type, identifier, right);
  }

  @Override
  public AssignStatAST visitStatAssignVar(BasicParser.StatAssignVarContext ctx) {
    // Visit lhs:
    AssignLhsAST left = visitAssignLhs(ctx.assignLhs());
    // Get lhs type:
    Type expectedType = left.leftToType(this.semanticErrHandler, this.symbolTable);
    // Visit rhs:
    AssignRhsAST right = visitAssignRhs(ctx.assignRhs());
    // Check compatible type:
    right.checkType(this.semanticErrHandler, this.symbolTable, expectedType);
    return new AssignStatAST(left, right);
  }

  @Override
  public ReadStatAST visitStatRead(BasicParser.StatReadContext ctx) {
    AssignLhsAST assignLhsAST = visitAssignLhs(ctx.assignLhs());
    // Check argument type:
    Type argType = assignLhsAST.leftToType(this.semanticErrHandler, this.symbolTable);
    if (!(argType.equals(new IntType()) || argType.equals(new CharType()))) {
      this.semanticErrHandler.report(
          new ReadError("", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine()));
    }
    return new ReadStatAST(assignLhsAST);
  }

  @Override
  public NormalStatAST visitStatFree(BasicParser.StatFreeContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    // Check argument type:
    Type type = expression.exprToType(this.semanticErrHandler, this.symbolTable);
    if (!(type.getTypeToken().equals(TypeToken.ARRAY))
        && !(type.getTypeToken().equals(TypeToken.PAIR))
        && !(type.getTypeToken().equals(TypeToken.ANY))) {
      this.semanticErrHandler.report(
          new IncompatibleTypeError(
              "",
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              type.getTypeToken()));
    }
    return new NormalStatAST(NormalStatToken.FREE, expression);
  }

  @Override
  public NormalStatAST visitStatReturn(BasicParser.StatReturnContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    // Check return type:
    if (this.returnType == null) {
      this.semanticErrHandler.report(
          new ReturnFromGlobalScopeError(
              "", ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine()));
    }
    expression.checkType(this.semanticErrHandler, this.symbolTable, this.returnType);
    return new NormalStatAST(NormalStatToken.RETURN, expression);
  }

  @Override
  public NormalStatAST visitStatExit(BasicParser.StatExitContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    expression.checkType(this.semanticErrHandler, this.symbolTable, new IntType());
    return new NormalStatAST(NormalStatToken.EXIT, expression);
  }

  @Override
  public NormalStatAST visitStatPrint(BasicParser.StatPrintContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    expression.checkType(this.semanticErrHandler, this.symbolTable, new AnyType());
    return new NormalStatAST(NormalStatToken.PRINT, expression);
  }

  @Override
  public NormalStatAST visitStatPrintln(BasicParser.StatPrintlnContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    expression.checkType(this.semanticErrHandler, this.symbolTable, new AnyType());
    return new NormalStatAST(NormalStatToken.PRINTLINE, expression);
  }

  @Override
  public ConditionStatAST visitStatCond(BasicParser.StatCondContext ctx) {
    // Remember original symbol table:
    SymbolTable origin = this.symbolTable;
    // Build expression AST and check its type:
    ExprAST expression = visitExpr(ctx.expr());
    expression.checkType(this.semanticErrHandler, this.symbolTable, new BoolType());
    // Create new symbol table and visit true branch:
    this.symbolTable = new SymbolTable(origin);
    StatAST trueStatements = visitStat(ctx.stat(0));
    SymbolTable trueBrSymbolTable = this.symbolTable;
    // Create new symbol table and visit false branch:
    this.symbolTable = new SymbolTable(origin);
    StatAST falseStatements = visitStat(ctx.stat(1));
    SymbolTable falseBrSymbolTable = this.symbolTable;
    // Reset symbol table:
    this.symbolTable = origin;
    List<StatAST> trueBranch = new ArrayList<>();
    List<StatAST> falseBranch = new ArrayList<>();
    if (trueStatements.countStatement() == 1) {
      trueBranch.add(trueStatements);
    } else if (trueStatements.countStatement() == 2) {
      trueBranch = ((CompositionStatAST) trueStatements).getStatements();
    }
    if (falseStatements.countStatement() == 1) {
      falseBranch.add(falseStatements);
    } else if (falseStatements.countStatement() == 2) {
      falseBranch = ((CompositionStatAST) falseStatements).getStatements();
    }
    return new ConditionStatAST(
        expression, trueBranch, falseBranch, trueBrSymbolTable, falseBrSymbolTable);
  }

  @Override
  public LoopStatAST visitStatLoop(BasicParser.StatLoopContext ctx) {
    // Remember original symbol table:
    SymbolTable origin = this.symbolTable;
    // Build expression AST and check its type:
    ExprAST condition = visitExpr(ctx.expr());
    condition.checkType(this.semanticErrHandler, this.symbolTable, new BoolType());
    // Create new symbol table and visit body:
    this.symbolTable = new SymbolTable(origin);
    StatAST statement = visitStat(ctx.stat());
    SymbolTable loopSymbolTable = this.symbolTable;
    // Reset symbol table:
    this.symbolTable = origin;
    List<StatAST> statements = new ArrayList<>();
    if (statement.countStatement() == 1) {
      statements.add(statement);
    } else if (statement.countStatement() == 2) {
      statements = ((CompositionStatAST) statement).getStatements();
    }
    return new LoopStatAST(condition, statements, loopSymbolTable);
  }

  @Override
  public BlockStatAST visitStatBegin(BasicParser.StatBeginContext ctx) {
    // Remember original symbol table:
    SymbolTable origin = this.symbolTable;
    // Create new symbol table and visit body:
    this.symbolTable = new SymbolTable(origin);
    StatAST statement = visitStat(ctx.stat());
    SymbolTable blockSymbolTable = this.symbolTable;
    // Reset symbol table:
    this.symbolTable = origin;
    List<StatAST> statements = new ArrayList<>();
    if (statement.countStatement() == 1) {
      statements.add(statement);
    } else if (statement.countStatement() == 2) {
      statements = ((CompositionStatAST) statement).getStatements();
    }
    return new BlockStatAST(statements, blockSymbolTable);
  }

  @Override
  public AssignLhsAST visitAssignLhs(BasicParser.AssignLhsContext ctx) {
    if (ctx.ident() != null) {
      return visitIdent(ctx.ident());
    } else if (ctx.arrayElem() != null) {
      return visitArrayElem(ctx.arrayElem());
    } else if (ctx.pairElem() != null) {
      return visitPairElem(ctx.pairElem());
    }
    return null;
  }

  @Override
  public AssignRhsAST visitAssignRhs(BasicParser.AssignRhsContext ctx) {
    if (ctx.arrayLiter() != null) {
      return visitArrayLiter(ctx.arrayLiter());
    } else if (ctx.pairElem() != null) {
      return visitPairElem(ctx.pairElem());
    } else if (ctx.call() != null) {
      return visitCall(ctx.call());
    } else if (ctx.expr() != null) {
      return visitExpr(ctx.expr());
    } else if (ctx.newpair() != null) {
      return visitNewpair(ctx.newpair());
    }
    return null;
  }

  @Override
  public CallRhsAST visitCall(BasicParser.CallContext ctx) {
    IdentifierLeaf identifier = visitIdent(ctx.ident());
    List<ExprAST> expressions = new ArrayList<>();
    for (BasicParser.ExprContext exprContext : ctx.expr()) {
      expressions.add(visitExpr(exprContext));
    }
    return new CallRhsAST(
        identifier, expressions, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public NewpairRhsAST visitNewpair(BasicParser.NewpairContext ctx) {
    ExprAST first = visitExpr(ctx.expr(0));
    ExprAST second = visitExpr(ctx.expr(1));
    return new NewpairRhsAST(
        first, second, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public PairElemAST visitPairElem(BasicParser.PairElemContext ctx) {
    if (ctx.pairElemFst() != null) {
      return visitPairElemFst(ctx.pairElemFst());
    } else if (ctx.pairElemSnd() != null) {
      return visitPairElemSnd(ctx.pairElemSnd());
    }
    return null;
  }

  @Override
  public PairelemFstAST visitPairElemFst(BasicParser.PairElemFstContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    return new PairelemFstAST(
        expression, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public PairelemSndAST visitPairElemSnd(BasicParser.PairElemSndContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    return new PairelemSndAST(
        expression, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public ExprAST visitExpr(BasicParser.ExprContext ctx) {
    if (ctx.exprIntLit() != null) {
      return visitExprIntLit(ctx.exprIntLit());
    } else if (ctx.exprBoolLit() != null) {
      return visitExprBoolLit(ctx.exprBoolLit());
    } else if (ctx.exprCharLit() != null) {
      return visitExprCharLit(ctx.exprCharLit());
    } else if (ctx.exprStrLit() != null) {
      return visitExprStrLit(ctx.exprStrLit());
    } else if (ctx.exprPairLit() != null) {
      return visitExprPairLit(ctx.exprPairLit());
    } else if (ctx.ident() != null) {
      return visitIdent(ctx.ident());
    } else if (ctx.arrayElem() != null) {
      return visitArrayElem(ctx.arrayElem());
    } else if (ctx.exprParen() != null) {
      return visitExprParen(ctx.exprParen());
    } else if (ctx.exprUnop() != null) {
      return visitExprUnop(ctx.exprUnop());
    } else if (ctx.MULDIVMOD() != null) {
      String operator = ctx.MULDIVMOD().getText();
      ExprAST left = visitExpr(ctx.expr(0));
      ExprAST right = visitExpr(ctx.expr(1));
      switch (operator) {
        case "*":
          return new BinopExprAST(
              BinopToken.MUL,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
        case "/":
          return new BinopExprAST(
              BinopToken.Div,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
        case "%":
          return new BinopExprAST(
              BinopToken.MOD,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
      }
    } else if (ctx.ADD() != null) {
      return new BinopExprAST(
          BinopToken.ADD,
          visitExpr(ctx.expr(0)),
          visitExpr(ctx.expr(1)),
          ctx.getStop().getLine(),
          ctx.getStop().getCharPositionInLine());
    } else if (ctx.NEG() != null) {
      return new BinopExprAST(
          BinopToken.SUB,
          visitExpr(ctx.expr(0)),
          visitExpr(ctx.expr(1)),
          ctx.getStop().getLine(),
          ctx.getStop().getCharPositionInLine());
    } else if (ctx.CMP() != null) {
      String operator = ctx.CMP().getText();
      ExprAST left = visitExpr(ctx.expr(0));
      ExprAST right = visitExpr(ctx.expr(1));
      switch (operator) {
        case ">":
          return new BinopExprAST(
              BinopToken.GT,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
        case "<":
          return new BinopExprAST(
              BinopToken.LT,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
        case ">=":
          return new BinopExprAST(
              BinopToken.GTE,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
        case "<=":
          return new BinopExprAST(
              BinopToken.LTE,
              left,
              right,
              ctx.getStop().getLine(),
              ctx.getStop().getCharPositionInLine());
      }
    } else if (ctx.EQ() != null) {
      String operator = ctx.EQ().getText();
      ExprAST left = visitExpr(ctx.expr(0));
      ExprAST right = visitExpr(ctx.expr(1));
      if (operator.equals("==")) {
        return new BinopExprAST(
            BinopToken.EQ,
            left,
            right,
            ctx.getStop().getLine(),
            ctx.getStop().getCharPositionInLine());
      } else if (operator.equals("!=")) {
        return new BinopExprAST(
            BinopToken.NEQ,
            left,
            right,
            ctx.getStop().getLine(),
            ctx.getStop().getCharPositionInLine());
      }
    } else if (ctx.ANDOR() != null) {
      String operator = ctx.ANDOR().getText();
      ExprAST left = visitExpr(ctx.expr(0));
      ExprAST right = visitExpr(ctx.expr(1));
      if (operator.equals("&&")) {
        return new BinopExprAST(
            BinopToken.AND,
            left,
            right,
            ctx.getStop().getLine(),
            ctx.getStop().getCharPositionInLine());
      } else if (operator.equals("||")) {
        return new BinopExprAST(
            BinopToken.OR,
            left,
            right,
            ctx.getStop().getLine(),
            ctx.getStop().getCharPositionInLine());
      }
    }
    return null;
  }

  @Override
  public IntlitExprLeaf visitExprIntLit(BasicParser.ExprIntLitContext ctx) {
    return new IntlitExprLeaf(
        Integer.parseInt(ctx.intLiter().getText()),
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine());
  }

  @Override
  public BoollitExprLeaf visitExprBoolLit(BasicParser.ExprBoolLitContext ctx) {
    String text = ctx.BOOL_LITER().getText();
    if (text.equals("true")) {
      return new BoollitExprLeaf(
          true, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }
    return new BoollitExprLeaf(
        false, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public CharlitExprLeaf visitExprCharLit(BasicParser.ExprCharLitContext ctx) {
    String string = ctx.CHAR_LITER().getText();
    string = string.replaceAll("\\\\0", "\0");
    string = string.replaceAll("\\\\b", "\b");
    string = string.replaceAll("\\\\t", "\t");
    string = string.replaceAll("\\\\n", "\n");
    string = string.replaceAll("\\\\f", "\f");
    string = string.replaceAll("\\\\r", "\r");
    string = string.replaceAll("\\\\'", "'");
    string = string.replaceAll("\\\\\\\\", "\\");
    string = string.replaceAll("\\\\\"", "\"");
    return new CharlitExprLeaf(
        string.charAt(1), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public StrlitExprLeaf visitExprStrLit(BasicParser.ExprStrLitContext ctx) {
    String text = ctx.STR_LITER().getText();
    text = text.substring(1, text.length() - 1);
    return new StrlitExprLeaf(
        text, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public PairlitExprLeaf visitExprPairLit(BasicParser.ExprPairLitContext ctx) {
    return new PairlitExprLeaf(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public ExprAST visitExprParen(BasicParser.ExprParenContext ctx) {
    return visitExpr(ctx.expr());
  }

  @Override
  public UnopExprAST visitExprUnop(BasicParser.ExprUnopContext ctx) {
    ExprAST expression = visitExpr(ctx.expr());
    if (ctx.NOT() != null) {
      return new UnopExprAST(
          UnopToken.NOT,
          expression,
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine());
    } else if (ctx.NEG() != null) {
      return new UnopExprAST(
          UnopToken.NEG,
          expression,
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine());
    } else if (ctx.LENGTH() != null) {
      return new UnopExprAST(
          UnopToken.LEN,
          expression,
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine());
    } else if (ctx.ORD() != null) {
      return new UnopExprAST(
          UnopToken.ORD,
          expression,
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine());
    } else if (ctx.CHR() != null) {
      return new UnopExprAST(
          UnopToken.CHR,
          expression,
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine());
    }
    return null;
  }

  @Override
  public types.Type visitType(BasicParser.TypeContext ctx) {
    if (ctx.baseType() != null) {
      return visitBaseType(ctx.baseType());
    } else if (ctx.arrayType() != null) {
      return visitArrayType(ctx.arrayType());
    } else if (ctx.pairType() != null) {
      return visitPairType(ctx.pairType());
    }
    return null;
  }

  @Override
  public Type visitBaseType(BasicParser.BaseTypeContext ctx) {
    String text = ctx.BASE_TYPE().getText();
    switch (text) {
      case "int":
        return new IntType();
      case "bool":
        return new BoolType();
      case "char":
        return new CharType();
      case "string":
        return new StringType();
    }
    return null;
  }

  @Override
  public PairType visitPairType(BasicParser.PairTypeContext ctx) {
    types.Type first = null;
    types.Type second = null;
    if (ctx.pairElemType(0).PAIR() != null) {
      first = new PairType(new AnyType(), new AnyType());
    } else if (ctx.pairElemType(0).baseType() != null) {
      first = visitBaseType(ctx.pairElemType(0).baseType());
    } else if (ctx.pairElemType(0).arrayType() != null) {
      first = visitArrayType(ctx.pairElemType(0).arrayType());
    }
    if (ctx.pairElemType(1).PAIR() != null) {
      second = new PairType(new AnyType(), new AnyType());
    } else if (ctx.pairElemType(1).baseType() != null) {
      second = visitBaseType(ctx.pairElemType(1).baseType());
    } else if (ctx.pairElemType(1).arrayType() != null) {
      second = visitArrayType(ctx.pairElemType(1).arrayType());
    }
    return new PairType(first, second);
  }

  @Override
  public AST visitPairElemType(BasicParser.PairElemTypeContext ctx) {
    return null;
  }

  @Override
  public ArrayType visitArrayType(BasicParser.ArrayTypeContext ctx) {
    if (ctx.baseType() != null) {
      return new ArrayType(visitBaseType(ctx.baseType()));
    } else if (ctx.arrayType() != null) {
      return new ArrayType(visitArrayType(ctx.arrayType()));
    } else if (ctx.pairType() != null) {
      return new ArrayType(visitPairType(ctx.pairType()));
    }
    return null;
  }

  @Override
  public ArrayElemAST visitArrayElem(BasicParser.ArrayElemContext ctx) {
    IdentifierLeaf identifier = visitIdent(ctx.ident());
    List<ExprAST> expressions = new ArrayList<>();
    for (BasicParser.ExprContext exprContext : ctx.expr()) {
      expressions.add(visitExpr(exprContext));
    }
    return new ArrayElemAST(
        identifier, expressions, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public AST visitIntLiter(BasicParser.IntLiterContext ctx) {
    return null;
  }

  @Override
  public ArraylitRhsAST visitArrayLiter(BasicParser.ArrayLiterContext ctx) {
    List<ExprAST> expressions = new ArrayList<>();
    for (BasicParser.ExprContext exprContext : ctx.expr()) {
      expressions.add(visitExpr(exprContext));
    }
    return new ArraylitRhsAST(
        expressions, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  @Override
  public IdentifierLeaf visitIdent(BasicParser.IdentContext ctx) {
    return new IdentifierLeaf(
        ctx.IDENT().getText(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
  }

  private boolean checkDuplicateId(String id, boolean isFunction) {
    Node node = this.symbolTable.lookup(id);
    if (node == null) {
      return false;
    }
    if (!isFunction) {
      return !node.getTypeToken().equals(TypeToken.FUNCTION);
    }
    return node.getTypeToken().equals(TypeToken.FUNCTION);
  }

  private Function buildFunctionFromContext(BasicParser.FuncContext ctx) {
    String functionName = ctx.ident().getText();
    Type returnType = visitType(ctx.type());
    List<Type> paramTypes = new ArrayList<>();
    for (BasicParser.ParamContext paramContext : ctx.param()) {
      FuncParamAST funcParamAST = visitParam(paramContext);
      paramTypes.add(funcParamAST.getType());
    }
    return new Function(functionName, returnType, paramTypes);
  }
}
