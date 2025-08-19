package com.github.kiu345.eclipse.llamawhip.adapter.local;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = LocalAIAdapterTest.ENV_LOCALAI_URL, matches = "http.*", disabledReason = "LOCALAI_URL not defined")
@Tag("adapter")
class LocalAIAdapterTest {
    public static final String ENV_LOCALAI_URL = "LOCALAI_URL";
    @SuppressWarnings("unused")
    private String serverURL;

    @BeforeEach
    void setUp() throws Exception {
        serverURL = System.getenv(ENV_LOCALAI_URL);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test() {
        fail("Not yet implemented");
    }

}
