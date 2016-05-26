package twitch;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.CharSet;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * For interacting with the Twitch API
 */
public class Twitch {
    private static final HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        getCurrentGame();
    }

    public static String getCurrentGame() {
        HttpGet get = new HttpGet("https://api.twitch.tv/kraken/channels/guardsmanbob");

        try(InputStream input = client.execute(get).getEntity().getContent()) {
            get.releaseConnection();
            System.out.println(new ObjectMapper().readTree(input).get("game").asText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentTitle() {
        HttpGet get = new HttpGet("https://api.twitch.tv/kraken/channels/guardsmanbob");

        try(InputStream input = client.execute(get).getEntity().getContent()) {
            get.releaseConnection();
            System.out.println(new ObjectMapper().readTree(input).get("status").asText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static void setStreamTitle() {

    }

    public static void setHost() {

    }

    public static void removeHost() {

    }
}
