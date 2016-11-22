package org.wltea.analyzer.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.solr.core.SolrResourceLoader;

public class IKSAnalyzer extends Analyzer {

	private IKSynonymFilterFactory syfilter = null;
	Map<String, String> argsMap = null;

	public IKSAnalyzer() {
		argsMap = new HashMap<String, String>();
		argsMap.put("expand", "true");
		argsMap.put("synonyms", "synonyms.txt");
		argsMap.put("autoupdate", "false");
		argsMap.put("flushtime", "10");
		try {
			syfilter = new IKSynonymFilterFactory(argsMap);
			syfilter.inform(new ClasspathResourceLoader());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	@Override
//	protected TokenStreamComponents createComponents(String arg0, Reader arg1) {
//		Tokenizer _IKTokenizer = new IKTokenizer(arg1, true);
//		TokenStream tokenstream = syfilter.create(_IKTokenizer);
//		return new TokenStreamComponents(_IKTokenizer, tokenstream);
//	}
	/**
	 * 由于Lucene5.0里把createComponents方法的第二个参数去掉了，所以需要对该方法做如下修改
	 */
	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		Tokenizer _IKTokenizer = new IKTokenizer(true);
		return new TokenStreamComponents(_IKTokenizer);
	}
}
