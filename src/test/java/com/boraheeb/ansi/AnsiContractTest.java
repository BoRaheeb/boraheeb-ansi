package com.boraheeb.ansi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Regression cases for diagnostics, structure, determinism and thread safety. **/
final class AnsiContractTest{
    @TestFactory
    List<DynamicTest> testDiagnostics(){
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamicTest("Valid calls across the whole API produce no stdout/stderr output", () -> {
            Captured<Void> c = capture(() -> {
                for(int i = 0; i <= 255; i++){
                    Ansi.fg256(i);
                    Ansi.bg256(i);
                    Ansi.fgRgb(i, 255 - i, i);
                    Ansi.bgRgb(255 - i, i, 255 - i);
                }
                Ansi.wrap(Ansi.BOLD, "x");
                Ansi.wrap(null, (Object) null);
                return null;
            });
            assertNoOutput(c);
        }));
        tests.add(dynamicTest("Invalid calls write warnings to stderr only, never stdout", () -> {
            Captured<Void> c = capture(() -> {
                Ansi.fg256(-1);
                Ansi.bgRgb(-1, -1, -1);
                return null;
            });
            assertEquals("", c.out, "stdout");
            assertEquals(4, c.errLines().size(), "stderr warning lines");
        }));
        tests.add(dynamicTest("fg and bg warnings identify their own method", () -> {
            Captured<String> fg = capture(() -> Ansi.fg256(300));
            Captured<String> bg = capture(() -> Ansi.bg256(300));
            assertTrue(fg.err.contains("fg256") && !fg.err.contains("bg256"), "fg warning mentions fg256, not bg256: " + fg.err);
            assertTrue(bg.err.contains("bg256") && !bg.err.contains("fg256"), "bg warning mentions bg256, not fg256: " + bg.err);
        }));
        tests.add(dynamicTest("Validation agrees with fallback: isValid256(x) == !(fg256(x) warns) for x in [-300, 600]", () -> {
            for(int x = -300; x <= 600; x++){
                int code = x;
                Captured<String> c = capture(() -> Ansi.fg256(code));
                boolean warned = !c.err.isEmpty();
                if(Ansi.isValid256(code) == warned){
                    fail("mismatch at x=" + code + ": expected <" + "isValid256=" + !warned + "> but was <" + "warned=" + warned + ">");
                }
            }
        }));
        tests.add(dynamicTest("Validation agrees with fallback: isValidRgbChannel(x) == !(fgRgb warns) for x in [-300, 600]", () -> {
            for(int x = -300; x <= 600; x++){
                int value = x;
                Captured<String> c = capture(() -> Ansi.fgRgb(value, 0, 0));
                boolean warned = !c.err.isEmpty();
                if(Ansi.isValidRgbChannel(value) == warned){
                    fail("mismatch at x=" + value + ": expected <" + "isValidRgbChannel=" + !warned + "> but was <" + "warned=" + warned + ">");
                }
            }
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testEdgeCases(){
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamicTest("Ansi is final with a single private constructor", () -> {
            assertTrue(Modifier.isFinal(Ansi.class.getModifiers()), "class is final");
            Constructor<?>[] ctors = Ansi.class.getDeclaredConstructors();
            assertEquals(1, ctors.length, "constructor count");
            assertTrue(Modifier.isPrivate(ctors[0].getModifiers()), "constructor is private");
        }));
        tests.add(dynamicTest("All public fields are static final Strings (immutable constants)", () -> {
            for(Field f : Ansi.class.getFields()){
                int m = f.getModifiers();
                assertTrue(Modifier.isStatic(m) && Modifier.isFinal(m), f.getName() + " is static final");
                assertEquals(String.class, f.getType(), f.getName() + " type");
            }
        }));
        tests.add(dynamicTest("Every 256-color code 0..255 yields a distinct, well-formed sequence", () -> {
            Set<String> seen = new TreeSet<>();
            for(int i = 0; i <= 255; i++){
                assertEquals(sgr("38;5;" + i), Ansi.fg256(i), "fg256(" + i + ")");
                seen.add(Ansi.fg256(i));
            }
            assertEquals(256, seen.size(), "distinct sequences");
        }));
        tests.add(dynamicTest("fg and bg variants never collide for the same input", () -> {
            for(int i = 0; i <= 255; i++){
                if(Ansi.fg256(i).equals(Ansi.bg256(i)) || Ansi.fgRgb(i, i, i).equals(Ansi.bgRgb(i, i, i))){
                    fail("collision at " + i + ": expected <" + "fg != bg" + "> but was <" + "fg == bg" + ">");
                }
            }
        }));
        tests.add(dynamicTest("Generated sequences contain no whitespace and end with the SGR suffix 'm'", () -> {
            for(String s : new String[]{Ansi.fg256(0), Ansi.bg256(255), Ansi.fgRgb(0, 0, 0), Ansi.bgRgb(255, 255, 255)}){
                assertTrue(s.startsWith(CSI), "starts with CSI: " + visible(s));
                assertTrue(s.endsWith("m"), "ends with 'm': " + visible(s));
                assertTrue(!s.matches(".*\\s.*"), "no whitespace: " + visible(s));
            }
        }));
        tests.add(dynamicTest("Invalid input never throws for extreme values", () -> {
            int[] extremes = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -1, 256, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
            capture(() -> {
                for(int v : extremes){
                    Ansi.fg256(v);
                    Ansi.bg256(v);
                    Ansi.fgRgb(v, v, v);
                    Ansi.bgRgb(v, v, v);
                }
                return null;
            });
        }));
        tests.add(dynamicTest("Repeated calls are deterministic", () -> {
            assertEquals(Ansi.fg256(123), Ansi.fg256(123), "fg256");
            assertEquals(Ansi.bgRgb(1, 2, 3), Ansi.bgRgb(1, 2, 3), "bgRgb");
            assertEquals(Ansi.wrap(Ansi.BOLD, "x"), Ansi.wrap(Ansi.BOLD, "x"), "wrap");
        }));
        tests.add(dynamicTest("wrap does not strip or deduplicate nested RESETs", () -> {
            String inner = Ansi.wrap(Ansi.FG_RED, "a");
            assertEquals(sgr("1") + sgr("31") + "a" + sgr("0") + sgr("0"), Ansi.wrap(Ansi.BOLD, inner), "nested");
        }));
        tests.add(dynamicTest("wrap(String, Object) with toString() returning null -> empty text", () -> {
            Object value = new Object(){
                @Override
                public String toString(){
                    return null;
                }
            };
            assertEquals(sgr("1") + sgr("0"), Ansi.wrap(Ansi.BOLD, value), "result");
        }));
        tests.add(dynamicTest("Concurrent valid calls return correct results (documented thread-safety)", () -> {
            ExecutorService pool = Executors.newFixedThreadPool(8);
            try{
                List<Future<String>> results = new ArrayList<>();
                for(int t = 0; t < 8; t++){
                    results.add(pool.submit(() -> {
                        for(int i = 0; i <= 255; i++){
                            if(!Ansi.fgRgb(i, 255 - i, i).equals(sgr("38;2;" + i + ";" + (255 - i) + ";" + i))){
                                return "bad result at " + i;
                            }
                        }
                        return "";
                    }));
                }
                for(Future<String> f : results){
                    assertEquals("", f.get(), "thread result");
                }
            }
            finally{
                pool.shutdownNow();
            }
        }));
        return tests;
    }
}
