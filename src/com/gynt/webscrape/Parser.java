package com.gynt.webscrape;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Parser {

	private Document xmldoc;

	private Parser() {

	}
	
	public Collection<Map<String, Object>> parse(String html, String baseURI) throws ParseException, XMLException {
		return parse(org.jsoup.parser.Parser.parse(html, baseURI));
	}

	public Collection<Map<String, Object>> parse(Document htmldoc) throws ParseException, XMLException {
		return parse(xmldoc, htmldoc);
	}
	
	public static final void verify(Document xmldoc) throws XMLException {
		for (Element array : xmldoc.select("array")) {
			if (array.children().size() == 0) {
				throw new XMLException("array must have child elements");
			}
			if (array.hasAttr("size")) {
				if (!array.attr("size").equals("exhaust")) {
					if (!array.attr("size").isEmpty()) {
						try {
							Integer.parseInt(array.attr("size"));
						} catch (NumberFormatException e) {
							throw new XMLException("size must be a number, or exhaust.");
						}
					}
				}
			}
		}
	}

	public static final Parser getInstance(String xml) throws XMLException {
		return getInstance(Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser()));
	}

	public static final Parser getInstance(Document xmldoc) throws XMLException {
		Parser result = new Parser();
		verify(xmldoc);
		result.xmldoc = xmldoc;
		return result;
	}

	private static final Element resolveQuery(Element xml, Element html) throws QueryException {
		Element queryfind = html.select(xml.attr("query")).first();
		if (queryfind == null)
			throw new QueryException("element not found for query");
		return queryfind;
	}

	private static final void extractData(Element xml, Element html, HashMap<String, Object> map) {
		if (xml.hasAttr("data_name")) {
			String data = "";
			if (xml.attr("data_attribute").equals("text")) {
				data = html.ownText();
			} else {
				data = html.attr(xml.attr("data_attribute"));
			}
			map.put(xml.attr("data_name"), data);
		}
		int i = 2;
		while (xml.hasAttr("data_name" + i)) {
			String data = "";
			if (xml.attr("data_attribute" + i).equals("text")) {
				data = html.ownText();
			} else {
				data = html.attr(xml.attr("data_attribute" + i));
			}
			map.put(xml.attr("data_name" + i), data);
			i++;
		}
	}

	private static final void parseArray(Element array, Element xml, Element html, Collection<Map<String, Object>> result)
			throws ParseException {
		if (!xml.nodeName().equals(html.nodeName()))
			throw new ParseException("nodenames do not correspond");
		int size = Integer.MAX_VALUE;
		int index = 1;
		if (array.hasAttr("size")) {
			if (!array.attr("size").equals("exhaust")) {
				if (!array.attr("size").isEmpty()) {
					size = Integer.parseInt(array.attr("size"));
				}
			}
		}
		while (xml != null) {
			HashMap<String, Object> objmap = new HashMap<String, Object>();
			extractData(xml, html, objmap);
			if (xml.children().size() > 0) {
				for (int i = 0; i < xml.children().size(); i++) {
					if (xml.child(i).hasAttr("query")) {
						parse(xml.child(i), resolveQuery(xml.child(i), html), result, objmap);
					} else {
						parse(xml.child(i), html.child(i), result, objmap);
					}
				}

			}
			xml = xml.nextElementSibling();
			html = html.nextElementSibling();
			if (html == null) {
				break;
			}
			if (xml == null) {
				xml = array.child(0);
				result.add(objmap);
				index++;
				if (index > size) {
					break;
				}
			}
		}
		return;
	}

	private static final void parse(Element xml, Element html, Collection<Map<String, Object>> objects,
			HashMap<String, Object> objmap) throws ParseException {
		if (xml.nodeName().equals(html.nodeName())) {
			extractData(xml, html, objmap);
			if (xml.children().size() > 0) {
				if (html.children().size() == 0)
					throw new ParseException("XML does have children, while HTML does not");
				if (xml.children().first().hasAttr("query")) {
					parse(xml.children().first(), resolveQuery(xml.children().first(), html), objects, objmap);
				} else {
					parse(xml.children().first(), html.children().first(), objects, objmap);
				}
			}
			// else {
			Element nextxml = xml.nextElementSibling();
			Element nexthtml = html.nextElementSibling();
			if (nextxml == null)
				return;
			if (nexthtml == null)
				throw new ParseException("next element does not exist.");
			parse(nextxml, nexthtml, objects, objmap);
			// }
		} else if (xml.nodeName().equals("array")) {
			if (xml.children().size() == 0)
				throw new RuntimeException("array must have children");

			Element currentxml = xml.child(0);
			Element currenthtml = html;

			parseArray(xml, currentxml, currenthtml, objects);

		} else {
			if (!xml.nodeName().equals(html.nodeName()))
				throw new ParseException("nodenames do not correspond");
		}
	}
	
	public static final Collection<Map<String, Object>> parse(Document xmlformatdoc, Document htmldoc) throws ParseException, XMLException {
		verify(xmlformatdoc);
		Collection<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		parse(xmlformatdoc, htmldoc, result, new HashMap<String, Object>());
		return result;
	}

	public static class ParseException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8704267655761029220L;

		private ParseException(String string) {
			super(string);
		}

	}

	public static class QueryException extends ParseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7200501531263020888L;

		private QueryException(String string) {
			super(string);
		}

	}

	public static class XMLException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3505624997997165003L;

		private XMLException(String string) {
			super(string);
		}

	}

	public static void main(String[] args) {
		if (args.length < 2 || args.length > 3) {
			System.err.println("Arguments: <xmlformatfile> <url> [<outputfile>]");
			System.exit(1);
		}
		String xmlformatfile = args[0];
		String url = args[1];
		try {
			String data = new String(Files.readAllBytes(new File(xmlformatfile).toPath()), StandardCharsets.UTF_8);
			try {
				Parser p = Parser.getInstance(data);
				try {
					Document page = Jsoup.connect(url).get();
					try {
						Collection<Map<String, Object>> result = p.parse(page);
						StringBuffer sb = new StringBuffer();
						sb.append("[");
						Iterator<Map<String, Object>> mapiterator = result.iterator();
						while (mapiterator.hasNext()) {
							Map<String, Object> map = mapiterator.next();
							sb.append("{");
							Iterator<Entry<String, Object>> entryiterator = map.entrySet().iterator();
							while (entryiterator.hasNext()) {
								Entry<String, Object> entry = entryiterator.next();
								sb.append("\"");
								sb.append(entry.getKey());
								sb.append("\"");

								sb.append(":");

								sb.append("\"");
								sb.append(entry.getValue());
								sb.append("\"");
								if (entryiterator.hasNext())
									sb.append(",");
							}
							sb.append("}");
							if (mapiterator.hasNext())
								sb.append(",");
						}
						sb.append("]");

						if (args.length == 3) {
							File f = new File(args[2]);
							try {
								Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
							} catch (IOException e) {
								System.err.println("[ERROR]\tFailed to write to file: " + f.toPath().toString());
							}
						} else {
							System.out.println(sb.toString());
						}
					} catch (ParseException e) {
						System.err.println("[ERROR]\tAn error occurred during parse of page: " + e.getMessage());
						System.exit(2);
					}
				} catch (IOException e) {
					System.err.println("[ERROR]\tCould not retrieve page: " + url);
					System.exit(3);
				}
			} catch (XMLException e) {
				System.err.println("[ERROR]\tThe xml formatfile is misformatted: " + e.getMessage());
				System.exit(4);
			}
		} catch (IOException e) {
			System.err.println("[ERROR]\tCannot find or open: " + new File(xmlformatfile).toPath().toString());
			System.exit(5);
		}
		
		System.exit(0);
	}

}
