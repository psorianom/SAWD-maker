//Created by E.P. Soriano Morales
package edu.eric.text2stanford;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pavel on 16/12/14.
 */
public class Utils {

    public static ArrayList<String> listFiles(String pathFolder) {
        ArrayList<String> filesPaths = new ArrayList<>();
        final File folder = new File(pathFolder);
        ArrayList<String> finalFilesPaths = new ArrayList<>();

        if (folder.isFile()) {
            finalFilesPaths.add(pathFolder);
            return finalFilesPaths;
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                filesPaths.addAll(listFiles(fileEntry.getPath()));
            } else {
                filesPaths.add(fileEntry.getPath());
//                System.out.println(fileEntry.getName());

            }
        }
        for (final String path : filesPaths) {
            String[] pathLevels = path.split("/");
            String fileName = pathLevels[pathLevels.length - 1];
            String firstChar = fileName.substring(0, 1);
            /// If filepath is not metadata json file, and if it has the required extensions and if it is not hidden file
            if (!path.contains("metadata") && !path.contains("parsed") && !path.contains("MMMatrix") && !firstChar.contains("."))
                finalFilesPaths.add(path);
        }
        return finalFilesPaths;
    }

    public static ArrayList<String> removeAlreadyParsedFolders(ArrayList<String> listStrings, String pickup, String limit) {
        int i;
        ArrayList<String> newList = listStrings;
        if (!pickup.isEmpty()) {
            for (i = 0; i < listStrings.size(); i++)
                if (listStrings.get(i).contains(pickup)) {
                    newList = new ArrayList<>(listStrings.subList(i, listStrings.size()));
                    break;
                }
        }

        if (!limit.isEmpty()) {
            for (i = 0; i < newList.size(); i++)
                if (newList.get(i).contains(limit)) {
                    newList = new ArrayList<>(newList.subList(0, i));
                    break;
                }
        }
        return newList;
    }


    public static String getFileName(String fullPath, boolean withParentFolder) {
        Path p = Paths.get(fullPath);

        String file = p.getFileName().toString();

        if (withParentFolder)
            file = p.getParent().getFileName() + "/" + file;
        return file;
    }

    public static void main(String[] args) {
        listFiles("/media/stuff/Pavel/Documents/Eclipse/workspace/data/these_graph/oanc/");
    }

    public static class DefaultDict<K, V> extends HashMap<K, V> {

        Class<V> klass;

        public DefaultDict() {
            this.klass = (Class<V>) ArrayList.class;
        }

        public DefaultDict(Class klass) {
            this.klass = klass;
        }

        @Override
        public V get(Object key) {
            V returnValue = super.get(key);
            if (returnValue == null) {
                try {
                    returnValue = klass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                this.put((K) key, returnValue);
            }
            return returnValue;
        }
    }
}
