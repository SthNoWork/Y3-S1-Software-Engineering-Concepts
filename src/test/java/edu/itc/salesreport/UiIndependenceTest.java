package edu.itc.salesreport;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UiIndependenceTest {
    private static final Pattern FORBIDDEN_IMPORTS = Pattern.compile("^import\\s+(javafx\\.|java\\.awt\\.|javax\\.swing\\.).*");

    @Test
    void corePackagesMustNeverImportUiToolkits() throws IOException {
        Path sourceRoot = Path.of("src/main/java/edu/itc/salesreport");
        List<Path> checkDirs = List.of(
                sourceRoot.resolve("model"),
                sourceRoot.resolve("ingest"),
                sourceRoot.resolve("engine"),
                sourceRoot.resolve("ui/console"),
                sourceRoot.resolve("ui/mvp")
        );

        List<String> violations = new ArrayList<>();

        for (Path dir : checkDirs) {
            if (!Files.exists(dir)) {
                continue;
            }
            try (Stream<Path> stream = Files.list(dir)) {
                List<Path> javaFiles = stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java")).toList();
                for (Path file : javaFiles) {
                    List<String> lines = Files.readAllLines(file);
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i).trim();
                        if (FORBIDDEN_IMPORTS.matcher(line).matches()) {
                            violations.add(file + " L" + (i + 1) + ": " + line);
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Architectural boundary violated! Toolkit-free packages must not import UI frameworks:\n"
                        + String.join("\n", violations));
    }
}
