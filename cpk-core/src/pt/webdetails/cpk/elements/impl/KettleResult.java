/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.elements.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper class for org.pentaho.di.core.Result to have a Serializable result for disk caching.
 */
public final class KettleResult implements Serializable {

  // region Constants and Definitions
  private static final long serialVersionUID = 110982374129L;

  protected transient Log logger = LogFactory.getLog( this.getClass() );
  private transient Result result;

  private KettleType kettleType;

  public static enum KettleType {
    JOB, TRANSFORMATION
  }
  // endregion

  // region Getters / Setters

  public boolean wasExecutedSuccessfully() { return this.result.getResult(); }

  public int getExitStatus() { return this.result.getExitStatus(); }

  public long getNumberOfErrors() { return this.result.getNrErrors(); }

  public List<ResultFile> getFiles() { return this.result.getResultFilesList(); }

  public List<RowMetaAndData> getRows() {
    if ( this.result.getRows() == null) {
      return Collections.emptyList();
    }

    return this.result.getRows();
  }

  /**
   * Gets the type (job or transformation) of the kettle that returned this result.
   * @return
   */
  public KettleType getKettleType() { return this.kettleType; }
  public KettleResult setKettleType( KettleType kettleType ) {
    this.kettleType = kettleType;
    return this;
  }
  // endregion

  // region Constructors

  public KettleResult( Result result ) {
    this.result = result;
  }

  // endregion

  // region Serialization
  private void writeObject( java.io.ObjectOutputStream out ) throws IOException
  {
    out.defaultWriteObject();

    String resultXmlString = this.result.getXML();
    out.writeUTF( resultXmlString );
  }

  private void readObject( java.io.ObjectInputStream in ) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    String resultXmlString = in.readUTF();
    try {
      Document document = XMLHandler.loadXMLString( resultXmlString );
      Node resultNode = XMLHandler.getSubNode( document, Result.XML_TAG );
      this.result = new Result( resultNode );
    } catch ( KettleException e ) {
      this.logger.error( "Unable to deserialize KettleResult.", e );
    }
  }

  // endregion

}
