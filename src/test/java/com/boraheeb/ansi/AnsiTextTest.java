package com.boraheeb.ansi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Regression cases for wrapping, SGR stripping and detection. **/
final class AnsiTextTest{
    @TestFactory
    List<DynamicTest> testWrapString(){
        List<DynamicTest> tests = new ArrayList<>();
        String reset = sgr("0");
        tests.add(dynamicTest("wrap(String, String) normal text", () ->
            assertEquals(sgr("31") + "Error!" + reset, Ansi.wrap(Ansi.FG_RED, "Error!"), "result")));
        tests.add(dynamicTest("wrap(String, String) empty text", () ->
            assertEquals(sgr("1") + reset, Ansi.wrap(Ansi.BOLD, ""), "result")));
        tests.add(dynamicTest("wrap(String, String) empty style", () ->
            assertEquals("x" + reset, Ansi.wrap("", "x"), "result")));
        tests.add(dynamicTest("wrap(String, String) null style -> no style prefix", () ->
            assertEquals("plain" + reset, Ansi.wrap(null, "plain"), "result")));
        tests.add(dynamicTest("wrap(String, String) null text -> empty text", () ->
            assertEquals(sgr("1") + reset, Ansi.wrap(Ansi.BOLD, (String) null), "result")));
        tests.add(dynamicTest("wrap(String, String) null style and null text -> RESET only", () ->
            assertEquals(reset, Ansi.wrap((String) null, (String) null), "result")));
        tests.add(dynamicTest("wrap(String, String) combined styles kept in order", () ->
            assertEquals(sgr("31") + sgr("1") + sgr("4") + "x" + reset, Ansi.wrap(Ansi.FG_RED + Ansi.BOLD + Ansi.UNDERLINE, "x"), "result")));
        tests.add(dynamicTest("wrap(String, String) generated 256/RGB styles", () -> {
            assertEquals(sgr("38;5;208") + "o" + reset, Ansi.wrap(Ansi.fg256(208), "o"), "fg256");
            assertEquals(sgr("38;2;255;165;0") + "o" + reset, Ansi.wrap(Ansi.fgRgb(255, 165, 0), "o"), "fgRgb");
        }));
        tests.add(dynamicTest("wrap(String, String) Unicode text unchanged", () -> {
            String text = "مرحبا • héllo • 日本語 • 😀";
            assertEquals(sgr("32") + text + reset, Ansi.wrap(Ansi.FG_GREEN, text), "result");
        }));
        tests.add(dynamicTest("wrap(String, String) newline text unchanged, single trailing RESET", () -> {
            String text = "line1\nline2\r\nline3\n";
            assertEquals(sgr("33") + text + reset, Ansi.wrap(Ansi.FG_YELLOW, text), "result");
        }));
        tests.add(dynamicTest("wrap(String, String) unconditional RESET even without style", () ->
            assertTrue(Ansi.wrap(null, "").endsWith(Ansi.RESET), "ends with RESET")));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testWrapObject(){
        List<DynamicTest> tests = new ArrayList<>();
        String reset = sgr("0");
        tests.add(dynamicTest("wrap(String, Object) Integer", () ->
            assertEquals(sgr("34") + "42" + reset, Ansi.wrap(Ansi.FG_BLUE, Integer.valueOf(42)), "result")));
        tests.add(dynamicTest("wrap(String, Object) negative Integer", () ->
            assertEquals(sgr("34") + "-7" + reset, Ansi.wrap(Ansi.FG_BLUE, Integer.valueOf(-7)), "result")));
        tests.add(dynamicTest("wrap(String, Object) Boolean", () -> {
            assertEquals(sgr("1") + "true" + reset, Ansi.wrap(Ansi.BOLD, Boolean.TRUE), "true");
            assertEquals(sgr("1") + "false" + reset, Ansi.wrap(Ansi.BOLD, Boolean.FALSE), "false");
        }));
        tests.add(dynamicTest("wrap(String, Object) autoboxed primitives", () -> {
            assertEquals(sgr("1") + "1.5" + reset, Ansi.wrap(Ansi.BOLD, 1.5), "double");
            assertEquals(sgr("1") + "c" + reset, Ansi.wrap(Ansi.BOLD, 'c'), "char");
        }));
        tests.add(dynamicTest("wrap(String, Object) null object -> empty text", () ->
            assertEquals(sgr("1") + reset, Ansi.wrap(Ansi.BOLD, (Object) null), "result")));
        tests.add(dynamicTest("wrap(String, Object) null style and null object -> RESET only", () ->
            assertEquals(reset, Ansi.wrap((String) null, (Object) null), "result")));
        tests.add(dynamicTest("wrap(String, Object) String typed as Object matches String overload", () -> {
            Object text = "same";
            assertEquals(Ansi.wrap(Ansi.FG_CYAN, "same"), Ansi.wrap(Ansi.FG_CYAN, text), "result");
        }));
        tests.add(dynamicTest("wrap(String, Object) uses toString()", () -> {
            Object value = new Object(){
                @Override
                public String toString(){
                    return "custom";
                }
            };
            assertEquals(sgr("36") + "custom" + reset, Ansi.wrap(Ansi.FG_CYAN, value), "result");
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testStripSgr(){
        List<DynamicTest> tests = new ArrayList<>();
        String esc = "\u001B";
        String reset = sgr("0");

        // Basic input.
        tests.add(checkStrip("plain text unchanged", "Hello, World!", "Hello, World!"));
        tests.add(checkStrip("empty string -> empty string", "", ""));
        tests.add(dynamicTest("null -> empty string, silent", () -> {
            Captured<String> c = capture(() -> Ansi.stripSgr((String) null));
            assertEquals("", c.result, "result");
            assertNoOutput(c);
        }));

        // Colors.
        tests.add(checkStrip("standard foreground", sgr("31") + "red" + reset, "red"));
        tests.add(checkStrip("standard background", sgr("44") + "blue bg" + reset, "blue bg"));
        tests.add(checkStrip("bright foreground and background", sgr("91") + "a" + sgr("101") + "b" + reset, "ab"));
        tests.add(checkStrip("256-color foreground and background", sgr("38;5;208") + "x" + sgr("48;5;17") + "y" + reset, "xy"));
        tests.add(checkStrip("RGB foreground and background", sgr("38;2;255;165;0") + "x" + sgr("48;2;0;0;0") + "y" + reset, "xy"));

        // Attributes and resets.
        tests.add(checkStrip("text attributes 1-9", sgr("1") + "b" + sgr("2") + "d" + sgr("3") + "i" + sgr("4") + "u" + sgr("5") + "s" +
            sgr("6") + "r" + sgr("7") + "v" + sgr("8") + "h" + sgr("9") + "t", "bdiusrvht"));
        tests.add(checkStrip("selective resets 22-29, 39, 49", "a" + sgr("22") + sgr("23") + sgr("24") + sgr("25") + sgr("27") + sgr("28") + sgr("29") + sgr("39") + sgr("49") + "b", "ab"));
        tests.add(checkStrip("double underline 21, overline 53, overline off 55", sgr("21") + "d" + sgr("24") + sgr("53") + "o" + sgr("55"), "do"));
        tests.add(checkStrip("empty-parameter reset ESC[m", "x" + esc + "[m" + "y", "xy"));
        tests.add(checkStrip("explicit reset ESC[0m", esc + "[0m" + "z", "z"));

        // Combinations and positions.
        tests.add(checkStrip("multiple parameters in one sequence", sgr("1;4;31;44") + "x" + reset, "x"));
        tests.add(checkStrip("consecutive sequences", sgr("1") + sgr("31") + sgr("48;5;0") + "x" + reset + reset, "x"));
        tests.add(checkStrip("sequence at beginning", sgr("32") + "start", "start"));
        tests.add(checkStrip("sequence in middle", "mid" + sgr("33") + "dle", "middle"));
        tests.add(checkStrip("sequence at end", "end" + reset, "end"));
        tests.add(checkStrip("only sequences -> empty", sgr("1") + sgr("31") + reset, ""));

        // Colon-separated parameters (ITU T.416 style).
        tests.add(checkStrip("colon 256-color", sgr("38:5:208") + "x" + sgr("48:5:17") + "y", "xy"));
        tests.add(checkStrip("colon RGB with empty color-space id", sgr("38:2::255:165:0") + "x" + sgr("48:2::0:0:0") + "y", "xy"));
        tests.add(checkStrip("colon underline style (curly)", sgr("4:3") + "x" + sgr("4:0"), "x"));
        tests.add(checkStrip("mixed colon and semicolon", sgr("1;38:2::1:2:3;4") + "x", "x"));

        // Unicode and whitespace.
        tests.add(checkStrip("Arabic text", sgr("1") + "مرحبا بالعالم" + reset, "مرحبا بالعالم"));
        tests.add(checkStrip("mixed Arabic, English, CJK, emoji, combining mark", sgr("31") + "مرحبا" + reset + " Hello " + sgr("38;5;46") + "日本語" + reset + " 😀 é", "مرحبا Hello 日本語 😀 é"));
        tests.add(checkStrip("LF, CRLF, tabs and spaces preserved", sgr("31") + "line1\n" + reset + "line2\r\n\t" + sgr("1") + "  indented  " + reset + "\n", "line1\nline2\r\n\t  indented  \n"));

        // Non-SGR sequences are preserved.
        tests.add(checkStrip("clear screen ESC[2J", esc + "[2J", esc + "[2J"));
        tests.add(checkStrip("cursor home ESC[H and position ESC[10;5H", esc + "[H" + esc + "[10;5H", esc + "[H" + esc + "[10;5H"));
        tests.add(checkStrip("cursor up ESC[3A and erase line ESC[K", esc + "[3A" + esc + "[K", esc + "[3A" + esc + "[K"));
        tests.add(checkStrip("hide/show cursor ESC[?25l / ESC[?25h", esc + "[?25l" + esc + "[?25h", esc + "[?25l" + esc + "[?25h"));
        tests.add(checkStrip("private-parameter ESC[>4;1m is not SGR", esc + "[>4;1m", esc + "[>4;1m"));
        tests.add(checkStrip("uppercase ESC[31M is not SGR", esc + "[31M", esc + "[31M"));
        tests.add(checkStrip("OSC window title with BEL", esc + "]0;My Title\u0007", esc + "]0;My Title\u0007"));
        tests.add(checkStrip("OSC 8 hyperlink with ST", esc + "]8;;https://example.com" + esc + "\\link" + esc + "]8;;" + esc + "\\", esc + "]8;;https://example.com" + esc + "\\link" + esc + "]8;;" + esc + "\\"));
        tests.add(checkStrip("non-SGR kept while surrounding SGR removed", esc + "[2J" + sgr("31") + "x" + reset + esc + "[H", esc + "[2J" + "x" + esc + "[H"));

        // Incomplete and malformed sequences are preserved.
        tests.add(checkStrip("missing final byte at end", "a" + esc + "[31", "a" + esc + "[31"));
        tests.add(checkStrip("missing final byte before text", esc + "[31 red", esc + "[31 red"));
        tests.add(checkStrip("lone ESC", "a" + esc + "b", "a" + esc + "b"));
        tests.add(checkStrip("bare ESC[ at end", "a" + esc + "[", "a" + esc + "["));
        tests.add(checkStrip("non-numeric parameter ESC[3x1m", esc + "[3x1m", esc + "[3x1m"));
        tests.add(checkStrip("space inside parameters ESC[ 31m", esc + "[ 31m", esc + "[ 31m"));
        tests.add(checkStrip("missing ESC: [31m", "[31m text", "[31m text"));
        tests.add(checkStrip("ESC without '[': ESC 31m", esc + " 31m", esc + " 31m"));
        tests.add(checkStrip("malformed kept, valid removed", esc + "[31" + sgr("1") + "x", esc + "[31" + "x"));

        // Single-pass behavior: a sequence formed by removing an inner one is kept.
        tests.add(dynamicTest("stripSgr: single pass keeps a sequence formed by removing an inner one", () -> {
            String input = esc + "[" + esc + "[0m" + "31mHello";
            String expected = esc + "[31mHello";
            Captured<String> c = capture(() -> Ansi.stripSgr(input));
            assertEquals(expected, c.result, "first call");
            assertNoOutput(c);
            // A second call removes the newly formed sequence: not idempotent for such input.
            assertEquals("Hello", Ansi.stripSgr(expected), "second call");
        }));

        // Round trip with the Ansi API.
        tests.add(dynamicTest("stripSgr removes everything wrap/fg256/fgRgb/constants add", () -> {
            assertEquals("Error!", Ansi.stripSgr(Ansi.wrap(Ansi.FG_RED + Ansi.BOLD, "Error!")), "wrap");
            assertEquals("o", Ansi.stripSgr(Ansi.wrap(Ansi.fg256(208) + Ansi.bg256(17), "o")), "fg256");
            assertEquals("o", Ansi.stripSgr(Ansi.wrap(Ansi.fgRgb(255, 165, 0) + Ansi.bgRgb(1, 2, 3), "o")), "rgb");
            assertEquals("o", Ansi.stripSgr(Ansi.FG_RGB_ORANGE + Ansi.BG_256_NAVY + "o" + Ansi.FG_DEFAULT + Ansi.BG_DEFAULT), "constants");
        }));
        tests.add(dynamicTest("stripSgr removes every public Ansi constant", () -> {
            for(Field f : Ansi.class.getFields()){
                String value = (String) f.get(null);
                assertEquals("a|b", Ansi.stripSgr("a|" + value + "b"), f.getName());
            }
        }));

        // Object overload.
        tests.add(dynamicTest("stripSgr(Object) String typed as Object matches String overload", () -> {
            Object text = sgr("1") + "bold" + reset;
            assertEquals("bold", Ansi.stripSgr(text), "result");
        }));
        tests.add(dynamicTest("stripSgr(Object) Integer and Boolean", () -> {
            assertEquals("42", Ansi.stripSgr(Integer.valueOf(42)), "Integer");
            assertEquals("-7", Ansi.stripSgr(Integer.valueOf(-7)), "negative Integer");
            assertEquals("true", Ansi.stripSgr(Boolean.TRUE), "Boolean");
        }));
        tests.add(dynamicTest("stripSgr(Object) null object -> empty string, silent", () -> {
            Captured<String> c = capture(() -> Ansi.stripSgr((Object) null));
            assertEquals("", c.result, "result");
            assertNoOutput(c);
        }));
        tests.add(dynamicTest("stripSgr(Object) custom toString() returning styled text", () -> {
            Object value = new Object(){
                @Override
                public String toString(){
                    return "\u001B[38;2;255;165;0m" + "orange" + "\u001B[0m";
                }
            };
            assertEquals("orange", Ansi.stripSgr(value), "result");
        }));
        tests.add(dynamicTest("stripSgr(Object) custom toString() returning null -> empty string", () -> {
            Object value = new Object(){
                @Override
                public String toString(){
                    return null;
                }
            };
            assertEquals("", Ansi.stripSgr(value), "result");
        }));
        tests.add(dynamicTest("stripSgr(Object) keeps single-pass behavior", () -> {
            Object input = esc + "[" + esc + "[0m" + "31mHello";
            assertEquals(esc + "[31mHello", Ansi.stripSgr(input), "result");
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testContainsSgr(){
        List<DynamicTest> tests = new ArrayList<>();
        String esc = "\u001B";
        tests.add(checkContains("null", null, false));
        tests.add(checkContains("empty string", "", false));
        tests.add(checkContains("plain ASCII", "Hello, World!", false));
        tests.add(checkContains("Unicode text", "مرحبا • 日本語 • 😀", false));
        tests.add(checkContains("standard SGR", sgr("31") + "Hello", true));
        tests.add(checkContains("RGB SGR", sgr("38;2;255;0;0") + "x", true));
        tests.add(checkContains("256-color SGR", sgr("48;5;208") + "x", true));
        tests.add(checkContains("curly underline", sgr("4:3") + "Hello", true));
        tests.add(checkContains("dotted underline", sgr("4:4") + "x", true));
        tests.add(checkContains("dashed underline", sgr("4:5") + "x", true));
        tests.add(checkContains("underline color 256 (colon)", sgr("58:5:196") + "x", true));
        tests.add(checkContains("underline color RGB (colon)", sgr("58:2::255:0:0") + "Hello", true));
        tests.add(checkContains("several sequences", sgr("1") + "a" + sgr("31") + "b" + sgr("0"), true));
        tests.add(checkContains("embedded in the middle", "before " + sgr("32") + " after", true));
        tests.add(checkContains("at the end", "text" + sgr("0"), true));
        tests.add(checkContains("empty-parameter ESC[m", esc + "[m", true));
        tests.add(checkContains("only separators ESC[;m", esc + "[;m", true));
        tests.add(checkContains("incomplete ESC[31", "a" + esc + "[31", false));
        tests.add(checkContains("bare ESC[", "a" + esc + "[", false));
        tests.add(checkContains("cursor movement ESC[2A and ESC[10;5H", esc + "[2A" + esc + "[10;5H", false));
        tests.add(checkContains("clear screen ESC[2J", esc + "[2J", false));
        tests.add(checkContains("private parameter ESC[?25l", esc + "[?25l", false));
        tests.add(checkContains("private parameter ESC[>4;1m", esc + "[>4;1m", false));
        tests.add(checkContains("uppercase ESC[31M", esc + "[31M", false));
        tests.add(checkContains("OSC title", esc + "]0;title\u0007", false));
        tests.add(checkContains("8-bit CSI U+009B", "\u009B31mx", false));
        tests.add(checkContains("bracket notation without ESC", "[31mHello", false));
        tests.add(checkContains("lone ESC", "a" + esc + "b", false));
        tests.add(checkContains("non-numeric parameter ESC[3x1m", esc + "[3x1m", false));
        tests.add(checkContains("overlapping fragment ESC[ + ESC[0m + 31m", esc + "[" + esc + "[0m" + "31m", true));
        tests.add(dynamicTest("containsSgr(s) is true exactly when stripSgr(s) changes s", () -> {
            String[] samples = {
                null, "", "plain", sgr("31") + "x", esc + "[31", esc + "[m", esc + "[2J", "[31m",
                esc + "[" + esc + "[0m31m", sgr("58:2::1:2:3"), esc + "]0;" + sgr("1") + "\u0007"
            };
            for(String s : samples){
                boolean changed = !Ansi.stripSgr(s).equals((s != null)? s : "");
                assertEquals(changed, Ansi.containsSgr(s), "sample " + visible(s));
            }
        }));
        tests.add(dynamicTest("containsSgr recognizes every generated sequence", () -> {
            assertTrue(Ansi.containsSgr(Ansi.fgRgb(255, 0, 0)), "fgRgb(255, 0, 0)");
            assertTrue(Ansi.containsSgr(Ansi.bg256(17)), "bg256");
            assertTrue(Ansi.containsSgr(Ansi.underlineRgb(1, 2, 3)), "underlineRgb");
            assertTrue(Ansi.containsSgr(Ansi.fgHex("#abc")), "fgHex");
            assertTrue(Ansi.containsSgr(Ansi.wrap(null, "x")), "wrap(null, x)");
            for(Field f : Ansi.class.getFields()){
                assertTrue(Ansi.containsSgr((String) f.get(null)), f.getName());
            }
        }));
        return tests;
    }
    @TestFactory
    List<DynamicTest> testContainsSgrObject(){
        List<DynamicTest> tests = new ArrayList<>();
        Object[][] cases = {
            {"null", null, false},
            {"empty String as Object", "", false},
            {"plain String as Object", "plain", false},
            {"SGR String as Object", sgr("31") + "red", true},
            {"plain StringBuilder", new StringBuilder("plain"), false},
            {"SGR StringBuilder", new StringBuilder(sgr("58:2::1:2:3") + "text"), true},
            {"Integer", Integer.valueOf(123), false},
            {"Double", Double.valueOf(1.5), false},
            {"Boolean true", Boolean.TRUE, false},
            {"Boolean false", Boolean.FALSE, false},
            {"custom plain toString", new Object(){
                @Override public String toString(){ return "custom plain text"; }
            }, false},
            {"custom SGR toString", new Object(){
                @Override public String toString(){ return sgr("4:3") + "custom styled text"; }
            }, true},
            {"custom non-SGR control sequence", new Object(){
                @Override public String toString(){ return CSI + "2J"; }
            }, false}
        };
        for(Object[] sample : cases){
            tests.add(dynamicTest("containsSgr(Object): " + sample[0] + " -> " + sample[2] + ", silently", () -> {
                Object value = sample[1];
                Captured<Boolean> c = capture(() -> {
                    boolean result = Ansi.containsSgr(value);
                    assertEquals(Ansi.containsSgr((value != null)? String.valueOf(value) : null), result, "String overload agreement");
                    return result;
                });
                assertEquals(sample[2], c.result, "result");
                assertNoOutput(c);
            }));
        }
        return tests;
    }
    private static DynamicTest checkContains(String name, String input, boolean expected){
        return dynamicTest("containsSgr: " + name + " -> " + expected, () -> {
            String copy = ((input != null)? new String(input.toCharArray()) : null);
            Captured<Boolean> c = capture(() -> Ansi.containsSgr(input));
            assertEquals(expected, c.result, "result");
            assertEquals(copy, input, "input unchanged");
            assertNoOutput(c);
        });
    }
}
