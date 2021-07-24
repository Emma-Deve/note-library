
import com.wuze.jdelasticsearch.pojo.JDBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuze
 * @desc ...
 * @date 2021-02-21 17:49:56
 */

//爬取京东首页数据工具类
public class HtmlParseUtil {

    /*public static void main(String[] args) throws Exception {
        List<JDBook> bookList = new HtmlParseUtil().parseJD("vue");
        for (JDBook jdBook : bookList) {
            System.out.println(jdBook);
        }

    }*/

    public List<JDBook> parseJD(String keyword) throws Exception {
        //获取请求（url）
        String url = "https://search.jd.com/Search?keyword="+keyword;
        //解析网页，jsoup 返回的 document 就是 浏览器的 Document对象（接下来对document的操作就跟前端 js 操作一样）
        Document document = Jsoup.parse(new URL(url),30000);//30s超时
        //获取搜索结果的 List 集合
        Element jGoodsList = document.getElementById("J_goodsList");
        //获取集合里面的单个元素
        Elements liGoodsList = jGoodsList.getElementsByTag("li");

        //创建List 集合
        List<JDBook> jdBookList = new ArrayList<>();

        //遍历所有 li 标签，获取单个商品信息
        for (Element perGoods : liGoodsList) {
            //1、添加 .text() 方法，才能转换成 String 输出
            //2、图片img 是懒加载方式，所以只提取 img 的src属性是爬取不到数据的，需要指定data-lazy-img 属性
            String img = perGoods.getElementsByTag("img").attr("data-lazy-img");//获取商品图片（先获取标签，再获取标签内属性）
            String price = perGoods.getElementsByClass("p-price").text();//书的价格
            String name = perGoods.getElementsByClass("p-name").text();//书的名称
            String bookShop = perGoods.getElementsByClass("p-shopnum").text();//书店名
            JDBook jdBook = new JDBook();
            jdBook.setImg(img);
            jdBook.setPrice(price);
            jdBook.setName(name);
            jdBook.setBookShop(bookShop);
            jdBookList.add(jdBook);

        }

        return jdBookList;


    }
}
