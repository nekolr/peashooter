package com.github.nekolr.peashooter.parser;

import com.github.nekolr.peashooter.PeashooterApplication;
import com.github.nekolr.peashooter.service.IRawParserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PeashooterApplication.class)
public class RawParserServiceTest {

    @Autowired
    private IRawParserService rawParserService;

    @Test
    public void testAutoParse() {
        Long datasourceId = 1L; // Replace with a valid datasource ID
        rawParserService.autoParse(datasourceId);
    }
}
