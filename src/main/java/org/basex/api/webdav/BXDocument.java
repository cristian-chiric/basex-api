package org.basex.api.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.basex.api.HTTPSession;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.io.IO;
import org.basex.server.Query;
import org.basex.server.Session;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;

/**
 * WebDAV resource representing an XML document.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXDocument extends BXAbstractResource implements FileResource {
  /**
   * Constructor.
   * @param dbname name of database this document belongs to.
   * @param docpath document path to root
   * @param s current session
   */
  public BXDocument(final String dbname, final String docpath,
      final HTTPSession s) {
    super(dbname, docpath, s);
  }

  @Override
  public Long getContentLength() {
    return null;
  }

  @Override
  public Date getCreateDate() {
    return null;
  }

  @Override
  public Long getMaxAgeSeconds(final Auth auth) {
    return null;
  }

  @Override
  public String processForm(final Map<String, String> parameters,
      final Map<String, FileItem> files) throws BadRequestException {
    return null;
  }

  @Override
  public String getContentType(final String accepts) {
    return MIMETYPEXML;
  }

  @Override
  public void sendContent(final OutputStream out, final Range range,
      final Map<String, String> params, final String contentType)
      throws IOException, BadRequestException {

    Session s = null;
    try {
      s = session.login();
      s.setOutputStream(out);
      final Query q = s.query("collection($path)");
      q.bind("$path", db + SEP + path);
      q.execute();
    } catch(final Exception ex) {
      error(ex);
    } finally {
      try { if(s != null) s.close(); } catch(final IOException e) { error(e); }
    }
  }

  @Override
  protected void copyToRoot(final Session s, final String n)
      throws BaseXException {

    // document is copied to the root: create new database with it
    final String nm = n.endsWith(IO.XMLSUFFIX) ?
        n.substring(0, n.length() - IO.XMLSUFFIX.length()) : n;
    s.execute(new CreateDB(nm));
    add(s, nm, "", n);
    s.execute(new Close());
  }

  @Override
  protected void copyTo(final Session s, final BXFolder f, final String n)
      throws BaseXException {

    // folder is copied to a folder in a database
    add(s, f.db, f.path, n);
    deleteDummy(s, f.db, f.path);
  }

  /**
   * Add a document to the specified target.
   * @param s current session
   * @param trgdb target database
   * @param trgdir target directory
   * @param name new name
   * @throws BaseXException database exception
   */
  protected void add(final Session s, final String trgdb, final String trgdir,
      final String name) throws BaseXException {

    final Query q = s.query("db:add($db, collection($doc), $name, $path)");
    q.bind("$db", trgdb);
    q.bind("$doc", db + SEP + path);
    q.bind("$name", name);
    q.bind("$path", trgdir);
    q.execute();
  }
}