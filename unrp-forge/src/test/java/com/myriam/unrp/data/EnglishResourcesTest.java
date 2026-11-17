package com.myriam.unrp.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnglishResourcesTest {
    private JsonObject locale(String name) throws Exception {
        return JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/unrp/lang/" + name + ".json"))).getAsJsonObject();
    }

    @Test void selectingAnotherClientLocaleStillShowsTheEnglishInterface() throws Exception {
        assertEquals(locale("en_us"), locale("fr_fr"));
    }

    @Test void recoveryAndContractItemsKeepTheirEnglishNames() throws Exception {
        var text = locale("en_us");
        assertEquals("Recovery Bed", text.get("block.unrp.recovery_bed").getAsString());
        assertEquals("Recovery Machine", text.get("block.unrp.recovery_machine").getAsString());
    }
}
