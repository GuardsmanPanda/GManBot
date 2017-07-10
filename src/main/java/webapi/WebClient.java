package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

public class WebClient {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static <E> E getJSonAndMapToType(String uriGET, Function<JsonNode, E> mapping) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uriGET)).GET().build();
        JsonNode rootNode = getJSonNodeFromRequest(request);
        if (rootNode.has("bobError")) throw new RuntimeException("Error getting information: " + request.uri());
        return mapping.apply(rootNode);
    }

    public static JsonNode getJSonNodeFromRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandler.asString());
            return new ObjectMapper().readTree(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return JsonNodeFactory.instance.objectNode().put("bobError", "error");
        }
    }

    public static String getStringFromRequest(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandler.asString()).body();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Executes an http request and returns the status code.
     * @param request The request to execute
     */
    public static int executeHttpRequest(HttpRequest request) {
        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandler.asString());
            System.out.println("Status code: " + response.statusCode());
            System.out.println(response.body());
            return response.statusCode();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
