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

import java.text.MessageFormat;

/**
 * Utilities for template file paths used by {@link TemplateCache} and
 * {@link hapax.parser.AbstractInclude parser include}.
 *
 * @author dcoker
 * @author jdp
 */
public final class Path {

    /**
     * Joins a set of path components into a single path separated by
     * '/'.  Prevents duplication of prefix path (base dir).  Excludes
     * the string ".." from any path component.
     *
     * @param components The components of the path
     * @return The components joined into a string, separated by
     *         slashes.  Runs of slashes are reduced to a single
     *         slash.  If present, leading slash on the initial
     *         component and trailing slash on the final component are
     *         preserved.
     */
    public static String toFile(String... components)
        throws TemplateException
    {
        StringBuilder path = new StringBuilder();
        int count = components.length;
        String el, ep;
        for (int i = 0; i < count; i++) {
            ep = path.toString();
            el = components[i];
            if (null != el && 0 != el.length()){
                if (el.contains("..")){
                    String msg = MessageFormat.format("illegal relative path expression {0}", el);
                    throw new TemplateException(msg);
                }
                else {
                    int epl = ep.length();
                    if (0 != epl){
                        /*
                         * Truncate the accumulated parent path if
                         * it's already present in the next argument,
                         * or when the next argument is a URI.
                         * 
                         * This permits an include to redirect to a
                         * URI.
                         */
                        int isuri = el.indexOf(':');
                        if (el.startsWith(ep) || -1 != isuri){
                            path.setLength(0);
                            path.append(el);
                        }
                        else {
                            int ept = (epl-1);
                            if (-1 < ept && '/' != path.charAt(ept) && '/' != el.charAt(0)){
                                path.append("/");
                            }
                            path.append(el);
                        }
                    }
                    else {
                        path.append(el);
                    }
                }
            }
        }
        String filename = path.toString();
        if (!filename.endsWith(".xtm")){
            path.append(".xtm");
            return path.toString();
        }
        else
            return filename;
    }

    private Path() {
        super();
    }

}
