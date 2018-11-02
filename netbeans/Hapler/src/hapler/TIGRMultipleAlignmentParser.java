/*
 *  Copyright 2011 Shawn Thomas O'Neil
 *
 *  This file is part of Hapler.
 *
 *  Hapler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Hapler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Hapler.  If not, see <http://www.gnu.org/licenses/>.
 */

package hapler;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author soneil
 */
public class TIGRMultipleAlignmentParser extends AbstractMultipleAlignmentParser {

	public TIGRMultipleAlignmentParser() {
	}

	public ArrayList<MultipleAlignment> openFile(String fileName) throws Exception {
		ArrayList<MultipleAlignment> multipleAlignmentList = new ArrayList<MultipleAlignment>();
		//Parsing code here...

		//try {
			BufferedReader br = null;
			if(fileName.compareTo("-") != 0) {
				FileInputStream fstream = new FileInputStream(fileName);
				DataInputStream in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
			}
			else {
				br = new BufferedReader(new InputStreamReader(System.in));
			}

			// gettingRead will be true if we are reading a read, false if we are
			// reading a consensus
			boolean gettingRead = false;
			MultipleAlignment alignment = null;
			Sequence sequence = null;
			int currentStartPosition = 0;
			String currentName = "";

			// A temporary structure for building up the sequence string
			ArrayList<String> sequenceArray = null;

			String strLine;

			boolean printedGapWarning = false;

			while ((strLine = br.readLine()) != null) {

				// If we are getting a new multiple alignment
				if(strLine.matches("^##.*")) {
					// If this isn't our first contig, we must have a read to stuff into an already
					// existing sequence, which we need to stuff into the old alignment
					if(alignment != null && gettingRead == true) { // our sequence is finished
						sequence = new Sequence();
						//sequence.addPiece(join(sequenceArray, ""), currentStartPosition);
						boolean seqHasGaps = sequence.addPieces(sequenceArray, currentStartPosition);
						if(!printedGapWarning && seqHasGaps) {
							System.out.println("#!WARNING: alignment " + alignment.getName() + " either has reads containing ~ characters or mate-pair reads. These are being SPLIT into separate reads (e.g., mate pair information is being ignore.)");
							printedGapWarning = true;
						}
						sequence.setName(currentName);
						alignment.addSequence(sequence);
					}
					gettingRead = false;
					alignment = createNewMultipleAlignment(strLine);
					multipleAlignmentList.add(alignment);
					//sequence = new Sequence();
					sequenceArray = new ArrayList<String>();
					//System.out.println(strLine);
				}
				// If we are getting a new read
				else if(strLine.matches("^#.*")) {
					// Here we must be done reading some sequence... now, did we just get a read or a consensus
					if(!gettingRead) {
						alignment.setGivenConsensus(join(sequenceArray,""));
					}
					else { // our last sequence is finished
						sequence = new Sequence();
						//sequence.addPiece(join(sequenceArray, ""), currentStartPosition);
						boolean seqHasGaps = sequence.addPieces(sequenceArray, currentStartPosition);
						if(!printedGapWarning && seqHasGaps) {
							System.out.println("#!WARNING: alignment " + alignment.getName() + " either has reads containing ~ characters or mate-pair reads. These are being SPLIT into separate reads (e.g., mate pair information is being ignore.)");
							printedGapWarning = true;
						}
						sequence.setName(currentName);
						alignment.addSequence(sequence);
					}
					gettingRead = true;
					//sequence = createNewSequence(strLine);
					currentName = this.extractNameFromTIGRSeqLine(strLine);
					currentStartPosition = this.extractStartPositionFromTIRGSeqLine(strLine);
					sequenceArray = new ArrayList<String>();
					//System.out.println(strLine + " read def");
				}
				// We are getting a line of either the consensus or the a read
				else {
					sequenceArray.add(strLine);
				}
			}
		//}
		/*catch (Exception e) {
			System.err.println("#############################");
			System.err.println("Error: " + e.getMessage());
			System.err.println("#############################");
			System.err.println();
			e.printStackTrace();
		}*/


		return multipleAlignmentList;
	}



	/**
	 * Given a line from a TIGR format assembly file starting with ##
	 * (defining a multiple alignment) create a shell of one that we can
	 * stick reads in
	 * @param inLine
	 * @return
	 */
	private MultipleAlignment createNewMultipleAlignment(String inLine) {
		MultipleAlignment alignment = new MultipleAlignment();
		String[] lineArray = inLine.split("[ \t\n\r]+");
		String name = lineArray[0].replaceFirst("^##", "");
		//int numReads = Integer.parseInt(lineArray[1]);
		//int numBases = Integer.parseInt(lineArray[2]);

		alignment.setName(name);
		return alignment;
	}


	/**
	 * Given a line from a TIGR format assembly file starting with #
	 * (defining a sequence) return the name of the sequence
	 * @param inLine
	 * @return
	 */
	private String extractNameFromTIGRSeqLine(String inLine) {
		//Sequence seq = new Sequence();
		String[] lineArray = inLine.split("[\\(\\)]");
		String name = lineArray[0].replaceFirst("^#","");
		//int startPosition = Integer.parseInt(lineArray[1]);

		//seq.setName(name);
		//seq.setStartPosition(startPosition);
		return name;
	}

	/**
	 * Given a line from a TIGR format assembly file starting with #
	 * (defining a sequence) return the name of the sequence
	 * @param inLine
	 * @return
	 */
	private int extractStartPositionFromTIRGSeqLine(String inLine) {
		//Sequence seq = new Sequence();
		String[] lineArray = inLine.split("[\\(\\)]");
		String name = lineArray[0].replaceFirst("^#","");
		int startPosition = Integer.parseInt(lineArray[1]);

		//seq.setName(name);
		//seq.setStartPosition(startPosition);
		return startPosition;
	}


	/**
	 * Given an arraylist of strings, joins them together to one big string.
	 * @param stringArrayList
	 * @param separator
	 * @return
	 */
	private String join(ArrayList<String> stringArrayList, String separator) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for(String piece : stringArrayList) {
			if(!first) sb.append(separator);
			sb.append(piece);
			first = false;
		}
		return sb.toString();
	}
}
