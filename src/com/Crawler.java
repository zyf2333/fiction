package com;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author zyf
 * @Description
 * @ClassName Crawler
 * @Date 2020/7/29 14:36
 **/
public class Crawler {

    private static final String URL = "http://www.biquku.la/0/";
    private static final String BASE_URL = URL+"165/";
    private static final String INDEX_URL = BASE_URL+"152691.html";

    public static void main(String[] args) {
        start(INDEX_URL);
    }

    public static void start(String url) {
        Document html = getHtml(url);
        String content = getContent(html);
        String title = getTitle(html);
        System.out.println("标题："+title);
        save("赘婿", title+content);
        String nextUrl = getNextUrl(html);
        if(StrUtil.isNotEmpty(title)
                &&StrUtil.isNotEmpty(nextUrl)&&nextUrl.endsWith(".html")){
            start(nextUrl);
        }
    }


    public static Document getHtml(String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36 Edg/84.0.522.44");
        String html = HttpUtil.get(url, params);
        html = HtmlUtil.removeHtmlTag(HtmlUtil.unescape(html), "script");
        return Jsoup.parse(html);
    }

    public static String getContent(Document html) {
        Element content = html.getElementById("content");
        if(content!=null){
            String text = content.text();
            return text+"\n";
        }
        return null;
    }

    public static String getTitle(Document html) {
        Elements h1s = html.getElementsByTag("h1");
        for (Element h1 : h1s) {
            if (h1.parent().attr("class").equals("bookname")) {
                return h1.text()+"\n";
            }
        }
        return null;
    }

    public static String getNextUrl(Document html) {
        Elements bottem1 = html.getElementsByClass("bottem1");
        for (Element element : bottem1) {
            Elements aList = element.getElementsByTag("a");
            for (Element a : aList) {
                if (a.text().equals("下一章")) {
                    return BASE_URL+a.attr("href");
                }
            }
        }
        return null;
    }

    public static void save(String name,String content) {
//        FileWriter writer = new FileWriter("src/"+name+"txt");
//        writer.append(content);
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile("src/"+name+"txt", "rw");
            FileChannel channel = file.getChannel();
            byte[] bytes = content.getBytes();
            ByteBuffer buffer = MappedByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            channel.position(channel.size());
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
