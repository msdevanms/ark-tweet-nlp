package edu.cmu.cs.lti.ark.tweetnlp;

import java.util.regex.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import fig.basic.Pair;
public class Twokenize {
	  static String Contractions = "(?i)(?:\\w+)(?:n't|'ve|'ll|'d|'re|'s|'m)$";
	  static Pattern Whitespace = Pattern.compile("[\\s\\u3000]+");

	  static String punctChars = "['“\\u0022.?!,:;]";
	  static String punctSeq   = punctChars+"+";
	  static String entity     = "&(?:amp|lt|gt|quot);";
	  static Pattern Pentity   = Pattern.compile("&(amp|lt|gt|quot);");
	  //  URLs

	  // David: I give the Larry David eye to this whole URL regex
	  // (http://www.youtube.com/watch?v=2SmoBvg-etU) There are
	  // TODO potentially better options, see:
	  //   http://daringfireball.net/2010/07/improved_regex_for_matching_urls
	  //   http://mathiasbynens.be/demo/url-regex

	  static String urlStart1  = "(?:https?://|\\bwww\\.)";
	  static String commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)";
	  static String ccTLDs	 = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|" +
	      "bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|" +
	      "er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|" +
	      "hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|" +
	      "lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|" +
	      "nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|" +
	      "sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|" +
	      "va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)";	//TODO: remove obscure country domains?
	  static String urlStart2  = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:"+commonTLDs+"|"+ccTLDs+")"+"(?:\\."+ccTLDs+")?(?U)(?=[\\W])";
	  static String urlBody    = "(?:[^\\.\\s<>][^\\s<>]*?)?";
	  static String urlExtraCrapBeforeEnd = "(?:"+punctChars+"|"+entity+")+?";
	  static String urlEnd     = "(?:\\.\\.+|[<>]|\\s|$)";
	  static String url        = "(?:"+urlStart1+"|"+urlStart2+")"+urlBody+"(?=(?:"+urlExtraCrapBeforeEnd+")?"+urlEnd+")";
	  

	  // Numeric
	  static String timeLike   = "\\d+(?::\\d+){1,2}";
	  //static String numNum     = "\\d+\\.\\d+";
	  static String numberWithCommas = "(?:(?<!\\d)\\d{1,3},)+?\\d{3}" + "(?=(?:[^,\\d]|$))";
	  static String numComb	 = "\\$?\\d+(?:\\.\\d+)+%?";

	  // Abbreviations
	  static String boundaryNotDot = "(?:$|\\s|[“\\u0022?!,:;]|" + entity + ")";
	  static String aa1  = "(?:[A-Za-z]\\.){2,}(?=" + boundaryNotDot + ")";
	  static String aa2  = "[^A-Za-z](?:[A-Za-z]\\.){1,}[A-Za-z](?=" + boundaryNotDot + ")";
	  static String standardAbbreviations = "\\b(?:[Mm]r|[Mm]rs|[Mm]s|[Dd]r|[Ss]r|[Jj]r|[Rr]ep|[Ss]en|[Ss]t)\\.";
	  static String arbitraryAbbrev = "(?:" + aa1 +"|"+ aa2 + "|" + standardAbbreviations + ")";
	  static String separators  = "(?:--+|―|—|~|–)";
	  static String decorations = "([♫♪]|[★☆]|[♥❤]|[\\u2639-\\u263b]|[\\ue001-\\uebbb])\\3*"; //backreference for ♥♥♥ etc.
	  static String thingsThatSplitWords = "[^\\s\\.,]";
	  static String embeddedApostrophe = thingsThatSplitWords+"+'" + thingsThatSplitWords + "+";

	  //  Emoticons
	  static String normalEyes = "(?iu)[:=]";
	  static String wink = "[;]";
	  static String noseArea = "(?:|o|O|-|[^a-zA-Z0-9 ])";
	  static String happyMouths = "[D\\)\\]]+";
	  static String sadMouths = "[\\(\\[]+";
	  static String tongue = "[pP]";
	  static String otherMouths = "[doO/\\\\vV]+"; // remove forward slash if http://'s aren't cleaned

	  // mouth repetition examples:
	  // @aliciakeys Put it in a love song :-))
	  // @hellocalyclops =))=))=)) Oh well

	  public static String OR(String... parts) {
		String prefix="(?:";
		StringBuilder sb = new StringBuilder();
		for (String s:parts){
			sb.append(prefix);
			prefix="|";
			sb.append(s);
		}
		sb.append(")");
	    return sb.toString();
	  }

	  static String basicface= "(?i)(♥|0|o|t|x|>|\\u0CA0|<|@|ʘ|•|・|◕|\\^|¬|\\*)[\\._+\\-+]\\2";
	  static String eastEmote= "[＼\\\\ƪ\\(（<>;ヽ\\-=~\\*]+(?:"+basicface+"|[^A-Za-z0-9\\s\\(\\):])+[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+";
	  static String emoticon = OR(
	      // Standard version  :) :( :] :D :P
	      OR(normalEyes, wink) + noseArea + OR(tongue, otherMouths, sadMouths, happyMouths),
	      
	      // reversed version (: D:  use positive lookbehind to remove "(word):"
	      // because eyes on the right side is more ambiguous with the standard usage of : ;
	      "(?<=(?: |^))" + OR(sadMouths,happyMouths,otherMouths) + noseArea + OR(normalEyes, wink),
	      
		  
		  //inspired by http://en.wikipedia.org/wiki/User:Scapler/emoticons#East_Asian_style
		  eastEmote, basicface
	      // iOS 'emoji' characters (some smileys, some symbols) [\ue001-\uebbb]  
	      // TODO should try a big precompiled lexicon from Wikipedia, Dan Ramage told me (BTO) he does this
	  	);

	  public static String allowEntities(String pat) {
	    // so we can write patterns with < and > and let them match escaped html too
	    return pat.replace("<", "(?:<|&lt;)").replace(">", "(?:>|&gt;)");
	  }
	  
	  static String Hearts = allowEntities("(?:<+/?3+)+"); //the other hearts are in decorations

	  static String Arrows = allowEntities("(?:<*[-―—=]*>+|<+[-―—=]*>*)");

	  // BTO 2011-06: restored Hashtag, AtMention protection (dropped in original scala port) because it fixes
	  // "hello (#hashtag)" ==> "hello (#hashtag )"  WRONG
	  // "hello (#hashtag)" ==> "hello ( #hashtag )"  RIGHT
	  // "hello (@person)" ==> "hello (@person )"  WRONG
	  // "hello (@person)" ==> "hello ( @person )"  RIGHT
	  // ... Some sort of weird interaction with edgepunct I guess, because edgepunct 
	  // has poor content-symbol detection.
	  
	  static String Hashtag = "#[a-zA-Z0-9_]+";  // also gets #1 #40 which probably aren't hashtags .. but good as tokens

	  static String AtMention = "@[a-zA-Z0-9_]+";
	  
	  // I was worried this would conflict with at-mentions
	  // but seems ok in sample of 5800: 7 changes all email fixes
	  // http://www.regular-expressions.info/email.html
	  static String Bound = "(?:\\W|^|$)";
	  static String Email = "(?<=" +Bound+ ")[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(?=" +Bound+")";

	  // We will be tokenizing using these regexps as delimiters
	  // Additionally, these things are "protected", meaning they shouldn't be further split themselves.
	  static Pattern Protected  = Pattern.compile(
	    OR(
	      Hearts,
	      Arrows,
	      url,
	      Email,
	      entity,
	      timeLike,
	      //numNum,
	      numberWithCommas,
		  numComb,
		  emoticon,
	      punctSeq,
	      arbitraryAbbrev,
	      separators,
	      decorations,
	      embeddedApostrophe,
		  Hashtag,  
	      AtMention
	     ));
	  
	  // Edge punctuation
	  // Want: 'foo' => ' foo '
	  // While also:   don't => don't
	  // the first is considered "edge punctuation".
	  // the second is word-internal punctuation -- don't want to mess with it.
	  // BTO (2011-06): the edgepunct system seems to be the #1 source of problems these days.  
	  // I remember it causing lots of trouble in the past as well.  Would be good to revisit or eliminate.
	  
	  // Note the 'smart quotes' (http://en.wikipedia.org/wiki/Smart_quotes)
	  static String edgePunctChars    = "'\"“”‘’«»{}\\(\\)\\[\\]\\*";
	  static String edgePunct    = "[" + edgePunctChars + "]";
	  static String notEdgePunct = "[a-zA-Z0-9]"; // content characters
	  static String offEdge = "(^|$|:|;|\\s)";  // colon here gets "(hello):" ==> "( hello ):"
	  static Pattern EdgePunctLeft  = Pattern.compile(offEdge + "("+edgePunct+"+)("+notEdgePunct+")");
	  static Pattern EdgePunctRight = Pattern.compile("("+notEdgePunct+")("+edgePunct+"+)" + offEdge);

	  public static String splitEdgePunct (String input) {
	    Matcher m1=EdgePunctLeft.matcher(input);
		input=m1.replaceAll("$1$2 $3");
	    m1 = EdgePunctRight.matcher(input);
		input=m1.replaceAll("$1 $2$3");
	    return input;
	  }

	  // The main work of tokenizing a tweet.
	  public static List<String> simpleTokenize (String text) {

		// Do the no-brainers first
	    String splitPunctText = splitEdgePunct(text);

	    int textLength = splitPunctText.length();

	    // Find the matches for subsequences that should be protected,
	    // e.g. URLs, 1.0, U.N.K.L.E., 12:53
	    Matcher matches = Protected.matcher(splitPunctText);
		//Storing as List[List[String]] to make zip easier later on 
		List<List<String>> bads = new ArrayList<List<String>>();	//linked list?
		List<Pair<Integer,Integer>> badSpans = new ArrayList<Pair<Integer,Integer>>();
		while(matches.find()){
			// The spans of the "bads" should not be split.
			if (matches.start()!=matches.end()){ //unnecessary?
				List<String> bad = new ArrayList<String>(1);
				bad.add(splitPunctText.substring(matches.start(),matches.end()));
				bads.add(bad);
				badSpans.add(new Pair<Integer, Integer>(matches.start(),matches.end()));
			}
		}

	    // Create a list of indices to create the "goods", which can be
	    // split. We are taking "bad" spans like 
	    //     List((2,5), (8,10)) 
	    // to create 
	    ///    List(0, 2, 5, 8, 10, 12)
	    // where, e.g., "12" here would be the textLength
		// has an even length and no indices are the same
	    List<Integer> indices = new ArrayList<Integer>(2+2*badSpans.size());
			indices.add(0);
			for(Pair<Integer,Integer> p:badSpans){
				indices.add(p.getFirst());
				indices.add(p.getSecond());
			}
			indices.add(textLength);
	    
	    // Group the indices and map them to their respective portion of the string
		List<List<String>> splitGoods = new ArrayList<List<String>>(indices.size()/2);
		for(int i=0; i<indices.size();i+=2){
			String goodstr=splitPunctText.substring(indices.get(i),indices.get(i+1));
			List<String> splitstr=Arrays.asList(goodstr.trim().split(" "));
			splitGoods.add(splitstr);
		}

	    //  Reinterpolate the 'good' and 'bad' Lists, ensuring that
	    //  additonal tokens from last good item get included
	    List<String> zippedStr= new ArrayList<String>();
	    int i;
	    for(i=0;i<bads.size();i++) {
			zippedStr=addAllnonempty(zippedStr,splitGoods.get(i));
			zippedStr=addAllnonempty(zippedStr,bads.get(i));
		}
	    	zippedStr=addAllnonempty(zippedStr,splitGoods.get(i));
		

	    // Split based on special patterns (like contractions) and check all tokens are non empty
	    //zippedStr.map(splitToken(_)).flatten.filter(_.length > 0)
		return zippedStr;
	  }  

	  public static List<String> addAllnonempty(List<String> master, List<String> smaller){
		  for (String s:smaller){
			  String strim=s.trim();
			  if(strim.length()>0)
				  master.add(strim);
		  }
		  return master;
	  }
	  // "foo   bar" => "foo bar"
	  public static String squeezeWhitespace (String input){
		return Whitespace.matcher(input).replaceAll(" ").trim();
	  }

	  // Final pass tokenization based on special patterns
	  public String splitToken (String token) {
	      // BTO: our POS tagger wants "ur" and "you're" to both be one token.
	      // Uncomment to get "you 're"
//	      case Contractions(stem, contr) => List(stem.trim, contr.trim)
	      return token.trim();
	  }

	  public static List<String> tokenize (String text){
		return simpleTokenize(squeezeWhitespace(text));
	  }
	    

	  // Very slight normalization for AFTER tokenization.
	  // The tokenization regexes are written to work on non-normalized text.
	  // (to make byte offsets easier to compute)
	  // Hm: 2+ repeated character normalization here?
	  // No, that's more linguistic, should be further down the pipeline 
	  public static String normalizeText(String text) {
	    return text.replaceAll("&lt;", "<").replaceAll("&gt;",">").replaceAll("&amp;","&");
	  }

	  public static List<String> tokenizeForTagger (String text) {
	    List<String> res = new ArrayList<String>();
	    List<String> pretokenized = tokenize(text);
	    for(String token:pretokenized){
	    	res.add(normalizeText(token));
	    }
	    return res;
	  }

	  /*
	   * todo:use stringbuilder to port this function
	  // Convenience method to produce a string representation of the 
	  // tokenized tweet in a standard-ish format.
	  def tokenizeToString (text: String): String = {
	  	tokenizeForTagger(text).mkString(" ");
	  }
	  */
}