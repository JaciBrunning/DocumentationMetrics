package jaci.documentation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class Gists {

    public static String upload(File json) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(json));
            List<String> str = new LinkedList<>();
            String ln;
            while ((ln = reader.readLine()) != null) {
                str.add(ln);
            }
            reader.close();
            String response = uploadGist(str);
            response = parseResponse(response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String uploadGist(List<String> lines) throws IOException {
        URL url = new URL("https://api.github.com/gists");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        String data = null;
        data = encodeFile(lines);

        conn.connect();
        StringBuilder stb = new StringBuilder();
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            stb.append(line).append("\n");
        }
        wr.close();
        rd.close();

        return stb.toString();
    }

    public static String encodeFile(List<String> lines) throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);

        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        String details = builder.toString();

        writer.beginObject();

        writer.name("public");
        writer.value(true);

        writer.name("description");
        writer.value("Java Documentation Report");

        writer.name("files");
        writer.beginObject();
        writer.name("metrics.json");
        writer.beginObject();
        writer.name("content");
        writer.value(details);
        writer.endObject();
        writer.endObject();

        writer.endObject();

        writer.close();

        return sw.toString();
    }

    public static String parseResponse(String response) {
        try {
            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            return obj.get("html_url").getAsString();
        } catch (Exception e) {
            System.err.println("Could not upload Crash Report to Gist");
        }
        return "";
    }

}
