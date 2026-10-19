package com.boraheeb.ansi;
// -- Libraries: ---------------------------------------------------------------
import java.util.regex.Pattern;
// -- Ansi Class: --------------------------------------------------------------
/**
* ANSI escape code constants and utilities for terminal text styling.
*
* <p>Provides three levels of color support:</p>
* <ul>
*   <li>Standard and Bright Colors (16) — {@code FG_RED}, {@code FG_BRIGHT_BLUE}, etc.</li>
*   <li>256-Color Palette — {@link #fg256(int)}, {@link #bg256(int)}, {@link #underline256(int)}</li>
*   <li>
*       True Color RGB — {@link #fgRgb(int, int, int)}, {@link #bgRgb(int, int, int)},
*       {@link #fgRgb(int)}, {@link #bgRgb(int)},
*       {@link #fgHex(String)}, {@link #bgHex(String)},
*       {@link #underlineRgb(int, int, int)}, {@link #underlineRgb(int)}, {@link #underlineHex(String)}
*   </li>
* </ul>
*
* <p>Text styling utilities:</p>
* <ul>
*   <li>{@link #wrap(String, String)}, {@link #wrap(String, Object)}</li>
*   <li>{@link #stripSgr(String)}, {@link #stripSgr(Object)}</li>
*   <li>{@link #containsSgr(String)}, {@link #containsSgr(Object)}</li>
* </ul>
*
* <p>Color utilities:</p>
* <ul>
*   <li>Color Validation — {@link #isValid256(int)}, {@link #isValidRgbChannel(int)}</li>
*   <li>Color Conversion — {@link #rgbTo256(int, int, int)}, {@link #color256ToRgb(int)}</li>
* </ul>
*
* <p>Example usage:</p>
* <pre>{@code
*   // Text attributes and standard colors.
*   System.out.println(Ansi.wrap(Ansi.FG_RED + Ansi.BOLD, "Error!"));
*   System.out.println(Ansi.wrap(Ansi.BG_BLUE + Ansi.FG_BRIGHT_WHITE, "Notice"));
*
*   // 256-color palette and true color RGB.
*   System.out.println(Ansi.wrap(Ansi.fg256(208), "Orange text"));
*   System.out.println(Ansi.wrap(Ansi.fgRgb(255, 165, 0), "True orange"));
*   System.out.println(Ansi.wrap(Ansi.fgRgb(0xFFA500), "Packed orange"));
*   System.out.println(Ansi.wrap(Ansi.fgHex("#FFA500"), "Hex orange"));
*   System.out.println(Ansi.wrap(Ansi.bgHex("#020A6D") + Ansi.FG_WHITE, "Navy background"));
*
*   // Predefined colors.
*   System.out.println(Ansi.wrap(Ansi.FG_RGB_CORAL + Ansi.BG_256_NAVY, "Coral on navy"));
*
*   // Underline styles and underline colors (terminal-dependent).
*   System.out.println(Ansi.wrap(Ansi.UNDERLINE_CURLY + Ansi.underlineRgb(255, 0, 0), "Spelling mistake"));
*   System.out.println(Ansi.wrap(Ansi.UNDERLINE + Ansi.underlineHex("#FF0000"), "Red underline"));
*
*   // Selective resets turn off one attribute and keep the others.
*   System.out.println(Ansi.BOLD + Ansi.FG_GREEN + "OK" + Ansi.FG_DEFAULT + " (still bold)" + Ansi.RESET);
*
*   // Detecting and removing SGR (Select Graphic Rendition) sequences.
*   String styled = Ansi.wrap(Ansi.FG_RED, "Error!");
*   Ansi.containsSgr(styled);   // true
*   Ansi.stripSgr(styled);      // "Error!"
*
*   // Color conversion.
*   Ansi.rgbTo256(255, 0, 0);   // 196
*   Ansi.color256ToRgb(196);    // 0xFF0000
* }</pre>
*
* <p>Terminals without ANSI support may display raw escape sequences.</p>
*
* <p>
*   Based on ANSI SGR (ECMA-48) for text attributes and the standard colors, with
*   commonly supported terminal extensions for bright, 256-color and true-color RGB
*   rendering. Actual rendering depends on the capabilities of the terminal.
*   Reference: <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape code — Wikipedia</a>
* </p>
*
* <p>This class is a static utility class and is thread-safe.</p>
*
* @author BoRaheeb
**/
public final class Ansi{
    // -- Constants: ---------------------------------------------------------------
    /** ANSI prefix for 256-color foreground codes. **/
    private static final String FG_256_PREFIX = "\u001B[38;5;";
    /** ANSI prefix for 256-color background codes. **/
    private static final String BG_256_PREFIX = "\u001B[48;5;";
    /** ANSI prefix for RGB (true color) foreground codes. **/
    private static final String FG_RGB_PREFIX = "\u001B[38;2;";
    /** ANSI prefix for RGB (true color) background codes. **/
    private static final String BG_RGB_PREFIX = "\u001B[48;2;";
    /** ANSI prefix for 256-color underline color codes (colon form). **/
    private static final String UNDERLINE_256_PREFIX = "\u001B[58:5:";
    /** ANSI prefix for RGB underline color codes (colon form, empty color-space id). **/
    private static final String UNDERLINE_RGB_PREFIX = "\u001B[58:2::";
    /** Separator between semicolon-form SGR parameters. **/
    private static final String SEMICOLON = ";";
    /** Separator used between colon-form sub-parameters. **/
    private static final String COLON = ":";
    /** ANSI escape code suffix. **/
    private static final String SUFFIX = "m";
    /**
    * Matches one complete SGR sequence: {@code ESC [}, optional numeric parameters
    * separated by {@code ;} or {@code :}, and the final byte {@code m}.
    **/
    private static final Pattern SGR_PATTERN = Pattern.compile("\u001B\\[[0-9;:]*m");
    /** Minimum valid value for 256-color codes. **/
    private static final int MIN_256 = 0;
    /** Maximum valid value for 256-color codes. **/
    private static final int MAX_256 = 255;
    /** Default 256-color code used as fallback when invalid input is provided. **/
    private static final int DEFAULT_256 = 244;
    /** Minimum valid value for RGB channels. **/
    private static final int MIN_RGB = 0;
    /** Maximum valid value for RGB channels. **/
    private static final int MAX_RGB = 255;
    /** Default RGB channel value used as fallback when invalid input is provided. **/
    private static final int DEFAULT_RGB = 180;
    /** Maximum valid packed 24-bit RGB value ({@code 0xRRGGBB}). **/
    private static final int MAX_PACKED_RGB = 0xFFFFFF;
    /** Packed RGB fallback used when invalid input is provided: {@link #DEFAULT_RGB} for every channel. **/
    private static final int DEFAULT_PACKED_RGB = ((DEFAULT_RGB << 16) | (DEFAULT_RGB << 8) | DEFAULT_RGB);
    /** First 256-color index of the 6×6×6 RGB cube. **/
    private static final int CUBE_START = 16;
    /** First 256-color index of the grayscale ramp. **/
    private static final int GRAY_START = 232;
    /**
    * Reference packed RGB values for the system colors 0–15. Terminals may configure
    * these colors differently, so the values are a convention, not the displayed colors.
    **/
    private static final int[] SYSTEM_COLORS_RGB = {
        0x000000, 0x800000, 0x008000, 0x808000, 0x000080, 0x800080, 0x008080, 0xC0C0C0,
        0x808080, 0xFF0000, 0x00FF00, 0xFFFF00, 0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF
    };
    /** Channel levels of the 6×6×6 RGB cube (indices 16–231). **/
    private static final int[] CUBE_LEVELS = {0, 95, 135, 175, 215, 255};
        // -- ANSI Basic Escape Codes: -------------------------------------------------
            // -- ANSI Text Formatting Escape Codes: ---------------------------------------
                /** Resets all active styles and colors ({@code "U+001B[0m"}). **/
                public static final String RESET = "\u001B[0m";
                /** Bold text ({@code "U+001B[1m"}). **/
                public static final String BOLD = "\u001B[1m";
                /** Dim (faint) text ({@code "U+001B[2m"}). **/
                public static final String DIM = "\u001B[2m";
                /** Italic text ({@code "U+001B[3m"}). **/
                public static final String ITALIC = "\u001B[3m";
                /** Underlined text ({@code "U+001B[4m"}). **/
                public static final String UNDERLINE = "\u001B[4m";
                /**
                * Double-underlined text ({@code "U+001B[21m"}).
                *
                * <p>
                *   Support is terminal-dependent, and some terminals interpret SGR 21 differently,
                *   for example as "bold off" or as a single underline. Use {@link #UNDERLINE_OFF}
                *   to turn it off.
                * </p>
                **/
                public static final String DOUBLE_UNDERLINE = "\u001B[21m";
                /**
                * Curly (wavy) underlined text ({@code "U+001B[4:3m"}).
                *
                * <p>
                *   A terminal-dependent SGR extension using a colon sub-parameter. Terminals
                *   without support may ignore it or fall back to a plain underline. This constant
                *   only generates the escape sequence and does not guarantee visual rendering.
                *   Use {@link #UNDERLINE_OFF} to turn it off.
                * </p>
                **/
                public static final String UNDERLINE_CURLY = "\u001B[4:3m";
                /**
                * Dotted underlined text ({@code "U+001B[4:4m"}).
                *
                * <p>
                *   A terminal-dependent SGR extension using a colon sub-parameter. Terminals
                *   without support may ignore it or fall back to a plain underline. This constant
                *   only generates the escape sequence and does not guarantee visual rendering.
                *   Use {@link #UNDERLINE_OFF} to turn it off.
                * </p>
                **/
                public static final String UNDERLINE_DOTTED = "\u001B[4:4m";
                /**
                * Dashed underlined text ({@code "U+001B[4:5m"}).
                *
                * <p>
                *   A terminal-dependent SGR extension using a colon sub-parameter. Terminals
                *   without support may ignore it or fall back to a plain underline. This constant
                *   only generates the escape sequence and does not guarantee visual rendering.
                *   Use {@link #UNDERLINE_OFF} to turn it off.
                * </p>
                **/
                public static final String UNDERLINE_DASHED = "\u001B[4:5m";
                /** Slow blinking text ({@code "U+001B[5m"}). **/
                public static final String BLINK = "\u001B[5m";
                /** Rapid blinking text ({@code "U+001B[6m"}). **/
                public static final String RAPID_BLINK = "\u001B[6m";
                /** Inverted foreground and background colors ({@code "U+001B[7m"}). **/
                public static final String INVERT = "\u001B[7m";
                /** Hidden (concealed) text ({@code "U+001B[8m"}). **/
                public static final String HIDDEN = "\u001B[8m";
                /** Strikethrough text ({@code "U+001B[9m"}). **/
                public static final String STRIKETHROUGH = "\u001B[9m";
                /**
                * Overlined text ({@code "U+001B[53m"}).
                *
                * <p>Draws a line above the text. Support is terminal-dependent. Use {@link #OVERLINE_OFF} to turn it off.</p>
                **/
                public static final String OVERLINE = "\u001B[53m";
            // -- ANSI Text Formatting Reset Escape Codes: ---------------------------------
                /**
                * Normal intensity ({@code "U+001B[22m"}).
                *
                * <p>Turns off both {@link #BOLD} and {@link #DIM}; other active attributes and colors are preserved.</p>
                **/
                public static final String NORMAL_INTENSITY = "\u001B[22m";
                /**
                * Italic off ({@code "U+001B[23m"}).
                *
                * <p>Turns off {@link #ITALIC}; other active attributes and colors are preserved.</p>
                **/
                public static final String ITALIC_OFF = "\u001B[23m";
                /**
                * Underline off ({@code "U+001B[24m"}).
                *
                * <p>
                *   Turns off {@link #UNDERLINE}, including {@link #DOUBLE_UNDERLINE},
                *   {@link #UNDERLINE_CURLY}, {@link #UNDERLINE_DOTTED} and {@link #UNDERLINE_DASHED}
                *   where supported; other active attributes and colors are preserved.
                * </p>
                **/
                public static final String UNDERLINE_OFF = "\u001B[24m";
                /**
                * Blink off ({@code "U+001B[25m"}).
                *
                * <p>Turns off both {@link #BLINK} and {@link #RAPID_BLINK}; other active attributes and colors are preserved.</p>
                **/
                public static final String BLINK_OFF = "\u001B[25m";
                /**
                * Invert off ({@code "U+001B[27m"}).
                *
                * <p>Turns off {@link #INVERT}; other active attributes and colors are preserved.</p>
                **/
                public static final String INVERT_OFF = "\u001B[27m";
                /**
                * Hidden off ({@code "U+001B[28m"}).
                *
                * <p>Turns off {@link #HIDDEN}, making text visible again; other active attributes and colors are preserved.</p>
                **/
                public static final String HIDDEN_OFF = "\u001B[28m";
                /**
                * Strikethrough off ({@code "U+001B[29m"}).
                *
                * <p>Turns off {@link #STRIKETHROUGH}; other active attributes and colors are preserved.</p>
                **/
                public static final String STRIKETHROUGH_OFF = "\u001B[29m";
                /**
                * Overline off ({@code "U+001B[55m"}).
                *
                * <p>Turns off {@link #OVERLINE}; other active attributes and colors are preserved.</p>
                **/
                public static final String OVERLINE_OFF = "\u001B[55m";
                /**
                * Default underline color ({@code "U+001B[59m"}).
                *
                * <p>
                *   Resets the underline color set by {@link #underline256(int)},
                *   {@link #underlineRgb(int, int, int)}, {@link #underlineRgb(int)} or
                *   {@link #underlineHex(String)} to the terminal's default underline color.
                *   It does not turn off underlining and does not reset the foreground or background
                *   colors. Support is terminal-dependent; this constant only generates the escape
                *   sequence and does not guarantee visual rendering.
                * </p>
                **/
                public static final String UNDERLINE_COLOR_DEFAULT = "\u001B[59m";
            // -- ANSI Standard Foreground Colors: -----------------------------------------
                /** Black foreground ({@code "U+001B[30m"}). **/
                public static final String FG_BLACK = "\u001B[30m";
                /** Red foreground ({@code "U+001B[31m"}). **/
                public static final String FG_RED = "\u001B[31m";
                /** Green foreground ({@code "U+001B[32m"}). **/
                public static final String FG_GREEN = "\u001B[32m";
                /** Yellow foreground ({@code "U+001B[33m"}). **/
                public static final String FG_YELLOW = "\u001B[33m";
                /** Blue foreground ({@code "U+001B[34m"}). **/
                public static final String FG_BLUE = "\u001B[34m";
                /** Purple (magenta) foreground ({@code "U+001B[35m"}). **/
                public static final String FG_PURPLE = "\u001B[35m";
                /** Cyan foreground ({@code "U+001B[36m"}). **/
                public static final String FG_CYAN = "\u001B[36m";
                /** White foreground ({@code "U+001B[37m"}). **/
                public static final String FG_WHITE = "\u001B[37m";
                /** Bright black (dark gray) foreground ({@code "U+001B[90m"}). **/
                public static final String FG_BRIGHT_BLACK = "\u001B[90m";
                /** Bright red foreground ({@code "U+001B[91m"}). **/
                public static final String FG_BRIGHT_RED = "\u001B[91m";
                /** Bright green foreground ({@code "U+001B[92m"}). **/
                public static final String FG_BRIGHT_GREEN = "\u001B[92m";
                /** Bright yellow foreground ({@code "U+001B[93m"}). **/
                public static final String FG_BRIGHT_YELLOW = "\u001B[93m";
                /** Bright blue foreground ({@code "U+001B[94m"}). **/
                public static final String FG_BRIGHT_BLUE = "\u001B[94m";
                /** Bright purple (magenta) foreground ({@code "U+001B[95m"}). **/
                public static final String FG_BRIGHT_PURPLE = "\u001B[95m";
                /** Bright cyan foreground ({@code "U+001B[96m"}). **/
                public static final String FG_BRIGHT_CYAN = "\u001B[96m";
                /** Bright white foreground ({@code "U+001B[97m"}). **/
                public static final String FG_BRIGHT_WHITE = "\u001B[97m";
                /**
                * Default foreground ({@code "U+001B[39m"}).
                *
                * <p>
                *   Resets only the foreground color; the background color and other active
                *   attributes (bold, underline, etc.) are preserved.
                * </p>
                *
                * <p>
                *   Restores the terminal's default foreground color, not any previously
                *   used custom color.
                * </p>
                **/
                public static final String FG_DEFAULT = "\u001B[39m";
            // -- ANSI Standard Background Colors: -----------------------------------------
                /** Black background ({@code "U+001B[40m"}). **/
                public static final String BG_BLACK = "\u001B[40m";
                /** Red background ({@code "U+001B[41m"}). **/
                public static final String BG_RED = "\u001B[41m";
                /** Green background ({@code "U+001B[42m"}). **/
                public static final String BG_GREEN = "\u001B[42m";
                /** Yellow background ({@code "U+001B[43m"}). **/
                public static final String BG_YELLOW = "\u001B[43m";
                /** Blue background ({@code "U+001B[44m"}). **/
                public static final String BG_BLUE = "\u001B[44m";
                /** Purple (magenta) background ({@code "U+001B[45m"}). **/
                public static final String BG_PURPLE = "\u001B[45m";
                /** Cyan background ({@code "U+001B[46m"}). **/
                public static final String BG_CYAN = "\u001B[46m";
                /** White background ({@code "U+001B[47m"}). **/
                public static final String BG_WHITE = "\u001B[47m";
                /** Bright black (dark gray) background ({@code "U+001B[100m"}). **/
                public static final String BG_BRIGHT_BLACK = "\u001B[100m";
                /** Bright red background ({@code "U+001B[101m"}). **/
                public static final String BG_BRIGHT_RED = "\u001B[101m";
                /** Bright green background ({@code "U+001B[102m"}). **/
                public static final String BG_BRIGHT_GREEN = "\u001B[102m";
                /** Bright yellow background ({@code "U+001B[103m"}). **/
                public static final String BG_BRIGHT_YELLOW = "\u001B[103m";
                /** Bright blue background ({@code "U+001B[104m"}). **/
                public static final String BG_BRIGHT_BLUE = "\u001B[104m";
                /** Bright purple (magenta) background ({@code "U+001B[105m"}). **/
                public static final String BG_BRIGHT_PURPLE = "\u001B[105m";
                /** Bright cyan background ({@code "U+001B[106m"}). **/
                public static final String BG_BRIGHT_CYAN = "\u001B[106m";
                /** Bright white background ({@code "U+001B[107m"}). **/
                public static final String BG_BRIGHT_WHITE = "\u001B[107m";
                /**
                * Default background ({@code "U+001B[49m"}).
                *
                * <p>
                *   Resets only the background color; the foreground color and other active
                *   attributes (bold, underline, etc.) are preserved.
                * </p>
                *
                * <p>
                *   Restores the terminal's default background color, not any previously
                *   used custom color.
                * </p>
                **/
                public static final String BG_DEFAULT = "\u001B[49m";
        // -- ANSI 256-Color Escape Codes: ---------------------------------------------
            // -- ANSI 256-Color Foreground Colors: ----------------------------------------
                /** Black foreground, 256-color index 16. **/
                public static final String FG_256_BLACK = fg256(16);
                /** Dark gray foreground, 256-color index 236. **/
                public static final String FG_256_DARK_GRAY = fg256(236);
                /** Gray foreground, 256-color index 244. **/
                public static final String FG_256_GRAY = fg256(244);
                /** Light gray foreground, 256-color index 250. **/
                public static final String FG_256_LIGHT_GRAY = fg256(250);
                /** White foreground, 256-color index 231. **/
                public static final String FG_256_WHITE = fg256(231);
                /** Red foreground, 256-color index 196. **/
                public static final String FG_256_RED = fg256(196);
                /** Crimson foreground, 256-color index 197. **/
                public static final String FG_256_CRIMSON = fg256(197);
                /** Orange foreground, 256-color index 208. **/
                public static final String FG_256_ORANGE = fg256(208);
                /** Gold foreground, 256-color index 220. **/
                public static final String FG_256_GOLD = fg256(220);
                /** Yellow foreground, 256-color index 226. **/
                public static final String FG_256_YELLOW = fg256(226);
                /** Green foreground, 256-color index 46. **/
                public static final String FG_256_GREEN = fg256(46);
                /** Lime foreground, 256-color index 118. **/
                public static final String FG_256_LIME = fg256(118);
                /** Teal foreground, 256-color index 37. **/
                public static final String FG_256_TEAL = fg256(37);
                /** Cyan foreground, 256-color index 51. **/
                public static final String FG_256_CYAN = fg256(51);
                /** Blue foreground, 256-color index 33. **/
                public static final String FG_256_BLUE = fg256(33);
                /** Sky blue foreground, 256-color index 117. **/
                public static final String FG_256_SKY_BLUE = fg256(117);
                /** Navy foreground, 256-color index 18. **/
                public static final String FG_256_NAVY = fg256(18);
                /** Purple foreground, 256-color index 93. **/
                public static final String FG_256_PURPLE = fg256(93);
                /** Violet foreground, 256-color index 141. **/
                public static final String FG_256_VIOLET = fg256(141);
                /** Magenta foreground, 256-color index 201. **/
                public static final String FG_256_MAGENTA = fg256(201);
                /** Pink foreground, 256-color index 205. **/
                public static final String FG_256_PINK = fg256(205);
                /** Brown foreground, 256-color index 94. **/
                public static final String FG_256_BROWN = fg256(94);
            // -- ANSI 256-Color Background Colors: ----------------------------------------
                /** Black background, 256-color index 16. **/
                public static final String BG_256_BLACK = bg256(16);
                /** Dark gray background, 256-color index 236. **/
                public static final String BG_256_DARK_GRAY = bg256(236);
                /** Gray background, 256-color index 244. **/
                public static final String BG_256_GRAY = bg256(244);
                /** Light gray background, 256-color index 250. **/
                public static final String BG_256_LIGHT_GRAY = bg256(250);
                /** White background, 256-color index 231. **/
                public static final String BG_256_WHITE = bg256(231);
                /** Red background, 256-color index 196. **/
                public static final String BG_256_RED = bg256(196);
                /** Crimson background, 256-color index 197. **/
                public static final String BG_256_CRIMSON = bg256(197);
                /** Orange background, 256-color index 208. **/
                public static final String BG_256_ORANGE = bg256(208);
                /** Gold background, 256-color index 220. **/
                public static final String BG_256_GOLD = bg256(220);
                /** Yellow background, 256-color index 226. **/
                public static final String BG_256_YELLOW = bg256(226);
                /** Green background, 256-color index 46. **/
                public static final String BG_256_GREEN = bg256(46);
                /** Lime background, 256-color index 118. **/
                public static final String BG_256_LIME = bg256(118);
                /** Teal background, 256-color index 37. **/
                public static final String BG_256_TEAL = bg256(37);
                /** Cyan background, 256-color index 51. **/
                public static final String BG_256_CYAN = bg256(51);
                /** Blue background, 256-color index 33. **/
                public static final String BG_256_BLUE = bg256(33);
                /** Sky blue background, 256-color index 117. **/
                public static final String BG_256_SKY_BLUE = bg256(117);
                /** Navy background, 256-color index 18. **/
                public static final String BG_256_NAVY = bg256(18);
                /** Purple background, 256-color index 93. **/
                public static final String BG_256_PURPLE = bg256(93);
                /** Violet background, 256-color index 141. **/
                public static final String BG_256_VIOLET = bg256(141);
                /** Magenta background, 256-color index 201. **/
                public static final String BG_256_MAGENTA = bg256(201);
                /** Pink background, 256-color index 205. **/
                public static final String BG_256_PINK = bg256(205);
                /** Brown background, 256-color index 94. **/
                public static final String BG_256_BROWN = bg256(94);
        // -- ANSI RGB Escape Codes: ---------------------------------------------------
            // -- ANSI RGB Foreground Colors: ----------------------------------------------
                /** Orange foreground, rgb(255, 165, 0). **/
                public static final String FG_RGB_ORANGE = fgRgb(255, 165, 0);
                /** Dark orange foreground, rgb(255, 140, 0). **/
                public static final String FG_RGB_DARK_ORANGE = fgRgb(255, 140, 0);
                /** Gold foreground, rgb(255, 215, 0). **/
                public static final String FG_RGB_GOLD = fgRgb(255, 215, 0);
                /** Dark gold foreground, rgb(184, 134, 11). **/
                public static final String FG_RGB_DARK_GOLD = fgRgb(184, 134, 11);
                /** Brown foreground, rgb(139, 69, 19). **/
                public static final String FG_RGB_BROWN = fgRgb(139, 69, 19);
                /** Chocolate foreground, rgb(210, 105, 30). **/
                public static final String FG_RGB_CHOCOLATE = fgRgb(210, 105, 30);
                /** Tan foreground, rgb(210, 180, 140). **/
                public static final String FG_RGB_TAN = fgRgb(210, 180, 140);
                /** Pink foreground, rgb(255, 105, 180). **/
                public static final String FG_RGB_PINK = fgRgb(255, 105, 180);
                /** Hot pink foreground, rgb(255, 20, 147). **/
                public static final String FG_RGB_HOT_PINK = fgRgb(255, 20, 147);
                /** Deep pink foreground, rgb(199, 21, 133). **/
                public static final String FG_RGB_DEEP_PINK = fgRgb(199, 21, 133);
                /** Coral foreground, rgb(255, 127, 80). **/
                public static final String FG_RGB_CORAL = fgRgb(255, 127, 80);
                /** Salmon foreground, rgb(250, 128, 114). **/
                public static final String FG_RGB_SALMON = fgRgb(250, 128, 114);
                /** Crimson foreground, rgb(220, 20, 60). **/
                public static final String FG_RGB_CRIMSON = fgRgb(220, 20, 60);
                /** Maroon foreground, rgb(128, 0, 0). **/
                public static final String FG_RGB_MAROON = fgRgb(128, 0, 0);
                /** Olive foreground, rgb(128, 128, 0). **/
                public static final String FG_RGB_OLIVE = fgRgb(128, 128, 0);
                /** Lime foreground, rgb(50, 205, 50). **/
                public static final String FG_RGB_LIME = fgRgb(50, 205, 50);
                /** Forest green foreground, rgb(34, 139, 34). **/
                public static final String FG_RGB_FOREST_GREEN = fgRgb(34, 139, 34);
                /** Mint foreground, rgb(152, 255, 152). **/
                public static final String FG_RGB_MINT = fgRgb(152, 255, 152);
                /** Teal foreground, rgb(0, 128, 128). **/
                public static final String FG_RGB_TEAL = fgRgb(0, 128, 128);
                /** Turquoise foreground, rgb(64, 224, 208). **/
                public static final String FG_RGB_TURQUOISE = fgRgb(64, 224, 208);
                /** Sky blue foreground, rgb(135, 206, 235). **/
                public static final String FG_RGB_SKY_BLUE = fgRgb(135, 206, 235);
                /** Deep sky blue foreground, rgb(0, 191, 255). **/
                public static final String FG_RGB_DEEP_SKY_BLUE = fgRgb(0, 191, 255);
                /** Steel blue foreground, rgb(70, 130, 180). **/
                public static final String FG_RGB_STEEL_BLUE = fgRgb(70, 130, 180);
                /** Navy foreground, rgb(0, 0, 128). **/
                public static final String FG_RGB_NAVY = fgRgb(0, 0, 128);
                /** Indigo foreground, rgb(75, 0, 130). **/
                public static final String FG_RGB_INDIGO = fgRgb(75, 0, 130);
                /** Violet foreground, rgb(138, 43, 226). **/
                public static final String FG_RGB_VIOLET = fgRgb(138, 43, 226);
                /** Magenta foreground, rgb(255, 0, 255). **/
                public static final String FG_RGB_MAGENTA = fgRgb(255, 0, 255);
                /** Plum foreground, rgb(221, 160, 221). **/
                public static final String FG_RGB_PLUM = fgRgb(221, 160, 221);
                /** Lavender foreground, rgb(230, 230, 250). **/
                public static final String FG_RGB_LAVENDER = fgRgb(230, 230, 250);
                /** Beige foreground, rgb(245, 245, 220). **/
                public static final String FG_RGB_BEIGE = fgRgb(245, 245, 220);
                /** Khaki foreground, rgb(240, 230, 140). **/
                public static final String FG_RGB_KHAKI = fgRgb(240, 230, 140);
                /** Silver foreground, rgb(192, 192, 192). **/
                public static final String FG_RGB_SILVER = fgRgb(192, 192, 192);
                /** Gray foreground, rgb(128, 128, 128). **/
                public static final String FG_RGB_GRAY = fgRgb(128, 128, 128);
                /** Dark gray foreground, rgb(64, 64, 64). **/
                public static final String FG_RGB_DARK_GRAY = fgRgb(64, 64, 64);
                /** Light gray foreground, rgb(211, 211, 211). **/
                public static final String FG_RGB_LIGHT_GRAY = fgRgb(211, 211, 211);
                /** Charcoal foreground, rgb(54, 69, 79). **/
                public static final String FG_RGB_CHARCOAL = fgRgb(54, 69, 79);
                /** Snow foreground, rgb(255, 250, 250). **/
                public static final String FG_RGB_SNOW = fgRgb(255, 250, 250);
            // -- ANSI RGB Background Colors: ----------------------------------------------
                /** Orange background, rgb(255, 165, 0). **/
                public static final String BG_RGB_ORANGE = bgRgb(255, 165, 0);
                /** Dark orange background, rgb(255, 140, 0). **/
                public static final String BG_RGB_DARK_ORANGE = bgRgb(255, 140, 0);
                /** Gold background, rgb(255, 215, 0). **/
                public static final String BG_RGB_GOLD = bgRgb(255, 215, 0);
                /** Dark gold background, rgb(184, 134, 11). **/
                public static final String BG_RGB_DARK_GOLD = bgRgb(184, 134, 11);
                /** Brown background, rgb(139, 69, 19). **/
                public static final String BG_RGB_BROWN = bgRgb(139, 69, 19);
                /** Chocolate background, rgb(210, 105, 30). **/
                public static final String BG_RGB_CHOCOLATE = bgRgb(210, 105, 30);
                /** Tan background, rgb(210, 180, 140). **/
                public static final String BG_RGB_TAN = bgRgb(210, 180, 140);
                /** Pink background, rgb(255, 105, 180). **/
                public static final String BG_RGB_PINK = bgRgb(255, 105, 180);
                /** Hot pink background, rgb(255, 20, 147). **/
                public static final String BG_RGB_HOT_PINK = bgRgb(255, 20, 147);
                /** Deep pink background, rgb(199, 21, 133). **/
                public static final String BG_RGB_DEEP_PINK = bgRgb(199, 21, 133);
                /** Coral background, rgb(255, 127, 80). **/
                public static final String BG_RGB_CORAL = bgRgb(255, 127, 80);
                /** Salmon background, rgb(250, 128, 114). **/
                public static final String BG_RGB_SALMON = bgRgb(250, 128, 114);
                /** Crimson background, rgb(220, 20, 60). **/
                public static final String BG_RGB_CRIMSON = bgRgb(220, 20, 60);
                /** Maroon background, rgb(128, 0, 0). **/
                public static final String BG_RGB_MAROON = bgRgb(128, 0, 0);
                /** Olive background, rgb(128, 128, 0). **/
                public static final String BG_RGB_OLIVE = bgRgb(128, 128, 0);
                /** Lime background, rgb(50, 205, 50). **/
                public static final String BG_RGB_LIME = bgRgb(50, 205, 50);
                /** Forest green background, rgb(34, 139, 34). **/
                public static final String BG_RGB_FOREST_GREEN = bgRgb(34, 139, 34);
                /** Mint background, rgb(152, 255, 152). **/
                public static final String BG_RGB_MINT = bgRgb(152, 255, 152);
                /** Teal background, rgb(0, 128, 128). **/
                public static final String BG_RGB_TEAL = bgRgb(0, 128, 128);
                /** Turquoise background, rgb(64, 224, 208). **/
                public static final String BG_RGB_TURQUOISE = bgRgb(64, 224, 208);
                /** Sky blue background, rgb(135, 206, 235). **/
                public static final String BG_RGB_SKY_BLUE = bgRgb(135, 206, 235);
                /** Deep sky blue background, rgb(0, 191, 255). **/
                public static final String BG_RGB_DEEP_SKY_BLUE = bgRgb(0, 191, 255);
                /** Steel blue background, rgb(70, 130, 180). **/
                public static final String BG_RGB_STEEL_BLUE = bgRgb(70, 130, 180);
                /** Navy background, rgb(0, 0, 128). **/
                public static final String BG_RGB_NAVY = bgRgb(0, 0, 128);
                /** Indigo background, rgb(75, 0, 130). **/
                public static final String BG_RGB_INDIGO = bgRgb(75, 0, 130);
                /** Violet background, rgb(138, 43, 226). **/
                public static final String BG_RGB_VIOLET = bgRgb(138, 43, 226);
                /** Magenta background, rgb(255, 0, 255). **/
                public static final String BG_RGB_MAGENTA = bgRgb(255, 0, 255);
                /** Plum background, rgb(221, 160, 221). **/
                public static final String BG_RGB_PLUM = bgRgb(221, 160, 221);
                /** Lavender background, rgb(230, 230, 250). **/
                public static final String BG_RGB_LAVENDER = bgRgb(230, 230, 250);
                /** Beige background, rgb(245, 245, 220). **/
                public static final String BG_RGB_BEIGE = bgRgb(245, 245, 220);
                /** Khaki background, rgb(240, 230, 140). **/
                public static final String BG_RGB_KHAKI = bgRgb(240, 230, 140);
                /** Silver background, rgb(192, 192, 192). **/
                public static final String BG_RGB_SILVER = bgRgb(192, 192, 192);
                /** Gray background, rgb(128, 128, 128). **/
                public static final String BG_RGB_GRAY = bgRgb(128, 128, 128);
                /** Dark gray background, rgb(64, 64, 64). **/
                public static final String BG_RGB_DARK_GRAY = bgRgb(64, 64, 64);
                /** Light gray background, rgb(211, 211, 211). **/
                public static final String BG_RGB_LIGHT_GRAY = bgRgb(211, 211, 211);
                /** Charcoal background, rgb(54, 69, 79). **/
                public static final String BG_RGB_CHARCOAL = bgRgb(54, 69, 79);
                /** Snow background, rgb(255, 250, 250). **/
                public static final String BG_RGB_SNOW = bgRgb(255, 250, 250);
    // -- Constructors: ------------------------------------------------------------
    /** Private constructor — This class is a static utility class and cannot be created. **/
    private Ansi(){}
    // -- Utility Methods: ---------------------------------------------------------
    /**
    * Returns a foreground 256-color escape code.
    *
    * <p>If the given code is out of range (0–255), a warning is printed to {@code System.err}, and the default code is used.</p>
    *
    * @param code the 256-color code (0–255)
    * @return the ANSI escape string for the given foreground color
    **/
    public static String fg256(int code){
        return (FG_256_PREFIX + clean256(code, "fg256") + SUFFIX);
    }
    /**
    * Returns a background 256-color escape code.
    *
    * <p>If the given code is out of range (0–255), a warning is printed to {@code System.err}, and the default code is used.</p>
    *
    * @param code the 256-color code (0–255)
    * @return the ANSI escape string for the given background color
    **/
    public static String bg256(int code){
        return (BG_256_PREFIX + clean256(code, "bg256") + SUFFIX);
    }
    /**
    * Returns an underline 256-color escape code ({@code ESC[58:5:<code>m}).
    *
    * <p>If the given code is out of range (0–255), a warning is printed to {@code System.err}, and the default code is used.</p>
    *
    * <p>
    *   Sets only the underline color; it does not enable underlining. Combine it with
    *   {@link #UNDERLINE} or another underline style, and use {@link #UNDERLINE_COLOR_DEFAULT}
    *   to reset it. Support is terminal-dependent.
    * </p>
    *
    * @param code the 256-color code (0–255)
    * @return the ANSI escape string for the given underline color
    **/
    public static String underline256(int code){
        return (UNDERLINE_256_PREFIX + clean256(code, "underline256") + SUFFIX);
    }
    /**
    * Returns a foreground RGB escape code.
    *
    * <p>If any of the given values are out of range (0–255), a warning is printed to {@code System.err}, and the default value is used.</p>
    *
    * @param r the red channel (0–255)
    * @param g the green channel (0–255)
    * @param b the blue channel (0–255)
    * @return the ANSI escape string for the given RGB foreground color
    **/
    public static String fgRgb(int r, int g, int b){
        return (
            FG_RGB_PREFIX +
            cleanRgb(r, "fgRgb", "red") + SEMICOLON +
            cleanRgb(g, "fgRgb", "green") + SEMICOLON +
            cleanRgb(b, "fgRgb", "blue") + SUFFIX
        );
    }
    /**
    * Returns a background RGB escape code.
    *
    * <p>If any of the given values are out of range (0–255), a warning is printed to {@code System.err}, and the default value is used.</p>
    *
    * @param r the red channel (0–255)
    * @param g the green channel (0–255)
    * @param b the blue channel (0–255)
    * @return the ANSI escape string for the given RGB background color
    **/
    public static String bgRgb(int r, int g, int b){
        return (
            BG_RGB_PREFIX +
            cleanRgb(r, "bgRgb", "red") + SEMICOLON +
            cleanRgb(g, "bgRgb", "green") + SEMICOLON +
            cleanRgb(b, "bgRgb", "blue") + SUFFIX
        );
    }
    /**
    * Returns an underline RGB escape code ({@code ESC[58:2::<r>:<g>:<b>m}).
    *
    * <p>If any of the given values are out of range (0–255), a warning is printed to {@code System.err}, and the default value is used.</p>
    *
    * <p>
    *   Uses the colon form with an empty color-space id. Sets only the underline color;
    *   it does not enable underlining. Combine it with {@link #UNDERLINE} or another
    *   underline style, and use {@link #UNDERLINE_COLOR_DEFAULT} to reset it. Support is terminal-dependent.
    * </p>
    *
    * @param r the red channel (0–255)
    * @param g the green channel (0–255)
    * @param b the blue channel (0–255)
    * @return the ANSI escape string for the given RGB underline color
    **/
    public static String underlineRgb(int r, int g, int b){
        return (
            UNDERLINE_RGB_PREFIX +
            cleanRgb(r, "underlineRgb", "red") + COLON +
            cleanRgb(g, "underlineRgb", "green") + COLON +
            cleanRgb(b, "underlineRgb", "blue") + SUFFIX
        );
    }
    /**
    * Returns a foreground RGB escape code from a packed 24-bit {@code 0xRRGGBB} value.
    *
    * <p>
    *   Bits 16–23 hold red, bits 8–15 green and bits 0–7 blue; bits 24–31 must be zero.
    *   The value is not ARGB, and alpha is not supported. For example,
    *   {@code fgRgb(0xFF0000)} returns {@code ESC[38;2;255;0;0m}, the same as
    *   {@link #fgRgb(int, int, int) fgRgb(255, 0, 0)}.
    * </p>
    *
    * <p>
    *   If the value is out of range ({@code 0x000000}–{@code 0xFFFFFF}), exactly one warning
    *   is printed to {@code System.err}, and the default color (180, 180, 180) is used.
    *   True color rendering is terminal-dependent.
    * </p>
    *
    * @param rgb the packed RGB value ({@code 0x000000}–{@code 0xFFFFFF})
    * @return the ANSI escape string for the given RGB foreground color
    **/
    public static String fgRgb(int rgb){
        int cleaned = cleanPackedRgb(rgb, "fgRgb");
        return fgRgb(
            ((cleaned >> 16) & 0xFF),
            ((cleaned >> 8) & 0xFF),
            (cleaned & 0xFF)
        );
    }
    /**
    * Returns a background RGB escape code from a packed 24-bit {@code 0xRRGGBB} value.
    *
    * <p>
    *   Bits 16–23 hold red, bits 8–15 green and bits 0–7 blue; bits 24–31 must be zero.
    *   The value is not ARGB, and alpha is not supported. For example,
    *   {@code bgRgb(0x020A6D)} returns {@code ESC[48;2;2;10;109m}, the same as
    *   {@link #bgRgb(int, int, int) bgRgb(2, 10, 109)}.
    * </p>
    *
    * <p>
    *   If the value is out of range ({@code 0x000000}–{@code 0xFFFFFF}), exactly one warning
    *   is printed to {@code System.err}, and the default color (180, 180, 180) is used.
    *   True color rendering is terminal-dependent.
    * </p>
    *
    * @param rgb the packed RGB value ({@code 0x000000}–{@code 0xFFFFFF})
    * @return the ANSI escape string for the given RGB background color
    **/
    public static String bgRgb(int rgb){
        int cleaned = cleanPackedRgb(rgb, "bgRgb");
        return bgRgb(
            ((cleaned >> 16) & 0xFF),
            ((cleaned >> 8) & 0xFF),
            (cleaned & 0xFF)
        );
    }
    /**
    * Returns an underline RGB escape code from a packed 24-bit {@code 0xRRGGBB} value.
    *
    * <p>
    *   Bits 16–23 hold red, bits 8–15 green and bits 0–7 blue; bits 24–31 must be zero.
    *   The value is not ARGB, and alpha is not supported. For example,
    *   {@code underlineRgb(0xFFA500)} returns {@code ESC[58:2::255:165:0m}, the same as
    *   {@link #underlineRgb(int, int, int) underlineRgb(255, 165, 0)}.
    * </p>
    *
    * <p>
    *   If the value is out of range ({@code 0x000000}–{@code 0xFFFFFF}), exactly one warning
    *   identifying {@code underlineRgb} is printed to {@code System.err}, and the default
    *   color (180, 180, 180) is used. Nothing is printed to {@code System.out}.
    * </p>
    *
    * <p>
    *   Uses the colon form with an empty color-space id. Sets only the underline color;
    *   it does not enable underlining. Combine it with {@link #UNDERLINE} or another
    *   underline style, and use {@link #UNDERLINE_COLOR_DEFAULT} to reset it.
    *   True color underline rendering is terminal-dependent.
    * </p>
    *
    * @param rgb the packed RGB value ({@code 0x000000}–{@code 0xFFFFFF})
    * @return the ANSI escape string for the given RGB underline color
    **/
    public static String underlineRgb(int rgb){
        int cleaned = cleanPackedRgb(rgb, "underlineRgb");
        return underlineRgb(
            ((cleaned >> 16) & 0xFF),
            ((cleaned >> 8) & 0xFF),
            (cleaned & 0xFF)
        );
    }
    /**
    * Returns a foreground RGB escape code from a CSS-style hex color.
    *
    * <p>
    *   Accepted formats are {@code #RGB}, {@code #RGBA}, {@code #RRGGBB} and {@code #RRGGBBAA}.
    *   The leading {@code #} is required, digits are case-insensitive, and whitespace is not
    *   trimmed. Short forms are expanded by digit duplication ({@code #F00} is {@code #FF0000}).
    * </p>
    *
    * <p>
    *   Alpha digits must be valid hex digits but are otherwise ignored: the result is always
    *   the opaque RGB color, and no transparency or alpha compositing is applied. For example,
    *   {@code fgHex("#F008")} returns {@code ESC[38;2;255;0;0m}, the same as
    *   {@link #fgRgb(int, int, int) fgRgb(255, 0, 0)}.
    * </p>
    *
    * <p>
    *   If the input is {@code null} or not in an accepted format, exactly one warning is printed
    *   to {@code System.err}, and the default color (180, 180, 180) is used.
    *   True color rendering is terminal-dependent.
    * </p>
    *
    * @param hex the hex color, such as {@code "#FF0000"}
    * @return the ANSI escape string for the given RGB foreground color
    **/
    public static String fgHex(String hex){
        return fgRgb(cleanHex(hex, "fgHex"));
    }
    /**
    * Returns a background RGB escape code from a CSS-style hex color.
    *
    * <p>
    *   Accepted formats are {@code #RGB}, {@code #RGBA}, {@code #RRGGBB} and {@code #RRGGBBAA}.
    *   The leading {@code #} is required, digits are case-insensitive, and whitespace is not
    *   trimmed. Short forms are expanded by digit duplication ({@code #F00} is {@code #FF0000}).
    * </p>
    *
    * <p>
    *   Alpha digits must be valid hex digits but are otherwise ignored: the result is always
    *   the opaque RGB color, and no transparency or alpha compositing is applied. For example,
    *   {@code bgHex("#020A6D40")} returns {@code ESC[48;2;2;10;109m}, the same as
    *   {@link #bgRgb(int, int, int) bgRgb(2, 10, 109)}.
    * </p>
    *
    * <p>
    *   If the input is {@code null} or not in an accepted format, exactly one warning is printed
    *   to {@code System.err}, and the default color (180, 180, 180) is used.
    *   True color rendering is terminal-dependent.
    * </p>
    *
    * @param hex the hex color, such as {@code "#FF0000"}
    * @return the ANSI escape string for the given RGB background color
    **/
    public static String bgHex(String hex){
        return bgRgb(cleanHex(hex, "bgHex"));
    }
    /**
    * Returns an underline RGB escape code from a CSS-style hex color.
    *
    * <p>
    *   Accepted formats are {@code #RGB}, {@code #RGBA}, {@code #RRGGBB} and {@code #RRGGBBAA}.
    *   The leading {@code #} is required, digits are case-insensitive, and whitespace is not
    *   trimmed. Short forms are expanded by digit duplication ({@code #F00} is {@code #FF0000}).
    * </p>
    *
    * <p>
    *   Alpha digits must be valid hex digits but are otherwise ignored: the result is always
    *   the opaque RGB color, and no transparency or alpha compositing is applied. For example,
    *   {@code underlineHex("#F008")} returns {@code ESC[58:2::255:0:0m}, the same as
    *   {@link #underlineRgb(int, int, int) underlineRgb(255, 0, 0)}.
    * </p>
    *
    * <p>
    *   If the input is {@code null} or not in an accepted format, exactly one warning identifying
    *   {@code underlineHex} is printed to {@code System.err}, and the default color (180, 180, 180)
    *   is used. Control characters and Unicode line and paragraph separators are escaped to keep
    *   the warning on one line. Nothing is printed to {@code System.out}.
    * </p>
    *
    * <p>
    *   Uses the colon form with an empty color-space id. Sets only the underline color;
    *   it does not enable underlining. Combine it with an underline style, for example
    *   {@code Ansi.UNDERLINE + Ansi.underlineHex("#FF0000")}, and use
    *   {@link #UNDERLINE_COLOR_DEFAULT} to reset it. True color underline rendering is terminal-dependent.
    * </p>
    *
    * @param hex the hex color, such as {@code "#FF0000"}
    * @return the ANSI escape string for the given RGB underline color
    **/
    public static String underlineHex(String hex){
        return underlineRgb(cleanHex(hex, "underlineHex"));
    }
    /**
    * Wraps the given text with the given style and appends {@link #RESET}.
    *
    * @param style the ANSI style string, or {@code null} for no style
    * @param text the text to wrap, or {@code null} for empty string
    * @return the styled string ending with {@link #RESET}
    **/
    public static String wrap(String style, String text){
        return (((style != null)? style : "") + ((text != null)? text : "") + RESET);
    }
    /**
    * Wraps the string representation of the given value with the given style.
    * A {@code null} value is treated as an empty string, consistent with
    * {@link #wrap(String, String)}.
    *
    * @param style the ANSI style string, or {@code null} for no style
    * @param value the value to wrap, or {@code null} for empty string
    * @return the styled string ending with {@link #RESET}
    **/
    public static String wrap(String style, Object value){
        return wrap(style, ((value != null)? String.valueOf(value) : null));
    }
    /**
    * Removes recognized ESC-based ANSI SGR (Select Graphic Rendition) escape sequences from the given text.
    *
    * <p>
    *   An SGR sequence is {@code ESC [}, optional numeric parameters separated by
    *   {@code ;} or {@code :}, and the final byte {@code m}. This covers resets
    *   (including the empty form {@code ESC[m}), text attributes, standard, bright,
    *   256-color and RGB colors, as produced by this class or by other tools.
    * </p>
    *
    * <p>Characters outside matched substrings are preserved unchanged. The following are not matched by themselves:</p>
    * <ul>
    *   <li>Ordinary characters, Unicode text, spaces, tabs and line endings.</li>
    *   <li>Non-SGR control sequences, such as cursor movement ({@code ESC[2A}), screen clearing ({@code ESC[2J}) and OSC sequences ({@code ESC]...}).</li>
    *   <li>Sequences with private or non-numeric parameters, such as {@code ESC[?25l} or {@code ESC[>4;1m}.</li>
    *   <li>Incomplete or malformed sequences, such as {@code ESC[31} without a final {@code m}.</li>
    * </ul>
    *
    * <p>
    *   Removal is a single pass over the original input. Removing a sequence can join
    *   the fragments around it into a new, complete SGR sequence, which is kept. For
    *   example, {@code ESC[} + {@code ESC[0m} + {@code 31mHello} becomes
    *   {@code ESC[31mHello}; calling this method again would then remove it. The method
    *   is therefore not idempotent for such malformed or overlapping fragments.
    * </p>
    *
    * <p>
    *   This method does not parse surrounding terminal-control structures, so
    *   SGR-shaped matches inside OSC or other payloads may also be removed. The
    *   preservation of every non-SGR control sequence and its payload is therefore
    *   not guaranteed.
    * </p>
    *
    * <p>
    *   Limitations: this is not a full ANSI parser or a security sanitizer, and its
    *   output may still contain escape sequences. The 8-bit CSI form ({@code U+009B})
    *   is not recognized.
    * </p>
    *
    * @param text the text to clean, or {@code null} for empty string
    * @return the text with all matching SGR sequences removed, never {@code null}
    **/
    public static String stripSgr(String text){
        return ((text != null)? SGR_PATTERN.matcher(text).replaceAll("") : "");
    }
    /**
    * Removes SGR escape sequences from the string representation of the given value.
    * A {@code null} value is treated as an empty string, consistent with
    * {@link #stripSgr(String)}.
    *
    * @param value the value to clean, or {@code null} for empty string
    * @return the text with all matching SGR sequences removed, never {@code null}
    **/
    public static String stripSgr(Object value){
        return stripSgr((value != null)? String.valueOf(value) : null);
    }
    /**
    * Returns {@code true} if the given text contains at least one SGR sequence.
    *
    * <p>
    *   Uses exactly the same matching rules as {@link #stripSgr(String)}: {@code ESC [},
    *   optional numeric parameters separated by {@code ;} or {@code :}, and the final byte
    *   {@code m}. Consequently, for non-null {@code s}, {@code containsSgr(s)}
    *   is {@code true} exactly when {@code stripSgr(s)} differs from {@code s}.
    *   For {@code null}, this method returns {@code false}.
    * </p>
    *
    * <p>
    *   A match is purely syntactic: it does not mean a terminal will interpret or render the
    *   sequence. Other control sequences (cursor movement, OSC, the 8-bit CSI form) are not
    *   detected, and an SGR-shaped substring inside a larger control sequence also matches.
    *   This is not a full ANSI parser or a security sanitizer.
    * </p>
    *
    * @param text the text to inspect, or {@code null}
    * @return {@code true} if a matching SGR sequence is found; {@code false} for {@code null}, empty text or text without one
    **/
    public static boolean containsSgr(String text){
        return ((text != null) && SGR_PATTERN.matcher(text).find());
    }
    /**
    * Returns whether the string representation of the given value contains
    * at least one SGR sequence matching {@link #containsSgr(String)}.
    *
    * <p>A {@code null} value returns {@code false}.</p>
    *
    * @param value the value to inspect, or {@code null}
    * @return {@code true} if a matching SGR sequence is found, otherwise {@code false}
    **/
    public static boolean containsSgr(Object value){
        return containsSgr((value != null)? String.valueOf(value) : null);
    }
    /**
    * Returns {@code true} if the given code is a valid 256-color index.
    *
    * @param code the code to validate
    * @return {@code true} if {@code code} is in range 0–255, otherwise {@code false}
    **/
    public static boolean isValid256(int code){
        return (code >= MIN_256 && code <= MAX_256);
    }
    /**
    * Returns {@code true} if the given value is a valid RGB channel value.
    *
    * @param value the value to validate
    * @return {@code true} if {@code value} is in range 0–255, otherwise {@code false}
    **/
    public static boolean isValidRgbChannel(int value){
        return (value >= MIN_RGB && value <= MAX_RGB);
    }
    /**
    * Returns the nearest 256-color index (16–255) for the given RGB color.
    *
    * <p>
    *   Only the 6×6×6 RGB cube (16–231) and the grayscale ramp (232–255) are searched; the
    *   system colors 0–15 are excluded because terminals may configure them. The distance is
    *   the squared Euclidean distance in RGB space, not a perceptual color distance. If
    *   several indices are equally near, the smallest index is returned. For example,
    *   {@code rgbTo256(255, 0, 0)} returns {@code 196} and {@code rgbTo256(4, 4, 4)} returns
    *   {@code 16}.
    * </p>
    *
    * <p>
    *   The conversion is lossy: many RGB colors map to the same index, and converting back
    *   with {@link #color256ToRgb(int)} does not in general recover the original color. For
    *   every index from 16 to 255, converting the result of {@link #color256ToRgb(int)} back
    *   with this method returns the same index.
    * </p>
    *
    * <p>If any of the given values are out of range (0–255), a warning is printed to {@code System.err}, and the default value is used.</p>
    *
    * @param r the red channel (0–255)
    * @param g the green channel (0–255)
    * @param b the blue channel (0–255)
    * @return the nearest 256-color index, from 16 to 255
    **/
    public static int rgbTo256(int r, int g, int b){
        int cleanedRed = cleanRgb(r, "rgbTo256", "red");
        int cleanedGreen = cleanRgb(g, "rgbTo256", "green");
        int cleanedBlue = cleanRgb(b, "rgbTo256", "blue");
        int bestIndex = CUBE_START;
        int bestDistance = Integer.MAX_VALUE;
        for(int index = CUBE_START; index <= MAX_256 && bestDistance > 0; index++){
            int rgb = paletteRgb(index);
            int distanceRed = (cleanedRed - ((rgb >> 16) & 0xFF));
            int distanceGreen = (cleanedGreen - ((rgb >> 8) & 0xFF));
            int distanceBlue = (cleanedBlue - (rgb & 0xFF));
            int distance = (distanceRed * distanceRed + distanceGreen * distanceGreen + distanceBlue * distanceBlue);
            if(distance < bestDistance){
                bestIndex = index;
                bestDistance = distance;
            }
        }
        return bestIndex;
    }
    /**
    * Returns the reference packed RGB value ({@code 0xRRGGBB}) of a 256-color index.
    *
    * <ul>
    *   <li>
    *       0–15: fixed reference system colors (for example 1 is {@code 0x800000} and 9 is {@code 0xFF0000}).
    *       Terminals may configure these colors, so the result is not guaranteed to match the displayed color.
    *   </li>
    *   <li>
    *       16–231: the 6×6×6 RGB cube, {@code index = 16 + 36*r + 6*g + b} with channel levels 0, 95, 135, 175, 215
    *       and 255 (for example 196 is {@code 0xFF0000}).
    *   </li>
    *   <li>232–255: grayscale, with every channel {@code 8 + 10*(index - 232)} (for example 244 is {@code 0x808080}).</li>
    * </ul>
    *
    * <p>
    *   The result never contains an alpha channel and can be passed to
    *   {@link #fgRgb(int)}, {@link #bgRgb(int)} or {@link #underlineRgb(int)}.
    * </p>
    *
    * <p>If the given index is out of range (0–255), a warning is printed to {@code System.err}, and the default code is used.</p>
    *
    * @param index the 256-color index (0–255)
    * @return the reference packed RGB value, from {@code 0x000000} to {@code 0xFFFFFF}
    **/
    public static int color256ToRgb(int index){
        return paletteRgb(clean256(index, "color256ToRgb"));
    }
    // -- Private Helper Methods: --------------------------------------------------
    /**
    * Validates {@code code} against the valid 256-color range (0–255).
    *
    * <p>
    *   If out of range, a warning is printed to {@code System.err},
    *   and {@link #DEFAULT_256} is returned as a fallback.
    * </p>
    *
    * @param code the code to validate
    * @param callerName the calling method name, for the warning message
    * @return a valid 256-color index
    **/
    private static int clean256(int code, String callerName){
        if(code < MIN_256 || code > MAX_256){
            System.err.println(
                "Ansi." + callerName + ": code (" + code + ") is out of range (0-255)" +
                " -> returning DEFAULT_256=" + DEFAULT_256
            );
            return DEFAULT_256;
        }
        return code;
    }
    /**
    * Validates {@code value} against the valid RGB channel range (0–255).
    *
    * <p>
    *   If out of range, a warning is printed to {@code System.err},
    *   and {@link #DEFAULT_RGB} is returned as a fallback.
    * </p>
    *
    * @param value the channel value to validate
    * @param callerName the calling method name, for the warning message
    * @param channel the channel name (red/green/blue), for the warning message
    * @return a valid RGB channel value
    **/
    private static int cleanRgb(int value, String callerName, String channel){
        if(value < MIN_RGB || value > MAX_RGB){
            System.err.println(
                "Ansi." + callerName + ": " + channel + " channel value (" + value + ") is out of range (0-255)" +
                " -> returning DEFAULT_RGB=" + DEFAULT_RGB
            );
            return DEFAULT_RGB;
        }
        return value;
    }
    /**
    * Validates {@code rgb} against the valid packed RGB range ({@code 0x000000}–{@code 0xFFFFFF}).
    *
    * <p>
    *   If out of range, a warning is printed to {@code System.err},
    *   and {@link #DEFAULT_PACKED_RGB} is returned as a fallback.
    * </p>
    *
    * @param rgb the packed RGB value to validate
    * @param callerName the calling method name, for the warning message
    * @return a valid packed RGB value
    **/
    private static int cleanPackedRgb(int rgb, String callerName){
        if(rgb < 0 || rgb > MAX_PACKED_RGB){
            System.err.println(
                "Ansi." + callerName + ": packed RGB value (0x" + Integer.toHexString(rgb).toUpperCase() + ")" +
                " is out of range (0x000000-0xFFFFFF)" +
                " -> returning DEFAULT_RGB=(" + DEFAULT_RGB + ", " + DEFAULT_RGB + ", " + DEFAULT_RGB + ")"
            );
            return DEFAULT_PACKED_RGB;
        }
        return rgb;
    }
    /**
    * Parses a {@code #RGB}, {@code #RGBA}, {@code #RRGGBB} or {@code #RRGGBBAA} hex color.
    *
    * <p>
    *   Alpha digits are validated but ignored. If the input is invalid, a warning is printed
    *   to {@code System.err}, and {@link #DEFAULT_PACKED_RGB} is returned as a fallback.
    * </p>
    *
    * @param hex the hex color to parse
    * @param callerName the calling method name, for the warning message
    * @return a valid packed RGB value
    **/
    private static int cleanHex(String hex, String callerName){
        int length = ((hex != null)? hex.length() : 0);
        if((length == 4 || length == 5 || length == 7 || length == 9) && hex.charAt(0) == '#'){
            boolean shortForm = (length <= 5);
            int rgb = 0;
            int index = 1;
            for(; index < length; index++){
                int digit = hexDigit(hex.charAt(index));
                if(digit < 0)
                    break;
                if(shortForm && index <= 3)
                    rgb = ((rgb << 8) | (digit << 4) | digit);
                else if(!shortForm && index <= 6)
                    rgb = ((rgb << 4) | digit);
            }
            if(index == length)
                return rgb;
        }
        System.err.println(
            "Ansi." + callerName + ": hex value (" + quoteForWarning(hex) + ")" +
            " is not a valid #RGB, #RGBA, #RRGGBB or #RRGGBBAA color" +
            " -> returning DEFAULT_RGB=(" + DEFAULT_RGB + ", " + DEFAULT_RGB + ", " + DEFAULT_RGB + ")"
        );
        return DEFAULT_PACKED_RGB;
    }
    /**
    * Returns the value of an ASCII hex digit, or {@code -1} if {@code character} is not one.
    *
    * @param character the character to convert
    * @return the digit value (0–15), or {@code -1}
    **/
    private static int hexDigit(char character){
        if(character >= '0' && character <= '9')
            return (character - '0');
        if(character >= 'a' && character <= 'f')
            return (character - 'a' + 10);
        if(character >= 'A' && character <= 'F')
            return (character - 'A' + 10);
        return -1;
    }
    /**
    * Returns {@code text} in double quotes for a warning message, or {@code null} if it is null.
    *
    * <p>
    *   ISO control characters (U+0000–U+001F and U+007F–U+009F) and the line and paragraph
    *   separators (U+2028, U+2029) are written as {@code \}{@code uXXXX}, so that a warning
    *   always stays on one line and cannot carry escape sequences.
    * </p>
    *
    * @param text the text to quote
    * @return the quoted text, or {@code "null"}
    **/
    private static String quoteForWarning(String text){
        if(text == null)
            return "null";
        StringBuilder quoted = new StringBuilder(text.length() + 2).append('"');
        for(int index = 0; index < text.length(); index++){
            char character = text.charAt(index);
            if(Character.isISOControl(character) || character == '\u2028' || character == '\u2029')
                quoted.append(String.format("\\u%04X", (int) character));
            else
                quoted.append(character);
        }
        return quoted.append('"').toString();
    }
    /**
    * Returns the reference packed RGB value of a valid 256-color index.
    *
    * <p>The single source of the palette used by {@link #color256ToRgb(int)} and {@link #rgbTo256(int, int, int)}.</p>
    *
    * @param index a 256-color index, already validated (0–255)
    * @return the reference packed RGB value
    **/
    private static int paletteRgb(int index){
        if(index < CUBE_START)
            return SYSTEM_COLORS_RGB[index];
        if(index < GRAY_START){
            int position = (index - CUBE_START);
            return ((CUBE_LEVELS[position / 36] << 16) | (CUBE_LEVELS[(position / 6) % 6] << 8) | CUBE_LEVELS[position % 6]);
        }
        int gray = (8 + 10 * (index - GRAY_START));
        return ((gray << 16) | (gray << 8) | gray);
    }
}
