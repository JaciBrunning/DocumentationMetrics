package jaci.documentation;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class FileStats {

    File f;
    int methodCount, publicCount, documented, documentedPublic;

    public FileStats(File file) {
        f = file;
    }

    public void addMethod(MethodDeclaration dec) {
        methodCount++;
        boolean pub = false;
        if (Modifier.isPublic(dec.getModifiers())) {
            publicCount++;
            pub = true;
        }

        if (dec.getComment() != null && !dec.getComment().equals("")) {
            documented++;
            if (pub) documentedPublic++;
        }
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getPublicMethodCount() {
        return publicCount;
    }

    public int getTotalDocumented() {
        return documented;
    }

    public int getPublicDocumented() {
        return documentedPublic;
    }

    public float getPercentageTotal() {
        if (methodCount == 0) return 0;
        return ((float)documented / methodCount) * 100;
    }

    public float getPercentagePublic() {
        if (publicCount == 0) return 0;
        return ((float)documentedPublic / publicCount) * 100;
    }

    public File getFile() {
        return f;
    }

    public void write(JsonWriter writer) throws IOException {
        writer.name("method_count");
        writer.value(getMethodCount());

        writer.name("public_count");
        writer.value(getPublicMethodCount());

        writer.name("documented");
        writer.value(getTotalDocumented());

        writer.name("public_documented");
        writer.value(getPublicDocumented());

        writer.name("percent_documented");
        writer.value(getPercentageTotal());

        writer.name("percent_public_documented");
        writer.value(getPercentagePublic());
    }

}
