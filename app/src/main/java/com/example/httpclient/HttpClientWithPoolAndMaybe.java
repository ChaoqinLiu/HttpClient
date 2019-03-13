package com.example.httpclient;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientWithPoolAndMaybe {

    /**
     * 全局连接池对象
     */
    private static final PoolingHttpClientConnectionManager connManage = new PoolingHttpClientConnectionManager();

    /**
     * 静态代码块配置连接池信息
     */
    static {
        //设置最大连接数
        connManage.setMaxTotal(200);
        //设置每个连接的路由数
        connManage.setDefaultMaxPerRoute(20);
    }

    /**
     * 获取Http客户端连接对象
     *
     * @param timeOut 超时时间
     * @return  Http 客户端连接对象
     */
    public static CloseableHttpClient getHttpClient(int timeOut){
        //创建Http请求配置参数
        RequestConfig requestConfig = RequestConfig.custom()
                //获取连接超时时间
        .setConnectionRequestTimeout(timeOut)
                //请求超时时间
        .setConnectTimeout(timeOut)
                //响应超时时间
        .setSocketTimeout(timeOut)
                .build();

        //创建httpClient
        return HttpClients.custom()
                //把请求相关的超时信息设置到连接客户端
        .setDefaultRequestConfig(requestConfig)
                //把请求重试设置到连接客户端
        .setRetryHandler(new RetryHandler())
                //配置连接池管理对象
        .setConnectionManager(connManage)
                .build();
    }

    /**
     * GET请求
     * @param url 请求地址
     * @param timeOut 超时时间
     * @return
     */
    public static Maybe<String> httpGet(String url,int timeOut){

        return Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull MaybeEmitter<String> emitter) throws Exception {
                emitter.onSuccess(url);
            }
        }).map(new Function<String, String>() {
            @Override
            public String apply(@NonNull String s) throws Exception {

                //获取客户端连接对象
                CloseableHttpClient httpClient = getHttpClient(timeOut);
                //创建GET请求对象
                HttpGet get = new HttpGet(url);

                CloseableHttpResponse response = httpClient.execute(get);
                String msg = null;
                try {
                    //执行请求
                    response = httpClient.execute(get);
                    //获取响应实例
                    HttpEntity entity = response.getEntity();
                    //获取响应信息
                    msg = EntityUtils.toString(entity,"UTF-8");
                } catch (ClientProtocolException e) {
                    System.out.println("协议错误");
                    e.printStackTrace();
                } catch (ParseException e) {
                    System.out.println("解析错误");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("I/O 错误");
                    e.printStackTrace();
                } finally {
                    try {
                        if (response != null) {
                            EntityUtils.consume(response.getEntity());
                            response.close();
                        }
                    } catch (IOException e) {
                        System.out.println("释放链接错误");
                        e.printStackTrace();
                    }
                }
                return msg;
            }
        });
    }

    public static void main(String[] args){

        httpGet("http://www.163.com",600)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }
}
