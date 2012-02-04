package rita.support;

public class RiNounInflector {
}

/*
// Various regular expressions used below
$letter           = "[a-zA-Z]";
$upper            = "[A-Z]";
$acronym_punct    = "[-._]";
$vowel            = case_insensitive_class("aeiou");
$consonant        = case_insensitive_class("b-df-hj-np-tv-z");
$vowel_sound      = case_insensitive_class("aefhilmnorsx");
$consonant_sound  = case_insensitive_class("bcdgjkpqtuvwyz");




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                   //
//   number rules:               //
//   an 8-year old an 80-year-old          //
//   an 11-year-old  a 110-year-old          //
//   a 73-year-old a 96-year-old         //
//                   //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@number_rules  = (
      ["8",     1],
      ["11[^0-9]|11\$", 1],
      ["[0-9]",   0]
      );



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                   //
//   acronym rules:                //
//   an e-type an x-ray          //
//   a     a T-type          //
//   an                //
//   a     a           //
//   a  b    a u           //
//   an s    an r            //
//                   //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@acronym_rules = (
      ["${vowel_sound}[A-Z]?${acronym_punct}",  1],
      ["${consonant_sound}[A-Z]?${acronym_punct}",  0],
      ["${vowel_sound}${letter}*${upper}",    1],
      ["${consonant_sound}${letter}*${upper}",  0],
      ["${vowel_sound}\$",        1],
      ["${consonant_sound}\$",      0]
     );




%exceptions = (
         
         // exceptions for 'e'
         "e" => [ 'eu',     'ew[^o]' ],
         
         // exceptions for 'h'
         "h" => [ 'haut',     'heir',
      "hombre\$",   'honest',
      'honou?r',    'hors//d\'oeuvres',
      'hors//de//combat', 'hour' ],
      
         // exceptions for 'l'
         "l" => [ "lbw\$" ],

         // exceptions for 'm'
         "m" => [ "mpg\$",    "mph\$" ],

         // exceptions for 'o'
         "o" => [ 'once',     'one-',
      "oner?\$",    'one[^aeiour]',
      'ou[aeiou]' ],

         // exceptions for 'r'
         "r" => [ "rpm\$" ],

         // exceptions for 's'
         "s" => [ "sae\$" ],

         // exceptions for 'u'
         "u" => [ 'ubi',      'uga',
      'ugr',      'ukr',
      'uku',      "ulotrichous\$",
      'ulu',      "ulysses\$",
      'unani',    'unia',
      'unic',     'unidir',
      'unidim',   'unif',
      'unijugate',    'unil[aeiou]',
      'uninucleate',    'unio',
      'unip',     'uniq',
      'uniramous',    'unis[aeiou]',
      'unit',     'univ',
      'unix',     'upas',
      'ur[aeiou]',    'us[aeiou]',
      'ut[aeiour]',   "uttoxeter\$",
      'uv'
          ],
         
         "y" => [ "y${consonant}" ]
         
        );

*/
