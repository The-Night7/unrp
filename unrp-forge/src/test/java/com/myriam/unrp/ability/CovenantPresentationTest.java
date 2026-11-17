package com.myriam.unrp.ability;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
class CovenantPresentationTest {
    @Test
    void finePrintNeverEntersPublicPages() {
        var laws = List.of(new CovenantPresentation.Law(1, "Secret penalty", true),
            new CovenantPresentation.Law(2, "No running. Speed", false));
        for (long tick : new long[] {0, 160, 320, 999999})
            assertEquals(List.of(laws.get(1)), CovenantPresentation.visiblePage(laws, tick));
        assertTrue(CovenantPresentation.visiblePage(List.of(laws.get(0)), 0).isEmpty());
    }
    @Test
    void everyVisibleLawAppearsInBoundedPagesAndTheCycleWraps() {
        var laws = IntStream.rangeClosed(1, 8)
                       .mapToObj(i -> new CovenantPresentation.Law(i, "Law " + i, false))
                       .toList();
        assertEquals(List.of(1, 2, 3),
            CovenantPresentation.visiblePage(laws, 0).stream().map(CovenantPresentation.Law::id).toList());
        assertEquals(List.of(4, 5, 6),
            CovenantPresentation.visiblePage(laws, 160).stream().map(CovenantPresentation.Law::id).toList());
        assertEquals(List.of(7, 8),
            CovenantPresentation.visiblePage(laws, 320).stream().map(CovenantPresentation.Law::id).toList());
        assertEquals(CovenantPresentation.visiblePage(laws, 0), CovenantPresentation.visiblePage(laws, 480));
    }
    @Test
    void displayBoundDoesNotModifyTheActualLaw() {
        var law = new CovenantPresentation.Law(4, "x".repeat(3000), false);
        var shown = CovenantPresentation.visiblePage(List.of(law), 0).get(0);
        assertEquals(512, shown.text().length());
        assertTrue(shown.text().endsWith("..."));
        assertEquals(3000, law.text().length());
        assertTrue(CovenantPresentation.visiblePage(List.of(), 0).isEmpty());
    }
}
