package com.wuze.jdelasticsearch.config;


import com.alibaba.fastjson.JSON;
import com.wuze.jdelasticsearch.pojo.JDBook;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wuze
 * @desc ...
 * @date 2021-02-21 15:06:07
 */
@Configuration
public class ElasticSearchUtils {



    /**
     * 注入 RestHighLevelClient
     * @return
     */
  @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        return client;
    }


    /*
    * 注意：RestHighLevelClient 需要传入方法，调用时再从 bean容器 里面@Autowired拿出
    * 所以这里不需要拿出，调用时再拿出（直接传入形参操作即可！）
    */
    //@Autowired
    //private static RestHighLevelClient restHighLevelClient;




    /////////////////////////////////////////////////////////
    //////////////////  索引操作  ////////////////////////////
    /////////////////////////////////////////////////////////

    //1 创建索引
    public static void creareIndex(String indexName, RestHighLevelClient restHighLevelClient){
        try {
            //1 创建索引请求
            CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
            //2 客户端执行请求,请求后获得响应
            CreateIndexResponse indexResponse = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
            System.out.println(indexResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    //2 获取索引（“索引”相当于“数据库”，只能判断其是否存在）
    public static void isExistsIndex(String indexName, RestHighLevelClient restHighLevelClient){
        try {
            //注意：GetIndexRequest导入的包是org.elasticsearch.client.indices.GetIndexRequest;
            GetIndexRequest indexRequest = new GetIndexRequest(indexName);
            boolean isExists = restHighLevelClient.indices().exists(indexRequest, RequestOptions.DEFAULT);
            System.out.println(isExists);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    //3 删除索引
    public static void deleteIndex(String indexName, RestHighLevelClient restHighLevelClient){
        try {
            DeleteIndexRequest deleteRequest = new DeleteIndexRequest(indexName);
            restHighLevelClient.indices().delete(deleteRequest, RequestOptions.DEFAULT);
        }catch (Exception e){
            e.printStackTrace();
        }

    }





    /////////////////////////////////////////////////////////
    //////////////////  文档操作  ////////////////////////////
    /////////////////////////////////////////////////////////


    //1、添加文档（添加 ”行“ ）
    public static void addDocument(JDBook jdBook, String indexName, RestHighLevelClient restHighLevelClient){
        try {
            //1、创建对象
            //User user = new User("wz1", 22);
            //2、创建请求(指定 “索引”（数据库）)
            IndexRequest indexRequest = new IndexRequest(indexName);
            //3、配置请求基本规则
            indexRequest.id("1");//行号为 1
            indexRequest.timeout(TimeValue.timeValueSeconds(1));//过期时间1s
            //4、将我们的数据放入请求(先将对象转换成json格式)
            indexRequest.source(JSON.toJSONString(jdBook), XContentType.JSON);
            //5、客户端发送请求，获取响应结果
            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

            System.out.println(response.toString());//输出响应结果，json--> string
            System.out.println(response.status());//输出 响应状态（Create/Update/...）

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    //2 获取文档信息（先查询文档是否存在）
    public static void getDocument(String indexName, String docId, RestHighLevelClient restHighLevelClient){
        try {
            //1、创建请求(指定 “索引”（数据库） 和 id（行号）)
            GetRequest request = new GetRequest(indexName,docId);
            //2、判断文档是否存在
            boolean isExists = restHighLevelClient.exists(request, RequestOptions.DEFAULT);
            //如果存在
            if(isExists){
                //发送请求，获取文档，得到响应
                GetResponse response =restHighLevelClient.get(request, RequestOptions.DEFAULT);
                System.out.println(response.getSourceAsString());//转换成字符串输出
                System.out.println(response);//原输出，跟命令行方式输出一致
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    //3 更新文档信息
    public static void updateDocument(JDBook jdBook, String indexName, String docId, RestHighLevelClient restHighLevelClient){
        try {
            //1、创建更新请求
            UpdateRequest updateRequest = new UpdateRequest(indexName,docId);
            //2、设置基本规则
            updateRequest.timeout(TimeValue.timeValueSeconds(1));

            //3、将我们的新数据放入请求(先将对象转换成json格式)
            //XContentType.JSON 指定类型为 json
            //User user = new User("wz222", 18);
            updateRequest.doc(JSON.toJSONString(jdBook), XContentType.JSON);

            //4、客户端发送 update 请求，得到响应
            UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            System.out.println(response);

        }catch (Exception e){
            e.printStackTrace();
        }


    }



    //4 删除文档
    public static void deleteDocument(String indexName, String docId, RestHighLevelClient restHighLevelClient){
        try {
            //1、创建删除的请求
            DeleteRequest deleteRequest = new DeleteRequest(indexName,docId);
            //2、客户端发起删除请求，得到响应
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

            System.out.println(deleteResponse.status());//输出响应状态

        }catch (Exception e){
            e.printStackTrace();
        }


    }



    //5 批量插入数据（List）
    public static void bulkDocumentList(List<JDBook> bookList, String indexName, RestHighLevelClient restHighLevelClient){
        try {
            //1、创建批量插入的请求
            BulkRequest bulkRequest = new BulkRequest();

            //2、传入批量数据（这里用List演示）
//            List<User> userList = new ArrayList<>();
//            userList.add(new User("阿泽1",11));
//            userList.add(new User("阿泽2",12));
//            userList.add(new User("阿泽3",13));

            //基本规则配置
            bulkRequest.timeout("10s");//设置超时时间

            //遍历，插入数据
            for (int i = 0; i < bookList.size(); i++) {
                // 指定索引
                // 设置id（否则生成随机id）（注意：最好不要这样指定id，否则每次把数据添加到这个索引，都是覆盖掉前面的值，而不是继续新增！！！）
                // 插入数据(json)
                bulkRequest.add(new IndexRequest(indexName)
                        //.id(""+(i+1))
                        .source(JSON.toJSONString(bookList.get(i)), XContentType.JSON)
                );
            }
            //发送请求，得到响应
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse.hasFailures());//是否失败，false为不失败，即 成功！

        }catch (Exception e){
            e.printStackTrace();
        }



    }



    //6 查询整个索引内的所有文档
    public static ArrayList<Map<String,Object>> searchAllDocument(String indexName, RestHighLevelClient restHighLevelClient){
        try {

            //1、创建查询请求（指定索引（数据库））(SearchRequest)
            SearchRequest searchRequest = new SearchRequest(indexName);

            //2、构建搜索条件(SearchSourceBuilder)
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            //3、创建查询条件（使用 工具类QueryBuilders ）
            //查询所有文档
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();

            //4、配置 “搜索条件” 基本规则 并执行 “查询条件”
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            sourceBuilder.query(matchAllQueryBuilder);

            //5、构建请求（绑定 “搜索条件”）
            searchRequest.source(sourceBuilder);

            //6、执行请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //将结果封装成List，并返回List
            ArrayList<Map<String,Object>> resopnseList = new ArrayList<>();

            //7、获取搜索结果
            //System.out.println(JSON.toJSONString(searchResponse.getHits()));
            //System.out.println("==========================================");
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                resopnseList.add(hit.getSourceAsMap());
                //System.out.println(hit.getSourceAsMap());
            }
            return resopnseList;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }



//根据 indexName 和 keyword 查询数据（条件查询）
    public static ArrayList<Map<String, Object>> searchDocumentByKeyWord(String indexName, String keyWord, RestHighLevelClient restHighLevelClient){
        try {

            //1、创建查询请求（指定索引（数据库））(SearchRequest)
            SearchRequest searchRequest = new SearchRequest(indexName);

            //2、构建搜索条件(SearchSourceBuilder)
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            //3、创建查询条件（使用 工具类QueryBuilders ）
            //查询所有文档
            //matchQuery：会将搜索词分词，再与目标查询字段进行匹配，若分词中的任意一个词与目标字段匹配上，则可查询到。
            //termQuery：不会对搜索词进行分词处理，而是作为一个整体与目标字段进行匹配，若完全匹配，则可查询到。
            //name 字段 绑定到 keyWord 属性！（从head工具可以查看，书名的字段叫“name”）
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", keyWord);

            //4、配置 “搜索条件” 基本规则 并执行 “查询条件”
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            sourceBuilder.query(matchQuery);

            //5、构建请求（绑定 “搜索条件”）
            searchRequest.source(sourceBuilder);

            //6、执行请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //将结果封装成List，并返回List
            ArrayList<Map<String, Object>> resopnseList = new ArrayList<>();

            //7、获取搜索结果
            //System.out.println(JSON.toJSONString(searchResponse.getHits()));
            //System.out.println("==========================================");
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                resopnseList.add(hit.getSourceAsMap());
                //System.out.println(hit.getSourceAsMap());
            }
            return resopnseList;


        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }



}
