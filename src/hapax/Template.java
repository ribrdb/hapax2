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

import hapax.parser.CTemplateParser;
import hapax.parser.SectionNode;
import hapax.parser.TemplateNode;
import static hapax.parser.TemplateNode.TemplateType.*;
import hapax.parser.TemplateParser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;

/**
 * Template executes the program defined by the tmpl_ list.  The list itself is
 * constructed by implementations of {@link TemplateParser}.
 *
 * Instead of constructing a Template directly, use an implementation of {@link
 * TemplateLoader} such as {@link TemplateCache}.
 *
 * @author dcoker
 * @author jdp
 */
public final class Template 
    extends Object
{
    /**
     * Parse and render a C template.
     */
    public final static String Eval(TemplateLoader context, TemplateDataDictionary dict, String source)
        throws TemplateException
    {
        Template template = new Template(CTemplateParser.Instance,source,context,"<string>");
        return template.renderToString(dict);
    }


    private final long lastModified;
    private final List<TemplateNode> template;
    private final TemplateLoader context;
    private final String resource;


    public Template(String template, TemplateLoader context, String resource)
        throws TemplateException
    {
        this(0L,template,context,resource);
    }
    public Template(long lastModified, String template, TemplateLoader context, String resource)
        throws TemplateException
    {
        this(lastModified, CTemplateParser.Instance, template, context, resource);
    }
    public Template(TemplateParser parser, String template, TemplateLoader context, String resource)
        throws TemplateException
    {
        this(0L, parser, template, context, resource);
    }
    public Template(long lastModified, TemplateParser parser, String template, TemplateLoader context, String resource)
        throws TemplateException
    {
        this(lastModified, parser.parse(context,template), context, resource);
    }
    private Template(long lastModified, List<TemplateNode> tmpl, TemplateLoader context, String resource) {
        super();
        this.lastModified = lastModified;
        this.template = tmpl;
        this.context = context;
        this.resource = resource;
    }


    public boolean hasLastModified(){
        return (0L < this.lastModified);
    }
    public long getLastModified(){
        return this.lastModified;
    }

    public void render(TemplateDataDictionary dict, PrintWriter writer)
        throws TemplateException
    {
        try {
            this.render(Top, this.template, dict, writer);
        }
        finally {
            dict.renderComplete();
        }
    }
    public String renderToString(TemplateDataDictionary dict)
        throws TemplateException
    {
        try {
            StringWriter buffer = new StringWriter();

            this.render(Top, this.template, dict, (new PrintWriter(buffer)));

            return buffer.toString();
        }
        finally {
            dict.renderComplete();
        }
    }

    private void render(int offset, List<TemplateNode> template, TemplateDataDictionary dict, PrintWriter writer)
        throws TemplateException
    {
        if (Top == offset && template == this.template && dict.debugAnnotationsEnabled()) {
            writer.write("{{#FILE=");
            writer.write(resource);
            writer.write("}}");
        }
        for (int position = 0, count = template.size(); position < count; position++) {

            TemplateNode node = template.get(position);

            switch (node.getTemplateType()){

            case TemplateTypeSection:

                position = this.renderSectionNode(offset, template, dict, position, ((SectionNode)node), writer);
                break;

            default:
                node.evaluate(dict, this.context, writer);
                break;
            }
        }
        if (Top == offset && template == this.template && dict.debugAnnotationsEnabled()) {
            writer.write("{{/FILE}}");
        }
    }
    private int renderSectionNode(int offset, List<TemplateNode> template, TemplateDataDictionary dict, int open,
                                  SectionNode section, PrintWriter writer)
        throws TemplateException
    {
        int next = (open + 1);
        int close = (open + section.getIndexOfCloseRelative());

        if (close >= next && close < template.size()){

            String sectionName = section.getSectionName();

            List<TemplateDataDictionary> data = dict.getSection(sectionName);

            if (null != data){

                if (data.size() == 0) {

                    Iterator.Define(dict,sectionName,0,1);
                    /*
                     * Once
                     */
                    if (dict.debugAnnotationsEnabled()) {
                        writer.write("{{#SEC=");
                        writer.write(sectionName);
                        writer.write("}}");
                    }
                    this.render(next, template.subList(next, close), dict, writer);
                    if (dict.debugAnnotationsEnabled()) {
                        writer.write("{{/SEC}}");
                    }
                }
                else {
                    /*
                     * Repeat
                     */
                    for (int cc = 0, count = data.size(); cc < count; cc++){

                        if (dict.debugAnnotationsEnabled()) {
                            writer.write("{{#SEC=");
                            writer.write(sectionName);
                            writer.write("}}");
                        }
                        TemplateDataDictionary child = data.get(cc);

                        Iterator.Define(child,sectionName,cc,count);

                        this.render(next, template.subList(next, close), child, writer);
                        if (dict.debugAnnotationsEnabled()) {
                            writer.write("{{/SEC}}");
                        }
                    }
                }
            } else if (dict.debugAnnotationsEnabled()) {
                writer.write("{{#SEC=");
                writer.write(section.getSectionName());
                writer.write("}}{{/SEC}}");
            }
            return close;
        }
        else
            throw new TemplateException("Missing close tag for section '" + section.getSectionName()+"' at line "+section.lineNumber+".");
    }

    private final static int Top = 0;
}
