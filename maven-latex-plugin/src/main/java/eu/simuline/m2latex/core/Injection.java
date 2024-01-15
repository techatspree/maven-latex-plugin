package eu.simuline.m2latex.core;

// import java.io.File;

/**
 * Represents a file to be injected in goal <code>inj</code>. 
 * Injection means that it is a resource of this software 
 * but can be inserted (injected) at {@link Settings#texSrcDirectory}. 
 * It is not injected if there is already a file with this name 
 * and it cannot be ensured that it is this software which wrote it. 
 * The mechanism to find out whether this is true is: reading the headline. 
 * To that end, a specific headline is inserted. 
 * It is preceeded with the comment sign {@link #commentStr()} 
 * appropriate for the format of the file, mostly a hash sign. 
 * The files for which {@link #doFilter()} return true, 
 * as e.g. for <code>.latexmkrc</code>, are filtered 
 * replacing parameter names of this software by current values. 
 * This allows to synchronize the settings of this software 
 * with the settings of <code>latexmk</code>. 
 * Finally, if {@link #setExecutable()} tells so, 
 * the resulting file is set executable, 
 * as is appropriate for scripts like <code>instVScode4tex.sh</code>. 
 */
public enum Injection {
  /**
   * The record file of latexmk. 
   * This must be filtered 
   * to be synchronized with the current settings 
   * of this piece of software. 
   */
  latexmkrc {
    String getFileName() {
      return ".latexmkrc";
    }

    boolean doFilter() {
      return true;
    }

    boolean hasShebang() {
      return true;
    }
  },
  /**
   * The record file of chktex. 
   * This is adapted to some use cases
   * of this piece of software. 
   */
  chktexrc {
    String getFileName() {
      return ".chktexrc";
    }

    boolean hasShebang() {
      return false;
    }
  },
  /**
   * The installation script for extensions of VS Code 
   * used for development of latex documents. 
   */
  vscodeExt {
    String getFileName() {
      return "instVScode4tex.sh";
    }

    boolean setExecutable() {
      return true;
    }

    boolean hasShebang() {
      return true;
    }
  },
  /**
   * Invokes the latex compiler configured with epoque time set to 0. 
   */
  ntlatex {
    String getFileName() {
      return "ntlatex";
    }

    boolean setExecutable() {
      return true;
    }

    boolean hasShebang() {
      return true;
    }
  },
  /**
   * Invokes pythontex and writes a log file. 
   * This is needed only because the current version of pythontex does not write log files. 
   * In future this shall change and so this injection will be superfluous. 
   */
  pythontexW {
    String getFileName() {
      return "pythontexW";
    }

    boolean setExecutable() {
      return true;
    }

    boolean hasShebang() {
      return true;
    }
  },
  /**
   * Invokes a diff tool for PDF files 
   * combining visual diff with <code>diff-pdf-visual</code> 
   * with diffing of PDF metainfo provided by <code>pdfinfo</code>. 
   */
  vmdiff {
    String getFileName() {
      return "vmdiff";
    }

    boolean setExecutable() {
      return true;
    }

    boolean hasShebang() {
      return true;
    }
  },
  /**
   * A header file loading package <code>graphicx</code> 
   * with the appropriate options. 
   * This is used to provide the command <code>includegraphics</code>. 
   */
  headerGrp {
    String getFileName() {
      return "headerGrp.tex";
    }

    /**
     * The comment string which is that of tex. 
     */
    String commentStr() {
      return "%";
    }

    // In the long run, the privacy settings are configurable 
    boolean doFilter() {
      return true;
    }

    boolean hasShebang() {
      return false;
    }
  },
  /**
   * A header file used to hide metainfo in a PDF file. 
   * This is mainly used for security, i.e. privacy reasons, 
   * but the current header file makes a latex main file 
   * which has no date in the text reproducible 
   * at least for lualatex and for pdflatex, but not for xelatex. 
   * <p>
   * In the long run, this shall be filtered 
   * to make the supression configurable. 
   */
  headerSuppressMetaPDF {
    String getFileName() {
      return "headerSuppressMetaPDF.tex";
    }

    /**
     * The comment string which is that of tex. 
     */
    String commentStr() {
      return "%";
    }

    // In the long run, the privacy settings are configurable 
    boolean doFilter() {
      return true;
    }

    boolean hasShebang() {
      return false;
    }
  },
  /**
   * The general header file mostly loading packages. 
   * Various main files may require additional headers 
   * but this is currently the minimum, 
   * so to speak the smallest common multiple. 
   * It is inspired by pandoc and it is written to work 
   * for all common latex compiler, 
   * for direct creation of PDF or for creation of DVI/XDV, 
   * as a final result or as an intermediate step, 
   * for document classes including also <code>beamer</code>, 
   * for package <code>tex4ht</code> and many other variants. 
   */
  header {
    String getFileName() {
      return "header.tex";
    }

    /**
     * The comment string which is that of tex. 
     */
    String commentStr() {
      return "%";
    }

    boolean doFilter() {
      return false;
    }

    boolean hasShebang() {
      return false;
    }
  };

  /**
   * Returns the filename of the resource. 
   * It is injected under this file name 
   * in the folder given by {@link Settings#texSrcDirectory}. 
   * 
   * @return
   *    the filename of the resource. 
   */
  abstract String getFileName();

  /**
   * Returns the character indicating a comment. 
   * 
   * @return
   *    the character indicating a comment. 
   *    By default this is <code>#</code> and is overwritten by need. 
   *    It is appropriate for bash, perl and also as chktexrc. 
   *    For TEX filtes it must be overwritten. 
   */
  String commentStr() {
    return "#";
  }

  /**
   * Returns whether this file must be filtered before being injected. 
   * By default this is <code>false</code> and is overwritten by need. 
   * 
   * @return
   *   whether this file must be filtered before being injected. 
   *   This is <code>false</code>, except for {@link Injection#latexmkrc}. 
   */
  boolean doFilter() {
    return false;
  }

  /**
   * Returns whether the according injection shall be executable. 
   * By default this is <code>false</code> and is overwritten by need. 
   * 
   * @return
   *    whether the according injection shall be executable. 
   */
  boolean setExecutable() {
    return false;
  }

  abstract boolean hasShebang();
}
