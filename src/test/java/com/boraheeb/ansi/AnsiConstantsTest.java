package com.boraheeb.ansi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static com.boraheeb.ansi.AnsiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/** Exact constant values and complete public-constant coverage, independent of execution order. **/
final class AnsiConstantsTest{
    @TestFactory
    List<DynamicTest> constantValues(){
        List<DynamicTest> tests = new ArrayList<>();
        for(Map.Entry<String, String> e : expectedConstants().entrySet()){
            tests.add(dynamicTest(e.getKey() + " == " + visible(e.getValue()), () -> {
                Field field = Ansi.class.getField(e.getKey());
                assertEquals(e.getValue(), field.get(null), e.getKey());
            }));
        }
        return tests;
    }

    @Test
    @DisplayName("Every public String constant in Ansi is verified by a test")
    void everyPublicConstantHasExplicitCoverage(){
        Set<String> actual = new TreeSet<>();
        for(Field field : Ansi.class.getFields()){
            if(field.getType() == String.class){
                actual.add(field.getName());
            }
        }
        assertEquals(expectedConstants().keySet(), actual, "explicitly covered public String constants");
    }

    @Test
    @DisplayName("WHITE constants agree with the 256-color generators at index 231")
    void whiteConstantsMatchGenerators(){
        assertEquals(Ansi.fg256(231), Ansi.FG_256_WHITE, "foreground");
        assertEquals(Ansi.bg256(231), Ansi.BG_256_WHITE, "background");
    }

    @Test
    @DisplayName("Underline constants are public static final Strings")
    void underlineConstantsAreImmutable() throws Exception{
        for(String name : new String[]{"UNDERLINE_CURLY", "UNDERLINE_DOTTED", "UNDERLINE_DASHED", "UNDERLINE_COLOR_DEFAULT"}){
            Field field = Ansi.class.getField(name);
            int modifiers = field.getModifiers();
            assertTrue(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers), name + " is public static final");
            assertEquals(String.class, field.getType(), name + " type");
        }
    }

    @Test
    @DisplayName("Selective reset constants are public static final Strings")
    void selectiveResetsAreImmutable() throws Exception{
        for(String name : new String[]{"NORMAL_INTENSITY", "ITALIC_OFF", "UNDERLINE_OFF", "BLINK_OFF", "INVERT_OFF", "HIDDEN_OFF", "STRIKETHROUGH_OFF", "FG_DEFAULT", "BG_DEFAULT"}){
            Field field = Ansi.class.getField(name);
            int modifiers = field.getModifiers();
            assertTrue(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers), name + " is public static final");
            assertEquals(String.class, field.getType(), name + " type");
        }
    }

    /** Independent expected values also drive each individually executed constant case. **/
    private static Map<String, String> expectedConstants(){
        Map<String, String> expected = new LinkedHashMap<>();
        String[] names = {
            "RESET", "BOLD", "DIM", "ITALIC", "UNDERLINE",
            "BLINK", "RAPID_BLINK", "INVERT", "HIDDEN", "STRIKETHROUGH"
        };
        for(int i = 0; i < names.length; i++){
            expected.put(names[i], sgr(String.valueOf(i)));
        }

        String[] colors = {"BLACK", "RED", "GREEN", "YELLOW", "BLUE", "PURPLE", "CYAN", "WHITE"};
        for(int i = 0; i < colors.length; i++){
            expected.put("FG_" + colors[i], sgr(String.valueOf(30 + i)));
            expected.put("FG_BRIGHT_" + colors[i], sgr(String.valueOf(90 + i)));
            expected.put("BG_" + colors[i], sgr(String.valueOf(40 + i)));
            expected.put("BG_BRIGHT_" + colors[i], sgr(String.valueOf(100 + i)));
        }

        expected.put("DOUBLE_UNDERLINE", sgr("21"));
        expected.put("OVERLINE", sgr("53"));
        expected.put("OVERLINE_OFF", sgr("55"));

        Map<String, String> underlines = new LinkedHashMap<>();
        underlines.put("UNDERLINE_CURLY", sgr("4:3"));
        underlines.put("UNDERLINE_DOTTED", sgr("4:4"));
        underlines.put("UNDERLINE_DASHED", sgr("4:5"));
        underlines.put("UNDERLINE_COLOR_DEFAULT", sgr("59"));
        for(Map.Entry<String, String> e : underlines.entrySet()){
            expected.put(e.getKey(), e.getValue());
        }
        Map<String, String> resets = new LinkedHashMap<>();
        resets.put("NORMAL_INTENSITY", sgr("22"));
        resets.put("ITALIC_OFF", sgr("23"));
        resets.put("UNDERLINE_OFF", sgr("24"));
        resets.put("BLINK_OFF", sgr("25"));
        resets.put("INVERT_OFF", sgr("27"));
        resets.put("HIDDEN_OFF", sgr("28"));
        resets.put("STRIKETHROUGH_OFF", sgr("29"));
        resets.put("FG_DEFAULT", sgr("39"));
        resets.put("BG_DEFAULT", sgr("49"));
        for(Map.Entry<String, String> e : resets.entrySet()){
            expected.put(e.getKey(), e.getValue());
        }
        Map<String, Integer> palette256 = new LinkedHashMap<>();
        Map<String, int[]> paletteRgb = new LinkedHashMap<>();
        palette256.put("BLACK", 16);
        palette256.put("DARK_GRAY", 236);
        palette256.put("GRAY", 244);
        palette256.put("LIGHT_GRAY", 250);
        palette256.put("WHITE", 231);
        palette256.put("RED", 196);
        palette256.put("CRIMSON", 197);
        palette256.put("ORANGE", 208);
        palette256.put("GOLD", 220);
        palette256.put("YELLOW", 226);
        palette256.put("GREEN", 46);
        palette256.put("LIME", 118);
        palette256.put("TEAL", 37);
        palette256.put("CYAN", 51);
        palette256.put("BLUE", 33);
        palette256.put("SKY_BLUE", 117);
        palette256.put("NAVY", 18);
        palette256.put("PURPLE", 93);
        palette256.put("VIOLET", 141);
        palette256.put("MAGENTA", 201);
        palette256.put("PINK", 205);
        palette256.put("BROWN", 94);

        paletteRgb.put("ORANGE", new int[]{255, 165, 0});
        paletteRgb.put("DARK_ORANGE", new int[]{255, 140, 0});
        paletteRgb.put("GOLD", new int[]{255, 215, 0});
        paletteRgb.put("DARK_GOLD", new int[]{184, 134, 11});
        paletteRgb.put("BROWN", new int[]{139, 69, 19});
        paletteRgb.put("CHOCOLATE", new int[]{210, 105, 30});
        paletteRgb.put("TAN", new int[]{210, 180, 140});
        paletteRgb.put("PINK", new int[]{255, 105, 180});
        paletteRgb.put("HOT_PINK", new int[]{255, 20, 147});
        paletteRgb.put("DEEP_PINK", new int[]{199, 21, 133});
        paletteRgb.put("CORAL", new int[]{255, 127, 80});
        paletteRgb.put("SALMON", new int[]{250, 128, 114});
        paletteRgb.put("CRIMSON", new int[]{220, 20, 60});
        paletteRgb.put("MAROON", new int[]{128, 0, 0});
        paletteRgb.put("OLIVE", new int[]{128, 128, 0});
        paletteRgb.put("LIME", new int[]{50, 205, 50});
        paletteRgb.put("FOREST_GREEN", new int[]{34, 139, 34});
        paletteRgb.put("MINT", new int[]{152, 255, 152});
        paletteRgb.put("TEAL", new int[]{0, 128, 128});
        paletteRgb.put("TURQUOISE", new int[]{64, 224, 208});
        paletteRgb.put("SKY_BLUE", new int[]{135, 206, 235});
        paletteRgb.put("DEEP_SKY_BLUE", new int[]{0, 191, 255});
        paletteRgb.put("STEEL_BLUE", new int[]{70, 130, 180});
        paletteRgb.put("NAVY", new int[]{0, 0, 128});
        paletteRgb.put("INDIGO", new int[]{75, 0, 130});
        paletteRgb.put("VIOLET", new int[]{138, 43, 226});
        paletteRgb.put("MAGENTA", new int[]{255, 0, 255});
        paletteRgb.put("PLUM", new int[]{221, 160, 221});
        paletteRgb.put("LAVENDER", new int[]{230, 230, 250});
        paletteRgb.put("BEIGE", new int[]{245, 245, 220});
        paletteRgb.put("KHAKI", new int[]{240, 230, 140});
        paletteRgb.put("SILVER", new int[]{192, 192, 192});
        paletteRgb.put("GRAY", new int[]{128, 128, 128});
        paletteRgb.put("DARK_GRAY", new int[]{64, 64, 64});
        paletteRgb.put("LIGHT_GRAY", new int[]{211, 211, 211});
        paletteRgb.put("CHARCOAL", new int[]{54, 69, 79});
        paletteRgb.put("SNOW", new int[]{255, 250, 250});
        for(Map.Entry<String, Integer> e : palette256.entrySet()){
            expected.put("FG_256_" + e.getKey(), sgr("38;5;" + e.getValue()));
            expected.put("BG_256_" + e.getKey(), sgr("48;5;" + e.getValue()));
        }
        for(Map.Entry<String, int[]> e : paletteRgb.entrySet()){
            int[] rgb = e.getValue();
            String body = rgb[0] + ";" + rgb[1] + ";" + rgb[2];
            expected.put("FG_RGB_" + e.getKey(), sgr("38;2;" + body));
            expected.put("BG_RGB_" + e.getKey(), sgr("48;2;" + body));
        }
        return Collections.unmodifiableMap(expected);
    }
}
