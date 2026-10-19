package com.boraheeb.ansi;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Class initialization is checked in isolation, regardless of JUnit execution order. **/
final class AnsiInitializationTest{
    @Test
    @DisplayName("Loading Ansi initializes all constants without warnings or output")
    void initializationIsSilent() throws Exception{
        URL location = Ansi.class.getProtectionDomain().getCodeSource().getLocation();
        // No parent delegates Ansi to a loader that may already have initialized it.
        try(URLClassLoader loader = new URLClassLoader(new URL[]{location}, null)){
            Captured<Class<?>> captured = capture(() -> Class.forName("com.boraheeb.ansi.Ansi", true, loader));
            assertSame(loader, captured.result.getClassLoader(), "fresh classloader");
            assertEquals("", captured.err, "stderr during class init");
            assertEquals("", captured.out, "stdout during class init");
        }
    }
}
