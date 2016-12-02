

/* First created by JCasGen Fri Sep 09 21:52:52 CEST 2016 */
package com.ctapweb.web.server.analysis.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.AnnotationBase;


/** 
 * Updated by JCasGen Fri Sep 09 21:52:52 CEST 2016
 * XML source: /home/xiaobin/sync/projects/eclipse/CTAP/ctap-web/src/main/java/ch/xiaobin/app/ctap/web/server/analysis/descriptor/CorpusTextCollectionReader.xml
 * @generated */
public class CorpusTextInfo extends AnnotationBase {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CorpusTextInfo.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected CorpusTextInfo() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public CorpusTextInfo(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public CorpusTextInfo(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated
   * @return value of the feature 
   */
  public long getId() {
    if (CorpusTextInfo_Type.featOkTst && ((CorpusTextInfo_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "com.ctapweb.web.server.analysis.type.CorpusTextInfo");
    return jcasType.ll_cas.ll_getLongValue(addr, ((CorpusTextInfo_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(long v) {
    if (CorpusTextInfo_Type.featOkTst && ((CorpusTextInfo_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "com.ctapweb.web.server.analysis.type.CorpusTextInfo");
    jcasType.ll_cas.ll_setLongValue(addr, ((CorpusTextInfo_Type)jcasType).casFeatCode_id, v);}    
  }

    