package rita.support;

import processing.core.PConstants;

/**
 * A subinterface of PConstants adding RiTa's own set. 
 */
public interface RiConstants extends PConstants
{ 
  // RITA EVENT-TYPE CONSTANTS =======================================
  
  /** Specifies 'UNKNOWN' as the event type for a RiTaEvent */
  public static final int UNKNOWN = -1;  
  
  /** Specifies 'TEXT_ENTERED' as the event type for a RiTaEvent */
  public static final int TEXT_ENTERED = 1;
  
  /** Specifies 'SPEECH_COMPLETED' as the event type for a RiTaEvent */
  public static final int SPEECH_COMPLETED = 2;
  
  /** Specifies 'BEHAVIOR_COMPLETED' as the event type for a RiTaEvent */
  public static final int BEHAVIOR_COMPLETED = 3;  
  
  /** Specifies 'TIMER_TICK' as the event type for a RiTaEvent   */
  public static final int TIMER_TICK = 4;

  /** 
   * @deprecated see {@link #TIMER_TICK}
   * @see #TIMER_TICK
   */
  public static final int TIMER_COMPLETED = TIMER_TICK;

  // TEXT BEHAVIOR CONSTANTS ========================================
  
  /** Specifies 'MOVE' as the behavior type for a RiTextBehavior  */
  public static final int MOVE = 1;
  
  /** Specifies 'FADE_COLOR' as the  behavior type for a RiTextBehavior */
  public static final int FADE_COLOR = 2;
  
  /** Specifies 'FADE_IN' as the  behavior type for a RiTextBehavior */
  public static final int FADE_IN = 3;
  
  /** Specifies 'FADE_OUT' as the  behavior type for a RiTextBehavior */
  public static final int FADE_OUT = 4;
  
  /** Specifies 'FADE_TO' as the behavior type for a RiTextBehavior */
  public static final int FADE_TO_TEXT = 5;
  
  /** Specifies 'TIMER' as the behavior type for a RiTextBehavior */
  public static final int TIMER = 6;   
  
  /** Specifies 'SCALE_TO' as the behavior type for a RiTextBehavior  */
  public static final int SCALE_TO = 7;
  
  /** Specifies 'LERP' as the behavior type for a RiTextBehavior  */
  public static final int LERP = 8; 
  
  /**
   * @invisible
   * Specifies 'BOUNDING_BOX_ALPHA' as the behavior type for a RiTextBehavior 
   * Note: no callback for events of this type
   */   
  public static final int BOUNDING_BOX_ALPHA = 133;
  
  // FEATURE CONSTANTS ======================================
  
  public static String WORD_BOUNDARY = " ";
  public static String PHONEME_BOUNDARY = "-";
  public static String SYLLABLE_BOUNDARY = "/";
  
  public static final String SENTENCE_BOUNDARY = "|";
  public static final String SYLLABLES = "syllables";
  public static final String PHONEMES = "phonemes";
  public static final String STRESSES = "stresses";
  public static final String MUTABLE = "mutable";
  public static final String TOKENS = "tokens";
  public static final String TEXT = "text";
  public static final String POS = "pos";
  public static final String ID = "id";
  
  /** Specifies 'Minim' as the audio library to use (default) */
  public static final int MINIM = 0;
  
  /** Specifies 'Sonia' as the audio library to use */  
  public static final int SONIA = 1;
  
  /** Specifies 'Ess' as the audio library to use */
  public static final int ESS   = 2;
  

  // ANIMATION CONSTANTS ====================================== 
  
  /** Specifies 'linear' as the motion type for moveXX() methods (default) */
  public static final int LINEAR = 0;
  
  /** Specifies 'ease-in' as the motion type for moveXX() methods (quadratic)*/ 
  public static final int EASE_IN = 1;
  
  /** Specifies 'ease-out' as the motion type for moveXX() methods (quadratic) */
  public static final int EASE_OUT = 2;
  
  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (quadratic) */
  public static final int EASE_IN_OUT = 3;
  
  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (cubic) */
  public static final int EASE_IN_OUT_CUBIC = 4;
  
  /** Specifies 'ease-in' as the motion type for moveXX() methods (cubic) */
  public static final int EASE_IN_CUBIC = 5;
  
  /** Specifies 'ease-out' as the motion type for moveXX() methods (cubic) */
  public static final int EASE_OUT_CUBIC = 6;
  
  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (quartic) */
  public static final int EASE_IN_OUT_QUARTIC = 7;
  
  /** Specifies 'ease-in' as the motion type for moveXX() methods (quartic) */
  public static final int EASE_IN_QUARTIC = 8;
  
  /** Specifies 'ease-out' as the motion type for moveXX() methods (quartic) */
  public static final int EASE_OUT_QUARTIC = 9; 
  
  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (circular) */
  public static final int EASE_IN_OUT_SINE = 10;

  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (circular) */
  public static final int EASE_IN_SINE = 11;

  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (circular) */
  public static final int EASE_OUT_SINE = 12;
  
  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (circular) */
  public static final int EASE_IN_OUT_EXPO = 13;

  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (circular) */
  public static final int EASE_IN_EXPO = 14;

  /** Specifies 'ease-in/out' as the motion type for moveXX() methods (circular) */
  public static final int EASE_OUT_EXPO = 15;
  
  // CONJUGATION CONSTANTS ======================================
  
  /** Specifies person as one of (first, scond or third) */
  public static final int FIRST_PERSON = 1;
  /** Specifies person as one of (first, scond or third) */
  public static final int SECOND_PERSON = 2;
  /** Specifies person as one of (first, scond or third) */
  public static final int THIRD_PERSON = 3;
  
  /** Specifies tense as one of (past, present or future) */
  public static final int PAST_TENSE = 4;
  /** Specifies tense as one of (past, present or future) */
  public static final int PRESENT_TENSE = 5;
  /** Specifies tense as one of (past, present or future) */
  public static final int FUTURE_TENSE = 6;
  
  /** Specifies agreement as one of (singular or plural) */
  public static final int SINGULAR = 7;  
  /** Specifies agreement as one of (singular or plural) */
  public static final int PLURAL = 8;
  
  // TAGGER CONSTANTS ======================================
  
  /** Type-constant for a maximum entropy-based tagger  */
  public static final int MAXENT_POS_TAGGER = 0;
  
  /** Type-constant for a rule or tranformation-based tagger  */
  public static final int BRILL_POS_TAGGER  = 1;

  /** Type-constant for the Pling-based stemmer */
  public static final int PLING_STEMMER = 0;
  
  /** Type-constant for the Porter-based stemmer */
  public static final int PORTER_STEMMER = 1;
}
