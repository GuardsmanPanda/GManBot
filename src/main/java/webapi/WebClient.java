package webapi;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import javax.naming.spi.ObjectFactory;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

public class WebClient {
    private static final HttpClient client = HttpClient.newHttpClient();


    public static <E> E getJSonAndMapToType(HttpRequest request, Function<JsonNode, E> mapping) {
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
}
