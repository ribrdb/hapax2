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

import hapax.Modifiers;
import hapax.TemplateDataDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;

import java.io.PrintWriter;
import java.util.List;

/**
 * Represents a node whose output is defined by a value from the
 * TemplateDataDictionary.
 *
 * This supports both {{PLAIN}} variables as well as one with {{MODIFERS:j}}.
 * The modifiers themselves are implemented in {@link Modifiers}.
 *
 * @author dcoker
 * @author jdp
 */
public final class VariableNode
    extends TemplateNode
{

    private final String variable;

    private final List<Modifiers.FLAGS> modifiers;


    VariableNode(int lno, String spec) {
        this(lno, spec.split(":"));
    }
    private VariableNode(int lno, String[] spec) {
        this(lno,spec[0],Modifiers.parseModifiers(spec));
    }
    private VariableNode(int lno, String variable, List<Modifiers.FLAGS> modifiers) {
        super(lno);
        this.variable = variable;
        this.modifiers = modifiers;
    }


    @Override
    public void evaluate(TemplateDataDictionary dict, TemplateLoader context,
                         PrintWriter out)
    {
        String t = dict.getVariable(variable);
        if (null == t)
            return;
        else if (this.modifiers.isEmpty())
            out.write(t);
        else {
            t = Modifiers.applyModifiers(t, this.modifiers);
            out.write(t);
        }
    }

}
