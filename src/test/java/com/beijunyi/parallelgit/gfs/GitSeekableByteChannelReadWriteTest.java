package com.beijunyi.parallelgit.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GitSeekableByteChannelReadWriteTest extends AbstractGitFileSystemTest {

  private static final byte[] ORIGINAL_TEXT_BYTES = "some plain text data".getBytes();

  @Before
  public void setupFileSystem() throws IOException {
    initRepository();
    writeFile("file.txt", ORIGINAL_TEXT_BYTES);
    commitToMaster();
    initGitFileSystem();
  }

  @Test
  public void gitByteChannelReadTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      ByteBuffer buf = ByteBuffer.allocate(ORIGINAL_TEXT_BYTES.length);
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.read(buf));
      Assert.assertArrayEquals(ORIGINAL_TEXT_BYTES, buf.array());
    }
  }

  @Test
  public void gitByteChannelReadWithSmallBufferTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      int size = ORIGINAL_TEXT_BYTES.length / 2;
      ByteBuffer buf = ByteBuffer.allocate(size);
      Assert.assertEquals(size, channel.read(buf));
      byte[] subArrayOfOriginal = new byte[size];
      System.arraycopy(ORIGINAL_TEXT_BYTES, 0, subArrayOfOriginal, 0, size);
      Assert.assertArrayEquals(subArrayOfOriginal, buf.array());
    }
  }

  @Test
  public void gitByteChannelReadWithBigBufferTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      int size = ORIGINAL_TEXT_BYTES.length * 2;
      ByteBuffer buf = ByteBuffer.allocate(size);
      Assert.assertEquals(ORIGINAL_TEXT_BYTES.length, channel.read(buf));
      byte[] subArrayOfResult = new byte[ORIGINAL_TEXT_BYTES.length];
      System.arraycopy(buf.array(), 0, subArrayOfResult, 0, ORIGINAL_TEXT_BYTES.length);
      Assert.assertArrayEquals(ORIGINAL_TEXT_BYTES, subArrayOfResult);
    }
  }

  @Test
  public void gitByteChannelPartialOverwriteFromMiddleTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    int overwritePos = 5;
    byte[] data = "other".getBytes();
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      ByteBuffer buf = ByteBuffer.wrap(data);
      channel.position(overwritePos);
      Assert.assertEquals(data.length, channel.write(buf));
    }
    byte[] expect = new byte[ORIGINAL_TEXT_BYTES.length];
    System.arraycopy(ORIGINAL_TEXT_BYTES, 0, expect, 0, ORIGINAL_TEXT_BYTES.length);
    System.arraycopy(data, 0, expect, overwritePos, data.length);
    Assert.assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelPartialOverwriteFromBeginningTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    byte[] data = "test".getBytes();
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      ByteBuffer buf = ByteBuffer.wrap(data);
      Assert.assertEquals(data.length, channel.write(buf));
    }
    byte[] expect = new byte[ORIGINAL_TEXT_BYTES.length];
    System.arraycopy(data, 0, expect, 0, data.length);
    System.arraycopy(ORIGINAL_TEXT_BYTES, data.length, expect, data.length, ORIGINAL_TEXT_BYTES.length - data.length);
    Assert.assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelCompleteOverwriteTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    byte[] data = "this is a big data array that will completely overwrite".getBytes();
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      ByteBuffer buf = ByteBuffer.wrap(data);
      Assert.assertEquals(data.length, channel.write(buf));
    }
    Assert.assertArrayEquals(data, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelTruncateAfterCurrentPositionTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    int pos = 4;
    int truncatePos = 10;
    byte[] expect = new byte[truncatePos];
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      channel.position(pos);
      channel.truncate(truncatePos);
      Assert.assertEquals(truncatePos, channel.size());
      Assert.assertEquals(pos, channel.position());
      System.arraycopy(ORIGINAL_TEXT_BYTES, 0, expect, 0, truncatePos);
    }
    Assert.assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test
  public void gitByteChannelTruncateBeforeCurrentPositionTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    int pos = 10;
    int truncatePos = 4;
    byte[] expect = new byte[truncatePos];
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      channel.position(pos);
      channel.truncate(truncatePos);
      Assert.assertEquals(truncatePos, channel.size());
      Assert.assertEquals(truncatePos, channel.position());
      System.arraycopy(ORIGINAL_TEXT_BYTES, 0, expect, 0, truncatePos);
    }
    Assert.assertArrayEquals(expect, Files.readAllBytes(file));
  }

  @Test(expected = NonReadableChannelException.class)
  public void nonReadableGitByteChannelTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE)) {
      channel.read(ByteBuffer.allocate(32));
    }
  }

  @Test(expected = NonWritableChannelException.class)
  public void nonWritableGitByteChannelTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      channel.write(ByteBuffer.wrap("some data".getBytes()));
    }
  }

  @Test(expected = ClosedChannelException.class)
  public void closedGitByteChannelTest() throws IOException {
    GitPath file = gfs.getPath("/file.txt");
    try(SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
      channel.close();
      channel.read(ByteBuffer.allocate(32));
    }
  }


}
