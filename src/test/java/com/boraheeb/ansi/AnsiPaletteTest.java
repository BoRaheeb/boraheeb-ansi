package com.boraheeb.ansi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Regression cases for palette conversion and round trips. **/
final class AnsiPaletteTest{
    @TestFactory
    List<DynamicTest> testColor256ToRgb(){
        List<DynamicTest> tests = new ArrayList<>();
        // Independent reference values, written out from the documented palette.
        int[] system = {
            0x000000, 0x800000, 0x008000, 0x808000, 0x000080, 0x800080, 0x008080, 0xC0C0C0,
            0x808080, 0xFF0000, 0x00FF00, 0xFFFF00, 0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF
        };
        for(int i = 0; i < system.length; i++){
            tests.add(checkColor256ToRgb(i, system[i]));
        }
        tests.add(checkColor256ToRgb(16, 0x000000));
        tests.add(checkColor256ToRgb(17, 0x00005F));
        tests.add(checkColor256ToRgb(21, 0x0000FF));
        tests.add(checkColor256ToRgb(52, 0x5F0000));
        tests.add(checkColor256ToRgb(59, 0x5F5F5F));
        tests.add(checkColor256ToRgb(102, 0x878787));
        tests.add(checkColor256ToRgb(196, 0xFF0000));
        tests.add(checkColor256ToRgb(208, 0xFF8700));
        tests.add(checkColor256ToRgb(231, 0xFFFFFF));
        tests.add(checkColor256ToRgb(232, 0x080808));
        tests.add(checkColor256ToRgb(233, 0x121212));
        tests.add(checkColor256ToRgb(244, 0x808080));
        tests.add(checkColor256ToRgb(254, 0xE4E4E4));
        tests.add(checkColor256ToRgb(255, 0xEEEEEE));
        tests.add(dynamicTest("color256ToRgb: all 256 indices are packed RGB, deterministic and silent", () -> {
            Captured<int[]> c = capture(() -> {
                int[] values = new int[256];
                for(int i = 0; i <= 255; i++){
                    values[i] = Ansi.color256ToRgb(i);
                }
                return values;
            });
            assertNoOutput(c);
            for(int i = 0; i <= 255; i++){
                assertTrue(c.result[i] >= 0 && c.result[i] <= 0xFFFFFF, "index " + i + " in 0x000000..0xFFFFFF");
                assertEquals(c.result[i], Ansi.color256ToRgb(i), "index " + i + " repeatable");
            }
        }));
        tests.add(dynamicTest("color256ToRgb: indices 16..255 are 240 distinct colors", () -> {
            Set<Integer> seen = new TreeSet<>();
            for(int i = 16; i <= 255; i++){
                seen.add(Ansi.color256ToRgb(i));
            }
            assertEquals(240, seen.size(), "distinct");
        }));
        for(int index : new int[]{-1, 256, Integer.MIN_VALUE, Integer.MAX_VALUE}){
            tests.add(dynamicTest("color256ToRgb(" + index + ") falls back to 244 (0x808080) with one warning", () -> {
                Captured<Integer> c = capture(() -> Ansi.color256ToRgb(index));
                assertEquals(0x808080, c.result, "result");
                assertWarnings(c, 1, "color256ToRgb", String.valueOf(index), String.valueOf(FALLBACK_256));
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testRgbTo256(){
        List<DynamicTest> tests = new ArrayList<>();
        // Independently computed nearest indices (see the per-case notes).
        tests.add(checkRgbTo256("black", 0, 0, 0, 16));
        tests.add(checkRgbTo256("white", 255, 255, 255, 231));
        tests.add(checkRgbTo256("red", 255, 0, 0, 196));
        tests.add(checkRgbTo256("green", 0, 255, 0, 46));
        tests.add(checkRgbTo256("blue", 0, 0, 255, 21));
        tests.add(checkRgbTo256("yellow", 255, 255, 0, 226));
        tests.add(checkRgbTo256("cyan", 0, 255, 255, 51));
        tests.add(checkRgbTo256("magenta", 255, 0, 255, 201));
        tests.add(checkRgbTo256("gray 128 (exact gray 244)", 128, 128, 128, 244));
        tests.add(checkRgbTo256("gray 8 (exact gray 232)", 8, 8, 8, 232));
        tests.add(checkRgbTo256("gray 238 (exact gray 255)", 238, 238, 238, 255));
        tests.add(checkRgbTo256("gray 100 (nearest gray 98 at 241)", 100, 100, 100, 241));
        tests.add(checkRgbTo256("gray 95 (exact cube 59)", 95, 95, 95, 59));
        tests.add(checkRgbTo256("exact cube orange 208", 255, 135, 0, 208));
        tests.add(checkRgbTo256("exact cube 102", 135, 135, 135, 102));
        tests.add(checkRgbTo256("arbitrary (200, 100, 50) -> cube 215/95/95 at 167", 200, 100, 50, 167));
        tests.add(checkRgbTo256("arbitrary (128, 0, 0) -> cube 135/0/0 at 88", 128, 0, 0, 88));
        tests.add(checkRgbTo256("arbitrary (255, 165, 0) -> cube 255/175/0 at 214", 255, 165, 0, 214));
        // Ties: equal distances must resolve to the smallest index.
        tests.add(checkRgbTo256("tie (4, 4, 4): 16 vs 232", 4, 4, 4, 16));
        tests.add(checkRgbTo256("tie (115, 0, 0): 52 vs 88", 115, 0, 0, 52));
        tests.add(checkRgbTo256("tie (0, 0, 115): 17 vs 18", 0, 0, 115, 17));
        tests.add(dynamicTest("rgbTo256 result is in 16..255 and nearest by squared distance, smallest index on ties", () -> {
            int[] palette = new int[256];
            for(int i = 16; i <= 255; i++){
                palette[i] = Ansi.color256ToRgb(i);
            }
            for(int r = 0; r <= 255; r += 15){
                for(int g = 0; g <= 255; g += 15){
                    for(int b = 0; b <= 255; b += 15){
                        int result = Ansi.rgbTo256(r, g, b);
                        assertTrue(result >= 16 && result <= 255, "range for " + r + "," + g + "," + b);
                        int best = distance(r, g, b, palette[result]);
                        for(int i = 16; i <= 255; i++){
                            int d = distance(r, g, b, palette[i]);
                            if(d < best || (d == best && i < result)){
                                fail("nearest for (" + r + ", " + g + ", " + b + ")" + ": expected <" + "index " + i + " (distance " + d + ")" + "> but was <" + "index " + result + " (distance " + best + ")" + ">");
                            }
                        }
                    }
                }
            }
        }));
        // Invalid channels: each one independently replaced by 180, one warning per channel.
        int[][] invalid = {
            {-1, 10, 300},
            {-1, 0, 0},
            {0, 256, 0},
            {0, 0, Integer.MAX_VALUE},
            {Integer.MIN_VALUE, Integer.MAX_VALUE, 10},
            {-5, 999, Integer.MIN_VALUE}
        };
        String[] channels = {"red", "green", "blue"};
        for(int[] in : invalid){
            int[] substituted = new int[3];
            List<String> bad = new ArrayList<>();
            for(int i = 0; i < 3; i++){
                boolean ok = (in[i] >= 0 && in[i] <= 255);
                substituted[i] = (ok? in[i] : FALLBACK_RGB);
                if(!ok){
                    bad.add(channels[i] + "=" + in[i]);
                }
            }
            String args = in[0] + ", " + in[1] + ", " + in[2];
            tests.add(dynamicTest("rgbTo256(" + args + ") behaves as rgbTo256(" + substituted[0] + ", " + substituted[1] + ", " + substituted[2] + ") with " + bad.size() + " warning(s)", () -> {
                int expected = Ansi.rgbTo256(substituted[0], substituted[1], substituted[2]);
                Captured<Integer> c = capture(() -> Ansi.rgbTo256(in[0], in[1], in[2]));
                assertEquals(expected, c.result, "result");
                List<String> lines = c.errLines();
                assertEquals(bad.size(), lines.size(), "warning count " + bad);
                for(int i = 0; i < bad.size(); i++){
                    String[] parts = bad.get(i).split("=");
                    assertContains("warning " + (i + 1), lines.get(i), "rgbTo256", parts[0], parts[1], String.valueOf(FALLBACK_RGB));
                }
                assertEquals("", c.out, "stdout");
            }));
        }
        tests.add(dynamicTest("rgbTo256(-1, 10, 300) == 127 (180, 10, 180 -> cube 175/0/175)", () -> {
            Captured<Integer> c = capture(() -> Ansi.rgbTo256(-1, 10, 300));
            assertEquals(127, c.result, "result");
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testColorRoundTrip(){
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamicTest("Round trip: rgbTo256(color256ToRgb(i)) == i for every index 16..255", () -> {
            Captured<Void> c = capture(() -> {
                for(int index = 16; index <= 255; index++){
                    int rgb = Ansi.color256ToRgb(index);
                    int back = Ansi.rgbTo256((rgb >>> 16) & 0xFF, (rgb >>> 8) & 0xFF, rgb & 0xFF);
                    if(back != index){
                        fail("round trip" + ": expected <" + String.valueOf(index) + "> but was <" + String.valueOf(back) + ">");
                    }
                }
                return null;
            });
            assertNoOutput(c);
        }));
        tests.add(dynamicTest("System colors 0..15 map to cube/gray indices (16..255), not back to themselves", () -> {
            for(int index = 0; index <= 15; index++){
                int rgb = Ansi.color256ToRgb(index);
                int back = Ansi.rgbTo256((rgb >>> 16) & 0xFF, (rgb >>> 8) & 0xFF, rgb & 0xFF);
                assertTrue(back >= 16 && back <= 255, "index " + index + " -> " + back);
            }
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testConversionIntegration(){
        List<DynamicTest> tests = new ArrayList<>();
        String reset = sgr("0");
        tests.add(dynamicTest("fg256(rgbTo256(255, 0, 0)) == ESC[38;5;196m", () ->
            assertEquals(sgr("38;5;196"), Ansi.fg256(Ansi.rgbTo256(255, 0, 0)), "result")));
        tests.add(dynamicTest("fgRgb(color256ToRgb(196)) == ESC[38;2;255;0;0m", () ->
            assertEquals(sgr("38;2;255;0;0"), Ansi.fgRgb(Ansi.color256ToRgb(196)), "result")));
        tests.add(dynamicTest("Converted colors work with wrap, stripSgr and containsSgr", () -> {
            Captured<String> c = capture(() -> Ansi.wrap(Ansi.bg256(Ansi.rgbTo256(0, 0, 255)) + Ansi.fgRgb(Ansi.color256ToRgb(244)), "x"));
            assertEquals(sgr("48;5;21") + sgr("38;2;128;128;128") + "x" + reset, c.result, "wrap");
            assertNoOutput(c);
            assertTrue(Ansi.containsSgr(c.result), "containsSgr");
            assertEquals("x", Ansi.stripSgr(c.result), "stripSgr");
            assertTrue(!Ansi.containsSgr(Ansi.stripSgr(c.result)), "stripped has no SGR");
        }));
        return tests;
    }
    private static DynamicTest checkColor256ToRgb(int index, int expected){
        return dynamicTest("color256ToRgb(" + index + ") == " + String.format("0x%06X", expected) + ", no warning", () -> {
            Captured<Integer> c = capture(() -> Ansi.color256ToRgb(index));
            assertEquals(String.format("0x%06X", expected), String.format("0x%06X", c.result), "result");
            assertNoOutput(c);
        });
    }
    private static DynamicTest checkRgbTo256(String name, int r, int g, int b, int expected){
        return dynamicTest("rgbTo256: " + name + " -> " + expected + ", no warning", () -> {
            Captured<Integer> c = capture(() -> Ansi.rgbTo256(r, g, b));
            assertEquals(expected, c.result, "result");
            assertNoOutput(c);
        });
    }
    private static int distance(int r, int g, int b, int rgb){
        int dr = r - ((rgb >> 16) & 0xFF);
        int dg = g - ((rgb >> 8) & 0xFF);
        int db = b - (rgb & 0xFF);
        return (dr * dr + dg * dg + db * db);
    }
}
