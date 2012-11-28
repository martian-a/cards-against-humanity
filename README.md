cards-against-humanity card generator
=====================================

A set of tools for generating a deck of Cards Against Humanity (CAgH), a game devised by Cards Against Humanity LLC.  For more information see http://www.cardsagainsthumanity.com/ where you can also buy a beautifully presented pre-printed deck or download a printable version of the official cards for free.  The game also has an entry on Wikipedia: http://en.wikipedia.org/wiki/Cards_Against_Humanity.

Although this tool was inspired by playing CAgH, it's not created by or endorsed by Cards Against Humanity LLC.  An Attribution-NonCommercial-ShareAlike 2.0 Generic (CC BY-NC-SA 2.0) licence applies to the card data and a GPL 3.0 licence to all else.  

See /src/main/data for exemplar data, including the original cards, localised custom card data, an English dictionary (for automated translation) and schema for validating card and dictionary files.

See /src/main/xsl for XSLT to implement translation, remove duplicates and generate a printable HTML5 deck of cards.

See /src/main/java for a command-line application to execute translation, de-duping and/or generation of a printable deck of cards.


