package org.pageseeder.psml.split;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.split.PSMLSplitter.Builder;

import java.io.File;
import java.io.IOException;

public class PSMLSplitterTest {
  private static final String SOURCE_FOLDER = "src/test/data/split";
  private static final String DEST_FOLDER = "build/test/split/result";
  private static final String COPY_FOLDER = "build/test/split/copy";

  @Test
  public void testEmptyConfig() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_1.psml");
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(new File(src, "empty_config.xml"));
    PSMLSplitter s = b.build();
    s.process();
  }

}