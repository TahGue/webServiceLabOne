import DAO.StudentManager;
import com.google.gson.Gson;
import models.Student;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {
    public static void main(String[] args) {

        ExecutorService executorService = Executors.newCachedThreadPool();
        int port = 5050;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port "+port);
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
                //   handleHeadMethods(resp,url);
                    break;
                case "POST":
                    handlePostMethods(resp,requestedInput,url);
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

    private static void handlePostMethods(OutputStream resp, BufferedReader requestData, String url) throws IOException {
        Gson gson = new Gson();


        HashMap<String,String> requestString = requestBody(requestData);

        Student student = StudentManager.createStudent( requestString.get("name"),requestString.get("tel"),requestString.get("email"),requestString.get("image"));

        String json = gson.toJson(student);
        String header="";
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";
        resp.write(header.getBytes());
        resp.write(data);
        resp.flush();

    }

    private static void handleGetMethods(OutputStream resp, String url) throws IOException {
        Gson gson = new Gson();
        String header="";
        switch(url){
            case "/students":
                List<Student> persons = StudentManager.fetchAll();
                String json = gson.toJson(persons);
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                 header = "HTTP/1.1 201 CREATED\r\nContent-Type: application/json\r\nContent-length: " + data.length + "\r\n\r\n";
                resp.write(header.getBytes());
                resp.write(data);
                resp.flush();
                return;

            default:
                if(url.contains("?id=")){
                 HashMap<String,String> params = requestParams(url);
                  int id = Integer.parseInt(params.get("id"));
                    System.out.println("id");
                    System.out.println(id);
                  Student student = StudentManager.fetchById(id);
                    System.out.println("id");
                    System.out.println(id);
                    System.out.println(student);
                    if(student.equals(null)){
                        header = "HTTP/1.1 404 Not Found\\r\\nContent-length: 0\\r\\n\\r\\n";
                        resp.write(header.getBytes());
                        resp.write(Integer.parseInt("Not Found"));
                        resp.flush();
                    }else{
                        String studentJson = gson.toJson(student);
                        byte[] studentData = studentJson.getBytes(StandardCharsets.UTF_8);
                        header = "HTTP/1.1 201 CREATED\r\nContent-Type: application/json\r\nContent-length: " + studentData.length + "\r\n\r\n";
                        resp.write(header.getBytes());
                        resp.write(studentData);
                        resp.flush();
                    }
                }else{
                    sendStaticFile(resp,url);
                }

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
            header = "HTTP/1.1 404 Not Found\\r\\nContent-length: 0\\r\\n\\r\\n";
            resp.write(header.getBytes());
            resp.write(("Not Found".getBytes(StandardCharsets.UTF_8)));
            resp.flush();
        }

        File f = Path.of("server", "target","web","static",fileName).toFile();
        if (!f.exists() && !f.isDirectory()) {
            header = "HTTP/1.1 404 Not Found\\r\\nContent-length: 0\\r\\n\\r\\n";
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

    private static HashMap<String,String> requestBody(BufferedReader requestInput) throws IOException {

        StringBuffer buffer = new StringBuffer();
        String string = null;
        int bodyLength = 0;
        while (!(string = requestInput.readLine()).equals("")) {
            buffer.append(string + "");
            if (string.startsWith("Content-Length:")) {
                bodyLength = Integer.valueOf(string.substring(string.indexOf(' ') + 1, string.length()));
            }
        }
        char[] body = new char[bodyLength];
        requestInput.read(body, 0, bodyLength);
        String requestBody = new String(body);
        System.out.println("requestBody");
        System.out.println(requestBody);
        System.out.println("urlDecoder");
        String decodedBody = URLDecoder.decode(requestBody,"UTF-8");
        String[] keysValues = decodedBody.split("&");
       HashMap<String,String> resultList = new HashMap<>();

       for(int i = 0; i<keysValues.length;i++){
           resultList.put(keysValues[i].split("=")[0],keysValues[i].split("=")[1]);
       }
      return  resultList;
    }

    private static HashMap<String,String> requestParams(String url) throws IOException {

        String[] urlSplits = url.split("\\?");
        System.out.println("urlSplits");
        System.out.println(urlSplits[0]);
        String paramsString = urlSplits[1];
        String[] allParams = paramsString.split("&");

        HashMap<String,String> resultList = new HashMap<>();

        for(int i = 0; i<allParams.length;i++){
            resultList.put(allParams[i].split("=")[0],allParams[i].split("=")[1]);
        }
        return  resultList;
    }

}
