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

/**
 * Line number string reader
 *
 * @author jdp
 */
public final class ParserReader
    extends java.lang.Object
    implements java.lang.CharSequence
{

    private char[] buffer;

    private int lno = 1;

    private String indentation = "\n";

    private int advance;


    public ParserReader(String string){
        super();
        if (null != string && 0 != string.length())
            this.buffer = string.toCharArray();
    }


    public int lineNumber(){
        return this.lno;
    }
    public int length(){
        char[] buf = this.buffer;
        if (null != buf)
            return buf.length;
        else
            return 0;
    }
    public boolean next(){
        this.advance += 1;
        return true;
    }
    public char charAt(int idx){

        idx += this.advance;

        char[] buf = this.buffer;
        if (null != buf){
            if (-1 < idx && idx < buf.length)
                return buf[idx];
            else
                throw new IndexOutOfBoundsException(String.valueOf(idx)+":{"+buf.length+'}');
        }
        else
            throw new IndexOutOfBoundsException(String.valueOf(idx)+":{0}");
    }
    public char charAtTest(int idx){

        idx += this.advance;

        char[] buf = this.buffer;
        if (null != buf){
            if (-1 < idx && idx < buf.length)
                return buf[idx];
            else
                return 0;
        }
        else
            return 0;
    }
    public int indexOf(String s){
        if (null != s && 0 != s.length()){
            char[] search = s.toCharArray();
            char[] buf = this.buffer;
            if (null != buf){
                int sc = 0;
                int scc = search.length;
                int start = -1;

                int idx = this.advance;
                int idxc = buf.length;

                for (; idx < idxc; idx++){

                    if (buf[idx] == search[sc++]){

                        if (-1 == start)
                            start = idx;

                        if (sc >= scc)
                            return start;
                        else
                            continue;
                    }
                    else {
                        start = -1;
                        sc = 0;
                        continue;
                    }
                }
            }
            return -1;
        }
        else
            throw new IllegalArgumentException(s);
    }
    /**
     * @param start Offset index, inclusive
     * @param end Offset index, exclusive
     */
    public String delete(int start, int end){

        char[] buf = this.buffer;
        if (null != buf){
            int buflen = buf.length;
            if ((-1 < start && start < buflen)&&(start <= end && end <= buflen)){
                if (start == end) {
                    this.indentation = null;
                    return "";
                } else {
                    int relen = (end-start);
                    char[] re = new char[relen];
                    System.arraycopy(buf,start,re,0,relen);
                    int nblen = (buflen-relen);
                    if (0 == nblen)
                        this.buffer = null;
                    else {
                        int term = (buflen-1);
                        char[] nb = new char[nblen];
                        if (0 == start){
                            /*
                             * Copy buffer tail to new buffer
                             */
                            System.arraycopy(buf,end,nb,0,nblen);
                            this.buffer = nb;
                        }
                        else if (term == end){
                            /*
                             * Copy buffer head to new buffer
                             */
                            System.arraycopy(buf,0,nb,0,nblen);
                            this.buffer = nb;
                        }
                        else {
                            /*
                             * Copy buffer head & tail to new buffer
                             */
                            int nbalen = start;
                            int nbblen = buflen-end;
                            System.arraycopy(buf,0,nb,0,nbalen);
                            System.arraycopy(buf,end,nb,nbalen,nbblen);
                            this.buffer = nb;
                        }
                    }
                    this.indentation = getIndentation(re,relen,"\n".equals(this.indentation));
                    return this.lines(re,0,relen);
                }
            }
            else
                throw new IndexOutOfBoundsException(String.valueOf(start)+':'+String.valueOf(start)+":{"+buf.length+'}');
        }
        else
            throw new IndexOutOfBoundsException(String.valueOf(start)+':'+String.valueOf(start)+":{0}");
    }
    public String truncate(){

        char[] buf = this.buffer;
        this.buffer = null;
        return this.lines(buf,0,-1);
    }
    public CharSequence subSequence(int start, int end){

        char[] buf = this.buffer;
        if (null != buf){
            int buflen = buf.length;
            if ((-1 < start && start < buflen)&&(start <= end && end <= buflen)){
                if (start == end)
                    return "";
                else {
                    int relen = (end-start);
                    char[] re = new char[relen];
                    System.arraycopy(buf,start,re,0,relen);
                    return new String(re,0,relen);
                }
            }
            else
                throw new IndexOutOfBoundsException(String.valueOf(start)+':'+String.valueOf(start)+":{"+buf.length+'}');
        }
        else
            throw new IndexOutOfBoundsException(String.valueOf(start)+':'+String.valueOf(start)+":{0}");
    }
    public String toString(){

        char[] buf = this.buffer;
        if (null != buf)
            return new String(buf,0,buf.length);
        else
            return "";
    }
    public String getIndentation(){
        return this.indentation;
    }

    protected String lines(char[] re, int ofs, int len){

        this.advance = 0;

        if (-1 == len)
            len = ((null != re)?(re.length):(0));

        this.lno += CountLines(re,ofs,len);

        return new String(re,ofs,len);
    }
    protected static int CountLines(char[] re, int ofs, int len){
        int num = 0;
        for (; ofs < len; ofs++){
            if ('\n' == re[ofs])
                num += 1;
        }
        return num;
    }
    protected static String getIndentation(char[] buf, int len, boolean firstLine) {
        int newline;
        for (newline = len - 1; newline >= 0 && buf[newline] != '\n'; --newline) {
            switch (buf[newline]) {
                case ' ': case '\t': break;
                default: return null;
            }
        }
        if (newline == -1) {
            if (firstLine) {
                StringBuilder sb = new StringBuilder(len + 1);
                sb.append('\n');
                sb.append(buf, 0, len);
                return sb.toString();
            } else {
                return null;
            }
        }
        if (newline == len - 1) {
            return null;
        }
        return new String(buf, newline, len - newline);
    }
}
