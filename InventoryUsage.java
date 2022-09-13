import java.sql.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.*;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale.Category;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset; 
import org.jfree.data.category.DefaultCategoryDataset; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;

public class InventoryUsage extends JPanel {
    class MealProduct {
        String productID;
        int count;
    }
    private Connection conn;
    private HashMap<String, Integer> amountsUsed;
    private ArrayList<String> mealIDs;
    private ArrayList<String> productIDs;

    private HashMap<String, ArrayList<MealProduct>> mealProducts;
    private HashMap<String, String> productIDToName;
    private HashMap<String, Integer> dayNums;

    private int startWeek = 1;
    private int endWeek = 1;
    private String startDay = "Monday";
    private String endDay = "Tuesday";

    JFreeChart chart;
    ChartPanel chartPanel;
    public void refreshChart() throws SQLException {
        removeAll();
        createChart();
        add(chartPanel, BorderLayout.CENTER);
    }
    private void createChart() throws SQLException{
        chart = ChartFactory.createBarChart(
         "Inventory Usage",           
         "Item",            
         "Amount Used",            
         refreshData(startWeek, endWeek, startDay, endDay),          
         PlotOrientation.VERTICAL,           
         false, true, false);

        CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
         domainAxis.setCategoryLabelPositions(
             CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/180 * 90.0));
         
        chartPanel = new ChartPanel( chart );        
    }

    public static ArrayList<String> queries(String raw, int weeka, int weekb, int daya, int dayb) {
        return null;
    }

    public void updateDayNums() throws SQLException{
        String getall = "SELECT DISTINCT week,day FROM orders";
        Statement stmt = conn.createStatement();
        ResultSet data = stmt.executeQuery(getall);
        ArrayList<String> updateQueries = new ArrayList<String>();
        while(data.next()) {
            int week = Integer.parseInt(data.getString("week"));
            String dayName = data.getString("day");
            int day = dayNums.get(dayName);
            int num = week*7 + day;
            String update = String.format("update orders set daynum=%d where week=%d and day=\'%s\'", num, week, dayName);
            updateQueries.add(update);
        }
        for(String update:updateQueries) {
            stmt.executeUpdate(update);
        }
        
    }

    private CategoryDataset refreshData(int weeka, int weekb, int daya, int dayb) throws SQLException {
        int datea = 7*weeka + daya;
        int dateb = 7*weekb + dayb;

        String getOrders = String.format("SELECT * FROM orders WHERE daynum>=%d AND daynum<=%d ORDER BY daynum", datea, dateb);
        //System.out.printf("Query: %s\n", getOrders);
        Statement stmt = conn.createStatement();
        ResultSet orderSet = stmt.executeQuery(getOrders);

        HashMap<String, Integer> mealCounts = new HashMap<String, Integer>();

        for(String mealID:mealIDs)
            mealCounts.put(mealID, 0);

        while(orderSet.next()) {
            String mealID = orderSet.getString("mealid");
            int mealCount = Integer.parseInt(orderSet.getString("numsold"));
            mealCounts.put(mealID, mealCounts.get(mealID) + mealCount);
        }
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(String mealID:mealIDs) {
            int mealCount = mealCounts.get(mealID);
            ArrayList<MealProduct> products = mealProducts.get(mealID);
            for(MealProduct mealProduct:products) {
                String productID = mealProduct.productID;
                if(productID.equals(""))
                    continue;
                int totalCount = mealCount * mealProduct.count;
                //System.out.printf("%s, %s, %d, %d\n", mealID, productID, mealCount, mealProduct.count);
                String inventoryQuery = String.format("select order_convert, stock_measurement, name from inventory where product_id=\'%s\'", productID);
                ResultSet inventorySet = stmt.executeQuery(inventoryQuery);
                inventorySet.next();
                String productName = inventorySet.getString("name");
                String stockMeasurement = inventorySet.getString("stock_measurement");
                float orderConvert = Float.parseFloat(inventorySet.getString("order_convert"));
                float totalUsed = totalCount * orderConvert;
                String xLabel = String.format("%s (%3.2f %s)", productName, totalUsed, stockMeasurement);
                dataset.addValue(totalUsed, "", xLabel);
                //System.out.printf("%f %s of %s used\n", totalCount*orderConvert, stockMeasurement, productName);
            }
        }
        return dataset;
    }

    private CategoryDataset refreshData(int weeka, int weekb, String daya, String dayb) throws SQLException {
        return refreshData(weeka, weekb, dayNums.get(daya), dayNums.get(dayb));
    }

    private JComboBox<String> createComboBox(Object[] data) {
        JComboBox<String> box = new JComboBox<String>();
        for(Object element:data) {
            box.addItem((String)element);
        }
        return box;
    }
    public InventoryUsage(Connection c, int sw, int ew, String sd, String ed) {
        super(new BorderLayout());
        startWeek = sw;
        endWeek = ew;
        startDay = sd;
        endDay = ed;
        //System.out.\/("Start end day: %s, %s\n", startDay, endDay);
        /*try {
            Class.forName("org.jfree.chart");
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }*/
        try {
            this.conn = DriverManager.getConnection(GUI.db, GUI.user, GUI.password);
        } catch(SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        amountsUsed = new HashMap<String, Integer>();
        productIDs = new ArrayList<String>();
        mealIDs = new ArrayList<String>();
        mealProducts = new HashMap<String, ArrayList<MealProduct>>();
        productIDToName = new HashMap<String, String>();
        dayNums = new HashMap<String, Integer>();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for(int i=0;i<days.length;i++) {
            dayNums.put(days[i], i);
        }

        ArrayList<String> weeks = new ArrayList<String>();

        // Initialize and update internal data structures
        try {
            String getAllQuery = "SELECT * FROM inventory";
            Statement stmt = conn.createStatement();
            ResultSet inventorySet = stmt.executeQuery(getAllQuery);
            while(inventorySet.next()) {
                String productID = inventorySet.getString("product_id");
                String productName = inventorySet.getString("name");

                productIDs.add(productID);
                productIDToName.put(productID, productName);
            }
            inventorySet.close();
            updateDayNums();
            String getMealIDs = "SELECT DISTINCT mealid FROM menu";
            ResultSet mealIDSet = stmt.executeQuery(getMealIDs);
            while(mealIDSet.next()) {
                String mealID = mealIDSet.getString("mealid");
                mealIDs.add(mealID);
                mealProducts.put(mealID, new ArrayList<MealProduct>());
            }
            String getAllMenu = "SELECT * FROM MENU";
            ResultSet menuSet = stmt.executeQuery(getAllMenu);
            while(menuSet.next()) {
                String mealID = menuSet.getString("mealid");
                String productID = menuSet.getString("productid");
                int count = Integer.parseInt(menuSet.getString("count"));

                MealProduct mp = new MealProduct();
                mp.count = count;
                mp.productID = productID;

                mealProducts.get(mealID).add(mp);
            }

            String getWeeks = "select distinct week from orders order by week;";
            ResultSet weekSet = stmt.executeQuery(getWeeks);
            while(weekSet.next()) {
                weeks.add(weekSet.getString("week"));
            }
            //refreshData(startWeek, endWeek, startDay, endDay);
            //createChart();
            //add(chartPanel, BorderLayout.CENTER);

            
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

        JComboBox<String> startWeekBox = createComboBox(weeks.toArray());
        JComboBox<String> endWeekBox = createComboBox(weeks.toArray());
        JComboBox<String> startDayBox = createComboBox(days);
        JComboBox<String> endDayBox = createComboBox(days);

        startDayBox.setSelectedItem(startDay);
        endDayBox.setSelectedItem(endDay);
        startWeekBox.setSelectedItem(""+startWeek);
        endWeekBox.setSelectedItem(""+endWeek);

        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startPanel.add(startWeekBox);
        startPanel.add(startDayBox);

        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        endPanel.add(endWeekBox);
        endPanel.add(endDayBox);


        JPanel refreshPanel = new JPanel();
        refreshPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { 
                remove(chartPanel);
                startWeek = Integer.parseInt((String)startWeekBox.getSelectedItem());
                endWeek = Integer.parseInt((String)endWeekBox.getSelectedItem());
                startDay = (String)startDayBox.getSelectedItem();
                endDay = (String)endDayBox.getSelectedItem();
                refreshButton.setText("Refreshing...");
                revalidate();
                try {
                    createChart();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
                add(chartPanel);
                refreshButton.setText("Refresh Data");
                revalidate();
            }
        });
        refreshPanel.add(refreshButton);

        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.add(startPanel);
        boxPanel.add(endPanel);
        boxPanel.add(refreshPanel);

        //add(boxPanel, BorderLayout.NORTH);
    }
}