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
package hapax.parser;

import hapax.Iterator;
import hapax.Modifiers;
import hapax.Path;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;

/**
 * Represents an <code>{{&gt;<i>name</i>}}</code> include section.
 *
 * @author dcoker
 * @author jdp
 */
public final class IncludeNode
    extends TemplateNode
    implements TemplateNode.Section
{

    private final String name;
    private final String indentation;

    final List<Modifiers.FLAGS> modifiers;


    IncludeNode(int lno, String spec, String indentation) {
        super(lno);
        String split[] = spec.split(":");
        this.name = split[0];
        this.indentation = indentation;
        this.modifiers = Modifiers.parseModifiersWithIndentation(split, indentation);
    }


    public String getSectionName(){
        return this.name;
    }
    @Override
    public final void evaluate(TemplateDataDictionary dict, TemplateLoader context, PrintWriter out)
        throws TemplateException
    {
        String sectionName = this.name;

        List<TemplateDataDictionary> section = dict.getSection(sectionName);

        if (null != section){

            String filename = this.resolveName(dict);

            Template template = context.getTemplate(filename);
            if (null != template){
                /*
                 * Modified rendering
                 */
                PrintWriter previous_printwriter = null;
                StringWriter sw = null;
                if (!this.modifiers.isEmpty()) {
                    previous_printwriter = out;
                    sw = new StringWriter();
                    out = new PrintWriter(sw);
                }

                if (section.size() == 0) {

                    Iterator.Define(dict,sectionName,0,1);
                    /*
                     * Once
                     */
                    template.render(dict, out);
                }
                else {
                    /*
                     * Repeat
                     */
                    for (int cc = 0, count = section.size(); cc < count; cc++){

                        TemplateDataDictionary child = section.get(cc);

                        Iterator.Define(child,sectionName,cc,count);

                        template.render(child, out);
                    }
                }

                /*
                 */
                if (previous_printwriter != null) {
                    String results = sw.toString();
                    out = previous_printwriter;
                    out.write(Modifiers.applyModifiersWithIndentation(results, this.modifiers, this.indentation));
                }
            }
        }
    }

    private String resolveName(TemplateDataDictionary dict)
        throws TemplateException
    {
        String name = this.name;
        /*
         * When it's quoted, it's protected from redirect
         */
        String basename = TrimQuotes(name);

        if (name == basename){

            String redirect = dict.getVariable(name);

            if (null != redirect && 0 != redirect.length())
                return redirect;
        }
        return basename;
    }

    private final static String TrimQuotes(String string){

        if ('"' == string.charAt(0)) {
            int stringLen = string.length();
            if ('"' == string.charAt(stringLen-1))
                string = string.substring(1,stringLen-1);
            else
                string = string.substring(1);
        } 
        return string;
    }
}
