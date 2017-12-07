/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package huyang.edu.cn;

import com.google.common.base.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public final class HadoopUtil {

  private static final Logger log = LoggerFactory.getLogger(HadoopUtil.class);

  private HadoopUtil() { }

  public static void delete(Configuration conf, Iterable<Path> paths) throws IOException {
    if (conf == null) {
      conf = new Configuration();
    }
    for (Path path : paths) {
      FileSystem fs = path.getFileSystem(conf);
      if (fs.exists(path)) {
        log.info("Deleting {}", path);
        fs.delete(path, true);
      }
    }
  }

  public static void delete(Configuration conf, Path... paths) throws IOException {
    delete(conf, Arrays.asList(paths));
  }

  public static FileStatus[] listStatus(FileSystem fs, Path path) throws IOException {
    try {
      return fs.listStatus(path);
    } catch (FileNotFoundException e) {
      return new FileStatus[0];
    }
  }

  public static FileStatus[] listStatus(FileSystem fs, Path path, PathFilter filter) throws IOException {
    try {
      return fs.listStatus(path, filter);
    } catch (FileNotFoundException e) {
      return new FileStatus[0];
    }
  }

  public static void cacheFiles(Path fileToCache, Configuration conf) {
    DistributedCache.setCacheFiles(new URI[]{fileToCache.toUri()}, conf);
  }

  public static Path getSingleCachedFile(Configuration conf) throws IOException {
    return getCachedFiles(conf)[0];
  }

  public static Path[] getCachedFiles(Configuration conf) throws IOException {
    LocalFileSystem localFs = FileSystem.getLocal(conf);
    Path[] cacheFiles = DistributedCache.getLocalCacheFiles(conf);

    URI[] fallbackFiles = DistributedCache.getCacheFiles(conf);

    // fallback for local execution
    if (cacheFiles == null) {

      Preconditions.checkState(fallbackFiles != null, "Unable to find cached files!");

      cacheFiles = new Path[fallbackFiles.length];
      for (int n = 0; n < fallbackFiles.length; n++) {
        cacheFiles[n] = new Path(fallbackFiles[n].getPath());
      }
    } else {

      for (int n = 0; n < cacheFiles.length; n++) {
        cacheFiles[n] = localFs.makeQualified(cacheFiles[n]);
        // fallback for local execution
        if (!localFs.exists(cacheFiles[n])) {
          cacheFiles[n] = new Path(fallbackFiles[n].getPath());
        }
      }
    }

    Preconditions.checkState(cacheFiles.length > 0, "Unable to find cached files!");

    return cacheFiles;
  }
}
