package com.beijunyi.parallelgit.utils;

import java.io.IOException;

import com.beijunyi.parallelgit.AbstractParallelGitTest;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TreeUtilsTest extends AbstractParallelGitTest {

  private static void assertNextEntry(TreeWalk treeWalk, String path) throws IOException {
    assertTrue(treeWalk.next());
    assertEquals(path, treeWalk.getPathString());
  }

  @Before
  public void setUp() throws Exception {
    initRepository();
  }

  @Test
  public void newTreeWalkTest() throws IOException {
    clearCache();
    writeMultipleToCache("/a.txt", "/b.txt", "/c/d.txt", "/c/e.txt", "/f/g.txt");
    RevTree tree = commitToMaster().getTree();
    TreeWalk treeWalk = TreeUtils.newTreeWalk(tree, repo);

    assertNextEntry(treeWalk, "a.txt");
    assertNextEntry(treeWalk, "b.txt");
    assertNextEntry(treeWalk, "c");
    assertNextEntry(treeWalk, "f");
    assertFalse(treeWalk.next());
  }

  @Test
  public void existsTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertTrue(TreeUtils.exists("a", tree, repo));
    assertTrue(TreeUtils.exists("a/b.txt", tree, repo));
    assertFalse(TreeUtils.exists("a/b", tree, repo));
  }

  @Test
  public void getObjectTest() throws IOException {
    AnyObjectId objectId = writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertEquals(objectId, TreeUtils.getObjectId("a/b.txt", tree, repo));
  }

  @Test
  public void isBlobTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertFalse(TreeUtils.isFileOrSymbolicLink("a", tree, repo));
    assertTrue(TreeUtils.isFileOrSymbolicLink("a/b.txt", tree, repo));
    assertFalse(TreeUtils.isFileOrSymbolicLink("a/b", tree, repo));
  }

  @Test
  public void isTreeTest() throws IOException {
    writeToCache("a/b.txt");
    RevTree tree = commitToMaster().getTree();
    assertTrue(TreeUtils.isDirectory("a", tree, repo));
    assertFalse(TreeUtils.isDirectory("a/b.txt", tree, repo));
    assertFalse(TreeUtils.isDirectory("a/b", tree, repo));
  }
}
