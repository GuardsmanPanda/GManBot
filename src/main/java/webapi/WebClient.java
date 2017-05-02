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
import java.util.function.Consumer;

public class WebClient {
    private static final HttpClient client = HttpClient.newHttpClient();


    public static JsonNode getJSonNodeFromRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandler.asString());
            return new ObjectMapper().readTree(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return JsonNodeFactory.instance.objectNode().put("status", "error");
        }
    }
}
