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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The data dictionary contains the definition of variables, and
 * controls the interpretation of includes and sections.
 * 
 * An include or section that is visible (defined) enters the scope of
 * a child dictionary.  
 * 
 * The child scope of an include or section inherits and overrides the
 * data definitions of variables and sections from its ancestors.
 * 
 * @author dcoker
 * @author jdp
 */
public class TemplateDictionary
    extends Object
    implements TemplateDataDictionary
{

    /**
     * Creates a top-level TemplateDictionary.
     *
     * @return a new TemplateDictionary
     */
    public static TemplateDictionary create() {
        return new TemplateDictionary();
    }


    protected LinkedHashMap<String, String> variables = new LinkedHashMap<String, String>();

    protected LinkedHashMap<String, List<TemplateDataDictionary>> sections = new LinkedHashMap<String, List<TemplateDataDictionary>>();

    protected TemplateDataDictionary parent;


    public TemplateDictionary() {
        super();
    }
    protected TemplateDictionary(TemplateDataDictionary parent) {
        super();
        this.parent = parent;
    }


    public TemplateDataDictionary getParent(){
        return this.parent;
    }
    /**
     * Called by template render.
     */
    public void renderComplete(){
        this.parent = null;
        this.variables.clear();
        for (List<TemplateDataDictionary> section : this.sections.values()){
            for (TemplateDataDictionary child: section){
                child.renderComplete();
            }
        }
        this.sections.clear();
    }
    /**
     * Deep clone of dictionary carries parent.
     */
    public TemplateDataDictionary clone(){
        try {
            TemplateDictionary clone = (TemplateDictionary)super.clone();
            clone.variables = (LinkedHashMap<String, String>)this.variables.clone();
            clone.sections = (LinkedHashMap<String, List<TemplateDataDictionary>>)this.sections.clone();
            for (Map.Entry<String,List<TemplateDataDictionary>> entry : clone.sections.entrySet()){
                List<TemplateDataDictionary> section = entry.getValue();
                List<TemplateDataDictionary> sectionClone = SectionClone(clone,section);
                entry.setValue(sectionClone);
            }
            return clone;
        }
        catch (java.lang.CloneNotSupportedException exc){
            throw new java.lang.Error(exc);
        }
    }
    /**
     * Deep clone replaces parent.
     */
    public TemplateDataDictionary clone(TemplateDataDictionary parent){
        TemplateDictionary clone = (TemplateDictionary)this.clone();
        if (null != parent){
            clone.parent = parent;
            return clone;
        }
        else
            throw new IllegalStateException();
    }

    /*
     * Variable API
     */

    public boolean hasVariable(String varName) {

        if (this.variables.containsKey(varName))
            return true;
        else if (this.parent != null)
            return this.parent.hasVariable(varName);
        else
            return false;
    }
    public String getVariable(String varName) {

        String value = this.variables.get(varName);

        if (null != value)

            return value;

        else if (this.parent != null) 

            return this.parent.getVariable(varName);
        else 
            return "";
    }
    public void setVariable(String varName, String val) {

        this.variables.put(varName, val);
    }
    public final void setVariable(String varName, int val) {

        this.setVariable(varName, String.valueOf(val));
    }

    /*
     * Section API
     */

    public boolean hasNotSection(String sectionName){

        return (!this.sections.containsKey(sectionName));
    }
    public boolean hasSection(String sectionName){

        return (this.sections.containsKey(sectionName));
    }
    /**
     * @return a list of TemplateDictionaries that iterate the
     * section, or null for a section not visible.
     */
    public List<TemplateDataDictionary> getSection(String sectionName) {

        List<TemplateDataDictionary> list = this.sections.get(sectionName);
        if (null != list)
            return list;
        else {
            /*
             * Inherit section
             */
            TemplateDataDictionary parent = this.parent;
            if (null != parent){

                List<TemplateDataDictionary> ancestor = parent.getSection(sectionName);
                if (null != ancestor){

                    ancestor = SectionClone(this,ancestor);

                    this.sections.put(sectionName,ancestor);

                    return ancestor;
                }
            }
            /*
             * Synthesize section
             */
            if (this.hasVariable(sectionName))
                return this.showSection(sectionName);
            else
                return null;
        }
    }
    /**
     * An aid to usage
     * @param from Embedded section or include name
     * @param to Target template name
     */
    public final List<TemplateDataDictionary> getSection(String from, String to) {
        return this.getSection(from);
    }
    public final TemplateDataDictionary addSectionExclusive(String of, String sectionName){
        if (this.hasNotSection(of))
            return this.addSection(sectionName);
        else
            return null;
    }
    public final TemplateDataDictionary addSectionExclusive(String of, String from, String to){
        if (this.hasNotSection(of))
            return this.addSection(from,to);
        else
            return null;
    }
    public TemplateDataDictionary addSection(String sectionName) {

        TemplateDictionary add = new TemplateDictionary(this);

        List<TemplateDataDictionary> section = this.sections.get(sectionName);
        if (null == section){
            section = new ArrayList<TemplateDataDictionary>();
            this.sections.put(sectionName, section);
        }

        section.add(add);
        return add;
    }
    /**
     * An aid to usage
     * @param from Embedded section or include name
     * @param to Target template name
     */
    public final TemplateDataDictionary addSection(String from, String to) {
        this.setVariable(from,to);
        return this.addSection(from);
    }
    /**
     * @return A section data list having at least one section
     * iteration data dictionary.
     */
    public List<TemplateDataDictionary> showSection(String sectionName) {

        List<TemplateDataDictionary> section = this.sections.get(sectionName);
        if (null == section){
            section = new ArrayList<TemplateDataDictionary>();
            TemplateDictionary show = new TemplateDictionary(this);
            section.add(show);
            this.sections.put(sectionName, section);
        }
        return section;
    }
    /**
     * An aid to usage
     * @param from Embedded section or include name
     * @param to Target template name
     */
    public final List<TemplateDataDictionary> showSection(String from, String to){
        this.setVariable(from,to);
        return this.showSection(from);
    }
    public void hideSection(String sectionName) {

        this.sections.remove(sectionName);
    }
    /**
     * An aid to usage
     * @param from Embedded section or include name
     * @param to Target template name
     */
    public void hideSection(String from, String to){

        this.sections.remove(from);
    }

    public final static List<TemplateDataDictionary> SectionClone(TemplateDataDictionary parent, List<TemplateDataDictionary> section){

        List<TemplateDataDictionary> sectionClone = (List<TemplateDataDictionary>)((ArrayList<TemplateDataDictionary>)section).clone();

        for (int sectionIndex = 0, sectionCount = sectionClone.size(); sectionIndex < sectionCount; sectionIndex++){
            TemplateDataDictionary sectionItem = sectionClone.get(sectionIndex);
            TemplateDataDictionary sectionItemClone = sectionItem.clone(parent);
            sectionClone.set(sectionIndex,sectionItemClone);
        }

        return sectionClone;
    }
}
