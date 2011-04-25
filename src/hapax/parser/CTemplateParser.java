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

import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import static hapax.parser.TemplateNode.TemplateType.*;

import java.text.MessageFormat;

import java.util.List;

/**
 * This parser turns strings containing the contents of a template into a list
 * of TemplateNodes.
 * 
 * @author dcoker
 * @author jdp
 */
public final class CTemplateParser
    extends Object
    implements TemplateParser
{
    /**
     * Shared stateless C template parser.
     */
    public final static TemplateParser Instance = new CTemplateParser();
    /**
     * Parse and render a C template.
     */
    public final static String Eval(TemplateLoader context, TemplateDataDictionary dict, String sourcecode)
        throws TemplateException
    {
        Template template = new Template(Instance,sourcecode,context);
        return template.renderToString(dict);
    }

    final static List<TemplateNode> Parse(TemplateLoader context, String template)
        throws TemplateParserException
    {
        return Instance.parse(context,template);
    }


    private enum NODE_TYPE {
        OPEN_SECTION, CLOSE_SECTION, VARIABLE, TEXT_NODE, INCLUDE_SECTION, COMMENT, END_INPUT ;
    }



    public CTemplateParser() {
        super();
    }


    private static NODE_TYPE next(ParserReader input) {

        int inlen = input.length();
        switch (inlen){
        case 0:
            return NODE_TYPE.END_INPUT;
        case 1:
        case 2:
        case 3:
        case 4:
            return NODE_TYPE.TEXT_NODE;
        default:
            if ('{' == input.charAt(0) && '{' == input.charAt(1)){

                switch (input.charAt(2)){
                case '{':
                    do {
                        input.next();
                    }
                    while ('{' == input.charAtTest(2));

                    return NODE_TYPE.TEXT_NODE;
                case '#':
                    return NODE_TYPE.OPEN_SECTION;
                case '/':
                    return NODE_TYPE.CLOSE_SECTION;
                case '>':
                    return NODE_TYPE.INCLUDE_SECTION;
                case '=':
                    return NODE_TYPE.VARIABLE;
                case '!':
                    return NODE_TYPE.COMMENT;
                default:
                    return NODE_TYPE.VARIABLE;
                }
            }
            return NODE_TYPE.TEXT_NODE;
        }
    }

    public List<TemplateNode> parse(TemplateLoader context, String template)
        throws TemplateParserException
    {
        List<TemplateNode> list = new java.util.ArrayList<TemplateNode>();
        ParserReader input = new ParserReader(template);
        TemplateNode node = null;
        while (true) {
            switch (next(input)) {
            case OPEN_SECTION:
                node = parseOpenSection(input);
                break;
            case CLOSE_SECTION:
                node = parseCloseSection(input);
                break;
            case VARIABLE:
                node = parseVariable(input);
                break;
            case TEXT_NODE:
                node = parseTextNode(input);
                break;
            case INCLUDE_SECTION:
                node = parseInclude(input);
                break;
            case COMMENT:
                node = parseComment(input);
                break;

            case END_INPUT:
                return this.close(list);

            default:
                throw new RuntimeException("Internal error parsing template.");
            }
            if (null != node)
                list.add(node);
        }
    }

    /**
     * Terminal scan
     */
    private static List<TemplateNode> close(List<TemplateNode> template)
        throws TemplateParserException
    {

        for (int cc = 0, count = template.size(); cc < count; cc++){

            TemplateNode node = template.get(cc);

            node.ofs = cc;

            switch (node.getTemplateType()){

            case TemplateTypeSection:{

                SectionNode section = (SectionNode)node;

                if (section.isOpenSectionTag())
                    section.indexOfClose = IndexOfClose(template,cc,section);
                else
                    section.indexOfClose = Integer.MAX_VALUE;//(flypaper)
            }
                break;

            default:
                break;
            }
        }

        return template;
    }

    private static TemplateNode parseTextNode(ParserReader input) {
        int lno = input.lineNumber();
        int next_braces = input.indexOf("{{");
        if (next_braces == -1) { // no more parser syntax

            String text = input.truncate();

            return (new TextNode(lno,text));
        }
        else {
            String text = input.delete(0, next_braces);
            if (text.length() > 0)
                return (new TextNode(lno,text));
            else
                return null;
        }
    }

    private static TemplateNode parseInclude(ParserReader input)
        throws TemplateParserException
    {
        int lno = input.lineNumber();
        String indentation = input.getIndentation();
        String consumed = parseClose(input);
        String token = consumed.substring(3,consumed.length()-2).trim();
        return (new IncludeNode(lno,token,indentation));
    }
    private static TemplateNode parseVariable(ParserReader input)
        throws TemplateParserException
    {
        int lno = input.lineNumber();
        String token;
        String consumed = parseClose(input);
        if ('=' == consumed.charAt(2))
            token = consumed.substring(3,consumed.length()-2).trim();
        else
            token = consumed.substring(2,consumed.length()-2).trim();
        return (new VariableNode(lno,token));
    }
    private static TemplateNode parseCloseSection(ParserReader input)
        throws TemplateParserException
    {
        int lno = input.lineNumber();
        String consumed = parseClose(input);
        String token = consumed.substring(3,consumed.length()-2).trim();
        return (SectionNode.Close(lno,token));
    }
    private static TemplateNode parseOpenSection(ParserReader input)
        throws TemplateParserException
    {
        int lno = input.lineNumber();
        String consumed = parseClose(input);
        String token = consumed.substring(3,consumed.length()-2).trim();
        return (SectionNode.Open(lno,token));
    }
    private static TemplateNode parseComment(ParserReader input)
        throws TemplateParserException
    {
        int lno = input.lineNumber();
        String consumed = parseClose(input);
        String token = consumed.substring(3,consumed.length()-2).trim();
        return (new CommentNode(lno,token));
    }

    private static String parseClose(ParserReader input)
        throws TemplateParserException
    {
        int close_braces = input.indexOf("}}");
        if (-1 == close_braces)
            throw new TemplateParserException("Unexpected or malformed input: " + input+" at "+input.lineNumber());
        else {
            int end = close_braces+2;
            return input.delete(0, end);
        }
    }

    private final static int IndexOfClose(List<TemplateNode> template, int node_ofs, SectionNode node)
        throws TemplateParserException
    {
        int stack = IndexOfCloseStackInit, ofs = (node_ofs+1), length = template.size();
        for (; ofs < length; ofs++) {

            TemplateNode tp = template.get(ofs);

            if (TemplateTypeSection == tp.getTemplateType()) {

                SectionNode section = (SectionNode) tp;

                if (section.isOpenSectionTag())

                    stack++;

                else if (IndexOfCloseStackInit == stack){

                    if (section.getSectionName().equals(node.getSectionName()))

                        return ofs;

                    else {

                        String msg = MessageFormat.format("Mismatched close tag: expecting a close tag for \"{0}\", but got close tag for \"{1}\" at line {2}.", 
                                                          node.getSectionName(),
                                                          section.getSectionName(),
                                                          section.lineNumber);
                        throw new TemplateParserException(msg);
                    }
                }
                else
                    stack--;
            }
        }
        return -1;
    }

    private final static int IndexOfCloseStackInit = 0;
}
