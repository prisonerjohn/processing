/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2013 The Processing Foundation
  Copyright (c) 2011-12 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along 
  with this program; if not, write to the Free Software Foundation, Inc.
  59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package processing.app.contrib;

import java.io.*;
import java.net.URL;

import processing.app.Base;
import processing.app.Editor;


interface ErrorWidget {
  void setErrorMessage(String msg);
}


public class ContributionManager {
  static public final ContributionListing contribListing;

  static {
    contribListing = ContributionListing.getInstance();
  }


  /**
   * Non-blocking call to download and install a contribution in a new thread.
   *
   * @param url
   *          Direct link to the contribution.
   * @param toBeReplaced
   *          The Contribution that will be replaced by this library being
   *          installed (e.g. an advertised version of a contribution, or the
   *          old version of a contribution that is being updated). Must not be
   *          null.
   */
  static void downloadAndInstall(final Editor editor,
                                 final URL url,
                                 final AdvertisedContribution ad,
                                 final JProgressMonitor downloadProgress,
                                 final JProgressMonitor installProgress,
                                 final ErrorWidget statusBar) {

    new Thread(new Runnable() {
      public void run() {
        String filename = url.getFile();
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        try {
          File contribZip = File.createTempFile("download", filename);
          contribZip.setWritable(true);  // necessary?

          try {
            ContributionListing.download(url, contribZip, downloadProgress);
            
            if (!downloadProgress.isCanceled() && !downloadProgress.isError()) {
              installProgress.startTask("Installing...", ProgressMonitor.UNKNOWN);
              InstalledContribution contribution = 
                ad.install(editor, contribZip, false, statusBar);

              if (contribution != null) {
                contribListing.replaceContribution(ad, contribution);
                refreshInstalled(editor);
              }
              installProgress.finished();
            }
            contribZip.delete();

          } catch (Exception e) {
            statusBar.setErrorMessage("Error during download and install.");
          }
        } catch (IOException e) {
          statusBar.setErrorMessage("Could not write to temporary directory.");
        }
      }
    }).start();
  }


  static public void refreshInstalled(Editor editor) {
    editor.getMode().rebuildImportMenu();
    editor.rebuildToolMenu();
  }


//  /**
//   * Used after unpacking a contrib download do determine the file contents.
//   */
//  static List<File> discover(ContributionType type, File tempDir) {
//    switch (type) {
//    case LIBRARY:
//      return Library.discover(tempDir);
////    case LIBRARY_COMPILATION:
////      // XXX Implement
////      return null;
//    case TOOL:
//      return ToolContribution.discover(tempDir);
//    case MODE:
//      return ModeContribution.discover(tempDir);
//    }
//    return null;
//  }


//  static String getPropertiesFileName(Type type) {
//    return type.toString() + ".properties";
////    switch (type) {
////    case LIBRARY:
////      return Library.propertiesFileName;
////    case LIBRARY_COMPILATION:
////      return LibraryCompilation.propertiesFileName;
////    case TOOL:
////      return ToolContribution.propertiesFileName;
////    case MODE:
////      return ModeContribution.propertiesFileName;
////    }
////    return null;
//  }


//  static InstalledContribution load(Base base, File folder, ContributionType type) {
//    switch (type) {
//    case LIBRARY:
//      return new Library(folder);
////    case LIBRARY_COMPILATION:
////      return LibraryCompilation.create(folder);
//    case TOOL:
//      return ToolContribution.load(folder);
//    case MODE:
//      return ModeContribution.load(base, folder);
//    }
//    return null;
//  }
//
//
//  static ArrayList<InstalledContribution> listContributions(ContributionType type, Editor editor) {
//    ArrayList<InstalledContribution> contribs = new ArrayList<InstalledContribution>();
//    switch (type) {
//    case LIBRARY:
//      contribs.addAll(editor.getMode().contribLibraries);
//      break;
////    case LIBRARY_COMPILATION:
////      contribs.addAll(LibraryCompilation.list(editor.getMode().contribLibraries));
////      break;
//    case TOOL:
//      contribs.addAll(editor.contribTools);
//      break;
//    case MODE:
//      contribs.addAll(editor.getBase().getModeContribs());
//      break;
//    }
//    return contribs;
//  }


//  static void initialize(InstalledContribution contribution, Base base) throws Exception {
//    if (contribution instanceof ToolContribution) {
//      ((ToolContribution) contribution).initializeToolClass();
//    } else if (contribution instanceof ModeContribution) {
//      ((ModeContribution) contribution).instantiateModeClass(base);
//    }
//  }


  /**
   * @param libFile
   *          a zip file containing the library to install
   * @param ad
   *          the advertised version of this library, if it was downloaded
   *          through the Contribution Manager. This is used to check the type
   *          of library being installed, and to replace the .properties file in
   *          the zip
   * @param confirmReplace
   *          true to open a dialog asking the user to confirm removing/moving
   *          the library when a library by the same name already exists
   * @return
   */
  /*
  static public InstalledContribution install(Editor editor, File libFile,
                                              AdvertisedContribution ad,
                                              boolean confirmReplace,
                                              ErrorWidget statusBar) {
    
    ContributionType type = ad.getType();
    
    // Unzip the file into the modes, tools, or libraries folder inside the 
    // sketchbook. Unzipping to /tmp is problematic because it may be on 
    // another file system, so move/rename operations will break.
    File sketchbookContribFolder = 
      getSketchbookContribFolder(editor.getBase(), type);
    File tempFolder = null; 
    
    try {
      tempFolder = 
        Base.createTempFolder(type.toString(), "tmp", sketchbookContribFolder);
    } catch (IOException e) {
      statusBar.setErrorMessage("Could not create a temporary folder to install.");
      return null;
    }
    ContributionManager.unzip(libFile, tempFolder);

    // Now go looking for a legit contrib inside what's been unpacked.
    File contribFolder = null;
    
    // Sometimes contrib authors place all their folders in the base directory 
    // of the .zip file instead of in single folder as the guidelines suggest. 
    if (InstalledContribution.isCandidate(tempFolder, type)) {
      contribFolder = tempFolder;
    }

    if (contribFolder == null) {
      // Find the first legitimate looking folder in what we just unzipped
      contribFolder = InstalledContribution.findCandidate(tempFolder, type);
    }
    
    InstalledContribution outgoing = null;

    if (contribFolder == null) {
      statusBar.setErrorMessage("Could not find a " + type + " in the downloaded file.");
      
    } else {
      File propFile = new File(contribFolder, type + ".properties");

      if (ad.writePropertiesFile(propFile)) {
        InstalledContribution newContrib =
          ContributionManager.create(editor.getBase(), contribFolder, type);

        outgoing = ContributionManager.installContribution(editor, newContrib,
                                                           confirmReplace,
                                                           statusBar);
      } else {
        statusBar.setErrorMessage("Error overwriting .properties file.");
      }
    }

    // Remove any remaining boogers
    if (tempFolder.exists()) {
      Base.removeDir(tempFolder);
    }
    return outgoing;
  }
  */


  /**
   * @param confirmReplace
   *          if true and the library is already installed, opens a prompt to
   *          ask the user if it's okay to replace the library. If false, the
   *          library is always replaced with the new copy.
   */
  /*
  static public InstalledContribution installContribution(Editor editor, InstalledContribution newContrib,
                                                          boolean confirmReplace, ErrorWidget statusBar) {
    ArrayList<InstalledContribution> oldContribs = 
      getContributions(newContrib.getType(), editor);
    
    String contribFolderName = newContrib.getFolder().getName();

    File contribTypeFolder =
      getSketchbookContribFolder(editor.getBase(), newContrib.getType());
    File contribFolder = new File(contribTypeFolder, contribFolderName);

    for (InstalledContribution oldContrib : oldContribs) {

      if ((oldContrib.getFolder().exists() && oldContrib.getFolder().equals(contribFolder)) ||
          (oldContrib.getId() != null && oldContrib.getId().equals(newContrib.getId()))) {

        if (ContributionManager.requiresRestart(oldContrib)) {
          // XXX: We can't replace stuff, soooooo.... do something different
          if (!backupContribution(editor, oldContrib, false, statusBar)) {
            return null;
          }
        } else {
          int result = 0;
          boolean doBackup = Preferences.getBoolean("contribution.backup.on_install");
          if (confirmReplace) {
            if (doBackup) {
              result = Base.showYesNoQuestion(editor, "Replace",
                     "Replace pre-existing \"" + oldContrib.getName() + "\" library?",
                     "A pre-existing copy of the \"" + oldContrib.getName() + "\" library<br>"+
                     "has been found in your sketchbook. Clicking “Yes”<br>"+
                     "will move the existing library to a backup folder<br>" +
                     " in <i>libraries/old</i> before replacing it.");
              if (result != JOptionPane.YES_OPTION || !backupContribution(editor, oldContrib, true, statusBar)) {
                return null;
              }
            } else {
              result = Base.showYesNoQuestion(editor, "Replace",
                     "Replace pre-existing \"" + oldContrib.getName() + "\" library?",
                     "A pre-existing copy of the \"" + oldContrib.getName() + "\" library<br>"+
                     "has been found in your sketchbook. Clicking “Yes”<br>"+
                     "will permanently delete this library and all of its contents<br>"+
                     "before replacing it.");
              if (result != JOptionPane.YES_OPTION || !oldContrib.getFolder().delete()) {
                return null;
              }
            }
          } else {
            if ((doBackup && !backupContribution(editor, oldContrib, true, statusBar)) ||
                (!doBackup && !oldContrib.getFolder().delete())) {
              return null;
            }
          }
        }
      }
    }

    if (contribFolder.exists()) {
      Base.removeDir(contribFolder);
    }

    // Move newLib to the sketchbook library folder
    if (newContrib.getFolder().renameTo(contribFolder)) {
      Base base = editor.getBase();
      // InstalledContribution contrib =
      ContributionManager.create(base, contribFolder, newContrib.getType());
//      try {
//        initialize(contrib, base);
//        return contrib;
//      } catch (Exception e) {
//        e.printStackTrace();
//      }

//      try {
//        FileUtils.copyDirectory(newLib.folder, libFolder);
//        FileUtils.deleteQuietly(newLib.folder);
//        newLib.folder = libFolder;
//      } catch (IOException e) {
    } else {
      String errorMsg = null;
      switch (newContrib.getType()) {
      case LIBRARY:
        errorMsg = "Could not move library \"" + newContrib.getName() + "\" to sketchbook.";
        break;
//      case LIBRARY_COMPILATION:
//        break;
      case TOOL:
        errorMsg = "Could not move tool \"" + newContrib.getName() + "\" to sketchbook.";
        break;
      case MODE:
        break;
      }
      statusBar.setErrorMessage(errorMsg);
    }
    return null;
  }
  */


  /*
  static public boolean writePropertiesFile(File propFile, AdvertisedContribution ad) {
    try {
      if (propFile.delete() && propFile.createNewFile() && propFile.setWritable(true)) {
        //BufferedWriter bw = new BufferedWriter(new FileWriter(propFile));
        PrintWriter writer = PApplet.createWriter(propFile);

        writer.println("name=" + ad.getName());
        writer.println("category=" + ad.getCategory());
        writer.println("authorList=" + ad.getAuthorList());
        writer.println("url=" + ad.getUrl());
        writer.println("sentence=" + ad.getSentence());
        writer.println("paragraph=" + ad.getParagraph());
        writer.println("version=" + ad.getVersion());
        writer.println("prettyVersion=" + ad.getPrettyVersion());

        writer.flush();
        writer.close();
      }
      return true;

    } catch (FileNotFoundException e) {
      e.printStackTrace();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
  */


//  static public File createLibraryBackupFolder(Editor editor, ErrorWidget logger) {
//    File libraryBackupFolder = new File(Base.getSketchbookLibrariesFolder(), "old");
//    return createBackupFolder(libraryBackupFolder, logger,
//                              "Could not create backup folder for library.");
//  }
//
//
//  static public File createToolBackupFolder(Editor editor, ErrorWidget logger) {
//    File libraryBackupFolder = new File(Base.getSketchbookToolsFolder(), "old");
//    return createBackupFolder(libraryBackupFolder, logger,
//                              "Could not create backup folder for tool.");
//  }
//
//
//  static private File createBackupFolder(File backupFolder,
//                                  ErrorWidget logger,
//                                  String errorMessage) {
//    if (!backupFolder.exists() || !backupFolder.isDirectory()) {
//      if (!backupFolder.mkdirs()) {
//        logger.setErrorMessage(errorMessage);
//        return null;
//      }
//    }
//    return backupFolder;
//  }


  /**
   * Returns a file in the parent folder that does not exist yet. If
   * parent/fileName already exists, this will look for parent/fileName(2)
   * then parent/fileName(3) and so forth.
   *
   * @return a file that does not exist yet
   */
  public static File getUniqueName(File parentFolder, String fileName) {
    File backupFolderForLib;
    int i = 1;
    do {
      String folderName = fileName;
      if (i >= 2) {
        folderName += "(" + i + ")";
      }
      i++;

      backupFolderForLib = new File(parentFolder, folderName);
    } while (backupFolderForLib.exists());

    return backupFolderForLib;
  }


//  static protected File createTemporaryFile(URL url, ErrorWidget statusBar) {
//    try {
////      //File tmpFolder = Base.createTempFolder("library", "download", Base.getSketchbookLibrariesFolder());
////      String[] segments = url.getFile().split("/");
////      File libFile = new File(tmpFolder, segments[segments.length - 1]);
//      String filename = url.getFile();
//      filename = filename.substring(filename.lastIndexOf('/') + 1);
//      File libFile = File.createTempFile("download", filename, Base.getSketchbookLibrariesFolder());
//      libFile.setWritable(true);
//      return libFile;
//
//    } catch (IOException e) {
//      statusBar.setErrorMessage("Could not create a temp folder for download.");
//    }
//    return null;
//  }


//  /**
//   * Creates a temporary folder and unzips a file to a subdirectory of the temp
//   * folder. The subdirectory is the only file of the tempo folder.
//   *
//   * e.g. if the contents of foo.zip are /hello and /world, then the resulting
//   * files will be
//   *     /tmp/foo9432423uncompressed/foo/hello
//   *     /tmp/foo9432423uncompress/foo/world
//   * ...and "/tmp/id9432423uncompress/foo/" will be returned.
//   *
//   * @return the folder where the zips contents have been unzipped to (the
//   *         subdirectory of the temp folder).
//   */
//  static public File unzipFileToTemp(File libFile, ErrorWidget statusBar) {
//    String fileName = ContributionManager.getFileName(libFile);
//    File tmpFolder = null;
//
//    try {
//      tmpFolder = Base.createTempFolder(fileName, "uncompressed", Base.getSketchbookLibrariesFolder());
////      tmpFolder = new File(tmpFolder, fileName);  // don't make another subdirectory
////      tmpFolder.mkdirs();
//    } catch (IOException e) {
//      statusBar.setErrorMessage("Could not create temp folder to uncompress zip file.");
//    }
//
//    ContributionManager.unzip(libFile, tmpFolder);
//    return tmpFolder;
//  }


  /**
   * Returns the name of a file without its path or extension.
   *
   * For example,
   *   "/path/to/helpfullib.zip" returns "helpfullib"
   *   "helpfullib-0.1.1.plb" returns "helpfullib-0.1.1"
   */
  static public String getFileName(File libFile) {
    String path = libFile.getPath();
    int lastSeparator = path.lastIndexOf(File.separatorChar);

    String fileName;
    if (lastSeparator != -1) {
      fileName = path.substring(lastSeparator + 1);
    } else {
      fileName = path;
    }

    int lastDot = fileName.lastIndexOf('.');
    if (lastDot != -1) {
      return fileName.substring(0, lastDot);
    }

    return fileName;
  }
  
  
  static public void deleteFlagged() {
    deleteFlagged(Base.getSketchbookLibrariesFolder());
    deleteFlagged(Base.getSketchbookModesFolder());
    deleteFlagged(Base.getSketchbookToolsFolder());
  }

  
  static private void deleteFlagged(File root) {
    File[] markedForDeletion = root.listFiles(new FileFilter() {
      public boolean accept(File folder) {
        return (folder.isDirectory() && 
                InstalledContribution.isDeletionFlagged(folder));
      }
    });
    for (File folder : markedForDeletion) {
      Base.removeDir(folder);
    }
  }
}
