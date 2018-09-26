package org.novasearch.tutorials.labs2018.RI_labs;
//Jhordy Castro  Bruno Ramos N�41675

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Lab1_Baseline {

	String indexPath = "./index";
	String docPath = "./data/Answers.csv";
	String queriesFilePath = "./eval/queries.offline.txt";

	boolean create = true;

	private IndexWriter idx; //creates or maintains the index

	public static void main(String[] args) {

		Analyzer analyzer = new StandardAnalyzer(); // analizes the text and creates tokens
		Similarity similarity = new ClassicSimilarity(); // determines how Lucene weights terms
		//List<Results> textFileResults = new ArrayList<>();
		Lab1_Baseline baseline = new Lab1_Baseline();

		// Create a new index
		baseline.openIndex(analyzer, similarity);
		baseline.indexDocuments();
		baseline.close();

		// Search the index
		baseline.indexSearch(analyzer, similarity);
	}

	public void openIndex(Analyzer analyzer, Similarity similarity) {
		try {
			// ====================================================
			// Configure the index to be created/opened
			//
			// IndexWriterConfig has many options to be set if needed.
			//
			// Example: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setSimilarity(similarity);
			if (create) {
				// Create a new index, removing any
				// previously indexed documents:
				iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			}

			// ====================================================
			// Open/create the index in the specified location
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			idx = new IndexWriter(dir, iwc);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void indexDocuments() {
		if (idx == null)
			return;

		// ====================================================
		// Parse the Answers data
		try (BufferedReader br = new BufferedReader(new FileReader(docPath))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine(); // The first line is dummy
			line = br.readLine();

			// ====================================================
			// Read documents
			while (line != null) {
				int i = line.length();

				// Search for the end of document delimiter
				if (i != 0)
					sb.append(line);
				sb.append(System.lineSeparator());
				if (((i >= 2) && (line.charAt(i - 1) == '"') && (line.charAt(i - 2) != '"'))
						|| ((i == 1) && (line.charAt(i - 1) == '"'))) {
					// Index the document
					indexDoc(sb.toString());

					// Start a new document
					sb = new StringBuilder();
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void indexDoc(String rawDocument) {

		Document doc = new Document();

		// ====================================================
		// Each document is organized as:
		// Id,OwnerUserId,CreationDate,ParentId,Score,Body
		Integer AnswerId = 0;
		try {

			// Extract field Id
			Integer start = 0;
			Integer end = rawDocument.indexOf(',');
			String aux = rawDocument.substring(start, end);
			AnswerId = Integer.decode(aux);

			// Index _and_ store the AnswerId field
			doc.add(new IntPoint("AnswerId", AnswerId));
			doc.add(new StoredField("AnswerId", AnswerId));

			// Extract field OwnerUserId
			start = end + 1;
			end = rawDocument.indexOf(',', start);
			aux = rawDocument.substring(start, end);
			Integer OwnerUserId = Integer.decode(aux);
			doc.add(new IntPoint("OwnerUserId", OwnerUserId));

			// Extract field CreationDate
			try {
				start = end + 1;
				end = rawDocument.indexOf(',', start);
				aux = rawDocument.substring(start, end);
				Date creationDate;
				creationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(aux);
				doc.add(new LongPoint("CreationDate", creationDate.getTime()));
			} catch (ParseException e1) {
				System.out.println("Error parsing date for document " + AnswerId);
			}
				
			// Extract field ParentId
			start = end + 1;
			end = rawDocument.indexOf(',', start);
			aux = rawDocument.substring(start, end);
			Integer ParentId = Integer.decode(aux);
			doc.add(new IntPoint("ParentId", ParentId));
					
			// Extract field Score
			start = end + 1;
			end = rawDocument.indexOf(',', start);
			aux = rawDocument.substring(start, end);
			Integer Score = Integer.decode(aux);
			doc.add(new IntPoint("Score", Score));

			// Extract field Body
			String body = rawDocument.substring(end + 1);
			doc.add(new TextField("Body", body, Field.Store.YES));

		// ====================================================
		// Add the document to the index
			if (idx.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
				System.out.println("adding " + AnswerId);
				idx.addDocument(doc);
			} else {
				idx.updateDocument(new Term("AnswerId", AnswerId.toString()), doc);
			}
		} catch (IOException e) {
			System.out.println("Error adding document " + AnswerId);
		} catch (Exception e) {
		System.out.println("Error parsing document " + AnswerId);
		}
	}

	// ====================================================
	// Comment and refactor this method yourself
	public void indexSearch(Analyzer analyzer, Similarity similarity) {

		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(similarity);

			BufferedReader in = null;
			in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			BufferedReader fReader = new BufferedReader(new FileReader(queriesFilePath));
			
			//BufferedReader in2 = null;
			//String n_docPath = "./eval/queries.offline.txt";
			//BufferedReader in2 = new BufferedReader(new InputStreamReader(n_docPath, StandardCharsets.UTF_8));
			
			
			QueryParser parser = new QueryParser("Body", analyzer);
			while (true) {
				System.out.println("Enter query: ");

				String line = in.readLine();

				if (line == null || line.length() == -1) {
					break;
				}

				line = line.trim();
				if (line.length() == 0) {
					break;
				}
			
				
				Query query;
				try {
					query = parser.parse(line);
				} catch (org.apache.lucene.queryparser.classic.ParseException e) {
					System.out.println("Error parsing query string.");
					continue;
				}
				
				
				TopDocs results = searcher.search(query, 5);
				ScoreDoc[] hits = results.scoreDocs;
				
				String header = "QueryId	Q0	DocId	Rank	Score	RunId ";

				int numTotalHits = results.totalHits;
				System.out.println(numTotalHits + " total matching documents");
				
				String line_string = "";
				//Integer queryID = 0;
				System.out.println(header);
				
				for (int j = 0; j < hits.length; j++) {
					try {
						String fileLines = fReader.readLine(); // The first line is dummy
					//while(fileLines != null){
							
							//String [] queryParts = fileLines.split(":");
							//queryID = Integer.parseInt(queryParts[0]);
							Document doc = searcher.doc(hits[j].doc);
							//String answer = doc.get("Body");
							Integer AnswerId = doc.getField("AnswerId").numericValue().intValue();
							//Integer queryid = doc.getField("QueryId").numericValue().intValue();
							
							
							line_string = "QueryID  " + queryid+	" QO 	" + AnswerId + "	" + (j+1) + "	" + hits[j].score + "		Run-1	";
							System.out.println(line_string);
						
						//}
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					
					/*Integer AnswerId = doc.getField("AnswerId").numericValue().intValue();
					
					
					System.out.println("AnswerId:	"+ AnswerId);
					
					System.out.println("Rank: " + (j+1));
					System.out.println("Score: " + hits[j].score);
					System.out.println("------------------------------------------");
					
					System.out.println("Answer: " + answer);
					System.out.println();*/
				}

				if (line.equals("")) {
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			try {
				reader.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			idx.close();
		} catch (IOException e) {
			System.out.println("Error closing the index.");
		}
	}

}
