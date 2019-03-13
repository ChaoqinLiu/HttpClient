package com.example.httpclient;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class HttpClientWithMaybe {

    public static void main(String[] args){

        Maybe.create(new MaybeOnSubscribe<String>() {

            @Override
            public void subscribe(@NonNull MaybeEmitter<String> e) throws Exception{

                String url = "http://www.163.com";
                e.onSuccess(url);
            }
        }).map(new Function<String, CloseableHttpResponse>() {

            @Override
            public CloseableHttpResponse apply(@NonNull String url) throws Exception{

                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet get = new HttpGet(url);
                return client.execute(get);

            }
        }).subscribe(new Consumer<CloseableHttpResponse>() {
            @Override
            public void accept(CloseableHttpResponse closeableHttpResponse) throws Exception {

                //服务器返回码
                int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
                System.out.println(statusCode);

                HttpEntity entity = closeableHttpResponse.getEntity();

                //服务器返回内容
                String respStr = null;
                if (entity != null) {
                    respStr = EntityUtils.toString(entity,"UTF-8");
                }

                System.out.println(respStr);

                //释放资源
                EntityUtils.consume(entity);
            }
        });
    }
}
