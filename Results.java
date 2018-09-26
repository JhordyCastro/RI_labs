package org.novasearch.tutorials.labs2018.RI_labs;

public class Results {

	private int queryID;
	/*
	private String q0;
	private int docID;
	private int rank;
	private String runID;
	*/
	
	public Results(int queryID) {
		
		this.queryID = queryID;
		/*this.q0 = "q0";
		this.docID = docID;
		this.rank = rank;
		this.runID = "runID";*/
	}
	
	public String toString(){
		return "QueryID	"+queryID+"\n";  /*+ "Q0		"+q0+"DocID		"+docID+"Rank"		+rank+"RunID		"+runID +"\n";*/
		
	}

}
