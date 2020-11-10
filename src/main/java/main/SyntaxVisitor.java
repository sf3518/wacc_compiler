package main;

import error.ErrorHandler;
import error.syntaxError.BadFormatError;
import error.syntaxError.MismatchedReturnError;
import parser.BasicParser;
import parser.BasicParserBaseVisitor;

public class SyntaxVisitor extends BasicParserBaseVisitor<Boolean> {

  private ErrorHandler syntaxErrHandler = new ErrorHandler();

  public ErrorHandler getSyntaxErrHandler() {
    return syntaxErrHandler;
  }

  @Override
  public Boolean visitProg(BasicParser.ProgContext ctx) {
    for (BasicParser.FuncContext funcContext : ctx.func()) {
      if (!visitFunc(funcContext)) {
        return false;
      }
    }
    return visitStat(ctx.stat());
  }

  @Override
  public Boolean visitFunc(BasicParser.FuncContext ctx) {
    if (!checkReturnExit(ctx.stat())) {
      syntaxErrHandler.report(
          new MismatchedReturnError(
              ctx.ident().getText(),
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine()));
      return false;
    }
    return true;
  }

  @Override
  public Boolean visitParam(BasicParser.ParamContext ctx) {
    return true;
  }

  @Override
  public Boolean visitStat(BasicParser.StatContext ctx) {
    if (ctx.statInitVar() != null) {
      return visitStatInitVar(ctx.statInitVar());
    } else if (ctx.statAssignVar() != null) {
      return visitStatAssignVar(ctx.statAssignVar());
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
      return visitStat(ctx.stat(0)) && visitStat(ctx.stat(1));
    }
    return true;
  }

  @Override
  public Boolean visitStatInitVar(BasicParser.StatInitVarContext ctx) {
    return visitAssignRhs(ctx.assignRhs());
  }

  @Override
  public Boolean visitStatAssignVar(BasicParser.StatAssignVarContext ctx) {
    return visitAssignRhs(ctx.assignRhs());
  }

  @Override
  public Boolean visitStatFree(BasicParser.StatFreeContext ctx) {
    return visitExpr(ctx.expr());
  }

  @Override
  public Boolean visitStatReturn(BasicParser.StatReturnContext ctx) {
    return visitExpr(ctx.expr());
  }

  @Override
  public Boolean visitStatExit(BasicParser.StatExitContext ctx) {
    return visitExpr(ctx.expr());
  }

  @Override
  public Boolean visitStatPrint(BasicParser.StatPrintContext ctx) {
    return visitExpr(ctx.expr());
  }

  @Override
  public Boolean visitStatPrintln(BasicParser.StatPrintlnContext ctx) {
    return visitExpr(ctx.expr());
  }

  @Override
  public Boolean visitStatCond(BasicParser.StatCondContext ctx) {
    return visitExpr(ctx.expr()) && visitStat(ctx.stat(0)) && visitStat(ctx.stat(1));
  }

  @Override
  public Boolean visitStatLoop(BasicParser.StatLoopContext ctx) {
    return visitExpr(ctx.expr()) && visitStat(ctx.stat());
  }

  @Override
  public Boolean visitStatBegin(BasicParser.StatBeginContext ctx) {
    return visitStat(ctx.stat());
  }

  @Override
  public Boolean visitAssignRhs(BasicParser.AssignRhsContext ctx) {
    if (ctx.expr() != null) {
      return visitExpr(ctx.expr());
    }
    return true;
  }

  @Override
  public Boolean visitExpr(BasicParser.ExprContext ctx) {
    if (ctx.exprIntLit() != null) {
      return visitExprIntLit(ctx.exprIntLit());
    }
    return true;
  }

  @Override
  public Boolean visitExprIntLit(BasicParser.ExprIntLitContext ctx) {
    try {
      Integer.parseInt(ctx.intLiter().getText());
    } catch (NumberFormatException e) {
      syntaxErrHandler.report(
          new BadFormatError(
              ctx.intLiter().getText(),
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine()));
      return false;
    }
    return true;
  }

  private boolean checkReturnExit(BasicParser.StatContext ctx) {
    if (ctx.statReturn() != null || ctx.statExit() != null) {
      return true;
    }
    if (ctx.statCond() != null) {
      return checkReturnExit(ctx.statCond().stat(0)) && checkReturnExit(ctx.statCond().stat(1));
    }
    if (ctx.statLoop() != null) {
      return checkReturnExit(ctx.statLoop().stat());
    }
    BasicParser.StatContext context = ctx.stat(1);
    if (context != null) {
      if (context.statReturn() != null || context.statExit() != null) {
        return true;
      }
      if (context.statCond() != null) {
        return checkReturnExit(context.statCond().stat(0))
            && checkReturnExit(context.statCond().stat(1));
      }
      if (ctx.statLoop() != null) {
        return checkReturnExit(ctx.statLoop().stat());
      }
    }
    return false;
  }
}
