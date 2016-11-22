package org.wltea.analyzer.dic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.conndb.DBHelper;

/**
 * 词典管理类,单子模式
 *
 * @author Administrator
 */
public class Dictionary {

    private static Logger logger = LoggerFactory.getLogger(Dictionary.class);

    /*
     * 词典单子实例
     */
    private static Dictionary singleton;

    /*
     * 主词典对象
     */
    private DictSegment _MainDict;

    /*
     * 停止词词典
     */
    private DictSegment _StopWordDict;
    /*
     * 量词词典
     */
    private DictSegment _QuantifierDict;

    /**
     * 配置对象
     */
    private Configuration cfg;

    private Dictionary(Configuration cfg) {
        this.cfg = cfg;
    }

    /**
     * 词典初始化 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
     * 只有当Dictionary类被实际调用时，才会开始载入词典， 这将延长首次分词操作的时间 该方法提供了一个在应用加载阶段就初始化字典的手段
     *
     * @return Dictionary
     */
    public static Dictionary initial(Configuration cfg) {
        synchronized (Dictionary.class) {
            if (singleton == null) {
                singleton = new Dictionary(cfg);
                logger.info("--初始化主词典--");
                singleton.loadMainDict(); //加载主词典
                try {
                    logger.info("--初始化扩展词典--");
                    singleton.loadExtDBDict(cfg.getDataBasePath(), cfg.getExtDictTableName(), cfg.getJdbcurl());//加载扩展词典
                    logger.info("--初始化禁用词--");
                    singleton.loadStopWordDBDict(cfg.getDataBasePath(), cfg.getExtStopwordsTableName(), cfg.getJdbcurl());//数据库加载禁用词
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("--初始化量词--");
                singleton.loadQuantifierDict(); //加载量词
            }
        }
        return singleton;
    }

    /**
     * 重新加载扩展词典和禁用词典
     **/
    public void reloadExtandStopWordDict() {

        if (singleton == null) {
            throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
        }
        // loadExtDict();//重新加载扩展词典
        // loadStopWordDict();//重新加载禁用词典
        loadExtDict(null);
        // loadExtDict();//重新加载扩展词典
        loadStopWordDict(null);
        // System.out.println("扩展词典刷新了.......");

    }

    public static Logger log = LoggerFactory.getLogger(Dictionary.class);

    /**
     * 重新加载扩展词典和禁用词典
     **/
    public void reloadExtandStopWordDict(List<String> extwords, List<String> stopwords) {

        if (singleton == null) {
            throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
        }
        // loadExtDict();//重新加载扩展词典
        // loadStopWordDict();//重新加载禁用词典

        // log.info("执行到扩展词前");
        loadExtDict(extwords);
        // log.info("执行到扩展词后");
        // loadExtDict();//重新加载扩展词典
        // log.info("执行到禁用词前");
        loadStopWordDict(stopwords);
        // log.info("执行到禁用词后");
        // System.out.println("扩展词典刷新了.......");

    }

    /**
     * 获取词典单子实例
     *
     * @return Dictionary 单例对象
     */
    public static Dictionary getSingleton() {
        if (singleton == null) {
            throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
        }
        return singleton;
    }

    /**
     * 批量加载新词条
     *
     * @param words Collection<String>词条列表
     */
    public void addWords(Collection<String> words) {
        if (words != null) {
            for (String word : words) {
                if (word != null) {
                    // 批量加载词条到主内存词典中
                    singleton._MainDict.fillSegment(word.trim().toLowerCase().toCharArray());
                }
            }
        }
    }

    /**
     * 检索匹配主词典
     *
     * @param charArray
     * @return Hit 匹配结果描述
     */
    public Hit matchInMainDict(char[] charArray) {
        return singleton._MainDict.match(charArray);
    }

    /**
     * 检索匹配主词典
     *
     * @param charArray
     * @param begin
     * @param length
     * @return Hit 匹配结果描述
     */
    public Hit matchInMainDict(char[] charArray, int begin, int length) {
        return singleton._MainDict.match(charArray, begin, length);
    }

    /**
     * 批量移除（屏蔽）词条
     *
     * @param words
     */
    public void disableWords(Collection<String> words) {
        if (words != null) {
            for (String word : words) {
                if (word != null) {
                    // 批量屏蔽词条
                    singleton._MainDict.disableSegment(word.trim().toLowerCase().toCharArray());
                }
            }
        }
    }

    /**
     * 检索匹配量词词典
     *
     * @param charArray
     * @param begin
     * @param length
     * @return Hit 匹配结果描述
     */
    public Hit matchInQuantifierDict(char[] charArray, int begin, int length) {
        return singleton._QuantifierDict.match(charArray, begin, length);
    }

    /**
     * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
     *
     * @param charArray
     * @param currentIndex
     * @param matchedHit
     * @return Hit
     */
    public Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
        DictSegment ds = matchedHit.getMatchedDictSegment();
        return ds.match(charArray, currentIndex, 1, matchedHit);
    }

    /**
     * 判断是否是停止词
     *
     * @param charArray
     * @param begin
     * @param length
     * @return boolean
     */
    public boolean isStopWord(char[] charArray, int begin, int length) {
        return singleton._StopWordDict.match(charArray, begin, length).isMatch();
    }

    /**
     * 加载主词典及扩展词典
     */
    private void loadMainDict() {
        // 建立一个主词典实例
        _MainDict = new DictSegment((char) 0);
        // 读取主词典文件
        logger.info("读取主词典文件:" + cfg.getMainDictionary());
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(cfg.getMainDictionary());
        if (is == null) {
            logger.info("主词典没有找到：主词典名称：{}", cfg.getMainDictionary());
            throw new RuntimeException("Main Dictionary not found!!!");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);

        } catch (IOException ioe) {
            logger.info("主词典加载异常~~~");
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从数据库加载扩展词
     **/
    public void loadExtDBDict(String type, String extDictTable, String jdbcUrl) throws Exception {
        logger.info("从数据库加载扩展词->type:[{}],extDictTable:[{}],jdbcUrl:[{}]", type, extDictTable, jdbcUrl);
        List<String> list = DBHelper.getKey(extDictTable, type, jdbcUrl);
        for (String word : list) {
            _MainDict.fillSegment(word.trim().toLowerCase().toCharArray());
        }
    }

    /**
     * 从数据库加载禁用词
     */
    public void loadStopWordDBDict(String type, String extStopwordsTable, String jdbcUrl) throws Exception {
        logger.info("从数据库加载禁用词->type:[{}],extStopwordsTable[{}],jdbcUrl[{}]", type, extStopwordsTable, jdbcUrl);
        DictSegment stopWordDict = new DictSegment((char) 0);
        List<String> list = DBHelper.getKey(extStopwordsTable, type, jdbcUrl);
        for (String word : list) {
            stopWordDict.fillSegment(word.trim().toLowerCase().toCharArray());
        }
        _StopWordDict = stopWordDict;
    }

    /**
     * 加载用户配置的扩展词典到主词库表
     */
    public void loadExtDict() {
        // 加载扩展词典配置
        List<String> extDictFiles = cfg.getExtDictionarys();
        RandomAccessFile raf = null;
        if (extDictFiles != null) {
            // InputStream is = null;
            for (String extDictName : extDictFiles) {
                // 读取扩展词典文件
                // System.out.println("加载扩展词典：" + extDictName);
                // is =
                // this.getClass().getClassLoader().getResourceAsStream(extDictName);
                URL u = this.getClass().getClassLoader().getResource(extDictName);
                // System.out.println("咱们的路径是： "+u.getPath());
                try {
                    raf = new RandomAccessFile(u.getPath(), "r");
                    String theWord = null;

                    // while((theWord = new
                    // String(raf.readLine().getBytes("iso8859-1"),"utf-8"))!=null){
                    while ((theWord = raf.readLine()) != null) {
                        if (theWord.trim().length() > 0) {
                            theWord = new String(theWord.getBytes("iso8859-1"), "utf-8");
                            // System.out.println("读取，词典载入主存： "+theWord);
                            _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    }

                    // 加载扩展词典数据到主内存词典中
                    // _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                } catch (IOException ioe) {
                    System.err.println("Extension Dictionary loading exception.");
                    ioe.printStackTrace();

                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                            raf = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 加载用户配置的扩展词典到主词库表 2014年5月4日新增方法
     */
    public void loadExtDict(List<String> extwords) {

        // _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
        // log.info("进入扩展词方法: ");
        try {
            for (String word : extwords) {
                // log.info("扩展词是： "+word);
                _MainDict.fillSegment(word.trim().toLowerCase().toCharArray());
            }

            // log.info("加载扩展词结束: ");
        } catch (Exception e) {
            // log.info("扩展词里，异常: "+e);
            e.printStackTrace();
        }

    }

    public void loadExtDictttt() {
        // 加载扩展词典配置
        List<String> extDictFiles = cfg.getExtDictionarys();
        if (extDictFiles != null) {
            InputStream is = null;
            for (String extDictName : extDictFiles) {
                // 读取扩展词典文件
                System.out.println("加载扩展词典：" + extDictName);
                is = this.getClass().getClassLoader().getResourceAsStream(extDictName);
                URL u = this.getClass().getClassLoader().getResource(extDictName);
                System.out.println("咱们的路径是： " + u.getPath());
                // 如果找不到扩展的字典，则忽略
                if (is == null) {
                    System.out.println("根本没找到，词典路径");
                    continue;
                }

                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
                    // br.reset();
                    String theWord = null;
                    do {
                        theWord = br.readLine();
                        // System.out.println("我的词:"+theWord);
                        if (theWord != null && !"".equals(theWord.trim())) {

                            // 加载扩展词典数据到主内存词典中
                            System.out.println("读取，词典载入主存： " + theWord);
                            _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    } while (theWord != null);

                } catch (IOException ioe) {
                    System.err.println("Extension Dictionary loading exception.");
                    ioe.printStackTrace();

                } finally {
                    try {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 加载用户扩展的停止词词典
     */
    public void loadStopWordDict() {
        // 建立一个主词典实例
        _StopWordDict = new DictSegment((char) 0);
        RandomAccessFile raf = null;
        // 加载扩展停止词典
        List<String> extStopWordDictFiles = cfg.getExtStopWordDictionarys();
        if (extStopWordDictFiles != null) {

            for (String extStopWordDictName : extStopWordDictFiles) {

                // System.out.println("加载禁用词词典：" + extStopWordDictName);
                // is =
                // this.getClass().getClassLoader().getResourceAsStream(extDictName);
                URL u = this.getClass().getClassLoader().getResource(extStopWordDictName);
                // System.out.println("咱们的禁用词路径是： "+u.getPath());
                try {

                    raf = new RandomAccessFile(u.getPath(), "r");
                    String theWord = null;

                    // while((theWord = new
                    // String(raf.readLine().getBytes("iso8859-1"),"utf-8"))!=null){
                    while ((theWord = raf.readLine()) != null) {
                        if (theWord.trim().length() > 0) {
                            theWord = new String(theWord.getBytes("iso8859-1"), "utf-8");
                            // System.out.println("读取，禁用词典载入主存： "+theWord);
                            _StopWordDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    }
                    // 加载扩展词典数据到主内存词典中
                    // _MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                } catch (IOException ioe) {
                    System.err.println("Extension Dictionary loading exception.");
                    ioe.printStackTrace();

                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                            raf = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * 2014年5月4日新增 加载用户的禁用词路径
     */
    public void loadStopWordDict(List<String> stopwords) {

        // 建立一个主词典实例
        _StopWordDict = new DictSegment((char) 0);
        for (String word : stopwords) {
            _StopWordDict.fillSegment(word.trim().toLowerCase().toCharArray());
        }

    }

    public void loadStopWordDict11111() {
        // 建立一个主词典实例
        _StopWordDict = new DictSegment((char) 0);
        // 加载扩展停止词典
        List<String> extStopWordDictFiles = cfg.getExtStopWordDictionarys();
        if (extStopWordDictFiles != null) {
            InputStream is = null;
            for (String extStopWordDictName : extStopWordDictFiles) {
                // System.out.println("加载扩展停止词典：" + extStopWordDictName);
                // 读取扩展词典文件
                is = this.getClass().getClassLoader().getResourceAsStream(extStopWordDictName);
                // 如果找不到扩展的字典，则忽略
                if (is == null) {
                    continue;
                }
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
                    String theWord = null;
                    do {
                        // System.out.println("禁用词:"+theWord);
                        theWord = br.readLine();
                        if (theWord != null && !"".equals(theWord.trim())) {
                            // System.out.println(theWord);
                            // 加载扩展停止词典数据到内存中
                            _StopWordDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    } while (theWord != null);

                } catch (IOException ioe) {
                    System.err.println("Extension Stop word Dictionary loading exception.");
                    ioe.printStackTrace();

                } finally {
                    try {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 加载量词词典
     */
    private void loadQuantifierDict() {
        // 建立一个量词典实例
        _QuantifierDict = new DictSegment((char) 0);
        // 读取量词词典文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(cfg.getQuantifierDicionary());
        if (is == null) {
            throw new RuntimeException("Quantifier Dictionary not found!!!");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    _QuantifierDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);

        } catch (IOException ioe) {
            System.err.println("Quantifier Dictionary loading exception.");
            ioe.printStackTrace();

        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
