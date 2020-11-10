package main;

import java.util.HashMap;
import java.util.Map;

public class StackManager {

  private Map<String, Integer> varOffsetMap;
  private int currOffset;
  private int allocOffset;
  private StackManager encStackManager;
  private int funcOffset;

  public StackManager(StackManager encStackManager, int currOffset) {
    varOffsetMap = new HashMap<>();
    this.currOffset = currOffset;
    this.allocOffset = this.currOffset;
    this.encStackManager = encStackManager;
    this.funcOffset = encStackManager.getFuncOffset();
  }

  public StackManager(int currOffset) {
    varOffsetMap = new HashMap<>();
    this.currOffset = currOffset;
    this.allocOffset = this.currOffset;
    this.encStackManager = null;
  }

  public int getCurrOffset() {
    return currOffset;
  }

  public void setCurrOffset(int currOffset) {
    this.currOffset = currOffset;
  }

  public int getAllocOffset() {
    return allocOffset;
  }

  public void setAllocOffset(int allocOffset) {
    this.allocOffset = allocOffset;
  }

  public StackManager getEncStackManager() {
    return encStackManager;
  }

  public int getFuncOffset() {
    return funcOffset;
  }

  public void setFuncOffset(int funcOffset) {
    this.funcOffset = funcOffset;
  }

  public void addCurrOffset(int offset) {
    this.currOffset += offset;
  }

  public void addVarOffset(String varName) {
    varOffsetMap.put(varName, currOffset - allocOffset);
  }

  public void addVarOffset(String varName, int offset) {
    varOffsetMap.put(varName, currOffset - offset);
  }

  private int getOffset(String varName) {
    return currOffset - varOffsetMap.get(varName);
  }

  public int lookupAllOffset(String varName) {
    int offsetDepth = 0;
    StackManager stackManager = this;
    while (stackManager != null) {
      if (stackManager.contains(varName)) {
        return stackManager.getOffset(varName) + offsetDepth;
      }
      offsetDepth += stackManager.getCurrOffset();
      stackManager = stackManager.getEncStackManager();
    }
    return 0;
  }

  private boolean contains(String varName) {
    return this.varOffsetMap.containsKey(varName);
  }
}
