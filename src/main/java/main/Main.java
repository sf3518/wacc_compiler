package main;

import ast.top.ProgAST;
import error.AntlrErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.BasicLexer;
import parser.BasicParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  public static int EXIT_SYNTAX_ERROR = 100;
  public static int EXIT_SEMANTIC_ERROR = 200;
  public static int EXIT_SUCCESS = 0;

  public static void main(String[] args) throws IOException {
    // Set up Antlr:
    String filePath = args[0];
    BasicLexer lexer = new BasicLexer(new ANTLRFileStream(filePath));
    lexer.removeErrorListeners();
    AntlrErrorListener errorListener = AntlrErrorListener.INSTANCE;
    lexer.addErrorListener(errorListener);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    BasicParser parser = new BasicParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    // Generate parse tree:
    BasicParser.ProgContext progContext = parser.prog();
    // Syntax check:
    if (errorListener.errorDetected()) {
      errorListener.printErrorMessage();
      System.exit(EXIT_SYNTAX_ERROR);
    }
    SyntaxVisitor syntaxVisitor = new SyntaxVisitor();
    if (!syntaxVisitor.visitProg(progContext)) {
      syntaxVisitor.getSyntaxErrHandler().printErrorMessage();
      System.exit(EXIT_SYNTAX_ERROR);
    }
    // Semantic check:
    // Generate AST:
    // Generate symbol table:
    BuildASTVisitor visitor = new BuildASTVisitor();
    ProgAST progAST = visitor.visitProg(progContext);
    // Create output file:
    Path path = Paths.get(filePath);
    String fileName = path.getFileName().toString();
    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    fileName += ".s";
    try {
      File myObj = new File(fileName);
      // Check if file already exists
      if (!myObj.createNewFile()) {
        System.out.println("File already exists.");
      }
    } catch (IOException e) {
      System.out.println("An error occurred in creating the assembly file.");
      e.printStackTrace();
    }
    // Write assembly code:
    CodeGenVisitor codeGenVisitor = new CodeGenVisitor();
    codeGenVisitor.visitProg(progAST);
    codeGenVisitor.getCodeGenerator().generateCode(fileName);
    System.exit(EXIT_SUCCESS);
  }
}
