import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
public class Client {
    static String contentType = "";
    static String url = "";
    static boolean loop = true;
    static Scanner sc = new Scanner(System.in);
    static String fileExtension = "";


    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

       String url ="";
       System.out.println("Choose a choice");
        System.out.println("1. Fetch students");
        System.out.println("2. Fetch student by Id");
        System.out.println("3. insert a new student");

        System.out.println("Choose a choice");
        System.out.println("Choose a choice");
       url = sc.nextLine();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .header("accept", contentType)
                .uri(URI.create("http://localhost/" + url))
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

    }

    private static void GetRequests(String url, String urlType) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .header("accept", contentType)
                .uri(URI.create("http://localhost/" + url))
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        if (url.equals("storage")) {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (urlType.equals("HEAD")) {
                System.out.println(response.headers());
            } else {
                ObjectMapper mapper = new ObjectMapper();
                List<Person> posts = mapper.readValue(response.body(), new TypeReference<>() {
                });
                posts.forEach(System.out::println);
            }
        } else if (url.contains("?")) {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (urlType.equals("HEAD")) {
                System.out.println(response.headers());
            } else {
                ObjectMapper mapper = new ObjectMapper();
                Person post = mapper.readValue(response.body(), new TypeReference<>() {
                });
                System.out.println(post);
            }

        } else if (url.contains(".")) {
            HttpResponse<byte[]> response = client.send(getRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (urlType.equals("HEAD")) {
                System.out.println(response.headers());
            } else {
                if (200 == response.statusCode()) {
                    byte[] bytes = response.body();
                    try (OutputStream out = new FileOutputStream(url)) {
                        out.write(bytes);
                    }
                }
            }
        }
    }

    public static CompletableFuture<Void> POSTRequest(Map<String, String> bodyText) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(bodyText);

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create("http://localhost/" + url))
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(System.out::println);
    }
}
