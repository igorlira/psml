/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * An immutable tree aggregating multiple trees together in order to generate a
 * deep table of contents.
 *
 * @author Christophe Lauret
 */
public final class PublicationTree implements Tree, Serializable, XMLWritable {

  /** As per requirement for Serializable */
  private static final long serialVersionUID = 4L;

  /**
   * Maximum number of reverse references to follow when serializing to XML
   */
  private static final int MAX_REVERSE_FOLLOW = 100;

  /**
   * The ID of the root of the tree.
   */
  private final long _rootid;

  /**
   * Map of trees that make up this tree.
   */
  private final Map<Long, DocumentTree> _map;

  /**
   * Creates a simple publication tree wrapping a document tree
   *
   * @param tree The document tree.
   */
  public PublicationTree(DocumentTree tree) {
    this._map = Collections.singletonMap(tree.id(), tree);
    this._rootid = tree.id();
  }

  /**
   * Creates a new tree wrapping existing publication tree with another document tree.
   *
   * <p>Note: the parent tree should have at least one reference to the publication tree.
   *
   * @param parent The new root for the tree
   * @param trunk  The rest of the tree
   */
  private PublicationTree(DocumentTree parent, PublicationTree trunk) {
    Map<Long, DocumentTree> map = new HashMap<>(trunk._map);
    map.put(parent.id(), parent);
    this._map = Collections.unmodifiableMap(map);
    this._rootid = parent.id();
  }

  /**
   * Creates a new tree appending existing publication tree with another document tree.
   *
   * <p>Note: the tree should have at least one reference from the existing publication tree.
   *
   * @param trunk  The rest of the tree
   * @param tree   The new tree to add
   */
  private PublicationTree(PublicationTree trunk, DocumentTree tree) {
    Map<Long, DocumentTree> map = new HashMap<>(trunk._map);
    map.put(tree.id(), tree);
    this._map = Collections.unmodifiableMap(map);
    this._rootid = trunk._rootid;
  }

  /**
   * @return The URI ID of the root
   */
  @Override
  public long id() {
    return root().id();
  }

  /**
   * @return The list of reverse references from the root.
   */
  @Override
  public List<Long> listReverseReferences() {
    return root().listReverseReferences();
  }

  /**
   * @return The list of URI ID of all forward cross-references.
   */
  @Override
  public List<Long> listForwardReferences() {
    List<Long> uris = new ArrayList<>();
    for (DocumentTree tree : this._map.values()) {
      uris.addAll(tree.listForwardReferences());
    }
    return uris;
  }

  /**
   * @return The title of the root.
   */
  @Override
  public String title() {
    return root().title();
  }

  /**
   * @return Indicates whether this publication contains the tree specified by its URI ID.
   */
  public boolean containsTree(long id) {
    return this._map.containsKey(id);
  }

  /**
   * @return the root of this tree.
   */
  public DocumentTree root() {
    return this._map.get(this._rootid);
  }

  /**
   * @id     the tree ID
   *
   * @return the a tree in the publication.
   */
  public DocumentTree tree(long id) {
    return this._map.get(id);
  }

  /**
   * Create a new publication tree by adding the specified document tree.
   *
   * @param tree The document tree to add
   *
   * @return The new publication tree
   */
  public PublicationTree add(DocumentTree tree) {
    return new PublicationTree(this, tree);
  }

  /**
   * Create a new root tree by adding the specified root.
   *
   * @param root The root to add
   *
   * @return The new root tree
   */
  public PublicationTree root(DocumentTree root) {
    return new PublicationTree(root, this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hashCode = 1;
    for (Long id : this._map.keySet()) {
      hashCode = prime*hashCode + Long.hashCode(id);
    }
    return hashCode;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    PublicationTree other = (PublicationTree)o;
    return !ids().equals(other.ids());
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    toXML(xml, -1, -1, null);
  }

  /**
   * Serialize the partial tree down to content ID.
   *
   * @param xml         The XML writer
   * @param cid         The ID of the content tree (leaf). If -1 output all.
   * @param cposition   If not -1 output content tree only at this position (occurrence number) in the tree.
   * @param number      The fragment numbering for the publication (optional)
   *
   * @throws IOException If thrown by XML writer
   */
  public void toXML(XMLWriter xml, long cid, int cposition, @Nullable FragmentNumbering number) throws IOException {
    xml.openElement("publication-tree", true);
    DocumentTree root = tree(cposition == -1 ? this._rootid : cid);
    if (root != null) {
      xml.attribute("id", Long.toString(root.id()));
      xml.attribute("title", root.title());
      if (this._map.size() == 1 || cposition != -1) {
        xml.attribute("content", "true");
      }
      List<Long> trees = null;
      // Collect partial tree nodes
      if (cid != -1 && cposition == -1) {
        trees = new ArrayList<>();
        collectReferences(tree(cid), trees);
      }
      Map<Long,Integer> doccount = new HashMap<>();
      toXML(xml, root.id(), 1, cid, trees, number, doccount, (cid == -1 || cposition == -1) ? 1 : cposition, new ArrayList<Long>());
    }
    xml.closeElement();
  }

  /**
   * Collect all the ancestor references to a tree.
   *
   * @param t      the tree
   * @param trees  the list of ancestor IDs
   */
  private void collectReferences(DocumentTree t, List<Long> trees) {
    if (t == null || trees.contains(t.id())) return;
    trees.add(t.id());
    int count = 0;
    for (Long ref : t.listReverseReferences()) {
      DocumentTree r = tree(ref);
      if (r != null) {
        collectReferences(tree(ref), trees);
        count++;
      }
      if (count >= MAX_REVERSE_FOLLOW) break;
    };
  }

  /**
   * Serialize a tree as XML.
   *
   * @param xml       The XML writer
   * @param id        The ID of the tree to serialize.
   * @param level     The level that we are currently at
   * @param cid       The ID of the content tree (leaf).
   * @param trees     The IDs of trees that cid is a descendant of (optional)
   * @param number    The fragment numbering for the publication (optional)
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level, long cid, @Nullable List<Long> trees,
      @Nullable FragmentNumbering number, Map<Long,Integer> doccount, Integer count, List<Long> ancestors) throws IOException {
    if (ancestors.contains(id)) throw new IllegalStateException("XRef loop detected on URIID " + id);
    ancestors.add(id);
    DocumentTree current = tree(id);
    for (Part<?> part : current.parts()) {
      toXML(xml, id, level, part, cid, trees, number, doccount, count, ancestors);
    }
    ancestors.remove(id);
  }

  /**
   * Serialize a part as XML.
   *
   * @param xml       The XML writer
   * @param id        The ID of the tree to output.
   * @param level     The level that we are currently at
   * @param part      The part to serialize
   * @param cid       The ID of the content tree (leaf).
   * @param trees     The IDs of trees that cid is a descendant of (optional)
   * @param number    The fragment numbering for the publication (optional)
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level, Part<?> part, long cid,  @Nullable List<Long> trees,
      @Nullable FragmentNumbering number, Map<Long,Integer> doccount, Integer count, List<Long> ancestors) throws IOException {
    Element element = part.element();
    // ignore paragraphs
    if (element instanceof Paragraph) return;
    boolean toNext = false;
    Long next = null;
    DocumentTree nextTree = null;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      // don't process embedded fragments
      if (Reference.DEFAULT_FRAGMENT.equals(ref.targetfragment())) {
        next = ref.uri();
        nextTree = tree(next);
        toNext = nextTree != null && (trees == null || trees.contains(next)) && id != cid;
      }
    }
    xml.openElement("part", !part.parts().isEmpty() || toNext);
    xml.attribute("level", level);
    if (toNext && cid == next) {
      xml.attribute("content", "true");
    } else if (element instanceof Heading) {
      xml.attribute("uri", Long.toString(id));
    }

    // Output the element
    Integer nextcount = null;
    if (nextTree != null) {
      nextcount = doccount.get(next);
      nextcount = nextcount == null ? 1 : nextcount + 1;
      doccount.put(next, nextcount);
      element.toXML(xml, level, number, next, nextcount, nextTree.numbered(), nextTree.prefix());
    } else {
      element.toXML(xml, level, number, id, count);
    }

    // Expand found reference
    if (toNext) {
      // Moving to the next tree (increase the level by 1)
      toXML(xml, next, level+1, cid, trees, number, doccount, nextcount, ancestors);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      toXML(xml, id, level+1, r, cid, trees, number, doccount, count, ancestors);
    }
    xml.closeElement();
  }

  /**
   * @return list of tree IDs
   */
  private Set<Long> ids() {
    return this._map.keySet();
  }

  @Override
  public void print(Appendable out) {
    // TODO
  }
}
