package de.fusionfactory.index_vivus.tokenizer;

import de.fusionfactory.index_vivus.xmlimport.GeorgesImporter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 13:49
 */
public class main {
	private Logger logger;

	private static String testArticle = "abhorreo\n" +
			"\n" +
			"ab-horreo, uī, ēre, I) vor etwas zurückschaudern, etwas verabscheuen, gegen etwas eine starke Abneigung haben, von etwas aus Abscheu oder Abneigung fernbleiben, -nichts wissen wollen, jmdm. od. einer Sache abhold sein, gegen jmd. od. etwas eingenommen sein, m. ab u. Abl., ab hac domo, Titin.: a pace, Caes.: ab re uxoria, Ter.: a ducenda uxore, Cic.: a Berenice, Tac.: mit bl. Abl. (s. Nipperd. zu Tac. ann. 14, 21), tanto facinore procul animo, Curt.: non abh. spectaculorum oblectamentis, Tac. – nachaug. m. Acc., cadaverum tabem, Suet.: pumilos, Suet.: exemplum huius modi, Dict.: m. Infinit., Augustin. serm. 184, 3. Porphyr. Hor. carm. 1, 1, 16. – absol., sin plane abhorrebit et absurdus erit, sollte er aber dazu gar keine Neigung u. Fähigkeit haben, Cic. de or. 2, 85: omnes aspernabantur, omnes abhorrebant, Cic. Clu. 41: ut aut cupiant (sc. reo) aut abhorreant, Cic. de or. 2, 185: postquam abhorrere eos videt, Auct. b. Afr. 73, 5. – II) übtr., gleichs. von Natur mit etwas nicht im Einklang-, im Widerspruch stehen, unverträglich-, ihm zuwider sein, nicht zusagen, zuwiderlaufen, zu etwas nicht passen, von etw. abweichen, von etw. verschieden-, fern-, ihm fremd sein, ab oculorum auriumque approbatione, den Augen und Ohren anstößig sein, Cic.: oratio abhorret a persona hominis gravissimi, Cic.: abh. a fide, unglaublich sein, Liv.: consilium abhorret a tuo scelere, Cic.: spes ab effectu haud abhorrens, Hoffnung der Ausführbarkeit, Liv.: temeritas tanta, ut non procul abhorreat ab insania, Cic.: longe ab ista suspicione abhorrere debet, Cic.: a quo (vitae statu) mea longissime ratio voluntasque abhorret, Cic.: orationes abhorrent inter se, widersprechen einander, Liv.: m. bl. Abl. (s. Nipperd. zu Tac. ann. 14, 21), abhorrens peregrinis auribus carmen, Curt.: neque abhorret vero, Tac.: nec abhorrebat moribus uxor, Flor.: u.m. bl. Dat., huic tam pacatae profectioni abhorrens mos, Liv.: nec abhorret a veritate m. folg. Acc. u. Infin., Suet. Cal. 12, 3. – Dah. abhorrens, unpassend, unstatthaft, carmen nunc abhorrens, Liv.: vestrae istae absurdae atque abhorrentes lacrimae, Liv. – / Abl. abhorrenti, Gell. 10, 12, 10.\n";

	public main(String[] args) {
		logger = Logger.getLogger(this.getClass());
		GeorgesImporter georgesImporter = new GeorgesImporter();
		Tokenizer tokenizer = new Tokenizer();
		List<String> germanWords = tokenizer.getTokenizedString(testArticle);
		for (String s : germanWords) {
			logger.info(s);
		}
	}

	public static void main(String[] args) {
		new main(args);
	}
}
