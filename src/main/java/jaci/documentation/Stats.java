package jaci.documentation;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Stats {

    static HashMap<File, FileStats> statsList;

    public static void main(String[] args) throws IOException, ParseException {
        getStats(args[0]);
    }

    public static void getStats(String dir) throws IOException, ParseException {
        System.out.println("[stat] Running Statistics");
        statsList = new HashMap<>();
        File root = new File(dir);
        runMetrics(root);
        int count = 0, pub = 0, doc = 0, docpub = 0;
        File fn = new File("stats");
        fn.mkdirs();
        JsonWriter writer = new JsonWriter(new FileWriter("stats/metrics.json"));
        writer.setIndent("\t");
        writer.beginObject();
        writer.name("files");
        writer.beginArray();
        for (Map.Entry<File, FileStats> pair : statsList.entrySet()) {
            FileStats st = pair.getValue();
            writer.beginObject();
            writer.name("name");
            String file = st.getFile().getAbsolutePath().replace(root.getAbsolutePath(), "");
            file = file.replaceFirst("\\\\|/", "");
            file = file.replace(".java", "").replaceAll("\\\\|/", ".");
            writer.value(file);
            count += st.getMethodCount();
            pub += st.getPublicMethodCount();
            doc += st.getTotalDocumented();
            docpub += st.getPublicDocumented();
            st.write(writer);
            writer.endObject();
        }
        writer.endArray();

        writer.name("overall");
        writer.beginObject();

        writer.name("method_count");
        writer.value(count);

        writer.name("public_count");
        writer.value(pub);

        writer.name("documented");
        writer.value(doc);

        writer.name("public_documented");
        writer.value(docpub);

        writer.name("percent_documented");
        writer.value(((float)doc / count) * 100);

        writer.name("percent_public_documented");
        writer.value(((float)docpub / pub) * 100);

        writer.name("file_count");
        writer.value(statsList.size());

        writer.endObject();
        writer.endObject();
        writer.close();
        System.out.println("[stat] Total Methods: " + count);
        System.out.println("[stat] Total Public Methods: " + pub);
        System.out.println("[stat] Documented Methods: " + doc);
        System.out.println("[stat] Documented Public Methods: " + docpub);
        System.out.println("[stat] Percentage Overall Documented: " + ((float)doc / count) * 100 + "%");
        System.out.println("[stat] Percentage Public Documented: " + ((float)docpub / pub) * 100 + "%");
        System.out.println("[stat] Finished Statistics");
        System.out.println();
        System.out.println("[stat] Uploading Gist...");
        String gistURL = Gists.upload(new File("stats/metrics.json"));
        System.out.println("[stat] Metrics uploaded to Gist: " + gistURL);
    }

    public static void runMetrics(File f) throws IOException, ParseException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null)
                for (File file : files) runMetrics(file);
        } else {
            try {
                CompilationUnit unit = JavaParser.parse(f);
                //new DocumentationMetrics().visit(unit, f);
                DocumentationMetrics metric = new DocumentationMetrics();
                metric.unit = unit;
                metric.visit(unit, f);
            } catch (Exception e) {
            }
        }
    }

    public static class DocumentationMetrics extends VoidVisitorAdapter<File> {
        CompilationUnit unit;
        @Override
        public void visit(MethodDeclaration m, File file) {
            FileStats stats;
            if (statsList.containsKey(file))
                stats = statsList.get(file);
            else stats = new FileStats(file);
            stats.addMethod(m);
            statsList.put(file, stats);
        }
    }

}
