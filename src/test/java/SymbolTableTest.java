import org.junit.Test;
import symbolTable.*;
import types.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SymbolTableTest {
  private SymbolTable symbolTable = new SymbolTable(null);
  private Map<String, Node> map;

  @Test
  public void intVariable() {
    symbolTable.insert("x", new IntType());
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("x"));
    assertEquals(map.get("x").getTypeToken(), TypeToken.INT);
  }

  @Test
  public void boolParam() {
    List<Type> typeParams = new ArrayList<>();
    typeParams.add(new BoolType());
    Function function = new Function("test", new IntType(), typeParams);
    symbolTable.insert(function.getName(), function);
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("test"));
    assertEquals(((Function) map.get("test")).getParamType(0).getTypeToken(), TypeToken.BOOL);
  }

  @Test
  public void functionChar() {
    Function function = new Function("returnChar", new CharType(), new ArrayList<>());
    symbolTable.insert(function.getName(), function);
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("returnChar"));
    assertEquals(map.get("returnChar").getTypeToken(), TypeToken.FUNCTION);
  }

  @Test
  public void stringVariable() {
    symbolTable.insert("string", new StringType());
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("string"));
    assertEquals(map.get("string").getTypeToken(), TypeToken.STRING);
  }

  @Test
  public void intArrayVariable() {
    ArrayType array = new ArrayType(new IntType());
    symbolTable.insert("array", array);
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("array"));
    assertEquals(map.get("array").getTypeToken(), TypeToken.ARRAY);
    assertEquals(((ArrayType) map.get("array")).getElemType().getTypeToken(), TypeToken.INT);
  }

  @Test
  public void intPairVariable() {
    Type intType = new IntType();
    PairType pair = new PairType(intType, intType);
    symbolTable.insert("pair", pair);
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("pair"));
    assertEquals(((PairType) map.get("pair")).getFirst(), intType);
    assertEquals(((PairType) map.get("pair")).getSecond(), intType);
  }

  @Test
  public void intCharPair() {
    Type intType = new IntType();
    Type charType = new CharType();
    PairType pair = new PairType(intType, charType);
    symbolTable.insert("intCharPair", pair);
    map = symbolTable.getDictionary();
    assertTrue(map.containsKey("intCharPair"));
    assertEquals(((PairType) map.get("intCharPair")).getFirst(), intType);
    assertEquals(((PairType) map.get("intCharPair")).getSecond(), charType);
  }
}
