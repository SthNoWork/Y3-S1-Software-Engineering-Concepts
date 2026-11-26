package edu.itc.salesreport.engine;

import edu.itc.salesreport.ingest.SalesDataGenerator;
import edu.itc.salesreport.model.MonthlyReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Every concurrent engine must give the same report as the sequential engine. */
@Timeout(value = 20, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class EngineEqualityTest {
    static final YearMonth MONTH = YearMonth.of(2026, 9);
    @TempDir static Path data;
    @TempDir static Path broken;
    static MonthlyReport expected;

    @BeforeAll
    static void generate() throws IOException {
        new SalesDataGenerator().generate(MONTH, 500, 42L, data);
        Files.writeString(data.resolve("2026-09/PNH-2026-09-05.csv"),
                "PNH,2026-09-05,PNH-999999,SKU-1001,Jasmine Rice 5kg,Grocery,0,6.50,0.00,CASH\n",
                StandardOpenOption.APPEND);
        expected = new SequentialReportEngine().generate(MONTH, data);

        new SalesDataGenerator().generate(MONTH, 50, 1L, broken);
        Files.writeString(broken.resolve("2026-09/REP-2026-09-02.csv"), "garbage\n");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    void poolEngineEqualsSequential(int threads) {
        assertEquals(expected, new PoolReportEngine(threads).generate(MONTH, data));
    }

    @Test
    void poolEngineFailsOnABadFile() {
        RuntimeException e = assertThrows(RuntimeException.class,
                () -> new PoolReportEngine(4).generate(MONTH, broken));
        assertInstanceOf(IOException.class, e.getCause());
    }
}
