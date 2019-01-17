package com.kingkung.train.api;

import com.kingkung.train.utils.FileUtil;
import com.kingkung.train.utils.Log;
import com.kingkung.train.utils.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TrainApiService {
    private static TrainApi service;

    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public TrainApiService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(Log::print);
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(logging)
                .cookieJar(new MyCookieJar())
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)));

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            X509TrustManager trustManager = new MyX509TrustManager();
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
            builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
        }

        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Urls.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(TrainApi.class);
    }

    public synchronized static TrainApi getTrainApi() {
        if (service == null) {
            new TrainApiService();
        }
        return service;
    }

    public class MyCookieJar implements CookieJar {

        @Override
        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> newCookies) {
            String host = httpUrl.host();
            if (newCookies == null || newCookies.size() < 1) {
                return;
            }
            List<Cookie> cookies = cookieStore.get(host);
            if (cookies == null || cookies.size() == 0) {
                List<Cookie> saveCookies = new ArrayList<>(newCookies);
                cookieStore.put(host, saveCookies);
                return;
            }
            for (Cookie newCookie : newCookies) {
                String name = newCookie.name();
                if (TextUtils.isEmpty(name)) {
                    continue;
                }
                Iterator<Cookie> it = cookies.iterator();
                while (it.hasNext()) {
                    Cookie cookie = it.next();
                    if (name.equals(cookie.name())) {
                        it.remove();
                    }
                }
                cookies.add(newCookie);
            }
            saveCookieIfNeed(httpUrl, cookies);
        }

        public void saveCookieIfNeed(HttpUrl httpUrl, List<Cookie> saveCookies) {
            if (httpUrl.encodedPath().equals(Urls.UAMAUTH_CLIENT)) {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(FileUtil.cookieFile, false));
                    for (int i = 0; i < saveCookies.size(); i++) {
                        bw.write(saveCookies.get(i).toString());
                        if (i < (saveCookies.size() - 1)) {
                            bw.write("\r\n");
                        }
                    }
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public List<Cookie> loadCookieIfNeed(HttpUrl httpUrl) {
            if (httpUrl.encodedPath().equals(Urls.UAMTK)) {
                List<Cookie> cookies = new ArrayList<>();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(FileUtil.cookieFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        cookies.add(Cookie.parse(httpUrl, line));
                    }
                    br.close();
                } catch (IOException e) {
                }
                return cookies;
            }
            return null;
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
            String host = httpUrl.host();
            List<Cookie> cookies = cookieStore.get(host);
            if (cookies == null || cookies.size() == 0) {
                cookies = loadCookieIfNeed(httpUrl);
                if (cookies != null || cookies.size() > 0) {
                    cookieStore.put(host, cookies);
                }
            }
            return cookies != null ? cookies : new ArrayList<>();
        }
    }

    public class MyX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
