package org.inventivetalent.canvas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CanvasClient {

    static List<Cookie> cookies = new ArrayList<>();

    static final String ENDPOINT = "https://y.arr.place";
    static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                    cookies = list;
                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                    return cookies;
                }
            })
            .build();
    static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    static final Gson GSON = new GsonBuilder().create();

    static String accessToken = ""

    public static CompletableFuture<CanvasState> getHello() {
        return get("/hello")
                .thenApply(CanvasClient::extractAccessToken)
                .thenApply(CanvasClient::bodyToString)
                .thenApply(str -> GSON.fromJson(str, CanvasState.class))
                .thenApply(body -> {
                    return body;
                });
    }

    public static CompletableFuture<JsonArray> getState() {
        return get("/state")
                .thenApply(CanvasClient::extractAccessToken)
                .thenApply(CanvasClient::bodyToString)
                .thenApply(str -> GSON.fromJson(str, JsonArray.class))
                .thenApply(body -> {
                    return body;
                });
    }

    public static CompletableFuture<BufferedImage> getChunkImage(String id) {
        return get("/pngs/" + id)
                .thenApply(Response::body)
                .thenApply(ResponseBody::byteStream)
                .thenApply(stream -> {
                    try {
                        return ImageIO.read(stream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    static String bodyToString(Response response) {
        if (response.code() != 200) {
            System.err.println("Error: " + response.code());
            try {
                System.err.println(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ResponseBody body = response.body();
        if (body == null) return null;
        try {
            return body.string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static JsonElement stringToJson(String json) {
        return GSON.fromJson(json, JsonElement.class);
    }

    static Response extractAccessToken(Response response) {
        String cookieHeader = response.header("Set-Cookie");
        if (cookieHeader != null) {
            String[] split0 = cookieHeader.split(";");
            String[] split1 = split0[0].split("=");
            if (split1[0].equals("access_token")) {
                accessToken = split1[1];
            }
        }

        return response;
    }

    static void addCommon(Request.Builder builder) {
        builder.addHeader("Authorization", "Bearer " + accessToken);
        builder.addHeader("Cookie", "access_token=" + accessToken);
        builder.addHeader("User-Agent", "CanvasPlugin");
    }

    public static CompletableFuture<Response> get(String path) {
        var builder = new Request.Builder()
                .get()
                .url(ENDPOINT + path);
        addCommon(builder);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return CLIENT.newCall(builder.build()).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    public static CompletableFuture<Response> post(String path, RequestBody requestBody) {
        var builder = new Request.Builder()
                .post(requestBody)
                .url(ENDPOINT + path);
        addCommon(builder);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return CLIENT.newCall(builder.build()).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

}
