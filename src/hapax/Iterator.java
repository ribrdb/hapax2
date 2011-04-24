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

/**
 * Section and include iterations (one or many) are evaluated with
 * special sections shown and hidden during their interpretation, as
 * defined here.
 * 
 * <dl>
 * 
 * <dt> <i>name</i><code>_it_First</code> </dt> <dd> A section only
 * visible in the first iteration. </dd>
 * 
 * <dt> <i>name</i><code>_it_NotFirst</code> </dt> <dd> A section only
 * visible after the first iteration. </dd>
 * 
 * <dt> <i>name</i><code>_it_Last</code> </dt> <dd> A section only
 * visible in the last iteration. </dd>
 * 
 * <dt> <i>name</i><code>_it_NotLast</code> </dt> <dd> A section only
 * visible before the first iteration. </dd>
 * 
 * <dt> <i>name</i><code>_it_Exclusive</code> </dt> <dd> A section only
 * visible after the first iteration and before the last
 * iteration. </dd>
 * 
 * </dl>
 * 
 * 
 * @author jdp
 */
public class Iterator 
    extends Object
{
    public final static class Suffix {
        public final static String First     = "_it_First";
        public final static String NotFirst  = "_it_NotFirst";
        public final static String Last      = "_it_Last";
        public final static String NotLast   = "_it_NotLast";
        public final static String Exclusive = "_it_Exclusive";
    }

    public final static void Define(TemplateDataDictionary dict, String sectionName, int cc, int count){
        if (0 == cc){
            dict.showSection(sectionName+Suffix.First);
            if (1 == count)
                dict.showSection(sectionName+Suffix.Last);
            else
                dict.showSection(sectionName+Suffix.NotLast);
        }
        else if (cc == (count-1)){
            dict.showSection(sectionName+Suffix.NotFirst);
            dict.showSection(sectionName+Suffix.Last);
        }
        else {
            dict.showSection(sectionName+Suffix.NotFirst);
            dict.showSection(sectionName+Suffix.NotLast);
            dict.showSection(sectionName+Suffix.Exclusive);
        }
    }

}
