package org.wltea.analyzer.cfg;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfig implements Configuration {

	Logger logger = LoggerFactory.getLogger(DefaultConfig.class);

	private static DefaultConfig defaultConfig = null;
	/*
	 * 分词器默认字典路径
	 */
	// private static final String PATH_DIC_MAIN =
	// "org/wltea/analyzer/dic/main2012.dic";
	// private static final String PATH_DIC_QUANTIFIER =
	// "org/wltea/analyzer/dic/quantifier.dic";
	private static final String PATH_DIC_MAIN = "main2012.dic";
	private static final String PATH_DIC_QUANTIFIER = "quantifier.dic";

	/*
	 * 分词器配置文件路径
	 */
	private static final String FILE_NAME = "IKAnalyzer.cfg.xml";

	// 配置属性——扩展字典表名
	private static final String EXT_DICT = "ext_dict_table";
	// 配置属性——扩展停止词典表名
	private static final String EXT_STOP = "ext_stopwords_table";
	// 配置同义词
	private static final String SYNONYMS_TABLE = "ext_synonyms_table";
	// 配置查询数据类型
	private static final String TYPE = "type";
	// 配置JDBC链接
	private static final String JDBC_PATH = "jdbcUrl";
	// 配置是否启用自动更新
	private static final String AUTO_UPDATE = "is_auto_update";
	// 配置第一次启用时间
	private static final String START_TIME = "start_time";
	// 配置更新间隔时间
	private static final String FLUSH_TIME = "flush_time";

	public String getSynonymsPath() {
		String ss = props.getProperty(SYNONYMS_TABLE);
		if (ss != null) {
			return ss;
		}
		return "";
	}

	public String getDataBasePath() {
		String ss = props.getProperty(TYPE);
		if (ss != null) {
			return ss;
		}
		return "";
	}

	private Properties props;
	/*
	 * 是否使用smart方式分词
	 */
	private boolean useSmart;

	/**
	 * 数据库路径
	 */
	private String dataBasePath;

	/**
	 * 返回单例
	 * 
	 * @return Configuration单例
	 */
	public static synchronized Configuration getInstance() {
		if(defaultConfig == null){
			return new DefaultConfig();
		}
        return defaultConfig;
	}

	/*
	 * 初始化配置文件
	 */
	private DefaultConfig() {
		props = new Properties();

		InputStream input = this.getClass().getClassLoader().getResourceAsStream(FILE_NAME);
		if (input != null) {
			try {
				logger.info("初始化配置文件成功->fileName:[{}]", FILE_NAME);
				props.loadFromXML(input);
			} catch (InvalidPropertiesFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 返回useSmart标志位 useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * 
	 * @return useSmart
	 */
	public boolean useSmart() {
		return useSmart;
	}

	/**
	 * 设置useSmart标志位 useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * 
	 * @param useSmart
	 */
	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}

	/**
	 * 获取主词典路径
	 * 
	 * @return String 主词典路径
	 */
	public String getMainDictionary() {

		return PATH_DIC_MAIN;
	}

	/**
	 * 获取量词词典路径
	 * 
	 * @return String 量词词典路径
	 */
	public String getQuantifierDicionary() {
		return PATH_DIC_QUANTIFIER;
	}

	/**
	 * 获取扩展字典配置路径
	 * 
	 * @return List<String> 相对类加载器的路径
	 */
	public List<String> getExtDictionarys() {
		List<String> extDictFiles = new ArrayList<String>(2);
		String extDictCfg = props.getProperty(EXT_DICT);

		if (extDictCfg != null) {
			// 使用;分割多个扩展字典配置
			String[] filePaths = extDictCfg.split(";");
			if (filePaths != null) {
				for (String filePath : filePaths) {
					if (filePath != null && !"".equals(filePath.trim())) {
						extDictFiles.add(filePath.trim());
					}
				}
			}
		}
		return extDictFiles;
	}

	/**
	 * 获取扩展停止词典配置路径
	 * 
	 * @return List<String> 相对类加载器的路径
	 */
	public List<String> getExtStopWordDictionarys() {
		List<String> extStopWordDictFiles = new ArrayList<String>(2);
		String extStopWordDictCfg = props.getProperty(EXT_STOP);

		if (extStopWordDictCfg != null) {
			// 使用;分割多个扩展字典配置
			String[] filePaths = extStopWordDictCfg.split(";");
			if (filePaths != null) {
				for (String filePath : filePaths) {
					if (filePath != null && !"".equals(filePath.trim())) {
						extStopWordDictFiles.add(filePath.trim());
					}
				}
			}
		}
		return extStopWordDictFiles;
	}

	public List<String> getDataBaseListPath() {

		List<String> extDataBaseDictFiles = new ArrayList<String>(2);
		String extStopWordDictCfg = props.getProperty(EXT_STOP);

		if (extStopWordDictCfg != null) {
			// 使用;分割多个扩展字典配置
			String[] filePaths = extStopWordDictCfg.split(";");
			if (filePaths != null) {
				for (String filePath : filePaths) {
					if (filePath != null && !"".equals(filePath.trim())) {
						extDataBaseDictFiles.add(filePath.trim());
					}
				}
			}
		}
		return extDataBaseDictFiles;

	}

	public void setDataBasePath(String path) {
		this.dataBasePath = props.getProperty(TYPE);
	}

	/**
	 * 获取拓展字典表名
	 */
	public String getExtDictTableName() {
		String ss = props.getProperty(EXT_DICT);
		logger.info("获取拓展字典表名->tableName:[{}]", ss);
		if (ss != null) {
			return ss;
		}
		return "";
	}

	/**
	 * 获取禁用字典表名
	 */
	public String getExtStopwordsTableName() {
		String ss = props.getProperty(EXT_STOP);
		logger.info("获取禁用字典表名->tableName:[{}]", ss);
		if (ss != null) {
			return ss;
		}
		return "";
	}

	/**
	 * 获取数据库链接
	 */
	public String getJdbcurl() {
		String ss = props.getProperty(JDBC_PATH);
		logger.info("获取数据库链接信息->JDBCUrl:[{}]", ss);
		if (ss != null) {
			return ss;
		}
		return "";
	}

	public String getExtSynonymsTableName() {
		String ss = props.getProperty(SYNONYMS_TABLE);
		logger.info("获取同义词词库->tableName:[{}]", ss);
		if (ss != null) {
			return ss;
		}
		return "";
	}

	public boolean isAutoUpdate() {
		String ss = props.getProperty(AUTO_UPDATE);
		logger.info("是否自动更新->tableName[{}]", ss);
		if (ss != null) {
			return ss.equals("true") ? true : false;
		}
		return false;
	}

	public long getStartTime() {
		String ss = props.getProperty(START_TIME);
		if (ss != null) {
			try {
				return Long.parseLong(ss);
			} catch (Exception e) {
				int result = 1;
				String[] str = ss.split("\\*");
				for (int i = 0; i < str.length; i++) {
					int arry = Integer.parseInt(str[i]);
					result *= arry;
				}
				return (long) result;
			}
		}
		return 0;
	}

	public long getFlushTime() {
		String ss = props.getProperty(FLUSH_TIME);
		if (ss != null) {
			try {
				return Long.parseLong(ss);
			} catch (Exception e) {
				int result = 1;
				String[] str = ss.split("\\*");
				for (int i = 0; i < str.length; i++) {
					int arry = Integer.parseInt(str[i]);
					result *= arry;
				}
				return result;
			}
		}
		return 0;
	}

	public static void main(String[] args) {

		Configuration config = DefaultConfig.getInstance();
		String dictTable = config.getExtDictTableName();
		String stopTable = config.getExtStopwordsTableName();
		String jdbcUrl = config.getJdbcurl();
		String synonyms = config.getExtSynonymsTableName();
		boolean isAutoUpdate = config.isAutoUpdate();

		long startTime = config.getStartTime();
		long flushTime = config.getFlushTime();

		System.out.println("dictTable :" + dictTable);
		System.out.println("stopTable :" + stopTable);
		System.out.println("jdbcUrl :" + jdbcUrl);
		System.out.println("synonyms :" + synonyms);
		System.out.println("isAutoUpdate:" + isAutoUpdate);
		System.out.println("startTime:" + startTime);
		System.out.println("flushTime:" + flushTime);
	}
}
