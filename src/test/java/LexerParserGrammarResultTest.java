import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import parser.BasicLexer;
import parser.BasicParser;

import static org.junit.Assert.assertEquals;

public class LexerParserGrammarResultTest {

  @Test
  public void validArray() {
    assertEquals(
        "(prog begin (stat (stat (stat (stat (stat (statInitVar (type (arrayType (baseType int) [ ])) (ident a) = (assignRhs (arrayLiter [ (expr (exprIntLit (intLiter 1))) , (expr (exprIntLit (intLiter 2))) , (expr (exprIntLit (intLiter 3))) ])))) ; (stat (statInitVar (type (arrayType (baseType int) [ ])) (ident b) = (assignRhs (arrayLiter [ (expr (exprIntLit (intLiter 3))) , (expr (exprIntLit (intLiter 4))) ]))))) ; (stat (statInitVar (type (arrayType (arrayType (baseType int) [ ]) [ ])) (ident c) = (assignRhs (arrayLiter [ (expr (ident a)) , (expr (ident b)) ]))))) ; (stat (statPrintln println (expr (arrayElem (ident c) [ (expr (exprIntLit (intLiter 0))) ] [ (expr (exprIntLit (intLiter 2))) ]))))) ; (stat (statPrintln println (expr (arrayElem (ident c) [ (expr (exprIntLit (intLiter 1))) ] [ (expr (exprIntLit (intLiter 0))) ]))))) end <EOF>)",
        getProgTree(
            "begin\n int[] a = [1,2,3];\n int[] b = [3,4];\n int[][] c = [a,b];\n println c[0][2];\n println c[1][0]\nend"));
  }

  @Test
  public void validBasicExit() {
    assertEquals(
        "(prog begin (stat (statExit exit (expr (exprIntLit (intLiter - 1))))) end <EOF>)",
        getProgTree("begin exit -1 end"));
  }

  @Test
  public void validBasicSkip() {
    // Comment:
    assertEquals(
        "(prog begin (stat (statSkip skip)) end <EOF>)",
        getProgTree("begin # I can write stuff on a comment line\n skip end"));
    // Comment in-line:
    assertEquals(
        "(prog begin (stat (statSkip skip)) end <EOF>)",
        getProgTree("begin skip # I can write comments in-line\n end"));
    // Skip:
    assertEquals("(prog begin (stat (statSkip skip)) end <EOF>)", getProgTree("begin skip end"));
  }

  @Test
  public void validExpression() {
    // Priority check:
    assertEquals(
        "(prog begin (stat (stat (stat (statInitVar (type (baseType int)) (ident x) = (assignRhs (expr (exprIntLit (intLiter 2)))))) ; (stat (statInitVar (type (baseType int)) (ident y) = (assignRhs (expr (exprIntLit (intLiter 4))))))) ; (stat (statInitVar (type (baseType bool)) (ident b) = (assignRhs (expr (expr (ident x)) == (expr (ident y))))))) end <EOF>)",
        getProgTree("begin int x = 2 ; int y = 4 ; bool b = x == y end"));
    assertEquals(
        "(prog begin (stat (stat (statInitVar (type (baseType int)) (ident x) = (assignRhs (expr (expr (exprParen ( (expr (expr (expr (exprIntLit (intLiter 1))) + (expr (exprIntLit (intLiter 2)))) + (expr (exprIntLit (intLiter 3)))) ))) - (expr (expr (expr (exprParen ( (expr (expr (exprIntLit (intLiter 1))) + (expr (exprIntLit (intLiter 2)))) ))) * (expr (exprParen ( (expr (expr (exprIntLit (intLiter 1))) - (expr (expr (exprIntLit (intLiter 3))) / (expr (exprIntLit (intLiter 5))))) )))) / (expr (exprParen ( (expr (expr (expr (exprIntLit (intLiter 2))) * (expr (exprParen ( (expr (expr (exprIntLit (intLiter 18))) - (expr (exprIntLit (intLiter 17)))) )))) + (expr (exprParen ( (expr (expr (expr (expr (exprIntLit (intLiter 3))) * (expr (exprIntLit (intLiter 4)))) / (expr (exprIntLit (intLiter 4)))) + (expr (exprIntLit (intLiter 6)))) )))) )))))))) ; (stat (statExit exit (expr (ident x))))) end <EOF>)",
        getProgTree(
            "begin int x = (1 + 2 + 3) - (1 + 2) * (1 - 3 / 5) / (2 * (18 - 17) + (3 * 4 / 4 + 6)); exit x end"));
    // Operator and sign:
    assertEquals(
        "(prog begin (stat (statPrintln println (expr (expr (exprIntLit (intLiter 1))) - (expr (exprIntLit (intLiter - 2)))))) end <EOF>)",
        getProgTree("begin println 1 - -2 end"));
    assertEquals(
        "(prog begin (stat (statPrintln println (expr (expr (exprIntLit (intLiter 1))) - (expr (exprIntLit (intLiter + 2)))))) end <EOF>)",
        getProgTree("begin println 1 - +2 end"));
    assertEquals(
        "(prog begin (stat (stat (stat (statInitVar (type (baseType int)) (ident x) = (assignRhs (expr (exprIntLit (intLiter - 4)))))) ; (stat (statInitVar (type (baseType int)) (ident y) = (assignRhs (expr (exprIntLit (intLiter 2))))))) ; (stat (statPrintln println (expr (expr (ident x)) / (expr (ident y)))))) end <EOF>)",
        getProgTree("begin int x = -4; int y = 2; println x / y end"));
  }

  @Test
  public void validFunction() {
    assertEquals(
        "(prog begin (func (type (baseType int)) (ident f) ( ) is (stat (statReturn return (expr (exprIntLit (intLiter 0))))) end) (stat (statSkip skip)) end <EOF>)",
        getProgTree("begin int f() is return 0 end skip end"));
    assertEquals(
        "(prog begin (func (type (baseType int)) (ident f) ( ) is (stat (statReturn return (expr (exprIntLit (intLiter 0))))) end) (stat (stat (statInitVar (type (baseType int)) (ident x) = (assignRhs (call call (ident f) ( ))))) ; (stat (statPrintln println (expr (ident x))))) end <EOF>)",
        getProgTree("begin int f() is return 0 end int x = call f() ; println x end"));
  }

  @Test
  public void validCondition() {
    // Basic:
    assertEquals(
        "(prog begin (stat (statCond if (expr (exprBoolLit true)) then (stat (statSkip skip)) else (stat (statSkip skip)) fi)) end <EOF>)",
        getProgTree("begin if true then skip else skip fi end"));
    // White space:
    assertEquals(
        "(prog begin (stat (stat (stat (statInitVar (type (baseType int)) (ident a) = (assignRhs (expr (exprIntLit (intLiter 13)))))) ; (stat (statCond if (expr (expr (ident a)) == (expr (exprIntLit (intLiter 13)))) then (stat (statAssignVar (assignLhs (ident a)) = (assignRhs (expr (exprIntLit (intLiter 1)))))) else (stat (statAssignVar (assignLhs (ident a)) = (assignRhs (expr (exprIntLit (intLiter 0)))))) fi))) ; (stat (statPrintln println (expr (ident a))))) end <EOF>)",
        getProgTree("begin int a=13; if a==13then a=1else a=0fi; println a end"));
  }

  @Test
  public void validIO() {
    // Print:
    assertEquals(
        "(prog begin (stat (statPrint print (expr (exprStrLit \"Hello World!\\n\")))) end <EOF>)",
        getProgTree("begin print \"Hello World!\n\" end"));
    // Read:
    assertEquals(
        "(prog begin (stat (stat (statInitVar (type (baseType int)) (ident x) = (assignRhs (expr (exprIntLit (intLiter 10)))))) ; (stat (statRead read (assignLhs (ident x))))) end <EOF>)",
        getProgTree("begin int x = 10; read x end"));
  }

  @Test
  public void validPairs() {
    // Basic:
    assertEquals(
        "(prog begin (stat (statInitVar (type (pairType pair ( (pairElemType (baseType int)) , (pairElemType (baseType int)) ))) (ident p) = (assignRhs (newpair newpair ( (expr (exprIntLit (intLiter 10))) , (expr (exprIntLit (intLiter 3))) ))))) end <EOF>)",
        getProgTree("begin pair(int, int) p = newpair(10, 3) end"));
    // Nested:
    assertEquals(
        "(prog begin (stat (stat (statInitVar (type (pairType pair ( (pairElemType (baseType int)) , (pairElemType (baseType int)) ))) (ident p) = (assignRhs (newpair newpair ( (expr (exprIntLit (intLiter 10))) , (expr (exprIntLit (intLiter 3))) ))))) ; (stat (statInitVar (type (pairType pair ( (pairElemType (baseType int)) , (pairElemType pair) ))) (ident q) = (assignRhs (newpair newpair ( (expr (exprIntLit (intLiter 1))) , (expr (ident p)) )))))) end <EOF>)",
        getProgTree(
            "begin pair(int, int) p = newpair(10, 3) ; pair(int, pair) q = newpair(1, p) end"));
  }

  @Test
  public void validRuntimeErr() {
    // Array index out of bounds:
    assertEquals(
        "(prog begin (stat (stat (statInitVar (type (arrayType (baseType int) [ ])) (ident a) = (assignRhs (arrayLiter [ (expr (exprIntLit (intLiter 1))) , (expr (exprIntLit (intLiter 2))) , (expr (exprIntLit (intLiter 3))) ])))) ; (stat (statPrintln println (expr (arrayElem (ident a) [ (expr (exprIntLit (intLiter 3))) ]))))) end <EOF>)",
        getProgTree("begin int[] a = [1,2,3] ; println a[3] end"));
    // Null dereference:
    assertEquals(
        "(prog begin (stat (stat (statInitVar (type (pairType pair ( (pairElemType pair) , (pairElemType pair) ))) (ident a) = (assignRhs (expr (exprPairLit null))))) ; (stat (statFree free (expr (ident a))))) end <EOF>)",
        getProgTree("begin pair(pair, pair) a = null ; free a end"));
  }

  @Test
  public void validScope() {
    assertEquals(
        "(prog begin (stat (statBegin begin (stat (statBegin begin (stat (statBegin begin (stat (statBegin begin (stat (statSkip skip)) end)) end)) end)) end)) end <EOF>)",
        getProgTree("begin begin begin begin begin skip end end end end end"));
  }

  @Test
  public void validSequence() {
    assertEquals(
        "(prog begin (stat (stat (statExit exit (expr (exprIntLit (intLiter 42))))) ; (stat (statPrintln println (expr (exprStrLit \"Should not print this.\"))))) end <EOF>)",
        getProgTree("begin exit 42 ; println \"Should not print this.\" end"));
  }

  @Test
  public void validVariable() {
    // Leading zeros:
    assertEquals(
        "(prog begin (stat (statInitVar (type (baseType int)) (ident x) = (assignRhs (expr (exprIntLit (intLiter 000005)))))) end <EOF>)",
        getProgTree("begin int x = 000005 end"));
    // Empty string:
    assertEquals(
        "(prog begin (stat (statInitVar (type (baseType string)) (ident s) = (assignRhs (expr (exprStrLit \"\"))))) end <EOF>)",
        getProgTree("begin string s = \"\" end"));
  }

  @Test
  public void validWhile() {
    assertEquals(
        "(prog begin (stat (stat (statLoop while (expr (exprBoolLit false)) do (stat (statSkip skip)) done)) ; (stat (statSkip skip))) end <EOF>)",
        getProgTree("begin while false do skip done ; skip end"));
  }

  private String getProgTree(String prog) {
    BasicLexer lexer = new BasicLexer(CharStreams.fromString(prog));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    BasicParser parser = new BasicParser(tokens);
    parser.setBuildParseTree(true);
    return parser.prog().toStringTree(parser);
  }
}
