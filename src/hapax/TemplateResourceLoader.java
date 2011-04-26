/*
 * Hapax2 Resource Loader
 * Copyright (c) 2007 Doug Coker
 * Copyright (c) 2009 John Pritchard
 * Copyright (c) 2010 Alan Stewart
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An in-memory cache of parsed {@link Template}s intended to be
 * shared across threads.
 * 
 * <p>
 * Templates are loaded from the classpath.
 *
 * @author Alan Stewart (alankstewart@gmail.com)
 */
public class TemplateResourceLoader implements TemplateLoader {
	private static final Map<String, Template> cache = new LinkedHashMap<String, Template>();
	protected final String baseDir;
	protected final TemplateParser parser;

	/**
	 * Creates a TemplateLoader for CTemplate language
	 */
	public static TemplateLoader create(String base_path) {
		return new TemplateResourceLoader(base_path);
	}

	/**
	 * Creates a TemplateLoader using the argument parser.
	 */
	public static TemplateLoader createForParser(String base_path, TemplateParser parser) {
		return new TemplateResourceLoader(base_path, parser);
	}

	public TemplateResourceLoader(String baseDir) {
		this.baseDir = baseDir;
		this.parser = null;
	}

	public TemplateResourceLoader(String baseDir, TemplateParser parser) {
		this.baseDir = baseDir;
		this.parser = parser;
	}

	public String getTemplateDirectory() {
		return this.baseDir;
	}

	public Template getTemplate(String resource) throws TemplateException {
		return getTemplate(new TemplateLoader.Context(this, baseDir), resource);
	}

	public Template getTemplate(TemplateLoader context, String resource) throws TemplateException {
		if (!resource.endsWith(".xtm")) {
			resource += ".xtm";
		}

		String templatePath = baseDir + resource;
		if (cache.containsKey(templatePath)) {
			return cache.get(templatePath);
		}

		InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath);
		if (is == null) {
		    is = getClass().getClassLoader().getResourceAsStream(resource);
		    if (null == is)
			throw new TemplateException("Template " + templatePath + " could not be found");
		}
		
		String contents;
		try {
			contents = copyToString(new InputStreamReader(is));
		} catch (IOException e) {
			throw new TemplateException(e);
		}

		Template template = parser == null ? new Template(contents, context, resource) : new Template(parser, contents, context, resource);

		synchronized (cache) {
			cache.put(templatePath, template);
		}

		return template;
	}

	private String copyToString(Reader in) throws IOException {
		StringWriter out = new StringWriter();
		try {
			char[] buffer = new char[4096];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
		} finally {
			try {
				in.close();
			} catch (IOException ignore) {}
			try {
				out.close();
			} catch (IOException ignore) {}
		}
		return out.toString();
	}
}

