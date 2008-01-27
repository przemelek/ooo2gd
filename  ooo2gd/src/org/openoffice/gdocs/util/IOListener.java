package org.openoffice.gdocs.util;

import java.util.EventListener;

public interface IOListener extends EventListener {
  void ioProgress(IOEvent ioEvent);
}
