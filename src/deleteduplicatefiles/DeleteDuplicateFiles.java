/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deleteduplicatefiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 */
public class DeleteDuplicateFiles {
    long maxFileSizeInBytes = 1024 * 1024 * 1024;
    
    // Set to skip hidden files
    private boolean skipHidden = true;

    //List<String> rootPaths = new ArrayList();
    private String rootPath = null;
    
    private List<File> skipPaths = new ArrayList();
    
    // Map from unique key to the list of things that have that uniue key (ie. MD5->[File])
    private HashMap<String,List<Thing>> things = new HashMap();
 
    
    public DeleteDuplicateFiles(String path) {
        this.rootPath = path;
    }

      
    public void addSkipFolder(String path) {
        File file = new File(path);
        //TODO: check is a directory and it exists!
        skipPaths.add(file);
    }
    
    
    public void setSkipHidden(boolean skipHidden) {
        this.skipHidden = skipHidden;
    }
    

    public void walk() {
        walk(false);
    }

    
    public void walk(boolean skipFilesInRoot) {
        System.out.println("Walking " + rootPath);
        long numFiles = walk(new File(rootPath), skipFilesInRoot, 0);
        System.out.println("Found " + numFiles + " file(s), of which " + things.size() + " are unique");
    }
    
    // File first walk of the directory tree
    private long walk(File root, boolean skipFilesInRoot, long totalFiles) {
        int numFiles = 0;
        if (!root.canRead()) {
            System.err.println("Cannot read " + root.getAbsolutePath());
            return totalFiles; 
        }
        
        File[] files = root.listFiles();
        //System.out.println("Processing " + files.length +" files in " + path + (skipFilesInRoot ? " (folders only)" : ""));
        Stack<File> subdirs = new Stack();

        if (files != null) {
            for (File f : files) {
                String path = f.getAbsolutePath();
                if (f.length() > maxFileSizeInBytes) {
                    System.out.println("Skipping large file " + path);
                } else if (skipHidden && f.isHidden()) {
                    System.out.println("Skipping hidden file " + path);
                } else if (f.isDirectory()) {
                    subdirs.push(f);
                } else {
                    if (!skipFilesInRoot) {
                        //System.out.println("File:" + f.getAbsoluteFile());
                        Thing t = new Thing(f);
                        try {
                            String key = t.getKey();
                            List<Thing> list;
                            if (things.containsKey(key))
                                list = things.get(key);
                            else {
                                list = new ArrayList();
                                things.put(key, list);
                            }
                            list.add(t);
                        } catch (Exception e) {
                            System.err.println("Something went wrong getting key for " + path);
                        }
                        numFiles++;
                        //if (numFiles % 100 == 0) System.out.print(".");
                    }
                }
            }
        }
        //System.out.println(" (" + numFiles + ")");
        
        for (File dir : subdirs) {
            if (skipPaths.contains(dir))
                System.out.println("Skipping directory " + dir.getAbsolutePath());
            else numFiles += walk(dir, false, totalFiles);
        }

        return totalFiles + numFiles;
    }
    
    
    public void process(boolean delete) {
        int numDeleted = 0;
        long size = 0;
        for (String key : things.keySet()) {
            List<Thing> list = things.get(key);
            if (list.size() > 1) {
                System.out.println(key + ", " + list.size() + " copies:");
                try {
                    Collections.sort(list);
                } catch (Exception e) {
                    for(Thing thing : list) {
                        System.err.println(thing.getAbsolutePath());
                    }
                    return;
                }
                boolean isFirst = true;
                for(Thing thing : list) {
                    if (isFirst) {
                        System.out.println("\tKeeping " + thing.getAbsolutePath());
                        isFirst = false;
                    } else {
                        size += (thing.sizeInBytes() / 1024);
                        if (delete) {
                            System.out.print("\tDeleting ");
                            thing.delete();
                        } else System.out.print("\tDuplicate ");
                        numDeleted++;
                        System.out.println(thing.getAbsolutePath());
                    }
                }
                //System.out.print("x");
            } //else System.out.print("o");
        }
        System.out.println((delete ? "Deleted " : "Could delete ") + numDeleted + " file(s) for a saving of " + (size / 1024)  + "MB");
    }

    
    public static void main(String[] args) {
        DeleteDuplicateFiles me = new DeleteDuplicateFiles("C:\\Users\\e1049172\\Documents");
        me.addSkipFolder("C:\\Users\\e1049172\\Documents\\Memento");
        me.addSkipFolder("C:\\Users\\e1049172\\Documents\\Outlook Files");
        //me.addSkipFolder("C:\\Users\\e1049172\\Documents\\Software\\VMs");
        
        // Walk the file tree and collect information
        me.walk(false);
        
        //
        me.process(false);
    }
}