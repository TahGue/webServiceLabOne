import DAO.StudentManager;
import com.google.gson.Gson;
import models.Student;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {
    public static void main(String[] args) {


        ExecutorService executorService = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(5050)) {

            while (true) {
                Socket client = serverSocket.accept();
                executorService.submit(() -> routing(client));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void routing(Socket client) {
        try {
            BufferedReader requestedInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String inputHeader = readRequest(requestedInput);
            String method = inputHeader.split(" ")[0];
            String url = inputHeader.split(" ")[1];

            OutputStream resp = client.getOutputStream();
            switch(method){
                case "GET":
                    handleGetMethods(resp,url);
                    break;
                case "HEAD":
              //      handleHeadMethods(resp,url);
                    break;
                case "POST":
              //      handlePostMethods(resp,url);
                    break;
                default:
               //     handleNotFound(resp,url);
            }
            requestedInput.close();
            resp.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleGetMethods(OutputStream resp, String url) throws IOException {
        Gson gson = new Gson();
        String header="";
        switch(url){
            case "/students":
                List<Student> persons = StudentManager.fetchAll();
                String json = gson.toJson(persons);
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                 header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";
                resp.write(header.getBytes());
                resp.write(data);
                resp.flush();
                return;
            default:
                sendStaticFile(resp,url);
        }
    }

    private static void sendStaticFile(OutputStream resp,String url) throws IOException {
        String header = "";
        byte[] data = new byte[0];
        String[] sections = url.split("/");
        System.out.println("sections");
      String fileName = sections[2];
      System.out.println(fileName);
        if((sections.length!=3) || !sections[1].equals("static")){
            header = "HTTP/1.1 401 Not Found\r\nContent-length: 0\r\n\r\n";
            resp.write(("Not Allowed".getBytes(StandardCharsets.UTF_8)));
            resp.flush();
        }

        File f = Path.of("server", "target","web","static",fileName).toFile();
        if (!f.exists() && !f.isDirectory()) {
            header = "HTTP/1.1 404 Not Found\r\nContent-length: 0\r\n\r\n";
            resp.write(header.getBytes());
            resp.write(("Not Found".getBytes(StandardCharsets.UTF_8)));
            resp.flush();
        } else {
            try (FileInputStream fileInputStream = new FileInputStream(f)) {
                data = new byte[(int) f.length()];
                fileInputStream.read(data);
                String contentType = Files.probeContentType(f.toPath());
                header = "HTTP/1.1 200 OK\r\nContent-type: " + contentType + "\r\nContent-length: " + data.length + "\r\n\r\n";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        resp.write(header.getBytes());
            resp.write(data);
            resp.flush();
    }


    private static String readRequest(BufferedReader inputFromClient) throws IOException {

        String type = "";
        String url = "";

        while (true) {
            String line = inputFromClient.readLine();
            if (line.startsWith("GET")) {
                type = "GET";
                url = line.split(" ")[1];

            } else if (line.startsWith("HEAD")) {
                type = "HEAD";
                url = line.split(" ")[1];

            } else if (line.startsWith("POST")) {
                type = "POST";
                url = line.split(" ")[1];
            }
            return type + " " + url;
        }
    }
}
