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
 * Implementors of TemplateLoader are responsible for returning a Template
 * object for the given identifier.
 *
 * The terms "directory" and "file" are conventional to collection and
 * item identifiers as may be supported by the loader.
 *
 * See {@link TemplateCache} for an example TemplateLoader.
 *
 * @author dcoker
 * @author jdp
 */
public interface TemplateLoader {

    public class Context
        extends Object
        implements TemplateLoader
    {

        private final TemplateLoader loader;
        private final String template_directory;


        public Context(TemplateLoader loader, String template_directory){
            super();
            this.loader = loader;
            this.template_directory = template_directory;
        }


        public String getTemplateDirectory() {
            return template_directory;
        }
        public Template getTemplate(String filename)
            throws TemplateException
        {
            return this.loader.getTemplate(this,filename);
        }
        public Template getTemplate(TemplateLoader context, String filename)
            throws TemplateException
        {
            return this.loader.getTemplate(context,filename);
        }
    }

    public String getTemplateDirectory();

    public Template getTemplate(String filename)
        throws TemplateException;

    public Template getTemplate(TemplateLoader context, String filename)
        throws TemplateException;

}
