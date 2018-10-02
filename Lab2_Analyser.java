/**
 * 
 */
package org.novasearch.tutorials.labs2018.RI_labs;

import java.util.List;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.commongrams.CommonGramsFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.jsoup.Jsoup;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;

/**
 * @author jmag
 *
 */
public class Lab2_Analyser extends Analyzer {

	/**
	 * An unmodifiable set containing some common English words that are not
	 * usually useful for searching.
	 */
	static List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
			"in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
			"these", "they", "this", "to", "was", "will", "with");
	static CharArraySet stopSet = new CharArraySet(stopWords, false);

	/** Default maximum allowed token length */
	private int maxTokenLength = 25;

	/**
	 * Builds an analyzer with the default stop words ({@link #STOP_WORDS_SET}).
	 */
	public Lab2_Analyser() {

	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {

		
		// THE FIELD IS IGNORED 
		// ___BUT___ 
		// you can provide different TokenStremComponents according to the fieldName
						/*Tokenizers Experiments*/
		final StandardTokenizer src = new StandardTokenizer();
	    //final WhitespaceTokenizer src = new WhitespaceTokenizer(); 
		//final UAX29URLEmailTokenizer src = new UAX29URLEmailTokenizer();
						/*Tokenizers Experiments*/
		TokenStream tok = null;
		tok = new StandardFilter(src);					// text into non punctuated text
		
//		tok = new LowerCaseFilter(tok);					// changes all texto into lowercase
//		tok = new StopFilter(tok, stopSet);				// removes stop words

//		tok = new CommonGramsFilter(tok, stopSet);	// creates word-grams with stopwords
//		tok = new ShingleFilter(tok, 2, 3);				// creates word-grams with neighboring works
//		tok = new NGramTokenFilter(tok,2,5);			// creates unbounded n-grams
					/*trocar para stopSet*/
	//	tok = new CommonGramsFilter(tok, stopSet);	// creates word-grams with stopwords

 //	tok = new NGramTokenFilter(tok,2,5);			// creates unbounded n-grams

//		tok = new EdgeNGramTokenFilter(tok,2,5);		// creates word-bounded n-grams
 		
//		tok = new SnowballFilter(tok, "English");		// stems workds according to the specified language
		
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
						/*Comment the next line for split the string on the spaces[WhiteSpaceTokenizer]*/
				src.setMaxTokenLength(Lab2_Analyser.this.maxTokenLength);
				
				//super.setReader(reader);
						/*HTMLCharStripper*/
			super.setReader(new HTMLStripCharFilter(reader));
						/*HTMLCharStripper*/
			}
		};
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		TokenStream result = new StandardFilter(in);
		result = new LowerCaseFilter(result);
		return result;
	}
	
	// ===============================================
	// Test the different filters
	public static void main(String[] args) throws IOException {

		//final String text = "This is a demonstration, of the TokenStream Lucene-API,";
		//final String text = "<p> An <a href='http://example.com/'><b>example</b></a> link.</p>";
		//final String text = "A web search engine is a software system that is designed to search for information on the WWW.";
						/*Jsoup*/
		final String html = "<p>An <a href='http://example.com/'><b>example</b></a> link.</p>";
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		String text = doc.body().text();
						/*Jsoup*/
		
		Lab2_Analyser analyzer = new Lab2_Analyser();
		TokenStream stream = analyzer.tokenStream("field", new StringReader(text));
		
		// get the CharTermAttribute from the TokenStream
		CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

		try {
			stream.reset();

			// print all tokens until stream is exhausted
			while (stream.incrementToken()) {
				System.out.println(termAtt.toString());
			}

			stream.end();
		} finally {
			stream.close();
		}
	}
}
