package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.ref;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public final class ReferenceTest {

  @Test
  public void testRef_basic() {
    Part<Reference> ref = ref(1, "Hello", 23L);
    Assert.assertEquals(1, ref.level());
    Assert.assertEquals(23L, ref.element().uri());
    Assert.assertEquals("Hello", ref.title());
  }

  @Test
  public void testAdjustLevel_zero() {
    int level = 1;
    long uri = 23L;
    String title = "Hello";
    Part<Reference> ref = ref(level, title, uri);
    Part<Reference> adj = ref.adjustLevel(0);
    Assert.assertSame(ref, adj);
    // Check ref hasn't changed
    Assert.assertEquals(level, ref.level());
    Assert.assertEquals(uri, ref.element().uri());
    Assert.assertEquals(title, ref.title());
    // Check values adjusted correctly
    Assert.assertEquals(level, adj.level());
    Assert.assertEquals(uri, adj.element().uri());
    Assert.assertEquals(title, adj.title());
  }

  @Test
  public void testAdjustLevel_minus_2() {
    int level = 3;
    long uri = 23L;
    String title = "Hello";
    Part<Reference> ref = ref(level, title, uri);
    Part<Reference> adj = ref.adjustLevel(-2);
    Assert.assertNotSame(ref, adj);
    // Check ref hasn't changed
    Assert.assertEquals(level, ref.level());
    Assert.assertEquals(uri, ref.element().uri());
    Assert.assertEquals(title, ref.title());
    // Check values adjusted correctly
    Assert.assertEquals(level-2, adj.level());
    Assert.assertEquals(uri, adj.element().uri());
    Assert.assertEquals(title, adj.title());
  }

  @Test
  public void testAdjustLevel_plus_1() {
    int level = 1;
    long uri = 23L;
    String title = "Hello";
    Part<Reference> ref = ref(level, title, uri);
    Part<Reference> adj = ref.adjustLevel(1);
    Assert.assertNotSame(ref, adj);
    // Check ref hasn't changed
    Assert.assertEquals(level, ref.level());
    Assert.assertEquals(uri, ref.element().uri());
    Assert.assertEquals(title, ref.title());
    // Check values adjusted correctly
    Assert.assertEquals(level+1, adj.level());
    Assert.assertEquals(uri, adj.element().uri());
    Assert.assertEquals(title, adj.title());
  }

  @Test
  public void testAdjustLevel_plus_3() {
    int level = 1;
    long uri = 23L;
    String title = "Hello";
    Part<Reference> ref = ref(level, title, uri);
    Part<Reference> adj = ref.adjustLevel(3);
    Assert.assertNotSame(ref, adj);
    // Check ref hasn't changed
    Assert.assertEquals(level, ref.level());
    Assert.assertEquals(uri, ref.element().uri());
    Assert.assertEquals(title, ref.title());
    // Check values adjusted correctly
    Assert.assertEquals(level+3, adj.level());
    Assert.assertEquals(uri, adj.element().uri());
    Assert.assertEquals(title, adj.title());
  }

  @Test
  public void testAdjustLevel_plus_1_deep() {
    int level = 1;
    long uri = 23L;
    String title = "Hello";
    Part<Reference> ref = ref(level, title, uri).attach(Arrays.asList(
        ref(level+1, "A", 24L),
        ref(level+1, "B", 25L)
    ));
    Part<Reference> adj = ref.adjustLevel(1);
    Assert.assertNotSame(ref, adj);
    // Check ref hasn't changed
    Assert.assertEquals(level, ref.level());
    Assert.assertEquals(uri, ref.element().uri());
    Assert.assertEquals(title, ref.title());
    Assert.assertEquals(level+1, ref.parts().get(0).level());
    Assert.assertEquals(level+1, ref.parts().get(1).level());
    // Check values adjusted correctly
    Assert.assertEquals(level+1, adj.level());
    Assert.assertEquals(uri, adj.element().uri());
    Assert.assertEquals(title, adj.title());
    Assert.assertEquals(level+2, adj.parts().get(0).level());
    Assert.assertEquals(level+2, adj.parts().get(1).level());
  }
/*
  @Test
  public void testReplace() {
    Part<Embed> suba = ref(2, "X", 11L);
    Part<Embed> subb = ref(2, "Y", 12L);
    Part<Embed> subc = ref(2, "Z", 13L);
    Part<Embed> ref = ref(1, "", 10L, ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Part<Embed> repa = ref.replace(suba);
    Part<Embed> repb = ref.replace(subb);
    Part<Embed> repc = ref.replace(subc);
    String abc = ref.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String xbc = repa.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String ayc = repb.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String abz = repc.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    Assert.assertEquals("ABC", abc);
    Assert.assertEquals("XBC", xbc);
    Assert.assertEquals("AYC", ayc);
    Assert.assertEquals("ABZ", abz);
  }

  @Test
  public void testReplace_not() {
    Part<Embed> sub = ref(2, "X", 23L);
    Part<Embed> ref = ref(1, "", 10L, ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Part<Embed> rep = ref.replace(sub);
    String abc = ref.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String abc2 = rep.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    Assert.assertEquals("ABC", abc);
    Assert.assertEquals("ABC", abc2);
  }
*/
  @Test
  public void testAttach() {
    List<Part<?>> subs = Arrays.asList(ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Part<Reference> ref = ref(1, "ROOT", 10L);
    Part<Reference> att = ref.attach(subs);
    String none = ref.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String abc = att.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    Assert.assertEquals("", none);
    Assert.assertEquals("ABC", abc);
  }
/*
  @Test
  public void testAttachIfMatch_not() {
    List<Ref> subs = Arrays.asList(ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Ref ref = ref(1, "ROOT", 10L);
    Ref att = ref.attachIfMatch(90L, subs);
    String none = ref.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String abc = att.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    Assert.assertEquals("", none);
    Assert.assertEquals("", abc);
  }

  @Test
  public void testAttachIfMatch_0() {
    List<Ref> subs = Arrays.asList(ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Ref ref = ref(1, 10L, "ROOT");
    Ref att = ref.attachIfMatch(10L, subs);
    String none = ref.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String abc = att.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    Assert.assertEquals("", none);
    Assert.assertEquals("ABC", abc);
  }

  @Test
  public void testAttachIfMatch_deep() {
    List<Ref> subs = Arrays.asList(ref(2, 21L, "d"), ref(2, 22L, "e"), ref(2, 23L, "f", Ref.DEFAULT_TYPE));
    Ref ref = ref(1, 10L, "ROOT", ref(2, 11L, "A"), ref(2, 12L, "B"), ref(2, 13L, "C"));
    Ref att = ref.attachIfMatch(12L, subs);
    String org = ref.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String abc = att.parts().stream().map(r -> r.title()).collect(Collectors.joining());
    String def = att.parts().get(1).parts().stream().map(r -> r.title()).collect(Collectors.joining());
    Assert.assertEquals("ABC", org);
    Assert.assertEquals("ABC", abc);
    Assert.assertEquals("def", def);
  }
*/
  @Test
  public void testFind() {
    Part<Reference> ref = ref(1, "ROOT", 10L, ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Part<Reference> root = Part.find(ref, 10L);
    Part<Reference> a = Part.find(ref, 11L);
    Part<Reference> b = Part.find(ref, 12L);
    Part<Reference> c = Part.find(ref, 13L);
    Assert.assertNotNull(root);
    Assert.assertEquals("ROOT", root.title());
    Assert.assertNotNull(a);
    Assert.assertEquals("A", a.title());
    Assert.assertNotNull(b);
    Assert.assertEquals("B", b.title());
    Assert.assertNotNull(c);
    Assert.assertEquals("C", c.title());
    Assert.assertNull(Part.find(ref, 20L));
  }

}