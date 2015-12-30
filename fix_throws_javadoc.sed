s/^\(  *\*  *@throws \(java.lang.\)\{0,1\}Exception\).*$/\1 a general exception was thrown/
s/^\(  *\*  *@throws FileNotFoundException\).*$/\1 an attempt to open a file from a pathname has failed/
s/^\(  *\*  *@throws FontException\).*$/\1 there was an error in a font/
s/^\(  *\*  *@throws FontLoadingException\).*$/\1 there was an error loading a font/
s/^\(  *\*  *@throws IOException\).*$/\1 an I\/O operation failed or was interrupted/
s/^\(  *\*  *@throws IllegalArgumentException\).*$/\1 method has been passed an illegal or inappropriate argument/
s/^\(  *\*  *@throws InvalidFontException\).*$/\1 the font is invalid/
s/^\(  *\*  *@throws PDFConfigurationException\).*$/\1 there was a system problem configuring PDF support/
s/^\(  *\*  *@throws PDFFontException\).*$/\1 there was an error in the font set or an individual font/
s/^\(  *\*  *@throws PDFIOException\).*$/\1 there was an error reading or writing a PDF file or temporary caches/
s/^\(  *\*  *@throws PDFInvalidDocumentException\).*$/\1 a general problem with the PDF document, which may now be in an invalid state/
s/^\(  *\*  *@throws PDFInvalidParameterException\).*$/\1 one or more of the parameters passed to a method is invalid/
s/^\(  *\*  *@throws PDFSecurityException\).*$/\1 some general security issue occurred during the processing of the request/
s/^\(  *\*  *@throws PDFUnableToCompleteOperationException\).*$/\1 the operation was unable to be completed/
s/^\(  *\*  *@throws UnsupportedEncodingException\).*$/\1 the character encoding is not supported/
s/^\(  *\*  *@throws UnsupportedFontException\).*$/\1 the font is not supported/
s/^\(  *\*  *@throws ClassNotFoundException\).*$/\1 a class definition could not be found/
