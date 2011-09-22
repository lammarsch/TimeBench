package timeBench.test;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import timeBench.data.relational.TemporalDataset;
import timeBench.data.util.DefaultIntervalComparator;
import timeBench.data.util.IntervalTreeIndex;

public class TestTemporalDataSet {
	/**
	 * Test
	 * 
	 * @param args
	 *            system arguments
	 */
	public static void main(String[] args) {
		
		TemporalDataset dataset = new TemporalDataset();
		dataset.addTemporalElement(1, 10, 1, 1);
		dataset.addTemporalElement(4, 12, 1, 1);
		dataset.addTemporalElement(6, 8, 1, 1);
		dataset.addTemporalElement(3, 14, 1, 1);
		dataset.addTemporalElement(2, 6, 1, 1);
		dataset.addTemporalElement(5, 13, 1, 1);
		
		Table dataElements = dataset.getDataElements();
		dataElements.addColumn("ID", String.class);
		dataElements.addColumn("Age", int.class);
		dataElements.addRows(6);
		dataElements.set(0, "ID", "Morg"); dataElements.set(0, "Age", 28);
		dataElements.set(1, "ID", "Ben"); dataElements.set(1, "Age", 26);
		dataElements.set(2, "ID", "Xu"); dataElements.set(2, "Age", 29);
		dataElements.set(3, "ID", "Fab"); dataElements.set(3, "Age", 21);
		dataElements.set(4, "ID", "Sha"); dataElements.set(4, "Age", 25);
		dataElements.set(5, "ID", "Tra"); dataElements.set(5, "Age", 29);
		
		dataset.addOccurrence(0, 0);
		dataset.addOccurrence(1, 1);
		dataset.addOccurrence(2, 2);
		dataset.addOccurrence(3, 3);
		dataset.addOccurrence(4, 4);
		dataset.addOccurrence(5, 5);
		
	}


}