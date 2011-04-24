/*
 * Hapax2
 * Copyright (c) 2007 Doug Coker
 * Copyright (c) 2009 John Pritchard
 * 
 * The MIT License
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hapax;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Static class that implements the methods used by the modifiers.
 *
 * @author dcoker
 * @author jdp
 */
public final class Modifiers {

    public enum FLAGS {
        H, // HTML
        X, // XML
        J, // JavaScript string literal
        U, // URL escaped (%xx)
        B, // turns \n into <br/>
    }

    public static String applyModifiers(String input, List<FLAGS> modifiers) {
        for (FLAGS modifier : modifiers) {
            switch (modifier) {
            case H:
                input = htmlEscape(input);
                break;
            case X:
                input = xmlEscape(input);
                break;
            case J:
                input = jsEscape(input);
                break;
            case U:
                input = urlEncode(input);
                break;
            case B:
                input = newlinesToBreaks(input);
            }
        }
        return input;
    }

    public static List<FLAGS> parseModifiers(String[] split) {
        List<FLAGS> list =
            new ArrayList<FLAGS>(10);
        for (int i = 1; i < split.length; i++) {
            list.add(FLAGS.valueOf(split[i].toUpperCase()));
        }
        return list;
    }

    /**
     * Given a string, returns a string suitable for use as a JavaScript string
     * literal.
     *
     * @param unescaped The value you wish to escape.
     *
     * @return The escaped string
     */
    public static String jsEscape(String unescaped) {
        final String also_safe = "_ ";
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < unescaped.length(); i++) {
            char ch = unescaped.charAt(i);
            if (Character.isLetterOrDigit(ch) || also_safe.indexOf(ch) != -1) {
                escaped.append(ch);
            } else if (ch < 256) {
                escaped.append("\\x").append(Integer.toHexString(ch));
            } else {
                escaped.append("\\u").append(String.format("%04x", (int) ch));
            }
        }
        return escaped.toString();
    }

    /**
     * Convert newline (LF) to X/HTML BR.
     */
    public static String newlinesToBreaks(String t) {
        return t.replaceAll("\n", "<br />");
    }

    /**
     * Given a string, returns a string suitable for inclusion as a URL
     * parameter.
     *
     * @param unescaped The value to escape.
     *
     * @return The escaped value.
     */
    private static String urlEncode(String unescaped) {
        try {
            return URLEncoder.encode(unescaped, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given a string, returns a string suitable for use in an XML file.
     *
     * @param unescaped The string to escape.
     *
     * @return The escaped string.
     */
    private static String xmlEscape(String unescaped) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < unescaped.length(); i++) {
            char ch = unescaped.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == ' ') {
                escaped.append(ch);
            } else {
                escaped.append("&#").append((int) ch).append(";");
            }
        }
        return escaped.toString();
    }

    /**
     * Given a string, returns a string suitable for use in an HTML document.
     *
     * @param unescaped The string to escape.
     *
     * @return The escaped string.
     */
    private static String htmlEscape(String unescaped) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < unescaped.length(); i++) {
            char ch = unescaped.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == ' ') {
                escaped.append(ch);
            } else if (ch == '&') {
                escaped.append("&amp;");
            } else if (ch == '"') {
                escaped.append("&quot;");
            } else if (ch == '\r' || ch == '\n' || ch == '\t') {
                escaped.append(ch);
            } else {
                escaped.append("&#").append((int) ch).append(";");
            }
        }
        return escaped.toString();
    }


    private Modifiers() {
        super();
    }
}
