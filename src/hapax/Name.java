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
import java.util.StringTokenizer;

/**
 * A section or variable name parsed into a list of components
 * delimited by solidus '/' (slash).
 * 
 * @author jdp
 */
public class Name 
    extends Object
    implements Iterable<Name.Component>
{

    /**
     * A name component parsed a <code>'['</code> <i>term</i>
     * <code>']'</code> or <code>':'</code> <i>term</i> suffix.
     * 
     * @author jdp
     */
    public final static class Component 
        extends Object
        implements Comparable<Component>
    {

        public final String source, component, term;

        public final int index;


        public Component(String source){
            super();
            StringTokenizer strtok = new StringTokenizer(source,"][");
            switch (strtok.countTokens()){
            case 1:
                this.component = strtok.nextToken();
                this.term = null;
                this.index = 0;
                this.source = component;
                break;
            case 2:
                this.component = strtok.nextToken();
                this.term = strtok.nextToken();
                int index;
                try {
                    index = Integer.parseInt(this.term);
                    if (0 > index)
                        throw new IllegalArgumentException(source);
                    else
                        source = component+'['+index+']';
                }
                catch (NumberFormatException exc){
                    index = 0;
                    source = component+'['+term+']';
                }
                this.index = index;
                this.source = source;
                break;
            default:
                throw new IllegalArgumentException(source);
            }
        }


        public int hashCode(){
            return this.source.hashCode();
        }
        public String toString(){
            return this.source;
        }
        public boolean equals(Object that){
            if (this == that)
                return true;
            else if (null == that)
                return false;
            else
                return this.source.equals(that.toString());
        }
        public int compareTo(Component that){
            if (this == that)
                return 0;
            else if (null == that)
                return 1;
            else if (null != this.term && null != that.term){
                int comp = this.component.compareTo(that.component);
                if (0 != comp)
                    return comp;
                else if (this.index != that.index){
                    if (this.index < that.index)
                        return -1;
                    else
                        return 1;
                }
                else
                    return this.term.compareTo(that.term);
            }
            else
                return this.component.compareTo(that.component);
        }
    }
    /**
     * Name path iterator.
     * 
     * @author jdp
     */
    public final class Iterator
        extends Object
        implements java.util.Iterator<Component>
    {
        private final Component[] list;
        private final int count;
        private int index;


        public Iterator(Component[] list){
            super();
            this.list = list;
            this.count = list.length;
        }

        public boolean hasNext(){
            return (this.index < this.count);
        }
        public Component next(){
            int index = this.index;
            if (index < this.count){
                Component next = this.list[index];
                this.index++;
                return next;
            }
            else
                throw new java.util.NoSuchElementException(String.valueOf(index));
        }
        public void remove(){
            throw new java.lang.UnsupportedOperationException();
        }
    }


    public final String source;

    public final Component path[];

    public final int count;


    public Name(String source){
        super();
        if (null != source){
            StringBuilder strbuf = new StringBuilder();
            StringTokenizer strtok = new StringTokenizer(source,"/");
            int count = strtok.countTokens();
            Component[] path = new Component[count];
            for (int cc = 0; cc < count; cc++){
                Component el = new Component(strtok.nextToken());
                path[cc] = el;
                if (0 != cc)
                    strbuf.append('/');
                strbuf.append(el.source);
            }
            this.source = strbuf.toString();
            this.path = path;
            this.count = count;
        }
        else
            throw new IllegalArgumentException();
    }


    public final String getSource(){
        return this.source;
    }
    public final boolean isIdentity(){
        return (1 == this.count);
    }
    public final int size(){
        return this.count;
    }
    public final Component get(int idx){
        if (-1 < idx && idx < this.count)
            return this.path[idx];
        else
            throw new ArrayIndexOutOfBoundsException(String.valueOf(idx));
    }
    public String getVariable(TemplateDataDictionary map){
        Component[] path = this.path;
        for (int cc = 0, count = this.count, term = (count-1); null != map && cc < count; cc++){
            Component c = path[cc];
            if (cc < term)
                map = this.getSection(c,map);
            else
                return this.getVariable(c,map);
        }
        return null;
    }
    public TemplateDataDictionary getSection(TemplateDataDictionary map){
        Component[] path = this.path;
        for (int cc = 0, count = this.count; null != map && cc < count; cc++){
            Component c = path[cc];
            map = this.getSection(c,getSection(map));
        }
        return map;
    }
    protected String getVariable(Component c, TemplateDataDictionary map){
        return map.getVariable(c.component);
    }
    protected TemplateDataDictionary getSection(Component c, TemplateDataDictionary map){
        List<TemplateDataDictionary> section = map.getSection(c.component);
        if (null != section){
            if (c.index < section.size())
                return section.get(c.index);
        }
        return null;
    }
    public final int hashCode(){
        return this.source.hashCode();
    }
    public final String toString(){
        return this.source;
    }
    public final boolean equals(Object that){
        if (this == that)
            return true;
        else if (null == that)
            return false;
        else
            return this.source.equals(that.toString());
    }
    public final java.util.Iterator<Component> iterator(){
        return new Iterator(this.path);
    }
}
