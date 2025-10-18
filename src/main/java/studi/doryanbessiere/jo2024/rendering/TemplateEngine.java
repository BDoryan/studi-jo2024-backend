package studi.doryanbessiere.jo2024.rendering;

import java.util.Map;

public interface TemplateEngine {
    String render(String templateName, Map<String, String> variables);
}
