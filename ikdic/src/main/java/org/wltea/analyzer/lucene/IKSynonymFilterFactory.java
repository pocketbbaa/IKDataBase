package org.wltea.analyzer.lucene;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.conndb.DBHelper;

public class IKSynonymFilterFactory extends TokenFilterFactory implements ResourceLoaderAware, Runnable {

	Logger logger = LoggerFactory.getLogger(IKSynonymFilterFactory.class);

	private String synonyms;
	private SynonymMap map;
	private boolean ignoreCase;
	private boolean expand;
	private ResourceLoader loader = null;
	boolean isAutoUpdate;
	Analyzer analyzer = null;
	private Configuration configuration = DefaultConfig.getInstance();

	private String type = configuration.getDataBasePath();// 数据库表的分类类型

	int flushtime;

	public IKSynonymFilterFactory(Map<String, String> args) throws IOException {
		super(args);

		this.expand = getBoolean(args, "expand", true);
		this.synonyms = get(args, "synonyms");
		this.ignoreCase = getBoolean(args, "ignoreCase", false);
		this.isAutoUpdate = getBoolean(args, "autoupdate", false);
		this.flushtime = getInt(args, "flushtime", 10);

	}

	public void inform(ResourceLoader loader) throws IOException {
		Analyzer analyzer = new Analyzer() {
			
			@Override
			protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
				WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
				TokenStream stream = IKSynonymFilterFactory.this.ignoreCase
						? new LowerCaseFilter(tokenizer) : tokenizer;
				return new Analyzer.TokenStreamComponents(tokenizer, stream);
			}

		};
		// System.out.println("<IKSynonymFilterFactory>inform---loadSolrSynonyms!");
		try {
			// this.map = loadSolrSynonyms(loader, true, analyzer);//加载配置文件使用
			this.map = loadDBSynonyms(loader, true, analyzer, this.type);// 从数据库加载使用

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Exception thrown while loading synonyms", e);
		}

		if ((this.isAutoUpdate) && (this.synonyms != null) && (!this.synonyms.trim().isEmpty())) {
			this.loader = loader;
			this.analyzer = analyzer;
		}
		if (isAutoUpdate) {
			ScheduledExecutorService updateService = Executors.newSingleThreadScheduledExecutor();
			updateService.scheduleAtFixedRate(this, 5, flushtime, TimeUnit.SECONDS);
		}

	}

	private SynonymMap loadSolrSynonyms(ResourceLoader loader, boolean dedup, Analyzer analyzer)
			throws IOException, ParseException {

		logger.info("从配置文件加载同义词-----");

		if (this.synonyms == null) {
			throw new IllegalArgumentException("Missing required argument 'synonyms'.");
		}
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);

		SolrSynonymParser parser = new SolrSynonymParser(dedup, this.expand, analyzer);
		File synonymFile = new File(this.synonyms);
		if (loader != null) {
			if (synonymFile.exists()) {
				decoder.reset();

				// parser.add(new
				// InputStreamReader(loader.openResource(this.synonyms)));
				parser.parse(new InputStreamReader(loader.openResource(this.synonyms), decoder));

			} else {
				List<String> files = splitFileNames(this.synonyms);
				for (String file : files) {
					decoder.reset();
					// parser.add(new
					// InputStreamReader(loader.openResource(file),
					parser.parse(new InputStreamReader(loader.openResource(file), decoder));
				}
			}
		}
		return parser.build();
	}

	/**
	 * 从数据库加载同义词
	 **/
	private SynonymMap loadDBSynonyms(ResourceLoader loader, boolean dedup, Analyzer analyzer, String type)
			throws Exception, ParseException {
		logger.info("进同义词了....从数据库加载同义词");
		List<String> list = DBHelper.getKey(configuration.getExtSynonymsTableName(), type, configuration.getJdbcurl());
		SolrSynonymParser parser = new SolrSynonymParser(dedup, this.expand, analyzer);
		StringWriter writer = new StringWriter();
		for(String string : list){
			writer.append(string).append("\n");
		}
		parser.parse(new StringReader(writer.getBuffer().toString()));
		// logger.info("获取到的同义词->dbtext:" + dbtxt);
		return parser.build();
	}

	public TokenStream create(TokenStream input) {

		if (input == null) {
			logger.info("input is null");
		}
		if (this.map == null) {
			logger.info("map is null");
		}

		return this.map.fst == null ? input : new SynonymFilter(input, this.map, this.ignoreCase);
	}

	static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void update() {

		try {

			this.map = loadDBSynonyms(loader, true, analyzer, type);
			logger.info(f.format(new Date()) + "   同义词库词库更新了.....");

		} catch (Exception e) {
			logger.info("<IKSynonymFilterFactory> IOException!!");
			e.printStackTrace();
		}
	}

	public void run() {
		this.update();

	}

}
