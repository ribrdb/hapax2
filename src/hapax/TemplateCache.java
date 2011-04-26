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

import hapax.parser.TemplateParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Templates;

/**
 * An in-memory cache of parsed {@link Templates} intended to be
 * shared across threads.
 *
 * @author dcoker
 * @author jdp
 */
public class TemplateCache
    extends Object
    implements TemplateLoader
{

    /**
     * Creates a TemplateLoader for CTemplate language
     */
    public static TemplateLoader create(String base_path)
    {
        return new TemplateCache(base_path);
    }
    /**
     * Creates a TemplateLoader using the argument parser.
     */
    public static TemplateLoader createForParser(String base_path, TemplateParser parser)
    {
        return new TemplateCache(base_path, parser);
    }


    protected final Map<String, Template> cache = new LinkedHashMap<String, Template>();

    protected final String baseDir;

    protected final TemplateParser parser;


    public TemplateCache(String baseDir){
        super();
        this.baseDir = baseDir;
        this.parser = null;
    }
    public TemplateCache(String baseDir, TemplateParser parser){
        super();
        this.baseDir = baseDir;
        this.parser = parser;
    }


    public String getTemplateDirectory() {
        return this.baseDir;
    }
    /**
     */
    public Template getTemplate(String resource)
        throws TemplateException
    {
        String filename = Path.toFile(this.baseDir, resource);
        try {
            URL url = new URL(filename);

            return this.read(url);
        }
        catch (java.net.MalformedURLException exc){

            File file = new File(filename);
            /*
             * stat fs once on cache hit
             */
            long fileLast = file.lastModified();

            Template template = this.hitCache(file.getPath(), fileLast);

            if (null != template)

                return template;
            else 
                return this.read( file, fileLast);
        }
    }
    /**
     */
    public Template getTemplate(TemplateLoader context, String filename)
        throws TemplateException
    {
        return this.getTemplate(Path.toFile(context.getTemplateDirectory(), filename));
    }
    /**
     * Url fetch with no caching.
     */
    protected Template read(URL url)
        throws TemplateException
    {
        String parent = Parent(url);

        TemplateLoader context = new TemplateLoader.Context(this, parent);

        String contents;

        InputStream in = null;
        try {
            in = url.openStream();

            contents = this.readToString(new java.io.InputStreamReader(in));
        }
        catch (IOException exc){
            throw new TemplateException(url.toString(),exc);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ignore){
            }
        }

        TemplateParser parser = this.parser;

        if (null == parser)
            return (new Template(contents, context, url.toString()));
        else
            return (new Template(parser, contents, context, url.toString()));
    }

    protected Template read(File file, long fileLast)
        throws TemplateException
    {
        TemplateLoader context = new TemplateLoader.Context(this, file.getParent());

        String contents;

        FileReader reader = null;
        try {
            reader = new FileReader(file);

            contents = this.readToString(reader);
        }
        catch (IOException exc) {
            throw new TemplateException(file.getPath(),exc);
        }
        finally {
            if (null != reader){
                try {
                    reader.close();
                }
                catch (IOException ignore){
                }
            }
        }

        Template template;

        TemplateParser parser = this.parser;
        if (null == parser)
            template = new Template(fileLast, contents, context, file.getPath());
        else
            template = new Template(fileLast, parser, contents, context, file.getPath());

        synchronized(this.cache){
            this.cache.put(file.getPath(),template);
        }
        return template;
    }

    protected final Template hitCache(String filename, long fileLast)
    {
        Template template = this.cache.get(filename);
        if (null != template){
            long templateLast = template.getLastModified();
            if (templateLast >= fileLast)
                return template;
        }
        return null;
    }

    protected final static String readToString(Reader in)
        throws IOException
    {
        StringBuilder string = new StringBuilder();
        char[] buf = new char[0x200];
        int read;
        while (0 < (read = in.read(buf,0,0x200))){
            string.append(buf,0,read);
        }
        return string.toString();
    }

    protected final static String Parent (URL u){
        String url = u.toExternalForm();
        int idx = url.lastIndexOf('/');
        if (-1 != idx)
            return url.substring(0,idx);
        else
            return url;
    }
}
