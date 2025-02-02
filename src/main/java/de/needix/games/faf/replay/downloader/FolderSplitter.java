package de.needix.games.faf.replay.downloader;

import java.io.File;

public class FolderSplitter {

    public static void main(String[] args) {
        // Path to the source folder containing the files
        String sourceFolderPath = "Z:\\";

        // Maximum number of files per subfolder
        int maxFilesPerFolder = 100000;

        splitFolderIntoSubfolders(sourceFolderPath, maxFilesPerFolder);
    }

    public static void splitFolderIntoSubfolders(String sourceFolderPath, int maxFilesPerFolder) {
        File sourceFolder = new File(sourceFolderPath);

        // Check if the source folder exists
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            System.out.println("The specified source folder does not exist or is not a directory: " + sourceFolderPath);
            return;
        }

        int folderIndex = 0;

        File currentSubfolder = createSubfolder(sourceFolder, folderIndex, false);
        while (currentSubfolder.exists()) {
            folderIndex++;
            currentSubfolder = createSubfolder(sourceFolder, folderIndex, false);
        }
        folderIndex--;
        currentSubfolder = createSubfolder(sourceFolder, folderIndex, true);

        File[] filesInCurrentSubfolder = currentSubfolder.listFiles();
        int fileCount = filesInCurrentSubfolder == null ? 0 : filesInCurrentSubfolder.length;
        while (fileCount >= maxFilesPerFolder) {
            folderIndex++;
            currentSubfolder = createSubfolder(sourceFolder, folderIndex, true);
            filesInCurrentSubfolder = currentSubfolder.listFiles();
            fileCount = filesInCurrentSubfolder == null ? 0 : filesInCurrentSubfolder.length;
        }

        File[] files = sourceFolder.listFiles((dir, name) -> name.matches("replay-\\d+\\.fafreplay"));
        if (files == null || files.length == 0) {
            System.out.println("No matching files found in the source folder.");
            return;
        }

        System.out.println("Total files to process: " + files.length);

        for (File file : files) {
            if (fileCount >= maxFilesPerFolder) {
                folderIndex++;
                fileCount = 0;
                currentSubfolder = createSubfolder(sourceFolder, folderIndex, true);
            }

            try {
                File destination = new File(currentSubfolder, file.getName());
                if (file.renameTo(destination)) {
                    fileCount++;
                } else {
                    System.err.println("Failed to move file: " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Error while moving file: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }

        System.out.println("Folder splitting complete. Created " + (folderIndex + 1) + " subfolders.");
    }

    private static File createSubfolder(File parentFolder, int index, boolean createIfMissing) {
        File subfolder = new File(parentFolder, "subfolder-" + index);
        if (createIfMissing && !subfolder.exists()) {
            if (subfolder.mkdir()) {
                System.out.println("Created subfolder: " + subfolder.getAbsolutePath());
            } else {
                System.err.println("Failed to create subfolder: " + subfolder.getAbsolutePath());
            }
        }
        return subfolder;
    }
}