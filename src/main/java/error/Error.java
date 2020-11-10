package error;

public interface Error {

  String getOffendingToken();

  int getLine();

  int getPosition();
}
