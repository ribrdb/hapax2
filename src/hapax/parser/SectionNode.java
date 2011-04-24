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

import hapax.TemplateDataDictionary;
import hapax.TemplateLoader;

import java.io.PrintWriter;

/**
 * Implementation of a {{#SECTION_NODE}} and the paired {{/SECTION_NODE}}.
 *
 * @author dcoker
 */
public final class SectionNode 
    extends TemplateNode
    implements TemplateNode.Section
{

    static SectionNode Open(int lno, String nodeName) {
        return new SectionNode(lno, nodeName, TYPE.OPEN);
    }
    static SectionNode Close(int lno, String nodeName) {
        return new SectionNode(lno, nodeName, TYPE.CLOSE);
    }

    enum TYPE {
        OPEN, CLOSE;
    }


    private final String sectionName_;
    private final TYPE type_;

    volatile int indexOfClose = -1;


    private SectionNode(int lno, String nodeName, TYPE node_type) {
        super(lno);
        this.sectionName_ = nodeName;
        this.type_ = node_type;
    }


    public final TemplateType getTemplateType(){
        return TemplateType.TemplateTypeSection;
    }

    public String getSectionName() {
        return sectionName_;
    }

    @Override
    public void evaluate(TemplateDataDictionary dict, TemplateLoader context,
                         PrintWriter collector)
    {
        // do nothing
    }

    public boolean isOpenSectionTag() {
        return type_ == TYPE.OPEN;
    }

    public boolean isCloseSectionTag() {
        return type_ == TYPE.CLOSE;
    }

    public int getIndexOfClose(){
        return this.indexOfClose;
    }
    public int getIndexOfCloseRelative(){
        int close = this.indexOfClose;
        int open = this.ofs;
        return (close-open);
    }
}
