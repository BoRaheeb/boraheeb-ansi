package com.boraheeb.ansi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Regression cases for HEX and packed RGB colors. **/
final class AnsiHexColorTest{
    @TestFactory
    List<DynamicTest> testHexValid(){
        List<DynamicTest> tests = new ArrayList<>();
        // Input -> expected "r;g;b", written out independently of the parser.
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("#000", "0;0;0");
        cases.put("#FFF", "255;255;255");
        cases.put("#F00", "255;0;0");
        cases.put("#0F0", "0;255;0");
        cases.put("#00F", "0;0;255");
        cases.put("#123", "17;34;51");
        cases.put("#F008", "255;0;0");
        cases.put("#0000", "0;0;0");
        cases.put("#FFFF", "255;255;255");
        cases.put("#FF0000", "255;0;0");
        cases.put("#020A6D", "2;10;109");
        cases.put("#abcdef", "171;205;239");
        cases.put("#ABCDEF", "171;205;239");
        cases.put("#aBcDeF", "171;205;239");
        cases.put("#FF000000", "255;0;0");
        cases.put("#FF000080", "255;0;0");
        cases.put("#FF0000FF", "255;0;0");
        cases.put("#020A6D40", "2;10;109");
        for(Map.Entry<String, String> e : cases.entrySet()){
            String hex = e.getKey();
            String body = e.getValue();
            tests.add(dynamicTest("fgHex(\"" + hex + "\") -> ESC[38;2;" + body + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.fgHex(hex));
                assertEquals(sgr("38;2;" + body), c.result, "result");
                assertNoOutput(c);
            }));
            tests.add(dynamicTest("bgHex(\"" + hex + "\") -> ESC[48;2;" + body + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.bgHex(hex));
                assertEquals(sgr("48;2;" + body), c.result, "result");
                assertNoOutput(c);
            }));
        }
        tests.add(dynamicTest("Changing only alpha does not change the color", () -> {
            for(String alpha : new String[]{"0", "1", "8", "a", "F"}){
                assertEquals(Ansi.fgHex("#F00"), Ansi.fgHex("#F00" + alpha), "#F00" + alpha);
                assertEquals(Ansi.bgHex("#F00"), Ansi.bgHex("#F00" + alpha), "#F00" + alpha + " bg");
            }
            for(String alpha : new String[]{"00", "01", "80", "Ff", "ff"}){
                assertEquals(Ansi.fgHex("#FF0000"), Ansi.fgHex("#FF0000" + alpha), "#FF0000" + alpha);
                assertEquals(Ansi.bgHex("#FF0000"), Ansi.bgHex("#FF0000" + alpha), "#FF0000" + alpha + " bg");
            }
        }));
        tests.add(dynamicTest("Fully transparent alpha still yields the opaque color, no HIDDEN/DIM", () -> {
            String fg = Ansi.fgHex("#FF000000");
            assertEquals(sgr("38;2;255;0;0"), fg, "result");
            assertTrue(!fg.contains(sgr("8")), "no HIDDEN");
            assertTrue(!fg.contains(sgr("2")), "no DIM");
        }));
        tests.add(dynamicTest("Every short digit expands by duplication (#XYZ == #XXYYZZ)", () -> {
            String digits = "0123456789abcdefABCDEF";
            for(char d : digits.toCharArray()){
                String s = "" + d;
                assertEquals(Ansi.fgHex("#" + s + s + s + s + s + s), Ansi.fgHex("#" + s + s + s), "#" + s + s + s);
            }
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testHexInvalid(){
        List<DynamicTest> tests = new ArrayList<>();
        String[] inputs = {
            null, "", " ", "\t", "FF0000", "0xFF0000", "#", "#1", "#12", "#12345", "#1234567",
            "#GG0000", "#FF00G0", "#F0G", "#FF0000GG", "#FF00008G", "#F00G",
            " #FF0000", "#FF0000 ", "\t#F00", "#F00\n", "#FF 000", "#FF-000", "#FF.000",
            "##FF000", "#+F0000", "#FF000\u0000", "#ＦＦ００００",
            "#FF0000FF0", "#FF0000FF00", "#FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "#F00\u0085", "#F0\u009B31m", "#F00\u2028", "#F00\u2029"
        };
        // Inputs with control characters must be identified in escaped form, keeping the warning on one line.
        Map<String, String> escaped = new LinkedHashMap<>();
        escaped.put("\t", "\\u0009");
        escaped.put("\t#F00", "\\u0009#F00");
        escaped.put("#F00\n", "#F00\\u000A");
        escaped.put("#FF000\u0000", "#FF000\\u0000");
        escaped.put("#F00\u0085", "#F00\\u0085");
        escaped.put("#F0\u009B31m", "#F0\\u009B31m");
        escaped.put("#F00\u2028", "#F00\\u2028");
        escaped.put("#F00\u2029", "#F00\\u2029");
        for(String hex : inputs){
            String identified = ((hex != null)? escaped.getOrDefault(hex, hex) : "null");
            String shown = ((hex != null)? "\"" + identified + "\"" : "null");
            for(String method : new String[]{"fgHex", "bgHex"}){
                String prefix = (method.equals("fgHex")? "38;2;" : "48;2;");
                tests.add(dynamicTest(method + "(" + shown + ") falls back to 180;180;180 with one warning", () -> {
                    Captured<String> c = capture(() -> (method.equals("fgHex")? Ansi.fgHex(hex) : Ansi.bgHex(hex)));
                    assertEquals(sgr(prefix + "180;180;180"), c.result, "result");
                    assertWarnings(c, 1, method, identified, String.valueOf(FALLBACK_RGB));
                    assertTrue(!c.err.contains("channel"), "no channel warning: " + visible(c.err));
                    String line = c.errLines().get(0);
                    for(char ch : line.toCharArray()){
                        boolean control = (ch < 0x20 || (ch >= 0x7F && ch <= 0x9F) || ch == 0x2028 || ch == 0x2029);
                        assertTrue(!control, "no raw control character in warning: " + visible(line));
                    }
                }));
            }
        }
        tests.add(dynamicTest("Warning for an input containing ESC does not carry the escape sequence", () -> {
            Captured<String> c = capture(() -> Ansi.fgHex("#F0" + "\u001B[31m"));
            assertEquals(sgr("38;2;180;180;180"), c.result, "result");
            assertWarnings(c, 1, "fgHex", "#F0\\u001B[31m");
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testPackedRgbValid(){
        List<DynamicTest> tests = new ArrayList<>();
        // Packed input -> expected "r;g;b", written out independently of bit extraction.
        int[] packed = {0x000000, 0x000001, 0x010203, 0x020A6D, 0x123456, 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF};
        String[] bodies = {"0;0;0", "0;0;1", "1;2;3", "2;10;109", "18;52;86", "255;0;0", "0;255;0", "0;0;255", "255;255;255"};
        for(int i = 0; i < packed.length; i++){
            int rgb = packed[i];
            String body = bodies[i];
            String[] channels = body.split(";");
            int r = Integer.parseInt(channels[0]);
            int g = Integer.parseInt(channels[1]);
            int b = Integer.parseInt(channels[2]);
            String shown = String.format("0x%06X", rgb);
            tests.add(dynamicTest("fgRgb(" + shown + ") -> ESC[38;2;" + body + "m, matches fgRgb(r, g, b), no warning", () -> {
                Captured<String> c = capture(() -> Ansi.fgRgb(rgb));
                assertEquals(sgr("38;2;" + body), c.result, "result");
                assertEquals(Ansi.fgRgb(r, g, b), c.result, "three-channel agreement");
                assertNoOutput(c);
            }));
            tests.add(dynamicTest("bgRgb(" + shown + ") -> ESC[48;2;" + body + "m, matches bgRgb(r, g, b), no warning", () -> {
                Captured<String> c = capture(() -> Ansi.bgRgb(rgb));
                assertEquals(sgr("48;2;" + body), c.result, "result");
                assertEquals(Ansi.bgRgb(r, g, b), c.result, "three-channel agreement");
                assertNoOutput(c);
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testPackedRgbInvalid(){
        List<DynamicTest> tests = new ArrayList<>();
        int[] inputs = {-1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x01000000, 0x80FF0000, 0xFFFFFFFF};
        for(int rgb : inputs){
            String shown = String.format("0x%X", rgb);
            for(String method : new String[]{"fgRgb", "bgRgb"}){
                String prefix = (method.equals("fgRgb")? "38;2;" : "48;2;");
                tests.add(dynamicTest(method + "(" + shown + ") falls back to 180;180;180 with one warning", () -> {
                    Captured<String> c = capture(() -> (method.equals("fgRgb")? Ansi.fgRgb(rgb) : Ansi.bgRgb(rgb)));
                    assertEquals(sgr(prefix + "180;180;180"), c.result, "result");
                    assertWarnings(c, 1, method, shown, String.valueOf(FALLBACK_RGB));
                    assertTrue(!c.err.contains("channel"), "no channel warning: " + visible(c.err));
                }));
            }
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testHexPackedIntegration(){
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamicTest("fgHex/fgRgb(packed)/fgRgb(r, g, b) agree; bg variants agree", () -> {
            assertEquals(Ansi.fgRgb(255, 0, 0), Ansi.fgHex("#FF0000"), "fgHex #FF0000");
            assertEquals(Ansi.fgRgb(0xFF0000), Ansi.fgHex("#F00"), "fgHex #F00 vs packed");
            assertEquals(Ansi.bgRgb(255, 0, 0), Ansi.bgHex("#FF0000"), "bgHex #FF0000");
            assertEquals(Ansi.bgRgb(0xFF0000), Ansi.bgHex("#F00"), "bgHex #F00 vs packed");
        }));
        tests.add(dynamicTest("Hex, packed and three-channel forms agree across 256 sample colors", () -> {
            for(int i = 0; i <= 255; i++){
                int r = i;
                int g = 255 - i;
                int b = (i * 37) & 0xFF;
                int packed = ((r << 16) | (g << 8) | b);
                String hexUpper = String.format("#%02X%02X%02X", r, g, b);
                String fg = Ansi.fgRgb(r, g, b);
                String bg = Ansi.bgRgb(r, g, b);
                assertEquals(fg, Ansi.fgRgb(packed), "fgRgb(packed) " + hexUpper);
                assertEquals(bg, Ansi.bgRgb(packed), "bgRgb(packed) " + hexUpper);
                assertEquals(fg, Ansi.fgHex(hexUpper), "fgHex " + hexUpper);
                assertEquals(bg, Ansi.bgHex(hexUpper.toLowerCase()), "bgHex lower " + hexUpper);
            }
        }));
        tests.add(dynamicTest("wrap works with fgHex/bgHex/fgRgb(packed)", () -> {
            Captured<String> c = capture(() -> Ansi.wrap(Ansi.fgHex("#F00") + Ansi.bgRgb(0x020A6D), "x"));
            assertEquals(sgr("38;2;255;0;0") + sgr("48;2;2;10;109") + "x" + sgr("0"), c.result, "result");
            assertNoOutput(c);
        }));
        tests.add(dynamicTest("stripSgr removes sequences from the new methods (both overloads agree)", () -> {
            String styled = Ansi.fgHex("#abcdef") + "a" + Ansi.bgHex("#020A6D40") + "b" + Ansi.fgRgb(0x123456) + Ansi.bgRgb(0xFFFFFF) + "c";
            assertEquals("abc", Ansi.stripSgr(styled), "String");
            assertEquals("abc", Ansi.stripSgr((Object) styled), "Object");
        }));
        return tests;
    }
}
