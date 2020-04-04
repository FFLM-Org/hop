/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.pipeline.transforms.ldifinput;

import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.fileinput.FileInputList;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaDate;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.iVariables;
import org.apache.hop.core.vfs.HopVFS;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.resource.ResourceDefinition;
import org.apache.hop.resource.IResourceNaming;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.TransformMetaInterface;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

public class LDIFInputMeta extends BaseTransformMeta implements TransformMetaInterface {
  private static Class<?> PKG = LDIFInputMeta.class; // for i18n purposes, needed by Translator!!

  public static final String[] RequiredFilesDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };
  private static final String NO = "N";
  private static final String YES = "Y";
  /**
   * Array of filenames
   */
  private String[] fileName;

  /**
   * Wildcard or filemask (regular expression)
   */
  private String[] fileMask;

  /**
   * Wildcard or filemask to exclude (regular expression)
   */
  private String[] excludeFileMask;

  /**
   * Flag indicating that we should include the filename in the output
   */
  private boolean includeFilename;

  /**
   * The name of the field in the output containing the filename
   */
  private String filenameField;

  /**
   * Flag indicating that a row number field should be included in the output
   */
  private boolean includeRowNumber;

  /**
   * The name of the field in the output containing the row number
   */
  private String rowNumberField;

  /**
   * The maximum number or lines to read
   */
  private long rowLimit;

  /**
   * The fields to import...
   */
  private LDIFInputField[] inputFields;

  private boolean addtoresultfilename;

  private String multiValuedSeparator;

  private boolean includeContentType;

  private String contentTypeField;

  private String DNField;

  private boolean includeDN;

  /**
   * file name from previous fields
   **/
  private boolean filefield;

  private String dynamicFilenameField;

  /**
   * Array of boolean values as string, indicating if a file is required.
   */
  private String[] fileRequired;

  /**
   * Array of boolean values as string, indicating if we need to fetch sub folders.
   */
  private String[] includeSubFolders;

  /**
   * Additional fields
   **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;
  private String sizeFieldName;

  public LDIFInputMeta() {
    super(); // allocate BaseTransformMeta
  }

  /**
   * @return Returns the extensionFieldName.
   */
  public String getExtensionField() {
    return extensionFieldName;
  }

  /**
   * @param field The extensionFieldName to set.
   */
  public void setExtensionField( String field ) {
    extensionFieldName = field;
  }

  /**
   * @return Returns the sizeFieldName.
   */
  public String getSizeField() {
    return sizeFieldName;
  }

  /**
   * @param field The sizeFieldName to set.
   */
  public void setSizeField( String field ) {
    sizeFieldName = field;
  }

  /**
   * @return Returns the shortFileFieldName.
   */
  public String getShortFileNameField() {
    return shortFileFieldName;
  }

  /**
   * @param field The shortFileFieldName to set.
   */
  public void setShortFileNameField( String field ) {
    shortFileFieldName = field;
  }

  /**
   * @return Returns the pathFieldName.
   */
  public String getPathField() {
    return pathFieldName;
  }

  /**
   * @param field The pathFieldName to set.
   */
  public void setPathField( String field ) {
    this.pathFieldName = field;
  }

  /**
   * @return Returns the hiddenFieldName.
   * @deprecated due to standards violation (is... reserved for boolean getters)
   */
  @Deprecated
  public String isHiddenField() {
    return hiddenFieldName;
  }

  /**
   * @return Returns the hiddenFieldName.
   */
  public String getHiddenField() {
    return hiddenFieldName;
  }

  /**
   * @param field The hiddenFieldName to set.
   * @deprecated due to standards violation (is... reserved for boolean getters)
   */
  @Deprecated
  public void setIsHiddenField( String field ) {
    hiddenFieldName = field;
  }

  /**
   * @param field The hiddenFieldName to set.
   */
  public void setHiddenField( String field ) {
    hiddenFieldName = field;
  }

  /**
   * @return Returns the lastModificationTimeFieldName.
   */
  public String getLastModificationDateField() {
    return lastModificationTimeFieldName;
  }

  /**
   * @param field The lastModificationTimeFieldName to set.
   */
  public void setLastModificationDateField( String field ) {
    lastModificationTimeFieldName = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getUriField() {
    return uriNameFieldName;
  }

  /**
   * @param field The uriNameFieldName to set.
   */
  public void setUriField( String field ) {
    uriNameFieldName = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getRootUriField() {
    return rootUriNameFieldName;
  }

  /**
   * @param field The rootUriNameFieldName to set.
   */
  public void setRootUriField( String field ) {
    rootUriNameFieldName = field;
  }

  /**
   * @return Returns the excludeFileMask.
   * @deprecated due to typo
   */
  @Deprecated
  public String[] getExludeFileMask() {
    return excludeFileMask;
  }

  /**
   * @return Returns the excludeFileMask.
   */
  public String[] getExcludeFileMask() {
    return excludeFileMask;
  }

  /**
   * @param excludeFileMask The excludeFileMask to set.
   */
  public void setExcludeFileMask( String[] excludeFileMask ) {
    this.excludeFileMask = excludeFileMask;
  }

  /**
   * @return Returns the input fields.
   */
  public LDIFInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields The input fields to set.
   */
  public void setInputFields( LDIFInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the fileMask.
   */
  public String[] getFileMask() {
    return fileMask;
  }

  /**
   * @param fileMask The fileMask to set.
   */
  public void setFileMask( String[] fileMask ) {
    this.fileMask = fileMask;
  }

  public String[] getFileRequired() {
    return this.fileRequired;
  }

  public void setFileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      this.fileRequired[ i ] = getRequiredFilesCode( fileRequiredin[ i ] );
    }
  }

  public String[] getIncludeSubFolders() {
    return includeSubFolders;
  }

  public void setIncludeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      this.includeSubFolders[ i ] = getRequiredFilesCode( includeSubFoldersin[ i ] );
    }
  }

  public String getRequiredFilesCode( String tt ) {
    if ( tt == null ) {
      return RequiredFilesCode[ 0 ];
    }
    if ( tt.equals( RequiredFilesDesc[ 1 ] ) ) {
      return RequiredFilesCode[ 1 ];
    } else {
      return RequiredFilesCode[ 0 ];
    }
  }

  public String getRequiredFilesDesc( String tt ) {
    if ( tt == null ) {
      return RequiredFilesDesc[ 0 ];
    }
    if ( tt.equals( RequiredFilesCode[ 1 ] ) ) {
      return RequiredFilesDesc[ 1 ];
    } else {
      return RequiredFilesDesc[ 0 ];
    }
  }

  /**
   * @return Returns the fileName.
   */
  public String[] getFileName() {
    return fileName;
  }

  /**
   * @param fileName The fileName to set.
   */
  public void setFileName( String[] fileName ) {
    this.fileName = fileName;
  }

  /**
   * @param filefield The filefield to set.
   */
  public void setFileField( boolean filefield ) {
    this.filefield = filefield;
  }

  /**
   * @return Returns the File field.
   */
  public boolean isFileField() {
    return filefield;
  }

  /**
   * @return Returns the includeFilename.
   * @deprecated due to standards violation
   */

  @Deprecated
  public boolean includeFilename() {
    return includeFilename;
  }

  /**
   * @return Returns the includeFilename.
   */
  public boolean getIncludeFilename() {
    return includeFilename;
  }

  /**
   * @param includeFilename The includeFilename to set.
   */
  public void setIncludeFilename( boolean includeFilename ) {
    this.includeFilename = includeFilename;
  }

  /**
   * @return Returns the includeRowNumber.
   * @deprecated due to standards violation
   */
  @Deprecated
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean getIncludeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @param includeRowNumber The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the includeContentType.
   * @deprecated due to standards violation
   */
  @Deprecated
  public boolean includeContentType() {
    return includeContentType;
  }

  /**
   * @return Returns the includeContentType.
   */
  public boolean getIncludeContentType() {
    return includeContentType;
  }

  /**
   * @param includeContentType The includeRowNumber to set.
   */
  public void setIncludeContentType( boolean includeContentType ) {
    this.includeContentType = includeContentType;
  }

  /**
   * @param includeDN The includeDN to set.
   */
  public void setIncludeDN( boolean includeDN ) {
    this.includeDN = includeDN;
  }

  /**
   * @return Returns the includeDN.
   * @deprecated due to standards violation
   */
  @Deprecated
  public boolean IncludeDN() {
    return includeDN;
  }

  /**
   * @return Returns the includeDN.
   */
  public boolean getIncludeDN() {
    return includeDN;
  }

  /**
   * @param multiValuedSeparator The multi-valued separator filed.
   */
  public void setMultiValuedSeparator( String multiValuedSeparator ) {
    this.multiValuedSeparator = multiValuedSeparator;
  }

  /**
   * @return Returns the multi valued separator.
   */
  public String getMultiValuedSeparator() {
    return multiValuedSeparator;
  }

  /**
   * @param addtoresultfilename The addtoresultfilename to set.
   */
  public void setAddToResultFilename( boolean addtoresultfilename ) {
    this.addtoresultfilename = addtoresultfilename;
  }

  /**
   * @return Returns the addtoresultfilename.
   * @deprecated because of standards violation
   */
  @Deprecated
  public boolean AddToResultFilename() {
    return addtoresultfilename;
  }

  /**
   * @return Returns the addtoresultfilename.
   */
  public boolean getAddToResultFilename() {
    return addtoresultfilename;
  }

  /**
   * @return Returns the rowLimit.
   */
  public long getRowLimit() {
    return rowLimit;
  }

  /**
   * @param rowLimit The rowLimit to set.
   */
  public void setRowLimit( long rowLimit ) {
    this.rowLimit = rowLimit;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param rowNumberField The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  /**
   * @return Returns the filenameField.
   */
  public String getFilenameField() {
    return filenameField;
  }

  /**
   * @return Returns the dynamic filename field (from previous transforms)
   */
  public String getDynamicFilenameField() {
    return dynamicFilenameField;
  }

  /**
   * @param dynamicFilenameField The dynamic filename field to set.
   */
  public void setDynamicFilenameField( String dynamicFilenameField ) {
    this.dynamicFilenameField = dynamicFilenameField;
  }

  /**
   * @param filenameField The filenameField to set.
   */
  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  /**
   * @return Returns the contentTypeField.
   */
  public String getContentTypeField() {
    return contentTypeField;
  }

  /**
   * @param contentTypeField The contentTypeField to set.
   */
  public void setContentTypeField( String contentTypeField ) {
    this.contentTypeField = contentTypeField;
  }

  /**
   * @return Returns the DNField.
   */
  public String getDNField() {
    return DNField;
  }

  /**
   * @param DNField The DNField to set.
   */
  public void setDNField( String DNField ) {
    this.DNField = DNField;
  }

  public void loadXML( Node transformNode, IMetaStore metaStore ) throws HopXMLException {
    readData( transformNode );
  }

  public Object clone() {
    LDIFInputMeta retval = (LDIFInputMeta) super.clone();

    int nrFiles = fileName.length;
    int nrFields = inputFields.length;

    retval.allocate( nrFiles, nrFields );

    System.arraycopy( fileName, 0, retval.fileName, 0, nrFiles );
    System.arraycopy( fileMask, 0, retval.fileMask, 0, nrFiles );
    System.arraycopy( excludeFileMask, 0, retval.excludeFileMask, 0, nrFiles );
    System.arraycopy( fileRequired, 0, retval.fileRequired, 0, nrFiles );
    System.arraycopy( includeSubFolders, 0, retval.includeSubFolders, 0, nrFiles );

    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[ i ] != null ) {
        retval.inputFields[ i ] = (LDIFInputField) inputFields[ i ].clone();
      }
    }

    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " ).append( XMLHandler.addTagValue( "filefield", filefield ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicFilenameField", dynamicFilenameField ) );
    retval.append( "    " + XMLHandler.addTagValue( "include", includeFilename ) );
    retval.append( "    " + XMLHandler.addTagValue( "include_field", filenameField ) );
    retval.append( "    " + XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " + XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "    " + XMLHandler.addTagValue( "contenttype", includeContentType ) );
    retval.append( "    " + XMLHandler.addTagValue( "contenttype_field", contentTypeField ) );
    retval.append( "    " + XMLHandler.addTagValue( "dn_field", DNField ) );
    retval.append( "    " + XMLHandler.addTagValue( "dn", includeDN ) );
    retval.append( "    " + XMLHandler.addTagValue( "addtoresultfilename", addtoresultfilename ) );
    retval.append( "    " + XMLHandler.addTagValue( "multiValuedSeparator", multiValuedSeparator ) );

    retval.append( "    <file>" + Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      retval.append( "      " + XMLHandler.addTagValue( "name", fileName[ i ] ) );
      retval.append( "      " + XMLHandler.addTagValue( "filemask", fileMask[ i ] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_filemask", excludeFileMask[ i ] ) );
      retval.append( "      " + XMLHandler.addTagValue( "file_required", fileRequired[ i ] ) );
      retval.append( "      " + XMLHandler.addTagValue( "include_subfolders", includeSubFolders[ i ] ) );
    }
    retval.append( "      </file>" + Const.CR );

    retval.append( "    <fields>" + Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      LDIFInputField field = inputFields[ i ];
      retval.append( field.getXML() );
    }
    retval.append( "      </fields>" + Const.CR );
    retval.append( "    " + XMLHandler.addTagValue( "limit", rowLimit ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "shortFileFieldName", shortFileFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "pathFieldName", pathFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "hiddenFieldName", hiddenFieldName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "lastModificationTimeFieldName", lastModificationTimeFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "uriNameFieldName", uriNameFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rootUriNameFieldName", rootUriNameFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "extensionFieldName", extensionFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sizeFieldName", sizeFieldName ) );
    return retval.toString();
  }

  private void readData( Node transformNode ) throws HopXMLException {
    try {
      filefield = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transformNode, "filefield" ) );
      dynamicFilenameField = XMLHandler.getTagValue( transformNode, "dynamicFilenameField" );
      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transformNode, "include" ) );
      filenameField = XMLHandler.getTagValue( transformNode, "include_field" );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transformNode, "rownum" ) );
      rowNumberField = XMLHandler.getTagValue( transformNode, "rownum_field" );
      includeContentType = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transformNode, "contenttype" ) );
      contentTypeField = XMLHandler.getTagValue( transformNode, "contenttype_field" );
      DNField = XMLHandler.getTagValue( transformNode, "dn_field" );
      includeDN = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transformNode, "dn" ) );
      addtoresultfilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transformNode, "addtoresultfilename" ) );
      multiValuedSeparator = XMLHandler.getTagValue( transformNode, "multiValuedSeparator" );

      Node filenode = XMLHandler.getSubNode( transformNode, "file" );
      Node fields = XMLHandler.getSubNode( transformNode, "fields" );
      int nrFiles = XMLHandler.countNodes( filenode, "name" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, "name", i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, "filemask", i );
        Node excludefilemasknode = XMLHandler.getSubNodeByNr( filenode, "exclude_filemask", i );
        Node fileRequirednode = XMLHandler.getSubNodeByNr( filenode, "file_required", i );
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, "include_subfolders", i );
        fileName[ i ] = XMLHandler.getNodeValue( filenamenode );
        fileMask[ i ] = XMLHandler.getNodeValue( filemasknode );
        excludeFileMask[ i ] = XMLHandler.getNodeValue( excludefilemasknode );
        fileRequired[ i ] = XMLHandler.getNodeValue( fileRequirednode );
        includeSubFolders[ i ] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        LDIFInputField field = new LDIFInputField( fnode );
        inputFields[ i ] = field;
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( transformNode, "limit" ), 0L );
      shortFileFieldName = XMLHandler.getTagValue( transformNode, "shortFileFieldName" );
      pathFieldName = XMLHandler.getTagValue( transformNode, "pathFieldName" );
      hiddenFieldName = XMLHandler.getTagValue( transformNode, "hiddenFieldName" );
      lastModificationTimeFieldName = XMLHandler.getTagValue( transformNode, "lastModificationTimeFieldName" );
      uriNameFieldName = XMLHandler.getTagValue( transformNode, "uriNameFieldName" );
      rootUriNameFieldName = XMLHandler.getTagValue( transformNode, "rootUriNameFieldName" );
      extensionFieldName = XMLHandler.getTagValue( transformNode, "extensionFieldName" );
      sizeFieldName = XMLHandler.getTagValue( transformNode, "sizeFieldName" );
    } catch ( Exception e ) {
      throw new HopXMLException( "Unable to load transform info from XML", e );
    }
  }

  public void allocate( int nrfiles, int nrFields ) {
    fileName = new String[ nrfiles ];
    fileMask = new String[ nrfiles ];
    excludeFileMask = new String[ nrfiles ];
    fileRequired = new String[ nrfiles ];
    includeSubFolders = new String[ nrfiles ];

    inputFields = new LDIFInputField[ nrFields ];
  }

  public void setDefault() {
    shortFileFieldName = null;
    pathFieldName = null;
    hiddenFieldName = null;
    lastModificationTimeFieldName = null;
    uriNameFieldName = null;
    rootUriNameFieldName = null;
    extensionFieldName = null;
    sizeFieldName = null;

    filefield = false;
    dynamicFilenameField = "";
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    includeContentType = false;
    includeDN = false;
    contentTypeField = "";
    DNField = "";
    multiValuedSeparator = ",";
    addtoresultfilename = false;

    int nrFiles = 0;
    int nrFields = 0;

    allocate( nrFiles, nrFields );

    for ( int i = 0; i < nrFiles; i++ ) {
      fileName[ i ] = "filename" + ( i + 1 );
      fileMask[ i ] = "";
      excludeFileMask[ i ] = "";
      fileRequired[ i ] = RequiredFilesCode[ 0 ];
      includeSubFolders[ i ] = RequiredFilesCode[ 0 ];
    }

    for ( int i = 0; i < nrFields; i++ ) {
      inputFields[ i ] = new LDIFInputField( "field" + ( i + 1 ) );
    }

    rowLimit = 0;
  }

  public void getFields( IRowMeta r, String name, IRowMeta[] info, TransformMeta nextTransform,
                         iVariables variables, IMetaStore metaStore ) throws HopTransformException {
    int i;
    for ( i = 0; i < inputFields.length; i++ ) {
      LDIFInputField field = inputFields[ i ];

      int type = field.getType();
      if ( type == IValueMeta.TYPE_NONE ) {
        type = IValueMeta.TYPE_STRING;
      }
      try {
        IValueMeta v =
          ValueMetaFactory.createValueMeta( variables.environmentSubstitute( field.getName() ), type );
        v.setLength( field.getLength(), field.getPrecision() );
        v.setOrigin( name );
        r.addValueMeta( v );
      } catch ( Exception e ) {
        throw new HopTransformException( e );
      }

    }

    if ( includeFilename ) {
      IValueMeta v = new ValueMetaString( variables.environmentSubstitute( filenameField ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( includeRowNumber ) {
      IValueMeta v = new ValueMetaInteger( variables.environmentSubstitute( rowNumberField ) );
      v.setLength( IValueMeta.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeContentType ) {
      IValueMeta v =
        new ValueMetaString( variables.environmentSubstitute( contentTypeField ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeDN ) {
      IValueMeta v = new ValueMetaString( variables.environmentSubstitute( DNField ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    // Add additional fields

    if ( getShortFileNameField() != null && getShortFileNameField().length() > 0 ) {
      IValueMeta v =
        new ValueMetaString( variables.environmentSubstitute( getShortFileNameField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getExtensionField() != null && getExtensionField().length() > 0 ) {
      IValueMeta v =
        new ValueMetaString( variables.environmentSubstitute( getExtensionField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getPathField() != null && getPathField().length() > 0 ) {
      IValueMeta v = new ValueMetaString( variables.environmentSubstitute( getPathField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getSizeField() != null && getSizeField().length() > 0 ) {
      IValueMeta v = new ValueMetaInteger( variables.environmentSubstitute( getSizeField() ) );
      v.setOrigin( name );
      v.setLength( 9 );
      r.addValueMeta( v );
    }
    if ( isHiddenField() != null && isHiddenField().length() > 0 ) {
      IValueMeta v =
        new ValueMetaBoolean( variables.environmentSubstitute( isHiddenField() ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( getLastModificationDateField() != null && getLastModificationDateField().length() > 0 ) {
      IValueMeta v =
        new ValueMetaDate( variables.environmentSubstitute( getLastModificationDateField() ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getUriField() != null && getUriField().length() > 0 ) {
      IValueMeta v = new ValueMetaString( variables.environmentSubstitute( getUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( getRootUriField() != null && getRootUriField().length() > 0 ) {
      IValueMeta v =
        new ValueMetaString( variables.environmentSubstitute( getRootUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public FileInputList getFiles( iVariables variables ) {
    return FileInputList.createFileList(
      variables, fileName, fileMask, excludeFileMask, fileRequired, includeSubFolderBoolean() );
  }

  private boolean[] includeSubFolderBoolean() {
    int len = fileName.length;
    boolean[] includeSubFolderBoolean = new boolean[ len ];
    for ( int i = 0; i < len; i++ ) {
      includeSubFolderBoolean[ i ] = YES.equalsIgnoreCase( includeSubFolders[ i ] );
    }
    return includeSubFolderBoolean;
  }

  public void check( List<CheckResultInterface> remarks, PipelineMeta pipelineMeta, TransformMeta transformMeta,
                     IRowMeta prev, String[] input, String[] output, IRowMeta info, iVariables variables,
                     IMetaStore metaStore ) {
    CheckResult cr;

    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDIFInputMeta.CheckResult.NoInputExpected" ), transformMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDIFInputMeta.CheckResult.NoInput" ), transformMeta );
      remarks.add( cr );
    }

    FileInputList fileInputList = getFiles( pipelineMeta );
    if ( fileInputList == null || fileInputList.getFiles().size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "LDIFInputMeta.CheckResult.NoFiles" ), transformMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "LDIFInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), transformMeta );
      remarks.add( cr );
    }
  }

  public ITransformData getTransformData() {
    return new LDIFInputData();
  }

  public ITransform getTransform( TransformMeta transformMeta, ITransformData data, int cnr, PipelineMeta tr,
                                Pipeline pipeline ) {
    return new LDIFInput( transformMeta, this, data, cnr, tr, pipeline );
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * Since the exported pipeline that runs this will reside in a ZIP file, we can't reference files relatively. So
   * what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
   * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like
   * that.
   *
   * @param variables                   the variable space to use
   * @param definitions
   * @param iResourceNaming
   * @param metaStore               the metaStore in which non-kettle metadata could reside.
   * @return the filename of the exported resource
   */
  public String exportResources( iVariables variables, Map<String, ResourceDefinition> definitions,
                                 IResourceNaming iResourceNaming, IMetaStore metaStore ) throws HopException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      if ( !filefield ) {
        for ( int i = 0; i < fileName.length; i++ ) {
          FileObject fileObject = HopVFS.getFileObject( variables.environmentSubstitute( fileName[ i ] ), variables );
          fileName[ i ] = iResourceNaming.nameResource( fileObject, variables, Utils.isEmpty( fileMask[ i ] ) );
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new HopException( e );
    }
  }
}
