package org.wltea.analyzer.conndb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHelper {

	public static Logger logger = LoggerFactory.getLogger(DBHelper.class);

	private static Connection conn;// 创建用于连接数据库的Connection对象

	private static final int PAGE_INDEX = 1;
	private static final int PAGE_SIZE = 2000;

	private static Connection getConn(String jdbcUrl) throws Exception {

		try {
			Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动
			String[] path = jdbcUrl.split(",");
			String url = path[0] + "?useUnicode=true&characterEncoding=UTF-8";
			conn = DriverManager.getConnection(url, path[1], path[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}

	public static List<String> getKey(String tableName, String type, String jdbcUrl) throws Exception {
		logger.info("连接数据库了 tableName:[{}],type:[{}],jdbcUrl:[{}]", tableName, type, jdbcUrl);
		int pageIndex = PAGE_INDEX;
		int pageSize = PAGE_SIZE;
		int start = pageIndex * pageSize - pageSize + 1;
		int end = pageSize;
		long startTime = System.currentTimeMillis();
		List<String> list = new ArrayList<>();
		List<String> tempList = Collections.emptyList();
		pageIndex++;
		while((tempList = getResult(tableName, type, jdbcUrl, start, end)).size() > 0){
			start = pageIndex * pageSize - pageSize + 1;
			end = pageSize;
			pageIndex++;
			list.addAll(tempList);
		};
		long endTime = System.currentTimeMillis();
		logger.info("分页获取数据，获取全部数据用了[{}]次", pageIndex);
		logger.info("获取数据库->tableName:[{}]词典所花费时间为-》time:[{}] 毫秒", tableName, endTime - startTime);
		return list;
	}

	/**
	 * 循环获取数据库数据
	 *
	 * @return
	 */
	private static List<String> getResult(String tableName, String type, String jdbcUrl, int start, int end)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		conn = getConn(jdbcUrl);
		String sql = "select text from " + tableName + " where type=" + type + " limit " + start + "," + end;
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<String> list = new ArrayList<>();
		while (rs.next()) {
			String data = rs.getString("text");
			if(data == null || (data = data.trim()).length() == 0){
				continue;
			}
			list.add(data);
		}
		rs.close();
		ps.close();
		conn.close();
		return list;

	}

	public static void main(String[] args) throws Exception {

		List<String> list = getKey("solr_dict", "0",
				"jdbc:mysql://120.55.194.82:3306/addition?useUnicode=true&characterEncoding=UTF-8,test,app_test_20160504");

		for (String s : list) {
			System.out.println(s);
		}
		// int pageIndex = 1;
		// int pageSize = 5;
		// do {
		// int start = pageIndex * pageSize - pageSize + 1;
		// int end = pageIndex * pageSize;
		// System.out.println("start:" + start);
		// System.out.println("end:" + end);
		// pageIndex++;
		// Thread.sleep(1000);
		// } while (true);

		/** 扩展字典 **/
		// String filePath = "brandlist_20141204.txt";
		// String filePath = "productlist_20141204.txt";
		// String filePath = "searchKey_20141204.txt";
		// String filePath = "taglist_20141204.txt";
		//
		// /** 禁用词 **/
		// // String filePath = "stopwords.txt";
		//
		// /** 同义词 **/
		// // String filePath = "syc_dic.txt";
		// // String filePath = "syn_brandlist_20141204.txt";
		// // String filePath = "syn_productlist_20141204.txt";
		//
		// /** 表名 **/
		// String table = "solr_dict";
		// // String table = "solr_stopword";
		// // String table = "solr_synonyms";
		//
		// String path = "D:\\王洋\\solr\\youpinVideoCore\\conf\\" + filePath;
		// String str;
		// BufferedReader in = new BufferedReader(new InputStreamReader(new
		// FileInputStream(path), "UTF8"));
		//
		// String sql = "insert into " + table + "
		// (Text,Type,CreateTime,UpdateTime) values (?, 0, now(),now())";
		// Connection connection = getConn(
		// "jdbc:mysql://120.55.194.82:3306/addition?useUnicode=true&characterEncoding=UTF-8,test,app_test_20160504");
		// // Connection connection =
		// //
		// getConn("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8,root,root");
		// PreparedStatement ps = connection.prepareStatement(sql);
		// final int batchSize = 1000;
		// int count = 0;
		//
		// while ((str = in.readLine()) != null) {
		// String strSb = str.substring(str.lastIndexOf(" ") + 1);
		// System.out.println(strSb);
		// ps.setString(1, strSb);
		// ps.addBatch();
		// if (++count % batchSize == 0) {
		// ps.executeBatch();
		// }
		// }
		// ps.executeBatch();
		// ps.close();
		// connection.close();

	}

}