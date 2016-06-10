/*
 * Copyright 2016 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.psml.md;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.html.HTMLNode;
import org.pageseeder.psml.html.HTMLText;

/**
 * This simple parser can produce a list of HTML nodes from textual content
 * using the markdown format.
 *
 * <p>This parser only matches inline HTML elements from a text inside a
 * block level element.
 *
 * <p>Implementation note: this algorithm is identical to the PSML namesake,
 * eventually, these should be merged.
 *
 * @author Christophe Lauret
 */
public class HTMLInlineParser {

  /**
   * Bold text: <code>**text**</code>
   */
  private static final String DOUBLE_EMPHASIS = "(\\*\\*(.*?)\\*\\*)";

  /**
   * Italic text: <code>*text*</code>
   */
  private static final String EMPHASIS = "(\\*(.*?)\\*)";

  /**
   * Bold text: <code>__text__</code>
   */
  private static final String DOUBLE_UNDERSCORE = "(__(.*?)__)";

  /**
   * Italic text: <code>_text_</code>
   */
  private static final String UNDERSCORE = "(\\b_(.*?)_\\b)";

  /**
   * Escaped code: <code>``code``</code>
   */
  private static final String CODE_ESCAPE = "(``\\s?(.*?)\\s?``)";

  /**
   * Code: <code>`code`</code>
   */
  private static final String CODE = "(`(.*?)`)";

  /**
   * Image: <code>![alt](src)</code>
   */
  private static final String IMAGE = "(\\!\\[(.*?)\\]\\((.*?)\\))";

  /**
   * References: <code>[title](url)</code>
   */
  private static final String REF = "(\\[(.*?)\\]\\((.*?)\\))";

  /**
   * Explicit links:
   *  <code>&lt;http://[url]&gt;</code>,
   *  <code>&lt;https://[url]&gt;</code>
   *  or <code>&lt;mailto:[email]&gt;</code>
   */
  private static final String LINK = "(<((https?://|mailto:)(.*?))>)";

  /**
   * Autolinks when text starts with <code>http://</code> or <code>https://</code>
   */
  private static final String LINK_AUTO = "(https?://\\S+[\\w/+=@\\-])";

  /**
   * Define the general pattern to use to match markdown.
   */
  private static final Pattern TOKENS = Pattern.compile(DOUBLE_EMPHASIS+"|"+DOUBLE_UNDERSCORE+"|"+EMPHASIS+"|"+UNDERSCORE+"|"+CODE_ESCAPE+"|"+CODE+"|"+IMAGE+"|"+REF+"|"+LINK+"|"+LINK_AUTO);

  public HTMLInlineParser() {
  }

  /**
   * Parses the text content and returns the corresponding list of nodes.
   *
   * @param content The text content to parse.
   *
   * @return
   */
  public List<HTMLNode> parse(String content) {
    List<HTMLNode> nodes = new ArrayList<>();
    Matcher m = TOKENS.matcher(content);
    int previousEnd = 0;
    while (m.find()) {
      // Any text before a match
      if (m.start() > previousEnd) {
        String text = content.substring(previousEnd, m.start());
        nodes.add(new HTMLText(text));
      }
      previousEnd = m.end();
      // Strong emphases with '**' (appear in bold)
      if (m.group(1) != null) {
        HTMLElement element = new HTMLElement(Name.strong);
        element.addNodes(parse(m.group(2)));
        nodes.add(element);
      }
      // Strong emphases with '__' (appear in bold)
      else if (m.group(3) != null) {
        HTMLElement element = new HTMLElement(Name.strong);
        element.addNodes(parse(m.group(4)));
        nodes.add(element);
      }
      // Normal emphases with '*' (appear in italic)
      if (m.group(5) != null) {
        HTMLElement element = new HTMLElement(Name.em);
        element.addNodes(parse(m.group(6)));
        nodes.add(element);
      }
      // Strong emphases with '_' (appear in italic)
      else if (m.group(7) != null) {
        HTMLElement element = new HTMLElement(Name.em);
        element.addNodes(parse(m.group(8)));
        nodes.add(element);
      }
      // Code with '`'
      else if (m.group(9) != null) {
        String code = m.group(10);
        HTMLElement monospace = new HTMLElement(Name.code);
        if (code.length() > 0) {
          monospace.addNode(new HTMLText(code));
        }
        nodes.add(monospace);
      }
      // Code escape with '``'
      else if (m.group(11) != null) {
        String code = m.group(12);
        HTMLElement monospace = new HTMLElement(Name.code);
        if (code.length() > 0) {
          monospace.addNode(new HTMLText(code));
        }
        nodes.add(monospace);
      }
      // Images as '![alt](src)'
      else if (m.group(13) != null) {
        String alt = m.group(14);
        String src = m.group(15);
        if (src.startsWith("http")) {
          // PageSeeder does not support external images
          HTMLElement link = new HTMLElement(Name.a);
          link.setAttribute("href", src);
          link.addNodes(parse(alt));
          nodes.add(link);
        } else {
          HTMLElement image = new HTMLElement(Name.img);
          image.setAttribute("alt", alt);
          image.setAttribute("src", src);
          nodes.add(image);
        }
      }
      // References as '[title](url)'
      else if (m.group(16) != null) {
        String ref = m.group(18);
        String text = m.group(17);
        HTMLElement link = new HTMLElement(Name.a);
        link.setAttribute("href", ref);
        link.addNodes(parse(text));
        nodes.add(link);
      }
      // Explicit links
      else if (m.group(19) != null) {
        String url = m.group(20);
        String text = m.group(22);
        HTMLElement link = new HTMLElement(Name.a);
        link.setAttribute("href", url);
        link.addNode(new HTMLText(text));
        nodes.add(link);
      }
      //  Auto links
      else if (m.group(23) != null) {
        String url = m.group(23);
        HTMLElement link = new HTMLElement(Name.a);
        link.setAttribute("href", url);
        link.addNode(new HTMLText(url));
        nodes.add(link);
      }
    }
    // Add the tail end
    if (previousEnd < content.length()) {
      String text = content.substring(previousEnd);
      nodes.add(new HTMLText(text));
    }
    return nodes;
  }

}