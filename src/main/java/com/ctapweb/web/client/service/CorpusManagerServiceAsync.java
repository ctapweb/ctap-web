package com.ctapweb.web.client.service;

import java.util.List;

import com.ctapweb.web.shared.Corpus;
import com.ctapweb.web.shared.CorpusFolder;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.Tag;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CorpusManagerServiceAsync {

	void getCorpusList(int offset, int limit, AsyncCallback<List<Corpus>> callback);

	void getCorpusCount(AsyncCallback<Integer> callback);

	void addCorpus(Corpus corpus, AsyncCallback<Void> callback);

	void deleteCorpus(Corpus corpus, AsyncCallback<Void> callback);

	void updateCorpus(Corpus corpus, AsyncCallback<Void> callback);

	void getTextCount(long corpusID, AsyncCallback<Integer> callback);

	void getTextList(long corpusID, long tagID, int offset, int limit,
			AsyncCallback<List<CorpusText>> callback);

	void addTextToCorpus(CorpusText corpusText, AsyncCallback<Void> callback);

	void updateText(CorpusText corpusText, AsyncCallback<Void> callback);

	void deleteText(CorpusText corpusText, AsyncCallback<Void> callback);

	void getCorpusName(long corpusID, AsyncCallback<String> callback);

	void addCorpusFolder(String folderName, AsyncCallback<Void> callback);

	void getCorpusFolderList(int offset, int limit, AsyncCallback<List<CorpusFolder>> callback);

	void getCorpusFolderCount(AsyncCallback<Integer> callback);

	void deleteCorpusFolder(CorpusFolder corpusFolder, AsyncCallback<Void> callback);

	void getCorpusList(long folderID, int offset, int limit,
			AsyncCallback<List<Corpus>> callback);

	void updateCorpusFolder(CorpusFolder corpusFolder, AsyncCallback<Void> callback);

	void getTagList(long corpusID, int offset, int limit, AsyncCallback<List<Tag>> callback);

	void getTagCount(AsyncCallback<Integer> callback);

	void addTag(String tagName, AsyncCallback<Void> callback);

	void updateTag(Tag tag, AsyncCallback<Void> callback);

	void deleteTag(Tag tag, AsyncCallback<Void> callback);

	void getTextCount(long corpusID, long tagID, AsyncCallback<Integer> callback);

	void getTextList(long corpusID, int offset, int limit,
			AsyncCallback<List<CorpusText>> callback);

	void tagText(CorpusText text, Tag tag, AsyncCallback<Void> callback);

	void getCorpusCount(long folderID, AsyncCallback<Integer> callback);

}
