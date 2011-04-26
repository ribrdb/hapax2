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

import java.util.List;

/**
 * The implementation of {@link TemplateDictionary} as consumed by
 * {@link Template} is the specification of this interface.
 * 
 * @author jdp
 */
public interface TemplateDataDictionary
    extends java.lang.Cloneable
{
    /**
     * Consumption complete.
     */
    public void renderComplete();
    /**
     * @return Deep clone of dictionary carries parent.
     */
    public TemplateDataDictionary clone();
    /**
     * @return Deep clone replaces parent.
     */
    public TemplateDataDictionary clone(TemplateDataDictionary parent);
    /**
     * @return First most immediate ancestor.
     */
    public TemplateDataDictionary getParent();
    /**
     * @return Get variable would be not null
     */
    public boolean hasVariable(String name);
    /**
     * @return Variable value from this or parent
     */
    public String getVariable(String name);
    /**
     * Define variable.
     */
    public void setVariable(String name, String value);
    /**
     * @return Child, child from ancestry, or synthetic on variable.
     */
    public List<TemplateDataDictionary> getSection(String sectionName);
    /**
     * @return Existing section, or with no existing section create new.
     */
    public List<TemplateDataDictionary> showSection(String sectionName);
    /**
     * @return New section.
     */
    public TemplateDataDictionary addSection(String sectionName);
    
    public void enableDebugAnnotations();
    
    public boolean debugAnnotationsEnabled();

}
