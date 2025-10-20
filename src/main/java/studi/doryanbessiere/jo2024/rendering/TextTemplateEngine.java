package studi.doryanbessiere.jo2024.rendering;

import org.springframework.stereotype.Component;
import studi.doryanbessiere.jo2024.rendering.exceptions.TemplateNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TextTemplateEngine implements TemplateEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        try {
            String content = loadTemplate(templateName);

            Matcher matcher = VARIABLE_PATTERN.matcher(content);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1).trim();
                String replacement = variables.getOrDefault(key, "").toString();
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(buffer);
            return buffer.toString();

        } catch (IOException e) {
            throw new TemplateNotFoundException("Template introuvable: " + templateName, e);
        }
    }

    private String loadTemplate(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream("templates/" + name + ".txt")) {
            if (stream == null) throw new IOException("Fichier introuvable");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
