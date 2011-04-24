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
import hapax.TemplateException;
import hapax.TemplateLoader;

import java.io.PrintWriter;

/**
 * All tokens in the template language are represented by instances of a
 * TemplateNode.
 *
 * @author dcoker
 * @author jdp
 */
public abstract class TemplateNode {

    public interface Section {

        public String getSectionName();
    }

    /**
     * Primary rendering types.
     * @see hapax.Template#render
     */
    public enum TemplateType {
        TemplateTypeSection,
        TemplateTypeNode
    }
 

    public final int lineNumber;

    volatile int ofs = -1;


    TemplateNode(int lno){
        super();
        this.lineNumber = lno;
    }


    public TemplateType getTemplateType(){
        return TemplateType.TemplateTypeNode;
    }
    public void evaluate(TemplateDataDictionary dict, TemplateLoader context,
                         PrintWriter collector) 
        throws TemplateException 
    {
    }

}
