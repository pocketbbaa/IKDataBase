package org.wltea.analyzer.test;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKSAnalyzer;

public class TestAnalysis {

	private static Logger logger = LoggerFactory.getLogger(TestAnalysis.class);

	public static void testAnalyzer(String text, IKSAnalyzer analyzer) throws Exception {
		TokenStream token = analyzer.tokenStream("", new StringReader(text));
		CharTermAttribute term = token.addAttribute(CharTermAttribute.class);
		token.reset();
		while (token.incrementToken()) {
			logger.info("IKSAnalyzer:" + term.toString());
		}
		token.end();
		token.close();

	}

	public static void testAnalyzer2(String text, IKAnalyzer analyzer) throws IOException {
		TokenStream token = analyzer.tokenStream("", new StringReader(text));

		CharTermAttribute term = token.addAttribute(CharTermAttribute.class);

		token.reset();
		while (token.incrementToken()) {
			System.out.println(term.toString());
		}

		token.end();
		token.close();
	}

	public static void main(String[] args) throws Exception {
		IKSAnalyzer analyzer = new IKSAnalyzer();
		IKAnalyzer analy = new IKAnalyzer(true);
		String temp = "A股B股AB股幸福香水水晶鞋喷雾香水炫香水雅芳美白护手霜雅芳幸福香水舒友阁雅芳 玫瑰花香止汗香体露";
		// String temp = "阿萨德阿萨德阿萨德";
		testAnalyzer2(temp, analy);
//		testAnalyzer(temp, analyzer);

	}
}