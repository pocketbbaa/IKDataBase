package org.wltea.analyzer.cfg;

import java.util.List;

/**
 * 配置管理类接口
 * 
 * @author Administrator
 *
 */
public interface Configuration {
	/**
	 * 返回useSmart标志位 useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * 
	 * @return useSmart
	 */
	public boolean useSmart();

	/**
	 * 设置useSmart标志位 useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * 
	 * @param useSmart
	 */
	public void setUseSmart(boolean useSmart);

	/**
	 * 获取主词典路径
	 * 
	 * @return String 主词典路径
	 */
	public String getMainDictionary();

	/**
	 * 获取量词词典路径
	 * 
	 * @return String 量词词典路径
	 */
	public String getQuantifierDicionary();

	/**
	 * 获取扩展字典配置路径
	 * 
	 * @return List<String> 相对类加载器的路径
	 */
	public List<String> getExtDictionarys();

	/**
	 * 获取扩展停止词典配置路径
	 * 
	 * @return List<String> 相对类加载器的路径
	 */
	public List<String> getExtStopWordDictionarys();

	/**
	 * 获取同义词典的路径
	 * 
	 * @return String 相对类的加载器
	 **/
	public String getSynonymsPath();

	/**
	 * 获取数据库表的配置路径
	 * 
	 * @return
	 */
	public List<String> getDataBaseListPath();

	/**
	 * 获取数据库表的配置路径
	 * 
	 * @return
	 */
	public String getDataBasePath();
	
	/**
	 * 获取数据库路径
	 * @param path
	 */
	public void setDataBasePath(String path);
	
	/**
	 * 获取扩展字典表名
	 * @return
	 */
	public String getExtDictTableName();
	
	/**
	 * 获取禁用字典表名
	 * @return
	 */
	public String getExtStopwordsTableName();
	
	/**
	 * 获取数据库链接
	 * @return
	 */
	public String getJdbcurl();
	
	/**
	 * 获取同义词库表名
	 * @return
	 */
	public String getExtSynonymsTableName();
	
	/**
	 * 获取是否启用自动更新
	 * @return
	 */
	public boolean isAutoUpdate();
	
	/**
	 * 获取第一次启动时间
	 * @return
	 */
	public long getStartTime();
	
	/**
	 * 获取间隔时间
	 * @return
	 */
	public long getFlushTime();
}
