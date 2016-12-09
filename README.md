# WebScrape
WebScrape is a single-class utility for scraping content from websites. It leverages JSOUP and user made XML format specifications to return a collection of JSON formatted content from a webpage.
### Usage
`java -jar webscrape-1.0.jar <xmlfile> <url> [<outputfile>]`

If no <outputfile> is specified, the output is dumped to the console.
### XML Format
To scrape a HTML document, a XML format specification is needed. 
The format consists of a hierarchy of elements that have the same node names as the relevant HTML elements. 
Next to this, WebScrape provides a custom element, 'array', to do repetitive scraping in a fast and comprehensive way.
Custom attributes of the XML elements can be used to search for HTML elements (CSS query) and to store data.

#### Example
An example of a XML format is as follows:

`youtubesearch.xml`:
```
<ol query="ol.item-section">
	<array size="exhaust">
		<li>
			<div>
				<div>
					<div/>
					<div>
						<h3>
							<a data_name="title" data_attribute="title" data_name2="url" data_attribute2="abs:href"/>
						</h3>
						<div>
							<a data_name="creator" data_attribute="text"/>
						</div>
						<div/>
						<div data_name="description" data_attribute="text"/>
					</div>
				</div>
			</div>
		</li>
	</array>
</ol>
```
This can then be run with:
`java -jar youtubesearch.xml "https://www.youtube.com/results?search_query=web+scrape"`

It will return JSON formatted output: a list of dictionaries with keys as specified in the XML file:
```
[
   {
      "creator":"Jordan Leigh",
      "description":"Whenever you need to import data from an external , hopefully they provide an API and make your life easy. But in the real?...",
      "title":"The Ultimate Introduction to Web Scraping and Browser Automation",
      "url":"https://www.youtube.com/watch?v=1UYBAn69Qrk"
   },
   {
      "creator":"SAF Business Analytics",
....etc.....
```
#### Array
If the line: `<array size="exhaust">` is changed to `<array size="5">`, the first five results will be returned. If the HTML file is exhausted before five iterations have finished, for example when only four results were given, four elements will be returned.
#### Query
The query attribute follows [Jsoups CSS query select] specification. If no result is found for the query, the parser will silently continue.
### JSOUP
WebScrape uses and needs the open source [JSOUP] project in a folder named `lib`.


[JSOUP]: <https://jsoup.org/>
[Jsoups CSS query select]: <https://jsoup.org/cookbook/extracting-data/selector-syntax>

### License
MIT
