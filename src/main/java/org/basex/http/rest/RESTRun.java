package org.basex.http.rest;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;

/**
 * REST-based evaluation of XQuery files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTRun extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTRun(final String in, final Map<String, String[]> vars, final byte[] it) {
    super(in, vars, it);
  }

  @Override
  void run(final HTTPContext http) throws HTTPException, IOException {
    // get root directory for files
    final String path = http.context().mprop.get(MainProp.HTTPPATH);

    // check if file is not found, is a folder or points to parent folder
    final IOFile root = new IOFile(path);
    final IOFile io = new IOFile(path, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPErr.NOT_FOUND_X.thrw(RES_NOT_FOUND_X, input);

    // perform query
    query(string(io.read()), http, io.path());
  }
}
