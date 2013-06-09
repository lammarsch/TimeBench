// Pattern Over Time Semantic Blended Levels Integrated Temporal Zoom

// TODO
// Labels (how best? multiple renderers per item?)
// soft fading (animation)

package timeBench.demo.vis;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.IntervalBarRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.Layout;
import prefuse.controls.ToolTipControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.io.DataIOException;
import prefuse.data.tuple.TupleSet;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import render.ArcRenderer;
import timeBench.action.analytical.PatternCountAction;
import timeBench.action.analytical.TreeDebundlingAction;
import timeBench.action.layout.GreedyDistributionLayout;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.PatternOverlayCheckLayout;
import timeBench.action.layout.ThemeRiverLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.TimeAxisLayout.Placement;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.controls.BranchHighlightControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.io.GraphMLTemporalDatasetReader;
import timeBench.ui.TimeAxisDisplay;
import timeBench.util.DebugHelper;
import timeBench.util.DemoEnvironmentFactory;
import visual.sort.SizeItemSorter;

public class POTSBLITZDemo {

    private static final String MAXX_FIELD = VisualItem.X2;
    private static final String ARCDIAGRAM_PATTERNS = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
    private static final String ARCDIAGRAM_EVENTS = "arcdiagram_events"; // Don't know if . is reserved in prefuse
    private static final String PATTERNTIMELINES = "patterntimelines";
    private static final String PATTERNTHEMERIVER = "patternthemeriver";
    
    private static final String PATTERNTIMELINES_DECORATOR = "patterntimelines_decorator";
    private static final String PATTERNTHEMERIVER_DECORATOR = "patternthemeriver_decorator";
    
    //private static final String PATTERNTIMELINES_EVENTS = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
    
    static private ArrayList<String> classes;

    private static void createVisualization(TemporalDataset patterns, TemporalDataset events,TemporalDataset flatPatterns, TemporalDataset countedPatterns) {
        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        display.setSize(1200, 600);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data
       
        VisualGraph vg = vis.addGraph(ARCDIAGRAM_PATTERNS, patterns);
        VisualGraph vge = vis.addGraph(ARCDIAGRAM_EVENTS, events);
        VisualGraph vgf = vis.addGraph(PATTERNTIMELINES,flatPatterns);
        
        vis.addDecorators(PATTERNTIMELINES_DECORATOR, PATTERNTIMELINES+".nodes");
        
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);
        vge.getNodeTable().addColumn(MAXX_FIELD, int.class);
        vgf.getNodeTable().addColumn(MAXX_FIELD, int.class);

        long border = (events.getSup() - events.getInf()) / 20;
        final AdvancedTimeScale timeScale = new AdvancedTimeScale(
                events.getInf() - border, events.getSup() + border,
                display.getWidth() - 1);
        final AdvancedTimeScale overviewTimeScale = new AdvancedTimeScale(timeScale);
        RangeAdapter rangeAdapter = new RangeAdapter(overviewTimeScale,
                timeScale);

        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
            }
        });

        // --------------------------------------------------------------------
        // STEP 2: set up renderers for the visual data
                
        // intRenderer.setAxis(Constants.Y_AXIS);
        RendererFactory rf = new RendererFactory() {
        	ArcRenderer arcRenderer = new ArcRenderer();
        	PolygonRenderer polygonRenderer = new PolygonRenderer(Constants.POLY_TYPE_STACK);
        	IntervalBarRenderer intRenderer = new IntervalBarRenderer(MAXX_FIELD);
        	LabelRenderer labelRenderer = new LabelRenderer("label");

                public Renderer getRenderer(VisualItem item) {
                	if(item.isInGroup(ARCDIAGRAM_PATTERNS))
                		return arcRenderer;
                	else if(item.isInGroup(PATTERNTHEMERIVER))
                		return polygonRenderer;
                	else if(item.isInGroup(PATTERNTIMELINES_DECORATOR) || item.isInGroup(PATTERNTHEMERIVER_DECORATOR))
                		return labelRenderer;
                	else return intRenderer;
                }
        };
 
        // DefaultRendererFactory rf = new DefaultRendererFactory(new
        // LabelRenderer("caption"));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        ActionList layout = new ActionList();
        
        AxisLayout y_axis = new AxisLayout(ARCDIAGRAM_EVENTS, VisualItem.VISIBLE, Constants.Y_AXIS);
        layout.add(y_axis);
        AxisLayout y_axis2 = new AxisLayout(ARCDIAGRAM_PATTERNS+".nodes", VisualItem.VISIBLE, Constants.Y_AXIS);
        layout.add(y_axis2);
        TimeAxisLayout time_axis = new IntervalAxisLayout(ARCDIAGRAM_PATTERNS, MAXX_FIELD, Constants.X_AXIS,
        		timeScale,Placement.MIDDLE,new BooleanLiteral(true));
        TimeAxisLayout time_axis2 = new IntervalAxisLayout(ARCDIAGRAM_EVENTS, MAXX_FIELD, timeScale);                       
        layout.add(time_axis);
        layout.add(time_axis2);
        
        PatternOverlayCheckLayout patternOverlapCheckLayout = new PatternOverlayCheckLayout(ARCDIAGRAM_PATTERNS,ARCDIAGRAM_EVENTS,PATTERNTIMELINES,6);
        layout.add(patternOverlapCheckLayout);
        
        TimeAxisLayout time_axis3 = new IntervalAxisLayout(PATTERNTIMELINES, MAXX_FIELD, Constants.X_AXIS,
        		timeScale,Placement.MIDDLE,new BooleanLiteral(true));
        GreedyDistributionLayout y_axis3 = new GreedyDistributionLayout(PATTERNTIMELINES, PATTERNTHEMERIVER, 14);
        layout.add(time_axis3);
        layout.add(y_axis3);
        
        ThemeRiverLayout themeRiver = new ThemeRiverLayout(PATTERNTHEMERIVER,countedPatterns,classes,timeScale);
        layout.add(themeRiver);

        layout.add(new DecoratorLayout(PATTERNTIMELINES_DECORATOR));
        //layout.add(new DecoratorLayout2(PATTERNTHEMERIVER_DECORATOR));
        
        layout.add(new DataColorAction(ARCDIAGRAM_EVENTS, "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] {DemoEnvironmentFactory.set3Qualitative[3],
        		DemoEnvironmentFactory.set3Qualitative[4], DemoEnvironmentFactory.set3Qualitative[6]}));
        layout.add(new DataColorAction(ARCDIAGRAM_PATTERNS+".nodes", "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] { DemoEnvironmentFactory.set3Qualitative[3],
        		DemoEnvironmentFactory.set3Qualitative[4], DemoEnvironmentFactory.set3Qualitative[6]}));
        layout.add(new DataColorAction(PATTERNTIMELINES, "class", prefuse.Constants.ORDINAL,
        		VisualItem.FILLCOLOR,DemoEnvironmentFactory.set3Qualitative));
        layout.add(new ColorAction(PATTERNTIMELINES_DECORATOR, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        /*layout.add(new ColorAction(PATTERNTHEMERIVER_DECORATOR, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));*/
        layout.add(new DataColorAction(PATTERNTHEMERIVER, "class", prefuse.Constants.ORDINAL,
        		VisualItem.FILLCOLOR,DemoEnvironmentFactory.set3Qualitative));       
        layout.add(new ColorAction(PATTERNTHEMERIVER, VisualItem.STROKECOLOR,ColorLib.color(Color.WHITE)));


        //layout.add(new DataColorAction(PATTERNTIMELINES, VisualItem.VISIBLE, ColorLib.gray(0),VisualItem.FILLCOLOR));
        //layout.add(new DataColorAction(PATTERNTHEMERIVER, VisualItem.VISIBLE, ColorLib.gray(0),VisualItem.FILLCOLOR));
        
        layout.add(new RepaintAction());
        
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, layout);
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, layout);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls
        display.setHighQuality(true);
        display.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        display.setItemSorter(new SizeItemSorter());


        //display.addControlListener(new ToolTipControl("caption"));
        display.addControlListener(new BranchHighlightControl());
        display.addControlListener(new ToolTipControl("label"));
              
//        vis.run("layout");
       
        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("arc diagram");
        env.setPaintWeekends(false);
        System.out.println("--------");
        env.show(display, rangeAdapter,false);
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
    	Locale.setDefault(Locale.US);
		TemporalDataset events = null;
		TemporalDataset patterns = null;
		TemporalDataset flatPatterns = null;
		TemporalDataset countedPatterns = null;
		try {
			GraphMLTemporalDatasetReader gmltdr = new GraphMLTemporalDatasetReader();
			events = gmltdr.readData("data/Dodgers-events.graphml.gz");
			
			//DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
			
			patterns = gmltdr.readData("data/Dodgers-patterns.graphml.gz");
						
			//DebugHelper.printTemporalDatasetForest(System.out,patterns, "label",TemporalObject.ID);						
		} catch (DataIOException e) {
			e.printStackTrace();
		}			
		
        //DataHelper.printMetadata(System.out, events.getNodeTable());
		//DataHelper.printMetadata(System.out, patterns.getNodeTable());
		
		TreeDebundlingAction action = new TreeDebundlingAction(patterns);
		action.run(0);
		flatPatterns = action.getTemporalDataset();
		classes = action.getClasses();

		System.out.println(flatPatterns.getNodeCount());
		DebugHelper.printTemporalDatasetTable(System.out, flatPatterns,"label","class",TemporalObject.ID);
		
		PatternCountAction action2 = new PatternCountAction(flatPatterns);
		action2.run(0);
		countedPatterns = action2.getTemporalDataset();   

		//System.out.println(flatPatterns.getNodeCount());
		//DataHelper.printTable(System.out,countedPatterns.getTemporalObjectTable());
		//try {
			//DataHelper.printTable(new PrintStream("test.txt"),countedPatterns.getTemporalObjectTable());
		//} catch (FileNotFoundException e) {e.printStackTrace();}
		
        createVisualization(patterns,events,flatPatterns,countedPatterns);
    }
    
    static class DecoratorLayout extends Layout {
        public DecoratorLayout(String group) {
            super(group);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run(double frac) {
            Iterator iter = super.m_vis.items(super.m_group);
            while (iter.hasNext()) {
                DecoratorItem item = (DecoratorItem) iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getCenterX());
                setY(item, null, bounds.getCenterY());
                item.setVisible(node.isVisible());
            }
        }
    }
    
    static class DecoratorLayout2 extends Layout {
        public DecoratorLayout2(String group) {
            super(group);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run(double frac) {
            Iterator iter = super.m_vis.items(super.m_group);
            while (iter.hasNext()) {
                DecoratorItem item = (DecoratorItem) iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();     
                double x = node.getDouble("labelX");
                double y = node.getDouble("labelY");
                if (x != 0 && y != 0 && m_vis.getDisplay(0).contains((int)x,(int)y)) {
                	setX(item, null, x);
                	setY(item, null, y);
                    item.setVisible(node.isVisible());
                } else
                    item.setVisible(false);
            }
        }
    }
}

