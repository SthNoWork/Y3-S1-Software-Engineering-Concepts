package edu.itc.salesreport.engine;

import java.nio.file.Path;
import java.util.List;

/** The data lines of one CSV file (header removed). The list is unmodifiable. */
public record FileContent(Path file, List<String> lines) {

    /** Poison pill for producer-consumer queues: compare it with ==. */
    public static final FileContent POISON = new FileContent(Path.of("POISON"), List.of());
}
