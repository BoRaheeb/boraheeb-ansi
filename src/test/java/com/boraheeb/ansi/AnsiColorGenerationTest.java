package com.boraheeb.ansi;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Regression cases for color generation and validation. **/
final class AnsiColorGenerationTest{
    @TestFactory
    List<DynamicTest> test256Valid(){
        List<DynamicTest> tests = new ArrayList<>();
        for(int code : new int[]{0, 1, 16, 244, 255}){
            tests.add(dynamicTest("fg256(" + code + ") -> ESC[38;5;" + code + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.fg256(code));
                assertEquals(sgr("38;5;" + code), c.result, "result");
                assertNoOutput(c);
            }));
            tests.add(dynamicTest("bg256(" + code + ") -> ESC[48;5;" + code + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.bg256(code));
                assertEquals(sgr("48;5;" + code), c.result, "result");
                assertNoOutput(c);
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> test256Invalid(){
        List<DynamicTest> tests = new ArrayList<>();
        for(int code : new int[]{-1, 256, Integer.MIN_VALUE, Integer.MAX_VALUE}){
            tests.add(dynamicTest("fg256(" + code + ") falls back to " + FALLBACK_256 + " with one warning", () -> {
                Captured<String> c = capture(() -> Ansi.fg256(code));
                assertEquals(sgr("38;5;" + FALLBACK_256), c.result, "result");
                assertWarnings(c, 1, "fg256", String.valueOf(code), String.valueOf(FALLBACK_256));
            }));
            tests.add(dynamicTest("bg256(" + code + ") falls back to " + FALLBACK_256 + " with one warning", () -> {
                Captured<String> c = capture(() -> Ansi.bg256(code));
                assertEquals(sgr("48;5;" + FALLBACK_256), c.result, "result");
                assertWarnings(c, 1, "bg256", String.valueOf(code), String.valueOf(FALLBACK_256));
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testRgbValid(){
        List<DynamicTest> tests = new ArrayList<>();
        int[][] colors = {{0, 0, 0}, {255, 255, 255}, {255, 165, 0}, {128, 64, 32}};
        for(int[] rgb : colors){
            String args = rgb[0] + ", " + rgb[1] + ", " + rgb[2];
            String body = rgb[0] + ";" + rgb[1] + ";" + rgb[2];
            tests.add(dynamicTest("fgRgb(" + args + ") -> ESC[38;2;" + body + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.fgRgb(rgb[0], rgb[1], rgb[2]));
                assertEquals(sgr("38;2;" + body), c.result, "result");
                assertNoOutput(c);
            }));
            tests.add(dynamicTest("bgRgb(" + args + ") -> ESC[48;2;" + body + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.bgRgb(rgb[0], rgb[1], rgb[2]));
                assertEquals(sgr("48;2;" + body), c.result, "result");
                assertNoOutput(c);
            }));
        }
        tests.add(dynamicTest("fgRgb/bgRgb preserve channel order (r;g;b) for distinct channels", () -> {
            assertEquals(sgr("38;2;1;2;3"), Ansi.fgRgb(1, 2, 3), "fg order");
            assertEquals(sgr("48;2;3;2;1"), Ansi.bgRgb(3, 2, 1), "bg order");
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testRgbInvalid(){
        List<DynamicTest> tests = new ArrayList<>();
        // {r, g, b} inputs; each invalid channel must be replaced by the fallback independently.
        int[][] cases = {
            {-1, 10, 20},
            {10, 256, 20},
            {10, 20, -1},
            {-1, 256, 20},
            {10, Integer.MIN_VALUE, Integer.MAX_VALUE},
            {-1, 256, 1000},
            {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE},
            {FALLBACK_RGB, -5, FALLBACK_RGB}
        };
        String[] channels = {"red", "green", "blue"};
        for(int[] in : cases){
            int[] expected = new int[3];
            List<String> invalid = new ArrayList<>();
            for(int i = 0; i < 3; i++){
                boolean valid = (in[i] >= 0 && in[i] <= 255);
                expected[i] = (valid? in[i] : FALLBACK_RGB);
                if(!valid){
                    invalid.add(channels[i] + "=" + in[i]);
                }
            }
            String args = in[0] + ", " + in[1] + ", " + in[2];
            String body = expected[0] + ";" + expected[1] + ";" + expected[2];
            for(String method : new String[]{"fgRgb", "bgRgb"}){
                String prefix = (method.equals("fgRgb")? "38;2;" : "48;2;");
                tests.add(dynamicTest(method + "(" + args + ") -> " + body + " with " + invalid.size() + " warning(s)", () -> {
                    Captured<String> c = capture(() -> (method.equals("fgRgb")?
                        Ansi.fgRgb(in[0], in[1], in[2]) :
                        Ansi.bgRgb(in[0], in[1], in[2])
                    ));
                    assertEquals(sgr(prefix + body), c.result, "result");
                    List<String> lines = c.errLines();
                    assertEquals(invalid.size(), lines.size(), "warning count " + invalid);
                    // Warnings are emitted in channel order (r, g, b), one per invalid channel.
                    for(int i = 0; i < invalid.size(); i++){
                        String[] parts = invalid.get(i).split("=");
                        assertContains("warning " + (i + 1), lines.get(i), method, parts[0], parts[1], String.valueOf(FALLBACK_RGB));
                    }
                    assertEquals("", c.out, "stdout");
                }));
            }
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testValidationMethods(){
        List<DynamicTest> tests = new ArrayList<>();
        int[] inputs = {Integer.MIN_VALUE, -256, -1, 0, 1, 127, 254, 255, 256, 1000, Integer.MAX_VALUE};
        for(int v : inputs){
            boolean expected = (v >= 0 && v <= 255);
            tests.add(dynamicTest("isValid256(" + v + ") == " + expected + ", silent", () -> {
                Captured<Boolean> c = capture(() -> Ansi.isValid256(v));
                assertEquals(expected, c.result, "result");
                assertNoOutput(c);
            }));
            tests.add(dynamicTest("isValidRgbChannel(" + v + ") == " + expected + ", silent", () -> {
                Captured<Boolean> c = capture(() -> Ansi.isValidRgbChannel(v));
                assertEquals(expected, c.result, "result");
                assertNoOutput(c);
            }));
        }
        return tests;
    }
}
