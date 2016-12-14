package de.gridka.dcache.nearline.hpss;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

import org.dcache.pool.nearline.spi.StageRequest;
import org.dcache.util.Checksum;
import org.dcache.vehicles.FileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diskCacheV111.util.CacheException;

class StageTask implements Callable<Set<Checksum>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Dc2HpssNearlineStorage.class);
  private Path path;
  private Path externalPath;
  
  public StageTask(StageRequest request, String mountpoint) {
    LOGGER.trace(String.format("Create new StageTask for %s.", request.toString()));
    FileAttributes fileAttributes = request.getFileAttributes();
    String pnfsId = fileAttributes.getPnfsId().toString();
    this.path = request.getFile().toPath();
    
    String hsmPath = String.format("/%s/%s/%s/%s",
        fileAttributes.getStorageInfo().getKey("group"),
        pnfsId.charAt(pnfsId.length() - 1),
        pnfsId.charAt(pnfsId.length() - 2),
        pnfsId
      );
    this.externalPath = Paths.get(mountpoint, hsmPath);
    LOGGER.trace(String.format("StageTask %s has to copy %s to %s.", request.toString(), externalPath, path));
  }
  
  public Set<Checksum> call () throws CacheException {
    try {
      LOGGER.debug(String.format("Copy %s to %s.", externalPath, path));
      Files.copy(externalPath, path);
    } catch (IOException e) {
      throw new CacheException(2, "Copy to " + externalPath.toString() + " failed.", e);
    }
    
    return Collections.emptySet();
  }
}