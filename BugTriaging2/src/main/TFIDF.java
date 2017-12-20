package main;

import java.util.Arrays;
import java.util.List;

public class TFIDF {
    public double tf(List<String> doc, String term) {
        double result = 0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / doc.size();
    }

    public double idf(List<List<String>> docs, String term) {
        double n = 0;
        for (List<String> doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
        }
        return Math.log(docs.size() / n);
    }

    public double tfIdf(List<String> doc, List<List<String>> docs, String term) {
        return tf(doc, term) * idf(docs, term);
    }

	public static void main(String[] args) {
		List<String> doc1 = Arrays.asList("Lorem", "ipsum", "dolor", "ipsum", "sit", "ipsum");
		List<String> doc2 = Arrays.asList("Vituperata", "incorrupte", "at", "ipsum", "pro", "quo");
		List<String> doc3 = Arrays.asList("Has", "persius", "disputationi", "id", "simul");
		List<List<String>> documents = Arrays.asList(doc1, doc2, doc3);

		TFIDF calculator = new TFIDF();
		double tfidf = calculator.tfIdf(doc1, documents, "ipsum");
		System.out.println("TF-IDF (ipsum) = " + tfidf);
	}

	
}