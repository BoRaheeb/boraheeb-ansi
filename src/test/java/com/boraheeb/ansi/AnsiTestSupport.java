package com.boraheeb.ansi;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.DynamicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Independent SGR expectations and diagnostic capture shared by the tests. **/
final class AnsiTestSupport{
    static final String CSI = "\u001B[";
    static final int FALLBACK_256 = 244;
    static final int FALLBACK_RGB = 180;

    private AnsiTestSupport(){}
    static final class Captured<T>{
        final T result;
        final String out;
        final String err;
        Captured(T result, String out, String err){
            this.result = result;
            this.out = out;
            this.err = err;
        }
        /** Returns the non-empty lines written to {@code System.err}. **/
        List<String> errLines(){
            List<String> lines = new ArrayList<>();
            for(String line : err.split("\\R")){
                if(!line.isEmpty()){
                    lines.add(line);
                }
            }
            return lines;
        }
    }

    static String sgr(String params){
        return (CSI + params + "m");
    }

    static String visible(Object value){
        if(value == null){
            return "null";
        }
        return String.valueOf(value)
            .replace("\u001B", "ESC")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }

    static void assertNoOutput(Captured<?> c){
        assertEquals("", c.err, "stderr");
        assertEquals("", c.out, "stdout");
    }

    static void assertWarnings(Captured<?> c, int count, String... fragments){
        List<String> lines = c.errLines();
        assertEquals(count, lines.size(), "warning count");
        for(String line : lines){
            assertContains("warning", line, fragments);
        }
        assertEquals("", c.out, "stdout");
    }

    static void assertContains(String what, String actual, String... fragments){
        for(String fragment : fragments){
            assertTrue(actual.contains(fragment), what + ": expected text containing \"" + fragment + "\", actual " + visible(actual));
        }
    }

    /** Captures UTF-8 diagnostics and restores both global streams even on failure. **/
    static <T> Captured<T> capture(Callable<T> action) throws Exception{
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        T result;
        try(PrintStream capturedOut = new PrintStream(out, true, "UTF-8");
            PrintStream capturedErr = new PrintStream(err, true, "UTF-8")){
            try{
                System.setOut(capturedOut);
                System.setErr(capturedErr);
                result = action.call();
            }
            finally{
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        }
        return new Captured<>(result, new String(out.toByteArray(), StandardCharsets.UTF_8),
            new String(err.toByteArray(), StandardCharsets.UTF_8));
    }

    /** Checks exact stripping, idempotence, overload agreement and silence. **/
    static DynamicTest checkStrip(String name, String input, String expected){
        return dynamicTest("stripSgr: " + name, () -> {
            Captured<String> c = capture(() -> Ansi.stripSgr(input));
            assertEquals(expected, c.result, "result");
            assertEquals(c.result, Ansi.stripSgr(c.result), "idempotent");
            assertEquals(c.result, Ansi.stripSgr((Object) input), "Object overload");
            assertNoOutput(c);
        });
    }
}
