package org.openoffice.gdocs.util;

import java.util.EventObject;

public class IOEvent extends EventObject {
  private static final int UNKNOWN_SIZE = -1;
  private final int totalSize;
  private final int completedSize;
  private final Throwable throwable;
  private final String message;
  private final boolean completed;
  
  public IOEvent(Object source, int totalSize, int completedSize, Throwable throwable, String message) {
      this(source,totalSize,completedSize,throwable,message,false);
  }  
  public IOEvent(Object source, int totalSize, int completedSize, Throwable throwable, String message, boolean completed) {
    super(source);
    this.totalSize = totalSize;
    this.completedSize = completedSize;
    this.throwable = throwable;
    this.message = message;
    this.completed = completed;
  }

    public static int getUNKNOWN_SIZE() {
        return UNKNOWN_SIZE;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getCompletedSize() {
        return completedSize;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getMessage() {
        return message;
    }
    
    public boolean isCompleted() {
        return completed;
    }
}
