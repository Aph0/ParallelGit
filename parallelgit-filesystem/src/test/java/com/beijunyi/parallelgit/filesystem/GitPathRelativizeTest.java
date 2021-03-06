package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitPathRelativizeTest extends AbstractGitFileSystemTest {

  @Before
  public void setupFileSystem() throws IOException {
    initGitFileSystem();
  }

  @Test
  public void absolutePathRelativizesParentTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath parent = gfs.getPath("/a/b");
    GitPath result = path.relativize(parent);
    assertEquals("..", result.toString());
  }

  @Test
  public void absolutePathRelativizesGrandParentTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath parent = gfs.getPath("/a");
    GitPath result = path.relativize(parent);
    assertEquals("../..", result.toString());
  }

  @Test
  public void absolutePathRelativizesChildTest() {
    GitPath path = gfs.getPath("/a/b");
    GitPath child = gfs.getPath("/a/b/c");
    GitPath result = path.relativize(child);
    assertEquals("c", result.toString());
  }

  @Test
  public void absolutePathRelativizesGrandChildTest() {
    GitPath path = gfs.getPath("/a");
    GitPath child = gfs.getPath("/a/b/c");
    GitPath result = path.relativize(child);
    assertEquals("b/c", result.toString());
  }

  @Test
  public void absolutePathRelativizesSiblingTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath sibling = gfs.getPath("/a/b/d");
    GitPath result = path.relativize(sibling);
    assertEquals("../d", result.toString());
  }

  @Test
  public void absolutePathRelativizesSamePathTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath samePath = gfs.getPath("/a/b/c");
    GitPath result = path.relativize(samePath);
    assertEquals("", result.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void absolutePathRelativizesEmptyPathTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath emptyPath = gfs.getPath("");
    path.relativize(emptyPath);
  }

  @Test(expected = IllegalArgumentException.class)
  public void absolutePathRelativizesRelativePathTest() {
    GitPath path = gfs.getPath("/a/b");
    GitPath relativePath = gfs.getPath("a/b/c");
    path.relativize(relativePath);
  }

  @Test
  public void rootPathRelativizesAbsolutePathTest() {
    GitPath path = gfs.getPath("/");
    GitPath absolutePath = gfs.getPath("/a/b/c");
    GitPath result = path.relativize(absolutePath);
    assertEquals("a/b/c", result.toString());
  }

  @Test
  public void rootPathRelativizesRootPathTest() {
    GitPath path = gfs.getPath("/");
    GitPath rootPath = gfs.getPath("/");
    GitPath result = path.relativize(rootPath);
    assertEquals("", result.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void rootPathRelativizesRelativePathTest() {
    GitPath path = gfs.getPath("/");
    GitPath relativePath = gfs.getPath("a/b/c");
    GitPath result = path.relativize(relativePath);
    assertEquals(relativePath.toString(), result.toString());
  }

  @Test
  public void relativePathRelativizesParentTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath parent = gfs.getPath("a/b");
    GitPath result = path.relativize(parent);
    assertEquals("..", result.toString());
  }

  @Test
  public void relativePathRelativizesGrandParentTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath parent = gfs.getPath("a");
    GitPath result = path.relativize(parent);
    assertEquals("../..", result.toString());
  }

  @Test
  public void relativePathRelativizesChildTest() {
    GitPath path = gfs.getPath("a/b");
    GitPath child = gfs.getPath("a/b/c");
    GitPath result = path.relativize(child);
    assertEquals("c", result.toString());
  }

  @Test
  public void relativePathRelativizesGrandChildTest() {
    GitPath path = gfs.getPath("a");
    GitPath child = gfs.getPath("a/b/c");
    GitPath result = path.relativize(child);
    assertEquals("b/c", result.toString());
  }

  @Test
  public void relativePathRelativizesSiblingTest() {
    GitPath path = gfs.getPath("/a/b/c");
    GitPath sibling = gfs.getPath("/a/b/d");
    GitPath result = path.relativize(sibling);
    assertEquals("../d", result.toString());
  }

  @Test
  public void relativePathRelativizesSamePathTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath samePath = gfs.getPath("a/b/c");
    GitPath result = path.relativize(samePath);
    assertEquals("", result.toString());
  }

  @Test
  public void relativePathRelativizesEmptyPathTest() {
    GitPath path = gfs.getPath("a/b/c");
    GitPath emptyPath = gfs.getPath("");
    GitPath result = path.relativize(emptyPath);
    assertEquals("../../..", result.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void relativePathRelativizesAbsolutePathTest() {
    GitPath path = gfs.getPath("a/b");
    GitPath absolutePath = gfs.getPath("/a/b/c");
    path.relativize(absolutePath);
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyPathRelativizesAbsolutePathTest() {
    GitPath path = gfs.getPath("");
    GitPath absolutePath = gfs.getPath("/a/b/c");
    GitPath result = path.relativize(absolutePath);
    assertEquals(absolutePath.toString(), result.toString());
  }

  @Test
  public void emptyPathRelativizesRelativePathTest() {
    GitPath path = gfs.getPath("");
    GitPath relativePath = gfs.getPath("a/b/c");
    GitPath result = path.relativize(relativePath);
    assertEquals(relativePath.toString(), result.toString());
  }

  @Test
  public void emptyPathRelativizesEmptyPathTest() {
    GitPath path = gfs.getPath("");
    GitPath emptyPath = gfs.getPath("");
    GitPath result = path.relativize(emptyPath);
    assertEquals(emptyPath.toString(), result.toString());
  }

}
