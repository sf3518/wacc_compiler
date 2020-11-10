// Generated from BasicParser.g4 by ANTLR 4.7.1

    package parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BasicParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BasicParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BasicParser#prog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProg(BasicParser.ProgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc(BasicParser.FuncContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(BasicParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(BasicParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statSkip}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatSkip(BasicParser.StatSkipContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statInitVar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatInitVar(BasicParser.StatInitVarContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statAssignVar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatAssignVar(BasicParser.StatAssignVarContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statRead}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatRead(BasicParser.StatReadContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statFree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatFree(BasicParser.StatFreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statReturn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatReturn(BasicParser.StatReturnContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statExit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatExit(BasicParser.StatExitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statPrint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatPrint(BasicParser.StatPrintContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statPrintln}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatPrintln(BasicParser.StatPrintlnContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statCond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatCond(BasicParser.StatCondContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statLoop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatLoop(BasicParser.StatLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#statBegin}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatBegin(BasicParser.StatBeginContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#assignLhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignLhs(BasicParser.AssignLhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#assignRhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignRhs(BasicParser.AssignRhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(BasicParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#newpair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewpair(BasicParser.NewpairContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#pairElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairElem(BasicParser.PairElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#pairElemFst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairElemFst(BasicParser.PairElemFstContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#pairElemSnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairElemSnd(BasicParser.PairElemSndContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(BasicParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprIntLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprIntLit(BasicParser.ExprIntLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprBoolLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBoolLit(BasicParser.ExprBoolLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprCharLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprCharLit(BasicParser.ExprCharLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprStrLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStrLit(BasicParser.ExprStrLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprPairLit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprPairLit(BasicParser.ExprPairLitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprParen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprParen(BasicParser.ExprParenContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#exprUnop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprUnop(BasicParser.ExprUnopContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(BasicParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#baseType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseType(BasicParser.BaseTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#pairType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairType(BasicParser.PairTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#pairElemType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPairElemType(BasicParser.PairElemTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(BasicParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#arrayElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayElem(BasicParser.ArrayElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#intLiter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLiter(BasicParser.IntLiterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#arrayLiter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLiter(BasicParser.ArrayLiterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BasicParser#ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdent(BasicParser.IdentContext ctx);
}