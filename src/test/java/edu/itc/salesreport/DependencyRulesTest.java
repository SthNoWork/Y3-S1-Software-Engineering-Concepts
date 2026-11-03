package edu.itc.salesreport;

import org.junit.jupiter.api.Test;

/**
 * Re-exports the architectural boundary check.
 */
class DependencyRulesTest {
    @Test
    void verifyBoundaries() throws Exception {
        new UiIndependenceTest().corePackagesMustNeverImportUiToolkits();
    }
}

