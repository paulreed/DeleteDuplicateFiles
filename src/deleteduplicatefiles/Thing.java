/*
 */
package deleteduplicatefiles;

import java.io.File;

/**
 */
public class Thing implements Comparable {
    String key = null;
    File f;
    
    
    public Thing(File f) {
        this.f = f;
    }
    
    
    public boolean delete() {
        return f.delete();
    }
    
    
    public String getKey() throws Exception {
        if (key == null)
            key = MD5Checksum.getMD5Checksum(f.getAbsolutePath());
        return key;
    }
    
    
    public String getAbsolutePath() {
        return f.getAbsolutePath();
    }
  
    
    public long getLastModified() {
        return f.lastModified();
    }

    
    public long sizeInBytes() {
        return f.length();
    }

    
    public int compareTo(Object o) {
        Thing that = (Thing)o;
        
        // First, compare last modified times
        int i = Long.valueOf(that.getLastModified()).compareTo(Long.valueOf(this.getLastModified()));
        if (i != 0) return i;
        
        // If they're the same, then use filename
        return that.getAbsolutePath().compareTo(this.getAbsolutePath()) ; 
    }
}
