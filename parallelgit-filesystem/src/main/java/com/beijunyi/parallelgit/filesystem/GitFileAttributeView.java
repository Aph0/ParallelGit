package com.beijunyi.parallelgit.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.*;
import javax.annotation.Nonnull;

public abstract class GitFileAttributeView implements FileAttributeView {

  protected final GitFileStore store;
  protected final GitPath path;

  public GitFileAttributeView(@Nonnull GitFileStore store, @Nonnull GitPath path) {
    this.store = store;
    this.path = path;
  }

  @Nonnull
  public abstract Map<String, Object> readAttributes(@Nonnull Collection<String> attributes) throws IOException;

  public static class Basic extends GitFileAttributeView implements BasicFileAttributeView {

    public static final FileTime EPOCH = FileTime.fromMillis(0);

    public static final String SIZE_NAME = "size";
    public static final String CREATION_TIME_NAME = "creationTime";
    public static final String LAST_ACCESS_TIME_NAME = "lastAccessTime";
    public static final String LAST_MODIFIED_TIME_NAME = "lastModifiedTime";
    public static final String FILE_KEY_NAME = "fileKey";
    public static final String IS_DIRECTORY_NAME = "isDirectory";
    public static final String IS_REGULAR_FILE_NAME = "isRegularFile";
    public static final String IS_SYMBOLIC_LINK_NAME = "isSymbolicLink";
    public static final String IS_OTHER_NAME = "isOther";

    public static final String BASIC_VIEW = "basic";
    public static final Collection<String> BASIC_KEYS =
      Collections.unmodifiableCollection(Arrays.asList(
                                                        SIZE_NAME,
                                                        CREATION_TIME_NAME,
                                                        LAST_ACCESS_TIME_NAME,
                                                        LAST_MODIFIED_TIME_NAME,
                                                        FILE_KEY_NAME,
                                                        IS_DIRECTORY_NAME,
                                                        IS_REGULAR_FILE_NAME,
                                                        IS_SYMBOLIC_LINK_NAME,
                                                        IS_OTHER_NAME
      ));

    public Basic(@Nonnull GitFileStore store, @Nonnull GitPath path) {
      super(store, path);
    }

    @Nonnull
    @Override
    public String name() {
      return BASIC_VIEW;
    }

    @Nonnull
    @Override
    public BasicFileAttributes readAttributes() throws IOException {
      return new GitFileAttributes.Basic(readAttributes(BASIC_KEYS));
    }

    @Override
    public void setTimes(@Nonnull FileTime lastModifiedTime, @Nonnull FileTime lastAccessTime, @Nonnull FileTime createTime) {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(@Nonnull Collection<String> keys) throws IOException {
      if(!store.fileExists(path) && !store.isDirectory(path))
        throw new NoSuchFileException(path);

      Map<String, Object> result = new HashMap<>();
      for(String key : keys) {
        switch(key) {
          case SIZE_NAME:
            result.put(key, store.getFileSize(path));
            break;
          case CREATION_TIME_NAME:
            result.put(key, EPOCH);
            break;
          case LAST_ACCESS_TIME_NAME:
            result.put(key, EPOCH);
            break;
          case LAST_MODIFIED_TIME_NAME:
            result.put(key, EPOCH);
            break;
          case FILE_KEY_NAME:
            result.put(key, null);
            break;
          case IS_DIRECTORY_NAME:
            result.put(key, store.isDirectory(path));
            break;
          case IS_REGULAR_FILE_NAME:
            result.put(key, store.isRegularFile(path));
            break;
          case IS_SYMBOLIC_LINK_NAME:
            result.put(key, store.isSymbolicLink(path));
            break;
          case IS_OTHER_NAME:
            result.put(key, false);
            break;
          default:
            throw new IllegalArgumentException("Attribute \"" + key + "\" is not supported");
        }
      }
      return Collections.unmodifiableMap(result);
    }
  }

  public static class Posix extends Basic implements PosixFileAttributeView {

    private static final Collection<PosixFilePermission> DEFAULT_PERMISSIONS =
      Collections.unmodifiableCollection(Arrays.asList(
                                                        PosixFilePermission.OWNER_READ,
                                                        PosixFilePermission.OWNER_WRITE,
                                                        PosixFilePermission.GROUP_READ,
                                                        PosixFilePermission.OTHERS_READ
      ));

    public static final String PERMISSIONS_NAME = "permissions";
    public static final String OWNER_NAME = "owner";
    public static final String GROUP_NAME = "group";

    public static final String POSIX_VIEW = "posix";
    public static final Collection<String> POSIX_KEYS =
      Collections.unmodifiableCollection(Arrays.asList(
                                                        SIZE_NAME,
                                                        CREATION_TIME_NAME,
                                                        LAST_ACCESS_TIME_NAME,
                                                        LAST_MODIFIED_TIME_NAME,
                                                        FILE_KEY_NAME,
                                                        IS_DIRECTORY_NAME,
                                                        IS_REGULAR_FILE_NAME,
                                                        IS_SYMBOLIC_LINK_NAME,
                                                        IS_OTHER_NAME,
                                                        PERMISSIONS_NAME,
                                                        OWNER_NAME,
                                                        GROUP_NAME
      ));

    private Path repoDir;
    private PosixFileAttributes repoAttributes;

    public Posix(@Nonnull GitFileStore store, @Nonnull GitPath path) {
      super(store, path);
    }

    @Nonnull
    public Path getRepoDir() {
      if(repoDir == null)
        repoDir = store.getRepository().getDirectory().toPath();
      return repoDir;
    }

    @Nonnull
    public PosixFileAttributes getRepoAttributes() throws IOException {
      if(repoAttributes == null)
        repoAttributes = Files.getFileAttributeView(getRepoDir(), PosixFileAttributeView.class).readAttributes();
      return repoAttributes;
    }

    @Nonnull
    @Override
    public String name() {
      return POSIX_VIEW;
    }

    @Nonnull
    @Override
    public PosixFileAttributes readAttributes() throws IOException {
      return new GitFileAttributes.Posix(readAttributes(POSIX_KEYS));
    }

    @Nonnull
    public Set<PosixFilePermission> getPermissions() throws IOException {
      Set<PosixFilePermission> perms = new HashSet<>(DEFAULT_PERMISSIONS);
      if(store.isExecutableFile(path))
        perms.add(PosixFilePermission.OWNER_EXECUTE);
      return Collections.unmodifiableSet(perms);
    }


    @Override
    public void setPermissions(@Nonnull Set<PosixFilePermission> perms) throws IOException {

    }

    @Nonnull
    public GroupPrincipal getGroup() throws IOException {
      return getRepoAttributes().group();
    }

    @Override
    public void setGroup(@Nonnull GroupPrincipal group) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public UserPrincipal getOwner() throws IOException {
      return getRepoAttributes().owner();
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Map<String, Object> readAttributes(@Nonnull Collection<String> keys) throws IOException {
      Set<String> basicKeys = new HashSet<>(keys);
      basicKeys.retainAll(BASIC_KEYS);
      Map<String, Object> result = new HashMap<>(super.readAttributes(basicKeys));
      Set<String> remainKeys = new HashSet<>(keys);
      remainKeys.removeAll(result.keySet());
      for(String key : remainKeys) {
        switch(key) {
          case PERMISSIONS_NAME:
            result.put(key, getPermissions());
            break;
          case OWNER_NAME:
            result.put(key, getOwner());
            break;
          case GROUP_NAME:
            result.put(key, getGroup());
            break;
          default:
            throw new IllegalArgumentException("Attribute \"" + key + "\" is not supported");
        }
      }
      return Collections.unmodifiableMap(result);
    }
  }

}
