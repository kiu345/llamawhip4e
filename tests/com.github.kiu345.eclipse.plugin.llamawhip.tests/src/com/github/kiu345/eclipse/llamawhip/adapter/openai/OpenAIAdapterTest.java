package com.github.kiu345.eclipse.llamawhip.adapter.openai;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = OpenAIAdapterTest.TOKEN, matches = ".+", disabledReason = "OPENAI_TOKEN not defined")
@Tag("adapter")
class OpenAIAdapterTest {
    public static final String TOKEN = "OPENAI_TOKEN";

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test() {
        fail("Not yet implemented");
    }

}
