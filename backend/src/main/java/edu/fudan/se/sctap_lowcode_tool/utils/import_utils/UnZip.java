package edu.fudan.se.sctap_lowcode_tool.utils.import_utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for handling ZIP file operations.
 */
public class UnZip {

    /**
     * Deletes a directory and all its contents.
     *
     * @param directoryToBeDeleted the directory to be deleted
     * @return true if the directory was successfully deleted, false otherwise
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * Deletes a directory and all its contents.
     *
     * @param directoryToBeDeleted the directory to be deleted
     * @return true if the directory was successfully deleted, false otherwise
     */
    public static boolean deleteDirectory(Path directoryToBeDeleted) {
        return deleteDirectory(directoryToBeDeleted.toFile());
    }


    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Unzips a multipart file to the specified destination directory.
     *
     * @param file    the multipart file to unzip
     * @param destDir the destination directory
     * @throws IOException if an I/O error occurs
     */
    public static void unzip(MultipartFile file, Path destDir) throws IOException {

        File destDirFile = destDir.toFile();

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(file.getInputStream());
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = UnZip.newFile(destDirFile, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }


}
