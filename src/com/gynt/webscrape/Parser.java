package com.gynt.webscrape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Parser {

	private Document xmldoc;
//	private List<Node> nodes;

	private Parser() {

	}

	public static final Parser getInstance(String xml) {
		Parser result = new Parser();
		result.xmldoc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());
		// TODO: Why not this?:
//		result.nodes = org.jsoup.parser.Parser.parseXmlFragment(xml, "");
		return result;
	}

	public static final Parser getInstance(Document xmldoc) {
		Parser result = new Parser();
		result.xmldoc = xmldoc;
		return result;
	}

	private static final void extractData(Element xml, Element html, HashMap<String, Object> map) {
		if (xml.hasAttr("_id")) {
			String data = "";
			if (xml.attr("_data").equals("text")) {
				data = html.ownText();
			} else {
				data = html.attr(xml.attr("_data"));
			}
			map.put(xml.attr("_id"), data);
		}
		int i = 2;
		while (xml.hasAttr("_id" + i)) {
			String data = "";
			if (xml.attr("_data" + i).equals("text")) {
				data = html.ownText();
			} else {
				data = html.attr(xml.attr("_data" + i));
			}
			map.put(xml.attr("_id" + i), data);
			i++;
		}
	}

	private Collection<HashMap<String, Object>> parseArray(Element startxml, Element xml, Element html) throws ParseException {
		if (!xml.nodeName().equals(html.nodeName()))
			throw new ParseException("nodenames do not correspond");
		Collection<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		while (xml != null) {
			HashMap<String, Object> objmap = new HashMap<String, Object>();
			extractData(xml, html, objmap);
			if (xml.children().size() > 0) {
				if (xml.hasAttr("_query")) {
					parseWithData(xml.child(0), html, objmap);
				} else {
					parseWithData(xml.child(0), html.child(0), objmap);
				}
			}
			xml = xml.nextElementSibling();
			html = html.nextElementSibling();
			System.out.println(xml);
			System.out.println(html);
			if (html == null) {
				return result;
			}
			if (xml == null) {
				xml = startxml;
				System.out.println("again");
//				System.out.println(xml.toString());
//				System.out.println(html.toString());
				result.add(objmap);
			}
		}
		return result;
	}

	private void parseWithData(Element xml, Element html, HashMap<String, Object> objmap) throws ParseException {
		if(xml.nodeName().equals(html.nodeName())) {
			extractData(xml, html, objmap);
			if (xml.children().size() > 0) {
				if (html.children().size() == 0)
					throw new ParseException("XML does have children, while HTML does not");
				if (xml.children().first().hasAttr("_query")) {
					parseWithData(xml.children().first(), html, objmap); // Create asynchrony
															// for query. //2.
															// xml=<tbody>,
															// html=<table>
				} else {
					parseWithData(xml.children().first(), html.children().first(), objmap); // 4.
																			// xml=<tr>,html=<tr>
				}
			} else {
				Element nextxml = xml.nextElementSibling();
				Element nexthtml = html.nextElementSibling();
				if (nextxml == null)
					return;
				if (nexthtml == null)
					throw new ParseException("next element does not exist.");
				parseWithData(nextxml, nexthtml, objmap);
			}
		} else if (xml.hasAttr("_query")) { // XML is one step deeper than html here.
										// call parentelement on html before
										// selecting?
			html = html.select(xml.attr("_query")).first();
			// now html and xml are on the same level/node.
			if (html == null)
				throw new QueryException("No such element for query: " + xml.attr("_query"));
			parseWithData(xml, html, objmap);
		} else if (xml.nodeName().equals("array")) {
			throw new XMLException("Nested arrays currently unsupported");
		} else {
			if (!xml.nodeName().equals(html.nodeName()))
				throw new ParseException("nodenames do not correspond");
			
		}
	}

	private void parse(Element xml, Element html, Collection<Map<String, Object>> objects) throws ParseException {
		if(xml.nodeName().equals(html.nodeName())) {
			if (xml.children().size() > 0) {
				if (html.children().size() == 0)
					throw new ParseException("XML does have children, while HTML does not");
				if (xml.children().first().hasAttr("_query")) {
					parse(xml.children().first(), html, objects); // Create asynchrony
															// for query. //2.
															// xml=<tbody>,
															// html=<table>
				} else {
					parse(xml.children().first(), html.children().first(), objects); // 4.
																			// xml=<tr>,html=<tr>
				}
			} else {
				Element nextxml = xml.nextElementSibling();
				Element nexthtml = html.nextElementSibling();
				if (nextxml == null || nexthtml == null)
					throw new ParseException("next element does not exist.");
				parse(nextxml, nexthtml, objects); // 5. xml=<array>,html=<tr>
			}
		} else if (xml.hasAttr("_query")) { // XML is one step deeper than html here.
										// call parentelement on html before
										// selecting?
			html = html.select(xml.attr("_query")).first(); // 1. xml=<table>,
															// html=<table> //3.
															// xml=<tbody>,
															// html=<tbody>
			// now html and xml are on the same level/node.
			if (html == null)
				throw new QueryException("No such element for query: " + xml.attr("_query"));
			parse(xml, html, objects);
		} else if (xml.nodeName().equals("array")) {
			// start loop
			if (xml.children().size() == 0)
				throw new XMLException("array must have children");
			Element loopstart = xml.child(0); // xml=<tr>

			Element currentxml = xml.child(0); // xml=<tr>
			Element currenthtml = html; // html=<tr>

			objects.addAll(parseArray(loopstart, currentxml, currenthtml));

		} else { // 2.xml=<table>, html=<table>
			if (!xml.nodeName().equals(html.nodeName()))
				throw new ParseException("nodenames do not correspond"); // 2.xml=<table>,
																			// html=<table>
		}
	}

	public Collection<Map<String, Object>> parse(String html, String baseURI) throws ParseException {
		return parse(org.jsoup.parser.Parser.parse(html, baseURI));
	}

	public Collection<Map<String, Object>> parse(Document htmldoc) throws ParseException {
		Collection<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		parse(xmldoc, htmldoc, result);
		return result;
	}

}
