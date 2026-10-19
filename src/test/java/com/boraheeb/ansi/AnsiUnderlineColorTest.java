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

/** Regression cases for underline styles and colors. **/
final class AnsiUnderlineColorTest{
    @TestFactory
    List<DynamicTest> testUnderline256(){
        List<DynamicTest> tests = new ArrayList<>();
        for(int code : new int[]{0, 1, 16, 196, 244, 255}){
            tests.add(dynamicTest("underline256(" + code + ") -> ESC[58:5:" + code + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.underline256(code));
                assertEquals(sgr("58:5:" + code), c.result, "result");
                assertNoOutput(c);
            }));
        }
        for(int code : new int[]{-1, 256, Integer.MIN_VALUE, Integer.MAX_VALUE}){
            tests.add(dynamicTest("underline256(" + code + ") falls back to " + FALLBACK_256 + " with one warning", () -> {
                Captured<String> c = capture(() -> Ansi.underline256(code));
                assertEquals(sgr("58:5:" + FALLBACK_256), c.result, "result");
                assertWarnings(c, 1, "underline256", String.valueOf(code), String.valueOf(FALLBACK_256));
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testUnderlineRgb(){
        List<DynamicTest> tests = new ArrayList<>();
        int[][] valid = {{0, 0, 0}, {255, 255, 255}, {255, 0, 0}};
        for(int[] rgb : valid){
            String args = rgb[0] + ", " + rgb[1] + ", " + rgb[2];
            String body = rgb[0] + ":" + rgb[1] + ":" + rgb[2];
            tests.add(dynamicTest("underlineRgb(" + args + ") -> ESC[58:2::" + body + "m, no warning", () -> {
                Captured<String> c = capture(() -> Ansi.underlineRgb(rgb[0], rgb[1], rgb[2]));
                assertEquals(sgr("58:2::" + body), c.result, "result");
                assertNoOutput(c);
            }));
        }
        tests.add(dynamicTest("underlineRgb preserves channel order (r:g:b) for distinct channels", () -> {
            assertEquals(sgr("58:2::1:2:3"), Ansi.underlineRgb(1, 2, 3), "1, 2, 3");
            assertEquals(sgr("58:2::3:2:1"), Ansi.underlineRgb(3, 2, 1), "3, 2, 1");
        }));
        // {r, g, b} inputs; each invalid channel must be replaced by the fallback independently.
        int[][] invalid = {
            {-1, 10, 20},
            {10, 256, 20},
            {10, 20, Integer.MAX_VALUE},
            {-1, 256, 20},
            {Integer.MIN_VALUE, 20, Integer.MAX_VALUE},
            {-1, 256, 1000},
            {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE},
            {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE}
        };
        String[] channels = {"red", "green", "blue"};
        for(int[] in : invalid){
            int[] expected = new int[3];
            List<String> bad = new ArrayList<>();
            for(int i = 0; i < 3; i++){
                boolean ok = (in[i] >= 0 && in[i] <= 255);
                expected[i] = (ok? in[i] : FALLBACK_RGB);
                if(!ok){
                    bad.add(channels[i] + "=" + in[i]);
                }
            }
            String args = in[0] + ", " + in[1] + ", " + in[2];
            String body = expected[0] + ":" + expected[1] + ":" + expected[2];
            tests.add(dynamicTest("underlineRgb(" + args + ") -> " + body + " with " + bad.size() + " warning(s)", () -> {
                Captured<String> c = capture(() -> Ansi.underlineRgb(in[0], in[1], in[2]));
                assertEquals(sgr("58:2::" + body), c.result, "result");
                List<String> lines = c.errLines();
                assertEquals(bad.size(), lines.size(), "warning count " + bad);
                // Warnings are emitted in channel order (r, g, b), one per invalid channel.
                for(int i = 0; i < bad.size(); i++){
                    String[] parts = bad.get(i).split("=");
                    assertContains("warning " + (i + 1), lines.get(i), "underlineRgb", parts[0], parts[1], String.valueOf(FALLBACK_RGB));
                }
                assertEquals("", c.out, "stdout");
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testUnderlineIntegration(){
        List<DynamicTest> tests = new ArrayList<>();
        String reset = sgr("0");
        tests.add(dynamicTest("Underline style + foreground + underline color compose in order", () -> {
            Captured<String> c = capture(() -> Ansi.wrap(Ansi.FG_CYAN + Ansi.UNDERLINE_CURLY + Ansi.underlineRgb(255, 0, 0), "BoRaheeb ANSI"));
            assertEquals(sgr("36") + sgr("4:3") + sgr("58:2::255:0:0") + "BoRaheeb ANSI" + reset, c.result, "result");
            assertNoOutput(c);
        }));
        tests.add(dynamicTest("Each underline style combines with underline256", () -> {
            assertEquals(sgr("4:4") + sgr("58:5:196") + "x" + reset, Ansi.wrap(Ansi.UNDERLINE_DOTTED + Ansi.underline256(196), "x"), "dotted");
            assertEquals(sgr("4:5") + sgr("58:5:46") + "x" + reset, Ansi.wrap(Ansi.UNDERLINE_DASHED + Ansi.underline256(46), "x"), "dashed");
            assertEquals(sgr("21") + sgr("58:5:33") + "x" + reset, Ansi.wrap(Ansi.DOUBLE_UNDERLINE + Ansi.underline256(33), "x"), "double");
        }));
        tests.add(dynamicTest("UNDERLINE_COLOR_DEFAULT introduces no other resets", () -> {
            String styled = Ansi.UNDERLINE + Ansi.underlineRgb(1, 2, 3) + "a" + Ansi.UNDERLINE_COLOR_DEFAULT + "b";
            assertEquals(sgr("4") + sgr("58:2::1:2:3") + "a" + sgr("59") + "b", styled, "result");
            for(String unwanted : new String[]{sgr("0"), CSI + "m", sgr("24"), sgr("39"), sgr("49")}){
                assertTrue(!styled.contains(unwanted), "no " + visible(unwanted) + " in " + visible(styled));
            }
        }));
        tests.add(checkStrip("removes underline styles, underline colors and SGR 59", sgr("4:3") + "c" + sgr("4:4") + "d" + sgr("4:5") + "e" + sgr("58:5:196") + "f" + sgr("58:2::255:0:0") + "g" + sgr("59") + sgr("24"), "cdefg"));
        tests.add(dynamicTest("stripSgr removes sequences generated by underline256/underlineRgb", () -> {
            Captured<String> c = capture(() -> Ansi.stripSgr(
                Ansi.UNDERLINE_CURLY + Ansi.underline256(0) + "a" + Ansi.underline256(255) + Ansi.underlineRgb(0, 0, 0) + "b" +
                Ansi.underlineRgb(255, 255, 255) + Ansi.UNDERLINE_COLOR_DEFAULT + Ansi.UNDERLINE_OFF
            ));
            assertEquals("ab", c.result, "result");
            assertNoOutput(c);
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testUnderlinePackedRgb(){
        List<DynamicTest> tests = new ArrayList<>();
        int[] packed = {
            0x000000, 0x000001, 0x010203, 0x020A6D, 0x123456, 0xFF0000,
            0x00FF00, 0x0000FF, 0xFFA500, 0xB4B4B4, 0xFFFFFE, 0xFFFFFF
        };
        String[] bodies = {
            "0:0:0", "0:0:1", "1:2:3", "2:10:109", "18:52:86", "255:0:0",
            "0:255:0", "0:0:255", "255:165:0", "180:180:180", "255:255:254", "255:255:255"
        };
        for(int i = 0; i < packed.length; i++){
            int rgb = packed[i];
            String body = bodies[i];
            String[] channels = body.split(":");
            tests.add(dynamicTest("underlineRgb(" + String.format("0x%06X", rgb) + ") agrees with channels, silently", () -> {
                Captured<String> c = capture(() -> Ansi.underlineRgb(rgb));
                assertEquals(sgr("58:2::" + body), c.result, "result");
                assertEquals(Ansi.underlineRgb(
                    Integer.parseInt(channels[0]), Integer.parseInt(channels[1]), Integer.parseInt(channels[2])
                ), c.result, "three-channel agreement");
                assertNoOutput(c);
            }));
        }
        for(int rgb : new int[]{-1, -2, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x01000000, 0x01000001, 0x80FF0000}){
            String shown = String.format("0x%X", rgb);
            tests.add(dynamicTest("underlineRgb(" + shown + ") uses packed fallback with exactly one warning", () -> {
                Captured<String> c = capture(() -> Ansi.underlineRgb(rgb));
                assertEquals(sgr("58:2::180:180:180"), c.result, "result");
                assertWarnings(c, 1, "Ansi.underlineRgb:", shown, "DEFAULT_RGB=(180, 180, 180)");
                assertEquals("Ansi.underlineRgb: packed RGB value (" + shown + ") is out of range (0x000000-0xFFFFFF)" +
                    " -> returning DEFAULT_RGB=(180, 180, 180)" + System.lineSeparator(), c.err, "exact diagnostic");
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testUnderlineHexValid(){
        List<DynamicTest> tests = new ArrayList<>();
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("#000", "0:0:0");
        cases.put("#FFF", "255:255:255");
        cases.put("#F00", "255:0:0");
        cases.put("#0F0", "0:255:0");
        cases.put("#00F", "0:0:255");
        cases.put("#123", "17:34:51");
        cases.put("#abc", "170:187:204");
        cases.put("#AbC", "170:187:204");
        cases.put("#F008", "255:0:0");
        cases.put("#0000", "0:0:0");
        cases.put("#FFFF", "255:255:255");
        cases.put("#aBcD", "170:187:204");
        cases.put("#000000", "0:0:0");
        cases.put("#FFFFFF", "255:255:255");
        cases.put("#FF0000", "255:0:0");
        cases.put("#FFA500", "255:165:0");
        cases.put("#020A6D", "2:10:109");
        cases.put("#abcdef", "171:205:239");
        cases.put("#ABCDEF", "171:205:239");
        cases.put("#aBcDeF", "171:205:239");
        cases.put("#FF000000", "255:0:0");
        cases.put("#FF000080", "255:0:0");
        cases.put("#FF0000FF", "255:0:0");
        cases.put("#020a6d40", "2:10:109");
        cases.put("#00000000", "0:0:0");
        cases.put("#FFFFFFFF", "255:255:255");
        for(Map.Entry<String, String> e : cases.entrySet()){
            tests.add(dynamicTest("underlineHex(" + e.getKey() + ") -> " + e.getValue() + ", silently", () -> {
                Captured<String> c = capture(() -> Ansi.underlineHex(e.getKey()));
                assertEquals(sgr("58:2::" + e.getValue()), c.result, "result");
                assertNoOutput(c);
            }));
        }
        tests.add(dynamicTest("underlineHex validates and ignores every alpha value", () -> {
            Captured<Void> c = capture(() -> {
                for(char alpha : "0123456789abcdefABCDEF".toCharArray()){
                    assertEquals(sgr("58:2::255:0:0"), Ansi.underlineHex("#F00" + alpha), "short alpha " + alpha);
                }
                for(int alpha = 0; alpha <= 255; alpha++){
                    assertEquals(sgr("58:2::2:10:109"), Ansi.underlineHex("#020A6D" + String.format("%02x", alpha)), "long alpha " + alpha);
                }
                return null;
            });
            assertNoOutput(c);
        }));
        tests.add(dynamicTest("underlineHex expands every short hex digit by duplication", () -> {
            Captured<Void> c = capture(() -> {
                for(char digit : "0123456789abcdefABCDEF".toCharArray()){
                    String d = String.valueOf(digit);
                    assertEquals(Ansi.underlineHex("#" + d + d + "11FF"), Ansi.underlineHex("#" + d + "1F"), "#" + d + "1F");
                    assertEquals(Ansi.underlineHex("#11" + d + d + "FF"), Ansi.underlineHex("#1" + d + "F"), "#1" + d + "F");
                    assertEquals(Ansi.underlineHex("#11FF" + d + d), Ansi.underlineHex("#1F" + d), "#1F" + d);
                }
                return null;
            });
            assertNoOutput(c);
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testUnderlineHexInvalid(){
        List<DynamicTest> tests = new ArrayList<>();
        Map<String, String> cases = new LinkedHashMap<>();
        for(String hex : new String[]{
            null, "", " ", "FF0000", "0xFF0000", "#", "#1", "#12", "#12345", "#1234567",
            "#123456789", "#FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "#GG0000", "#FF00G0", "#F0G",
            "#F00G", "#FF0000G0", "#FF00000G", " #FF0000", "#FF0000 ", "#FF 000", "#FF-000",
            "#FF.000", "##FF000", "#+F0000", "#ＦＦ００００"
        }){
            cases.put(hex, hex);
        }
        cases.put("\t", "\\u0009");
        cases.put("\t#F00", "\\u0009#F00");
        cases.put("#F00\n", "#F00\\u000A");
        cases.put("#F00\r\n", "#F00\\u000D\\u000A");
        cases.put("#F00\b", "#F00\\u0008");
        cases.put("#F00\f", "#F00\\u000C");
        cases.put("#FF000\u0000", "#FF000\\u0000");
        cases.put("#F00\u007F", "#F00\\u007F");
        cases.put("#F00\u0085", "#F00\\u0085");
        cases.put("#F0\u009B31m", "#F0\\u009B31m");
        cases.put("#F0\u001B[31m", "#F0\\u001B[31m");
        cases.put("#F00\u2028", "#F00\\u2028");
        cases.put("#F00\u2029", "#F00\\u2029");
        for(Map.Entry<String, String> e : cases.entrySet()){
            String shown = ((e.getKey() == null)? "null" : "\"" + e.getValue() + "\"");
            tests.add(dynamicTest("underlineHex(" + shown + ") falls back with one safely escaped warning", () -> {
                Captured<String> c = capture(() -> Ansi.underlineHex(e.getKey()));
                assertEquals(sgr("58:2::180:180:180"), c.result, "result");
                assertWarnings(c, 1, "Ansi.underlineHex:", shown, "DEFAULT_RGB=(180, 180, 180)");
                assertEquals("Ansi.underlineHex: hex value (" + shown + ") is not a valid #RGB, #RGBA, #RRGGBB or #RRGGBBAA color" +
                    " -> returning DEFAULT_RGB=(180, 180, 180)" + System.lineSeparator(), c.err, "exact diagnostic");
                for(char ch : c.errLines().get(0).toCharArray()){
                    assertTrue(!(ch < 0x20 || (ch >= 0x7F && ch <= 0x9F) || ch == 0x2028 || ch == 0x2029), "no raw control character");
                }
            }));
        }
        return tests;
    }
    @TestFactory
    List<DynamicTest> testUnderlineHexPackedIntegration(){
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamicTest("Underline hex, packed and channel forms agree across 256 sample colors", () -> {
            Captured<Void> c = capture(() -> {
                for(int r = 0; r <= 255; r++){
                    int g = 255 - r;
                    int b = (r * 37) & 0xFF;
                    String expected = sgr("58:2::" + r + ":" + g + ":" + b);
                    int packed = (r << 16) | (g << 8) | b;
                    assertEquals(expected, Ansi.underlineRgb(r, g, b), "channels");
                    assertEquals(expected, Ansi.underlineRgb(packed), "packed");
                    assertEquals(expected, Ansi.underlineHex(String.format("#%06X", packed)), "uppercase hex");
                    assertEquals(expected, Ansi.underlineHex(String.format("#%06x", packed)), "lowercase hex");
                }
                return null;
            });
            assertNoOutput(c);
        }));
        String[] styles = {Ansi.UNDERLINE, Ansi.UNDERLINE_CURLY, Ansi.UNDERLINE_DOTTED, Ansi.UNDERLINE_DASHED};
        String[] parameters = {"4", "4:3", "4:4", "4:5"};
        for(int i = 0; i < styles.length; i++){
            String style = styles[i];
            String parameter = parameters[i];
            for(String method : new String[]{"underlineRgb", "underlineHex"}){
                tests.add(dynamicTest(method + " composes with underline " + parameter + ", reset, wrap and SGR utilities", () -> {
                    Captured<Void> c = capture(() -> {
                        String color = (method.equals("underlineRgb")? Ansi.underlineRgb(0xFFA500) : Ansi.underlineHex("#FFA500"));
                        String styled = style + color;
                        String body = "orange" + Ansi.UNDERLINE_COLOR_DEFAULT + "default";
                        String expected = sgr(parameter) + sgr("58:2::255:165:0") + "orange" + sgr("59") + "default" + sgr("0");
                        String wrapped = Ansi.wrap(styled, body);
                        assertEquals(expected, wrapped, "wrap String");
                        assertEquals(expected, Ansi.wrap(styled, (Object) new StringBuilder(body)), "wrap Object");
                        assertTrue(Ansi.containsSgr(wrapped), "containsSgr String");
                        assertTrue(Ansi.containsSgr((Object) new StringBuilder(wrapped)), "containsSgr Object");
                        assertEquals("orangedefault", Ansi.stripSgr(wrapped), "stripSgr String");
                        assertEquals("orangedefault", Ansi.stripSgr((Object) new StringBuilder(wrapped)), "stripSgr Object");
                        assertTrue(!Ansi.containsSgr(Ansi.stripSgr(wrapped)), "stripped text has no SGR");
                        return null;
                    });
                    assertNoOutput(c);
                }));
            }
        }
        return tests;
    }
}
