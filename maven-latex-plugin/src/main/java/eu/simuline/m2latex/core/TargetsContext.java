package eu.simuline.m2latex.core;

/**
 * Represents the context in which the target set occurs. 
 */
public enum TargetsContext {
  /**
   * Represents the context of {@link Settings#getTargets()}. 
   * Here the set of targets is a setting. 
   */
  targetsSetting {
    String context() {
      return "setting 'targets'";
    }
  },
  /**
   * Represents the context of {@link Settings#getDocClassesToTargets()}. 
   * Here the set of targets occurs within a chunk in a setting. 
   */
  inChunkSetting {
    String context() {
      return "a chunk of setting 'docClassesToTargets'";
    }
  },
  /**
   * Represents the context of a magic comment in a latex main file. 
   * Here, the set of targets occurs within a comment of the form 
   * <code>%! LMP ... targets=<target set>
   */
  targetsMagic {
    String context() {
      return "magic comment 'targets'";
    }
  };

  abstract String context();
}
