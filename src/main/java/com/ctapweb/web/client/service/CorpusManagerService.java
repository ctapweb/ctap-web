/**
 * 
 */
package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.Corpus;
import com.ctapweb.web.shared.CorpusFolder;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.Tag;
import com.ctapweb.web.shared.exception.AccessToResourceDeniedException;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.EmptyInfoException;
import com.ctapweb.web.shared.exception.ResourceAlreadyExistsException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/** Provides services for the corpus manager.
 * @author xiaobin
 *
 */
@RemoteServiceRelativePath("corpusManager")
public interface CorpusManagerService extends RemoteService {

/**
 * Gets the number of corpora owned by the user.
 * @param userCookieValue
 * @return
 * @throws UserNotLoggedInException
 * @throws DatabaseException
 */
	Integer getCorpusCount() 
			throws UserNotLoggedInException,  DatabaseException;

	/**
	 * Gets number of corpora in a certain folder.
	 * @param userCookieValue
	 * @param folderID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	Integer getCorpusCount(long folderID) 
			throws UserNotLoggedInException,  DatabaseException;

	Integer getCorpusFolderCount() 
			throws UserNotLoggedInException,  DatabaseException;

	Integer getTagCount() 
			throws UserNotLoggedInException,  DatabaseException;
/**
 * Gets a list of corpora owned by the user.
 * @param userCookieValue
 * @param offset Starting item from result set. Used for paging.
 * @param limit The number of items to return.
 * @return
 * @throws UserNotLoggedInException
 * @throws DatabaseException
 */
	List<Corpus> getCorpusList(int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;
	
	/**
	 * Gets a list of corpora from a certain folder.
	 * @param userCookieValue
	 * @param folderID
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws DatabaseException
	 */
	List<Corpus> getCorpusList(long folderID, int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;

	List<CorpusFolder> getCorpusFolderList(int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException;

	List<Tag> getTagList(long corpusID, int offset, int limit) 
			throws UserNotLoggedInException, DatabaseException, AccessToResourceDeniedException;

	Void tagText(CorpusText text, Tag tag) 
			throws UserNotLoggedInException, DatabaseException, AccessToResourceDeniedException;
	/**
	 * Add a new corpus to database.
	 * @param userCookieValue
	 * @param corpus The corpus object.
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws EmptyInfoException
	 * @throws DatabaseException
	 */
	Void addCorpus(Corpus corpus)
			throws UserNotLoggedInException, EmptyInfoException, DatabaseException;

	Void addCorpusFolder(String folderName)
			throws UserNotLoggedInException, EmptyInfoException, 
			ResourceAlreadyExistsException, DatabaseException;

	Void addTag(String tagName)
			throws UserNotLoggedInException, EmptyInfoException, 
			ResourceAlreadyExistsException, DatabaseException;

	/**
	 * Delete the specified corpus.
	 * @param userCookieValue
	 * @param corpus
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void deleteCorpus(Corpus corpus) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	Void deleteCorpusFolder(CorpusFolder corpusFolder) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	Void deleteTag(Tag tag) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;
/**
 * Update corpus information. 
 * @param userCookieValue
 * @param corpus
 * @return
 * @throws EmptyInfoException
 * @throws UserNotLoggedInException
 * @throws AccessToResourceDeniedException
 * @throws DatabaseException
 */
	Void updateCorpus(Corpus corpus) 
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, DatabaseException;

	Void updateCorpusFolder(CorpusFolder corpusFolder) 
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, ResourceAlreadyExistsException,
			DatabaseException;

	Void updateTag(Tag tag) 
			throws EmptyInfoException, UserNotLoggedInException, 
			AccessToResourceDeniedException, ResourceAlreadyExistsException, 
			DatabaseException;

	/**
	 * Gets the corpus name. 
	 * @param userCookieValue
	 * @param corpusID
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	String getCorpusName(long corpusID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

/**
 * Gets the number texts included in this corpus.
 * @param userCookieValue
 * @param corpusID
 * @return
 */
	Integer getTextCount(long corpusID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	Integer getTextCount(long corpusID, long tagID)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Gets all the texts in the specified corpus.
	 * @param userCookieValue
	 * @param corpusID
	 * @param offset
	 * @param limit
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	List<CorpusText> getTextList(long corpusID, int offset, int limit)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	List<CorpusText> getTextList(long corpusID, long tagID, int offset, int limit)
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Adds a new text to the specified corpus.
	 * @param userCookieValue
	 * @param corpusText
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws EmptyInfoException
	 * @throws DatabaseException
	 */
	Void addTextToCorpus(CorpusText corpusText)
			throws UserNotLoggedInException, AccessToResourceDeniedException,
			EmptyInfoException, DatabaseException;

	/**
	 * Updates text details.
	 * @param userCookieValue
	 * @param corpusText
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws EmptyInfoException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void updateText(CorpusText corpusText) 
			throws UserNotLoggedInException, EmptyInfoException, AccessToResourceDeniedException, DatabaseException;

	/**
	 * Deletes text from corpus.
	 * @param userCookieValue
	 * @param corpusText
	 * @return
	 * @throws UserNotLoggedInException
	 * @throws AccessToResourceDeniedException
	 * @throws DatabaseException
	 */
	Void deleteText(CorpusText corpusText) 
			throws UserNotLoggedInException, AccessToResourceDeniedException, DatabaseException;

}
