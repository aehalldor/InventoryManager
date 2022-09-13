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
import java.util.HashMap;




/*
  TODO:
  1) Change credentials for your own team's database
  2) Change SQL command to a relevant query that retrieves a small amount of data
  3) Create a JTextArea object using the queried data
  4) Add the new object to the JPanel p
*/

public class ManagerGUI extends JFrame implements ActionListener {
    private Connection conn;

    public static ResultSet getData(Connection conn, String query) {
        try {
          Statement stmt = conn.createStatement();
          ResultSet results = stmt.executeQuery(query);
          return results;
        } catch(SQLException e) {
          e.printStackTrace();
          return null;
        }

    }

    public static int sendData(Connection conn, String query) {
      try {
        Statement stmt = conn.createStatement();
        int results = stmt.executeUpdate(query);
        return results;
      } catch(SQLException e) {
        e.printStackTrace();
        return -1;
      }

  }
    
  
  //CONTROLL VARIABLES
    int startWeek = -1;
    int endWeek = -1;
    String startDay = "Null";
    int sdayIndex = -1;
    String endDay = "Null";
    int edayIndex = -1;
    JTable dailyGrid;
    JTable mealSales;
    //storing prog id
    ArrayList<String> prodID = new ArrayList<String>();
    //ArrayList<String>   = new ArrayList<String>();
    //storing count per ordersize
    ArrayList<String> cpero = new ArrayList<String>();
    ArrayList<Float> gcpi = new ArrayList<Float>();
    ArrayList<Float> fill_level = new ArrayList<Float>();
    //selectedproduct for when making an order
    String selectedProd = "Null";
    Integer counttoOrder=0;
    Float priceofProd=(float)0.0;
    ArrayList<String> mealID = new ArrayList<String>();
    Float priceUpdate=(float)0.0;
    String selectedMeal="Null";
    String mealName = " ";
    String mealIDb = " ";
    Float mealPrice =(float)0.0;
    String tab = " ";

    //METHOD THAT GETS TOTAL REVENUE FROM ALL TIMES 
    public String totalRevenue(){
        ResultSet ordersCount = getData(conn, "SELECT COUNT(*) FROM revenue;");
        ResultSet revenue = getData(conn, "SELECT * FROM revenue;");

        Float total = (float) 0.0;
        try {
            int count = -1;
            if(ordersCount.next()) {
              count = Integer.parseInt(ordersCount.getString("count"));
            }
            if(count<0) {
              System.out.println("Error getting order count!");
              System.exit(-1);
            }
            
            for(int i=0;i<count;i++) {
                revenue.next();
                total += revenue.getFloat("revenue");
            }
        } catch(Exception r) {
            r.printStackTrace();
        }

        return "$"+total.toString();
    }
    //getting day num
    public int getDayNum(int week, String day){
        ResultSet orders = getData(conn, "SELECT * FROM orders WHERE week ="+week+" AND day ='"+day+"';");
        int x = -1;
        try {
            orders.next();
            x = Integer.parseInt(orders.getString("daynum"));
        } catch(Exception r) {
            r.printStackTrace();
        }
        return x;
    }

    //METHOD TO UPDATE DAILY SALES TABLE
    public JTable salesTable(Integer start, Integer end){

        //getting all menu items and names
        ResultSet menuCount = getData(conn, "SELECT COUNT(*) FROM menu;");
        ResultSet menu = getData(conn, "SELECT * FROM menu;");
        ArrayList<String> mealid = new ArrayList<String>();
        ArrayList<String> mealname = new ArrayList<String>();
        try {
            int count = -1;
            if(menuCount.next()) {
                count = Integer.parseInt(menuCount.getString("count"));
            }
            if(count<0) {
                System.out.println("Error getting inventory count!");
                System.exit(-1);
            }
            
            String temp = " ";
            for(int i=0;i<count;i++) {
                menu.next();
                if (!menu.getString("mealid").equals(temp)){
                    mealid.add(menu.getString("mealid"));
                    mealname.add(menu.getString("mealname"));
                    temp = menu.getString("mealid");
                }
            }
        } catch(Exception r) {
            r.printStackTrace();
        }
        String[][] data = new String[mealid.size()][3];
        for(int i=0; i<mealid.size();i++){
            data[i][0] = mealid.get(i);
            data[i][1] = mealname.get(i);
            data[i][2] = "0";
        }

        //reading day by day 
        for(int i = start; i <=end; i++){
            ResultSet ordersCounts = getData(conn, "SELECT COUNT(*) FROM orders WHERE daynum ="+i+";");
            ResultSet orders = getData(conn, "SELECT * FROM orders WHERE daynum ="+i+";");
            ArrayList<String> dmealid = new ArrayList<String>();
            ArrayList<Integer> countt = new ArrayList<Integer>();
            try {
                int count = -1;
                if(ordersCounts.next()) {
                    count = Integer.parseInt(ordersCounts.getString("count"));
                }
                if(count<0) {
                    System.out.println("Error getting inventory count!");
                    System.exit(-1);
                }
                
                String temp = " ";
                for(int j=0;j<count;j++) {
                    orders.next();
                    if (!orders.getString("mealid").equals(temp)){
                        dmealid.add(orders.getString("mealid"));
                        countt.add(Integer.parseInt(orders.getString("numsold")));
                        //System.out.println(Integer.parseInt(orders.getString("numsold")));
                        temp = menu.getString("mealid");
                    }
                }
            } catch(Exception r) {
                r.printStackTrace();
            }
            for(int x=0; x<dmealid.size();x++){
                for(int y=0; y<mealid.size();y++){
                    //System.out.println(dmealid.get(x)+ "!="+data[y][0] +data[y][0]);
                    if(dmealid.get(x).equals(data[y][0])){
                        //System.out.println(dmealid.get(x)+ "=="+data[y][0] );
                        int update = countt.get(x) + Integer.parseInt(data[y][2]);
                        data[y][2] = Integer.toString(update);
                    }
                }
            }
    
        }
        //for (int i=0; i<mealid.size();i++){
        Arrays.sort(data,(a,b)->Integer.compare(Integer.parseInt(b[2]),Integer.parseInt(a[2])));
        //Collections.reverse(Arrays.asList(data)); 
        
        
        String[] colNames = { "ID", "NAME", "COUNT"};
        mealSales = new JTable(data, colNames);
        mealSales.setBounds(30, 40, 200, 300);
        mealSales.setBackground(Color.white);

        //adjusting colum witdth
        TableColumnModel colmod2 = mealSales.getColumnModel();
        TableColumn perr2 = colmod2.getColumn(1);
        perr2.setPreferredWidth(130);

        return mealSales;
    }

    //MAKING SIMPLE STOCK TABLE METHOD
    //this also stores all productid in a global array list
    public JTable stockTable(){
        ResultSet invCount = getData(conn, "SELECT COUNT(*) FROM inventory;");
        ResultSet inv = getData(conn, "SELECT * FROM inventory;");
        ArrayList<String> productid = new ArrayList<String>();
        ArrayList<String> name = new ArrayList<String>();
        ArrayList<Integer> stock = new ArrayList<Integer>();
        ArrayList<String> measurment = new ArrayList<String>();
        ArrayList<Float> fill_level = new ArrayList<Float>();
        ArrayList<Float> items_needed = new ArrayList<Float>();

        try {
            int count = -1;
            if(invCount.next()) {
                count = Integer.parseInt(invCount.getString("count"));
            }
            if(count<0) {
                System.out.println("Error getting inventory count!");
                System.exit(-1);
            }
            
            for(int i=0;i<count;i++) {
                inv.next();
                productid.add(inv.getString("product_id"));
                prodID.add(inv.getString("product_id"));
                name.add(inv.getString("name"));
                stock.add(inv.getInt("stock"));
                measurment.add(inv.getString("stock_measurement"));
                gcpi.add(inv.getFloat("ordered_by_cost"));
                fill_level.add(inv.getFloat("fill_level"));
                Float need = inv.getFloat("fill_level") - inv.getInt("stock");
                if(need > 0){
                    items_needed.add(need);
                }
                else{
                    items_needed.add(0.0f);
                }
            }
        } catch(Exception r) {
            r.printStackTrace();
        }
        String[][] data = new String[productid.size()][6];
        for(int i=0; i<productid.size();i++){
            data[i][0] = productid.get(i);
            data[i][1] = name.get(i);
            data[i][2] = stock.get(i).toString();
            data[i][3] = measurment.get(i);
            data[i][4] = fill_level.get(i).toString();
            data[i][5] = items_needed.get(i).toString();
        }

        String[] colNames = { "ID", "Product", "Stock", "Measurement", "Fill Level", "Items Needed"};
        JTable stockTable = new JTable(data, colNames);
        return stockTable;
    }
    
    public JTable orderTable(){
        ResultSet invCount = getData(conn, "SELECT COUNT(*) FROM inventory;");
        ResultSet inv = getData(conn, "SELECT * FROM inventory;");
        //ResultSet invOrd = getData(conn, "SELECT * FROM invorders;");
        ArrayList<Integer> conv = new ArrayList<Integer>();
        ArrayList<String> measurment = new ArrayList<String>();
        ArrayList<String> per = new ArrayList<String>();
        //cpi taken from invorders table
        ArrayList<Float> cpi = new ArrayList<Float>();
        //GETTING CONVERSION FACTOr
        try {
            int count = -1;
            if(invCount.next()) {
                count = Integer.parseInt(invCount.getString("count"));
            }
            if(count<0) {
                System.out.println("Error getting inventory count!");
                System.exit(-1);
            }
            
            for(int i=0;i<count;i++) {
                inv.next();
                //invOrd.next();
                conv.add(inv.getInt("conversion_factor"));
                measurment.add(inv.getString("stock_measurement"));
                per.add(inv.getString("ordered_by"));
                cpi.add(inv.getFloat("ordered_by_cost"));
                //gcpi.add(invOrd.getFloat("cpi"));
            }
        } catch(Exception r) {
            r.printStackTrace();
        }

        String[][] data = new String[conv.size()][2];
        for(int i=0; i<conv.size();i++){
            data[i][0] = conv.get(i).toString() + " " +  measurment.get(i) + " per " + per.get(i);

            //updating cpero
            cpero.add(conv.get(i).toString() + " " +  measurment.get(i) + " per " + per.get(i));
            data[i][1] = "$" + cpi.get(i).toString();
        }

        String[] colNames = { "Ordered By", "Cost"};
        JTable orderTable = new JTable(data, colNames);
        return orderTable;
    }

    public JTable menuTable(){
        ResultSet menuCount = getData(conn, "SELECT COUNT(*) FROM menu;");
        ResultSet menu = getData(conn, "SELECT * FROM menu;");
        ArrayList<String> mealid = new ArrayList<String>();
        ArrayList<String> mealname = new ArrayList<String>();
        ArrayList<Float> price = new ArrayList<Float>();

        try {
            int count = -1;
            if(menuCount.next()) {
                count = Integer.parseInt(menuCount.getString("count"));
            }
            if(count<0) {
                System.out.println("Error getting inventory count!");
                System.exit(-1);
            }
            
            String temp = " ";
            for(int i=0;i<count;i++) {
                menu.next();
                if (!menu.getString("mealid").equals(temp)){
                    mealid.add(menu.getString("mealid"));
                    mealID.add(menu.getString("mealid"));
                    mealname.add(menu.getString("mealname"));
                    price.add(menu.getFloat("price"));
                    temp = menu.getString("mealid");
                }
            }
        } catch(Exception r) {
            r.printStackTrace();
        }

        String[][] data = new String[mealid.size()][3];
        for(int i=0; i<mealid.size();i++){
            data[i][0] = mealid.get(i);
            data[i][1] = mealname.get(i);
            data[i][2] = "$"+ price.get(i).toString();
        }

        String[] colNames = { "Meal ID", "Meal Name", "Price"};
        JTable menuTable = new JTable(data, colNames);
        return menuTable;
    }
   
    public JTable itemsinMeal(String mealID){
        ResultSet menuCount = getData(conn, "SELECT COUNT(*) FROM menu WHERE mealid='"+mealID+"';");
        ResultSet menu = getData(conn, "SELECT * FROM menu WHERE mealid='"+mealID+"';");
        ArrayList<String> productid = new ArrayList<String>();
        ArrayList<String> mealname = new ArrayList<String>();
        ArrayList<Integer> countt = new ArrayList<Integer>();
        ArrayList<Float> price = new ArrayList<Float>();

        try {
            int count = -1;
            if(menuCount.next()) {
                count = Integer.parseInt(menuCount.getString("count"));
            }
            if(count<0) {
                System.out.println("Error getting inventory count!");
                System.exit(-1);
            }
            for(int i=0;i<count;i++) {
                menu.next();
                    productid.add(menu.getString("productid"));
                    mealname.add(menu.getString("mealname"));
                    countt.add(menu.getInt("count"));
                    price.add(menu.getFloat("price"));
            }
        } catch(Exception r) {
            r.printStackTrace();
        }

        String[][] data = new String[productid.size()][2];
        mealName = mealname.get(0);
        mealPrice = price.get(0);
        for(int i=0; i<productid.size();i++){
            data[i][0] = productid.get(i);
            data[i][1] = countt.get(i).toString();
        }

        String[] colNames = { "Product", "Count"};
        JTable menuTable = new JTable(data, colNames);
        return menuTable;
    }
    //RE-READS DATA FROM DATABASE TO UPDATE ON GUI
    public JScrollPane updateMenu(){
        JPanel menuDisplay = new JPanel();
        menuDisplay.setBackground(Color.gray);

        JPanel displayMenu = new JPanel();
        displayMenu.setLayout(new BoxLayout(displayMenu, BoxLayout.Y_AXIS));
        displayMenu.setBackground(Color.orange);
        mealID.clear();
        JTable menuTable = menuTable();
        displayMenu.add(new JLabel("Menu:"));
        displayMenu.add(menuTable);
        displayMenu.setSize(500, 200);
        displayMenu.setBorder(BorderFactory.createLineBorder(Color.black));

        //adjusting colum witdth
        TableColumnModel colmod2 = menuTable.getColumnModel();
        TableColumn perr2 = colmod2.getColumn(1);
        perr2.setPreferredWidth(130);

        menuDisplay.add(displayMenu);
        JScrollPane scrollMenu = new JScrollPane(menuDisplay);
        return scrollMenu;
    }
    //RE-READS DATA FROM DATABASE TO UPDATE ON GUI
    public JScrollPane updateInv(){
        JPanel stockDisplay = new JPanel();
        stockDisplay.setBackground(Color.gray);

        /*
            display stock is a simple way display the current stock
            calls stockTable() method to create the table
        */
        JPanel displayStock = new JPanel();
        displayStock.setLayout(new BoxLayout(displayStock, BoxLayout.Y_AXIS));
        displayStock.setBackground(Color.blue);
        JTable stockTable = stockTable();
        displayStock.add(new JLabel("ProdID : Name : Stock : Msm : Fill Lvl : Amount to Order"));
        displayStock.add(stockTable);
        displayStock.setSize(500, 200);
        displayStock.setBorder(BorderFactory.createLineBorder(Color.black));

        TableColumnModel colmod = stockTable.getColumnModel();
        TableColumn perr = colmod.getColumn(1);
        perr.setPreferredWidth(120);
        JTable orderTable = orderTable();

        stockDisplay.add(displayStock);
        JScrollPane scroll = new JScrollPane(stockDisplay);
        return scroll;
    }

    public JPanel timeSelect(int x){
         /*
        Code for making drop down
        1. Reads all weeks with available data from sql table
        2. Puts into array
        3. Made into a drop down (wComboBox)
            Dropdown - button - text showing what week is selected
        */
        //PUTTING WEEKS INTO AN ARRAY FROM SQL
        ArrayList<String> weeksToChoose = new ArrayList<String>();
        //adding null week if none wanted
        weeksToChoose.add("Null                 "); //spaces for allignment

        ResultSet weeks = getData(conn, "SELECT DISTINCT week FROM orders order by week;");
        try {
            while(weeks.next()) {
                String toAdd = "Week "+weeks.getString("week");
                weeksToChoose.add(toAdd);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        //making drop down by weeks 
        JComboBox<String> wComboBox = new JComboBox<>(weeksToChoose.toArray(new String[0]));
        
         /*
        This chunk creates day drop down. 
        Does not need to read from sql because days of the week are given
        */
        //DAY DROP DOWN
        String[] daysToChoose = {"Null                 ", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        JComboBox<String> dComboBox = new JComboBox<>(daysToChoose);

        JPanel dayDropDown = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dayDropDown.add(dComboBox);

        JPanel time = new JPanel(new FlowLayout(FlowLayout.LEFT));
        time.add(wComboBox);
        time.add(dComboBox);

        wComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (x == 1){
                    //UPDATING SELECTED WEEK VALUE
                    String sel = wComboBox.getItemAt(wComboBox.getSelectedIndex());
                    String weeknum= sel.substring(5);
                    startWeek = Integer.parseInt(weeknum);
                    if(startWeek > endWeek){
                        endWeek = -1;
                    }
                    //System.out.println("start: " +startWeek);
                }else{
                    String sel = wComboBox.getItemAt(wComboBox.getSelectedIndex());
                    String weeknum= sel.substring(5);
                    if(startWeek > Integer.parseInt(weeknum)){
                        wComboBox.setSelectedIndex(0);
                    }else{
                        endWeek = Integer.parseInt(weeknum);
                        //System.out.println("end: " +endWeek);
                        if(startWeek == endWeek){
                            if (sdayIndex > edayIndex){
                                dComboBox.setSelectedIndex(0);
                            }
                        }
                    }
                }
            }

        });

        dComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (x == 1){
                    //UPDATING SELECTED WEEK VALUE
                    String sel = dComboBox.getItemAt(dComboBox.getSelectedIndex());
                    sdayIndex = dComboBox.getSelectedIndex();
                    startDay = sel;
                    if(startWeek == endWeek){
                        if (startWeek != -1){
                            if(!endDay.equals("Null")){
                                if (sdayIndex > edayIndex){
                                    endDay = "Null";
                                    edayIndex = -1;
                                }
                            }
                        }
                    }
                    //System.out.println("startDay: " +startDay);
                    //System.out.println("startind: " +sdayIndex);
                }else{
                    String sel = dComboBox.getItemAt(dComboBox.getSelectedIndex());
                    edayIndex = dComboBox.getSelectedIndex();
                    endDay = sel;
                    if(startWeek == endWeek){
                        if (startWeek != -1){
                            if (sdayIndex > edayIndex){
                                endDay = "Null";
                                edayIndex = -1;
                                dComboBox.setSelectedIndex(0);
                            }
                        }
                    }
                    //System.out.println("endDay: " +endDay);
                    //System.out.println("endind: " +edayIndex);
                }
            }

        });

        return time;
    }

    public JPanel salesData(){
        JPanel salesData = new JPanel();
        salesData.setLayout(new BoxLayout(salesData, BoxLayout.Y_AXIS));
        salesData.setBackground(Color.gray);
        if(startWeek != -1 && endWeek != -1 && !startDay.equals("Null") && !endDay.equals("Null")){
            JPanel mmsg = new JPanel();
            mmsg.setBackground(Color.gray);
            JLabel msg = new JLabel("Week "+startWeek + " ["+startDay+"] -------> Week "+endWeek + " ["+endDay+"]", SwingConstants.CENTER);
            msg.setForeground(Color.black);
            mmsg.add(msg);
            mmsg.setMaximumSize(mmsg.getPreferredSize());

            int start = getDayNum(startWeek, startDay);
            int end = getDayNum(endWeek, endDay);
            int days = (end - start)+1;
            JPanel mmsg2 = new JPanel();
            mmsg2.setBackground(Color.gray);
            JLabel msg2 = new JLabel("["+days + " days]", SwingConstants.CENTER);
            msg2.setForeground(Color.white);
            mmsg2.add(msg2);
            mmsg2.setMaximumSize(mmsg2.getPreferredSize());

            JPanel displayTable = new JPanel();
            displayTable.setLayout(new BoxLayout(displayTable, BoxLayout.Y_AXIS));
            displayTable.setBackground(Color.yellow);
            JTable counTable = salesTable(start,end);
            displayTable.add(new JLabel("MEALID:MEALNAME:#SOLD"));
            displayTable.add(counTable);
            displayTable.setSize(500, 200);
            displayTable.setBorder(BorderFactory.createLineBorder(Color.black));
            

            salesData.add(mmsg);
            salesData.add(mmsg2);
            salesData.add(displayTable);
        }else{
            JPanel mmsg = new JPanel();
            mmsg.setBackground(Color.gray);
            JLabel msg = new JLabel("| ERROR: Make sure that either all times are selected or that Start time comes before end time |");
            msg.setForeground(Color.BLACK);
            msg.setFont(new Font("Calibri", Font.BOLD, 15));
            mmsg.add(msg);
            salesData.add(mmsg);
        }
        return salesData;
    }

    public ManagerGUI(Connection conn, int width, int height) {
        super("Manager Interface");
        this.conn = conn;
        setSize(1000, height);
        setLayout(new BorderLayout());
        
        //HOME PANEL
        JPanel panel1 = new JPanel(new BorderLayout());


        /*  
        contains TOTAL REVENURE AT TOP 
            timePanel also contains two drop downs
             1. wComboBox (week select)
             2. dComboBox (day select) *REQUIRES WEEK TO BE SELECTED*
        */
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));

        //TOTAL REVANUE
        String total = totalRevenue();
        JPanel totalRev = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel totals = new JLabel("Total Rev: " + total);
        totalRev.add(totals);
        totalRev.setBackground(Color.white);
        timePanel.add(totalRev);

       

        //timePanel.add(weekDropDown);
        

       
        JPanel startTime = timeSelect(1);
        startTime.add(new JLabel("[Start Time]"));
        JPanel endTime = timeSelect(2);
        endTime.add(new JLabel("[End Time]"));
        JPanel sTime = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectTime =new JButton("SET TIME");
        selectTime.setForeground(Color.RED);
        selectTime.setPreferredSize(new Dimension(206, 18));
        sTime.add(selectTime);

        //adding to time panel
        timePanel.add(startTime);
        timePanel.add(endTime);
        timePanel.add(sTime);
        //day dropdown creation done----------------------------------------------------------------


        /*
            Sales Data showcases the data 
        */
        JPanel salesData = new JPanel();
        salesData.setLayout(new BoxLayout(salesData, BoxLayout.Y_AXIS));
        salesData.setBackground(Color.gray);

        /*
            Invdata for inventory usage chart
        */
        JPanel invData = new JPanel();
        invData.setLayout(new BoxLayout(invData, BoxLayout.Y_AXIS));
        invData.setBackground(Color.gray);

        JPanel mmsg = new JPanel();
        mmsg.setBackground(Color.gray);
        JLabel msg = new JLabel("| Select start and end times |");
        msg.setForeground(Color.BLACK);
        msg.setFont(new Font("Calibri", Font.BOLD, 15));
        mmsg.add(msg);
        salesData.add(mmsg);
        JPanel mmsg2 = new JPanel();
        mmsg2.setBackground(Color.gray);
        JLabel msg2 = new JLabel("| Select start and end times |");
        msg2.setForeground(Color.BLACK);
        msg2.setFont(new Font("Calibri", Font.BOLD, 15));
        mmsg2.add(msg2);
        invData.add(mmsg2);

        selectTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salesData.removeAll();
                salesData.repaint();
                salesData.add(salesData());
                salesData.repaint();
                
                invData.removeAll();
                invData.repaint();
                InventoryUsage invPanel = new InventoryUsage(conn, startWeek, endWeek, startDay, endDay);
                try {
                    invPanel.refreshChart();
                } catch(SQLException ex) {ex.printStackTrace();}
                invData.add(invPanel);
                invData.repaint();

                if(tab.equals("s")){
                    panel1.removeAll();
                    panel1.repaint();
                    panel1.add(timePanel, BorderLayout.NORTH);
                    panel1.add(salesData, BorderLayout.CENTER);
                    panel1.revalidate();
                }else if(tab.equals("i")){
                    panel1.removeAll();
                    panel1.repaint();
                    panel1.add(timePanel, BorderLayout.NORTH);
                    //add(charts, BorderLayout.EAST);
                    panel1.add(invData, BorderLayout.CENTER);
                    panel1.revalidate();
                }

            }
        });


        //STOCK DATA----------------------
        JScrollPane inventory = updateInv();
        

        //ORDER ------------------------------------------------
        
        /*
            Create orders will contain drop downs to make orders
        */
        JPanel createOrder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createOrder.setLayout(new BoxLayout(createOrder, BoxLayout.Y_AXIS));
        createOrder.setBackground(Color.red);
        createOrder.add(new JLabel("Place Order:"));

        //PRODUCT SELECT DROP DOWN
        JPanel productSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> prodComboBox = new JComboBox<>(prodID.toArray(new String[0]));
        JButton prodButton = new JButton("Select");
        productSelect.add(prodComboBox);
        productSelect.add(prodButton);
        productSelect.setMaximumSize(productSelect.getPreferredSize());
        productSelect.setBackground(Color.red);

        JPanel countSelect = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField intField = new JTextField(12);
        countSelect.add(intField);
        countSelect.setBackground(Color.red);
        countSelect.setMaximumSize(productSelect.getPreferredSize());
        
        JPanel text = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel countpero = new JLabel("[Select a product]");
        text.add(countpero);
        text.setBackground(Color.red);
        text.setMaximumSize(text.getPreferredSize());

        JPanel tcostDisp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel cost = new JLabel("Total Cost: $X.XX");
        tcostDisp.add(cost);
        tcostDisp.setBackground(Color.red);
        tcostDisp.setMaximumSize(tcostDisp.getPreferredSize());

        //buy button
        JPanel buy = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton buyButton = new JButton("Buy");
        buyButton.setBackground(Color.red);
        buy.add(buyButton);

        //WHEN INPUT IN TEXT BOX, CALCULATE TOTAL COST
        intField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                counttoOrder=Integer.parseInt(intField.getText());
                Float totalCost= counttoOrder * priceofProd;
                cost.setText("Total Cost: $" + totalCost.toString());
            }
        });
        

        //WHEN PROD BUTTON CLICKED, STORE SELECTEDPRODUCT & output CPERO
        prodButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedProd = prodComboBox.getItemAt(prodComboBox.getSelectedIndex());
                for(int i = 0; i < cpero.size(); i++){
                    if(prodID.get(i).equals(selectedProd)){
                        countpero.setText("["+cpero.get(i)+"]");
                        priceofProd = gcpi.get(i);
                        break;
                    }
                }
            }
        });

        buyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedProd = prodComboBox.getItemAt(prodComboBox.getSelectedIndex());
                counttoOrder=Integer.parseInt(intField.getText());
                ResultSet conv = getData(conn, "SELECT * FROM inventory WHERE product_id = '"+selectedProd+"';");
                int cfact = 0;
                int curr = 0;
                try{
                    conv.next();
                    cfact = conv.getInt("conversion_factor");   
                    curr = conv.getInt("stock");
                } catch(Exception r) {
                    r.printStackTrace();
                }
                int updateStock = (counttoOrder * cfact)+curr;
                sendData(conn, "UPDATE inventory SET stock = "+ updateStock +" WHERE product_id = '"+selectedProd+"';");
                panel1.remove(inventory);
                panel1.revalidate();
                JScrollPane inventory = updateInv();
                panel1.add(inventory,BorderLayout.CENTER);
                panel1.revalidate();
                
            }
        });

        createOrder.add(productSelect);
        createOrder.add(countSelect);
        createOrder.add(text);
        createOrder.add(tcostDisp);
        createOrder.add(buy);

        //MANUAL INVENTORY UPDATE
        JPanel updateInv = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updateInv.setLayout(new BoxLayout(updateInv, BoxLayout.Y_AXIS));
        updateInv.setBackground(Color.LIGHT_GRAY);
        updateInv.add(new JLabel("Manual Update"));
        
        //FILL LEVEL
        JPanel fillField = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField fillTextField = new JTextField(6);
        JLabel fillLabel = new JLabel("[Fill Level]");
        fillField.add(fillTextField);
        fillField.setBackground(Color.LIGHT_GRAY);
        fillField.add(fillLabel);

        //ID FEILD
        JPanel prodidSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField prodidField = new JTextField(6);
        JLabel prodidPromt = new JLabel("[ProdID]");
        prodidSelect.add(prodidField);
        prodidSelect.setBackground(Color.LIGHT_GRAY);
        prodidSelect.add(prodidPromt);
        //idSelect.setMaximumSize(idSelect.getPreferredSize());

        //NAME FEILD
        JPanel stockSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField stockField = new JTextField(6);
        JLabel stockPromt = new JLabel("[Count]");
        //nameField.setMaximumSize(nameField.getPreferredSize());
        stockSelect.add(stockField);
        stockSelect.setBackground(Color.LIGHT_GRAY);
        stockSelect.add(stockPromt);

        JButton stockButton = new JButton("Update");
        JButton restockButton = new JButton("Restock All");

        //UPDATE PRICE
        stockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String selProd = prodidField.getText();
                if(!(stockField.getText().equals(""))){
                    Float newStock=Float.parseFloat(stockField.getText());
                    stockField.setText("");
                    sendData(conn, "UPDATE inventory SET stock = "+ newStock +" WHERE product_id = '"+selProd+"';");
                }
                if(!(fillTextField.getText().equals(""))){
                    Float newFillLevel=Float.parseFloat(fillTextField.getText());
                    fillTextField.setText("");
                    sendData(conn, "UPDATE inventory SET fill_level = "+ newFillLevel +" WHERE product_id = '"+selProd+"';");
                }
                panel1.remove(inventory);
                panel1.revalidate();
                JScrollPane inventory = updateInv();
                panel1.add(inventory,BorderLayout.CENTER);
                panel1.revalidate();

            }
        });

        
        updateInv.add(prodidSelect);
        updateInv.add(stockSelect);
        updateInv.add(fillField);
        updateInv.add(stockButton);
        updateInv.add(restockButton);

        //add meal item
        JPanel newProd = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newProd.setLayout(new BoxLayout(newProd, BoxLayout.Y_AXIS));
        newProd.setBackground(Color.LIGHT_GRAY);
        newProd.add(new JLabel("New Item"));

        //ID FEILD
        JPanel produidSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField produidField = new JTextField(6);
        JLabel produidPromt = new JLabel("[ProdID]");
        produidSelect.add(produidField);
        produidSelect.setBackground(Color.LIGHT_GRAY);
        produidSelect.add(produidPromt);
        //idSelect.setMaximumSize(idSelect.getPreferredSize());

        //NAME FEILD
        JPanel prodnameSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField prodnameField = new JTextField(6);
        JLabel prodnamePromt = new JLabel("[Name]");
        //nameField.setMaximumSize(nameField.getPreferredSize());
        prodnameSelect.add(prodnameField);
        prodnameSelect.setBackground(Color.LIGHT_GRAY);
        prodnameSelect.add(prodnamePromt);

        //PRICE FEILD
        JPanel prodsSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField prodsField = new JTextField(6);
        JLabel prodsPromt = new JLabel("[Stock]");
        prodsSelect.add(prodsField);
        prodsSelect.setBackground(Color.LIGHT_GRAY);
        prodsSelect.add(prodsPromt);

        JPanel mesSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField mesField = new JTextField(6);
        JLabel mesPromt = new JLabel("[Measure]");
        mesSelect.add(mesField);
        mesSelect.setBackground(Color.LIGHT_GRAY);
        mesSelect.add(mesPromt);

        //priceSelect.setMaximumSize(priceSelect.getPreferredSize());
        JButton addProdButton =new JButton(" Add ");
        JButton delProdButton =new JButton(" Delete ");

        restockButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                for(int i = 0; i < prodID.size(); i++){
                    String currItem = prodID.get(i); 
                    ResultSet currStock = getData(conn, "SELECT stock FROM inventory WHERE product_id=\'"+currItem+"\';");
                    ResultSet currFill = getData(conn, "SELECT fill_level FROM inventory WHERE product_id=\'"+currItem+"\';");
                    float g = 0;
                    try{
                        currStock.next();
                        currFill.next();
                        Float stockFloat = Float.parseFloat(currStock.getString("stock"));
                        Float fillFloat = Float.parseFloat(currFill.getString("fill_level"));
                        if (stockFloat < fillFloat){
                            sendData(conn, "UPDATE inventory SET stock = "+ fillFloat +" WHERE product_id = \'"+currItem+"\';");
                        }
                        System.out.println(fillFloat);
                        
                    } catch(Exception r) {
                        r.printStackTrace();
                    }

                    
                }
                panel1.remove(inventory);
                panel1.revalidate();
                JScrollPane inventory = updateInv();
                panel1.add(inventory,BorderLayout.CENTER);
                panel1.revalidate();
                
            }
        });

        addProdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String idF=produidField.getText();
                String nameF=prodnameField.getText();
                Float priceF=Float.parseFloat(prodsField.getText());
                String mesF=mesField.getText();
                produidField.setText("");
                prodnameField.setText("");
                prodsField.setText("");
                mesField.setText("");
                sendData(conn, "INSERT INTO inventory(product_id,name,stock,stock_measurement) VALUES('"+idF+"','"+nameF+"',"+priceF+", '"+mesF+"');");
                panel1.remove(inventory);
                panel1.revalidate();
                JScrollPane inv = updateInv();
                panel1.add(inv,BorderLayout.CENTER);
                panel1.revalidate();

            }
        });
        delProdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String idF=produidField.getText();
                produidField.setText("");
                sendData(conn, "DELETE FROM inventory WHERE product_id='"+idF+"';");
                panel1.remove(inventory);
                panel1.revalidate();
                JScrollPane inv = updateInv();
                panel1.add(inv,BorderLayout.CENTER);
                panel1.revalidate();

            }
        });

        newProd.add(produidSelect);
        newProd.add(prodnameSelect);
        newProd.add(prodsSelect);
        newProd.add(mesSelect);
        newProd.add(addProdButton);
        newProd.add(delProdButton);
        


        //MENU DATA----------------------
        JScrollPane menu = updateMenu();

        JPanel menuEdits = new JPanel();
        
        JButton adjPrice = new JButton("Adjust Price");
        //UPDATE PRICE
        adjPrice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JFrame jFrame = new JFrame();
                jFrame.setTitle("Price Updator");

 
                JPanel udatePrice = new JPanel();
        
                JPanel mealSelect = new JPanel();
                JTextField mealField = new JTextField(6);
                mealSelect.add(mealField);
                JLabel mealPromt = new JLabel("[MealID]");
                mealSelect.add(mealPromt);

                mealSelect.setBounds(100, 50, 130, 20);
        
                JPanel pricetSelect = new JPanel();
                JTextField floatField = new JTextField(6);
                pricetSelect.add(floatField);
                JLabel priceUPromt = new JLabel("[Price]");
                pricetSelect.add(priceUPromt);

                pricetSelect.setBounds(100, 100, 130, 20);
        
                JButton mealButton = new JButton("Update");
                mealButton.setBounds(100, 150, 130, 20);


                udatePrice.add(mealButton);
                udatePrice.add(mealSelect);
                udatePrice.add(pricetSelect);
                udatePrice.setLayout(null);
                udatePrice.setSize(350, 250);
                
                
                jFrame.add(udatePrice,BorderLayout.CENTER);
                
                jFrame.setLayout(null);
                jFrame.setSize(350, 250);
                jFrame.setVisible(true);
                
                    
                //UPDATE PRICE
                mealButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        priceUpdate=Float.parseFloat(floatField.getText());
                        floatField.setText("");
                        selectedMeal = mealField.getText();
                        sendData(conn, "UPDATE menu SET price = "+ priceUpdate +" WHERE mealid = '"+selectedMeal+"';");

                        panel1.remove(menu);
                        panel1.revalidate();
                        JScrollPane menu = updateMenu();
                        panel1.add(menu,BorderLayout.CENTER);
                        panel1.revalidate();
                    }
                });
            }
        });
        
        JButton adddelMeal = new JButton("Add/Remove");
        //ADD / DELETE MEAL
        adddelMeal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JFrame jFrame = new JFrame();
                jFrame.setTitle("Add/Delete Meal");

 
                //add meal item
                JPanel newMeal = new JPanel();
                
                //ID FEILD
                JPanel idSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField idField = new JTextField(6);
                JLabel idPromt = new JLabel("[MealID]");
                idSelect.add(idField);
                idSelect.add(idPromt);
                idSelect.setBounds(100, 50, 130, 22);
                //idSelect.setMaximumSize(idSelect.getPreferredSize());

                //NAME FEILD
                JPanel nameSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField nameField = new JTextField(6);
                JLabel namePromt = new JLabel("[Name]");
                //nameField.setMaximumSize(nameField.getPreferredSize());
                nameSelect.add(nameField);
                nameSelect.add(namePromt);
                nameSelect.setBounds(100, 83, 130, 22);

                //PRICE FEILD
                JPanel priceSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField priceField = new JTextField(6);
                JLabel pricePromt = new JLabel("[Price]");
                priceSelect.add(priceField);
                priceSelect.add(pricePromt);
                priceSelect.setBounds(100, 116, 130, 22);

                //priceSelect.setMaximumSize(priceSelect.getPreferredSize());
                JButton addMealButton =new JButton(" Add ");
                addMealButton.setBounds(50, 150, 100, 20);

                JButton delMealButton =new JButton("Remove");
                delMealButton.setBounds(150, 150, 100, 20);

                newMeal.add(idSelect);
                newMeal.add(nameSelect);
                newMeal.add(priceSelect);
                newMeal.add(addMealButton);
                newMeal.add(delMealButton);
                newMeal.setLayout(null);
                newMeal.setSize(350, 250);
                
                
                jFrame.add(newMeal,BorderLayout.CENTER);
                
                jFrame.setLayout(null);
                jFrame.setSize(350, 250);
                jFrame.setVisible(true);
                
                addMealButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        String idF=idField.getText();
                        String nameF=nameField.getText();
                        Float priceF=Float.parseFloat(priceField.getText());
                        idField.setText("");
                        nameField.setText("");
                        priceField.setText("");
                        sendData(conn, "INSERT INTO menu(mealid,productid,mealname,price) VALUES('"+idF+"','','"+nameF+"',"+priceF+");");
                        panel1.remove(menu);
                        panel1.revalidate();
                        JScrollPane menu = updateMenu();
                        panel1.add(menu,BorderLayout.CENTER);
                        panel1.revalidate();

                    }
                });
                delMealButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        String idF=idField.getText();
                        idField.setText("");
                        nameField.setText("");
                        priceField.setText("");
                        sendData(conn, "DELETE FROM menu WHERE mealid='"+idF+"';");
                        panel1.remove(menu);
                        panel1.revalidate();
                        JScrollPane menu = updateMenu();
                        panel1.add(menu,BorderLayout.CENTER);
                        panel1.revalidate();
                    }
                });
                
            }
        });

        JButton editMeal= new JButton("Edit Meal");
        //EDIT MEAL BUTTON (ADD PRODUCTS)
        editMeal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JFrame jFrame = new JFrame();
                jFrame.setTitle("Edit Meal");

 
                //add meal item
                JPanel mealSelect = new JPanel();
                
                //ID FEILD
                JPanel idSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField idField = new JTextField(6);
                JLabel idPromt = new JLabel("[MealID]");
                idSelect.add(idField);
                idSelect.add(idPromt);
                idSelect.setBounds(100, 50, 130, 22);
               
                JButton selectMeal =new JButton("Select");
                selectMeal.setBounds(100, 150, 130, 20);

                mealSelect.add(idSelect);
                mealSelect.add(selectMeal);
                mealSelect.setLayout(null);
                mealSelect.setSize(350, 250);
                
                
                jFrame.add(mealSelect,BorderLayout.CENTER);
                
                jFrame.setLayout(null);
                jFrame.setSize(350, 250);
                jFrame.setVisible(true);
                
                selectMeal.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        String idF=idField.getText();
                        mealIDb=idField.getText();

                        JPanel mealDetails = new JPanel();

                        //ID FEILD
                        JPanel idSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JTextField idField2 = new JTextField(6);
                        JLabel idPromt = new JLabel("[Product]");
                        idSelect.add(idField2);
                        idSelect.add(idPromt);
                        idSelect.setBounds(100, 50, 140, 24);
                        //idSelect.setMaximumSize(idSelect.getPreferredSize());

                        //NAME FEILD
                        JPanel countSelect = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JTextField countField = new JTextField(6);
                        JLabel countPromt = new JLabel("[Count]");
                        //nameField.setMaximumSize(nameField.getPreferredSize());
                        countSelect.add(countField);
                        countSelect.add(countPromt);
                        countSelect.setBounds(100, 74, 140, 24);
                        
                        JButton addProdButton =new JButton(" Add ");
                        addProdButton.setBounds(50, 104, 100, 20);

                        JButton delProdButton =new JButton("Remove");
                        delProdButton.setBounds(150, 104, 100, 20);


                        JTable itemsinMeal= itemsinMeal(idF);
                        itemsinMeal.setBounds(50, 128, 200, 200);

                        mealDetails.add(idSelect);
                        mealDetails.add(countSelect);
                        mealDetails.add(addProdButton);
                        mealDetails.add(delProdButton);
                        
                        mealDetails.setLayout(null);
                        mealDetails.setSize(350, 124);

                        jFrame.remove(mealSelect);
                        jFrame.setTitle(mealName);
                        jFrame.revalidate();
                        jFrame.add(mealDetails);
                        jFrame.add(itemsinMeal);
                        jFrame.setSize(350, 400);
                        jFrame.revalidate();

                        addProdButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                String idF=idField2.getText();
                                Integer count=Integer.parseInt(countField.getText());
                                idField2.setText("");
                                countField.setText("");
                                sendData(conn, "INSERT INTO menu(mealid,productid,mealname,price,count) VALUES('"+mealIDb+"','"+idF+"','"+mealName+"',"+mealPrice+", "+count+");");
                                jFrame.remove(itemsinMeal);
                                jFrame.revalidate();
                                JTable itemsinMeal= itemsinMeal(mealIDb);
                                itemsinMeal.setBounds(50, 128, 200, 200);
                                jFrame.add(itemsinMeal);
                                jFrame.setSize(350, 400);
                                jFrame.revalidate();
                            }
                        });
                        delProdButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                String idF=idField2.getText();
                                idField2.setText("");
                                countField.setText("");
                                sendData(conn, "DELETE FROM menu WHERE mealid='"+mealIDb+"' AND productid='"+idF+"';");
                                jFrame.remove(itemsinMeal);
                                jFrame.revalidate();
                                JTable itemsinMeal= itemsinMeal(mealIDb);
                                itemsinMeal.setBounds(50, 128, 200, 200);
                                jFrame.add(itemsinMeal);
                                jFrame.setSize(350, 400);
                                jFrame.revalidate();
                            }
                        });
                    }
                });
                
            }
        });


        menuEdits.add(adjPrice);
        menuEdits.add(adddelMeal);
        menuEdits.add(editMeal);



        //SELECT WHAT YOU WANT TO DISPLAY 
        JPanel charts = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton stockChart = new JButton("Inventory");
        JButton salesChart = new JButton("Sales Data");
        JButton menuChart = new JButton("Menu");
        JButton invChart = new JButton("Stock Usage");

        charts.add(stockChart);
        charts.add(menuChart);
        charts.add(salesChart);
        charts.add(invChart);
        timePanel.add(charts);

        

        stockChart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tab = " ";
                panel1.removeAll();
                panel1.repaint();
                panel1.add(timePanel, BorderLayout.NORTH);
                //add(charts, BorderLayout.EAST);
                panel1.add(inventory,BorderLayout.CENTER);
                panel1.add(updateInv,BorderLayout.WEST);
                panel1.add(newProd,BorderLayout.EAST);
                panel1.add(createOrder, BorderLayout.SOUTH);
                panel1.revalidate();
            }
        });

        salesChart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tab = "s";
                panel1.removeAll();
                panel1.repaint();
                panel1.add(timePanel, BorderLayout.NORTH);
                //add(charts, BorderLayout.EAST);
                panel1.add(salesData, BorderLayout.CENTER);
                panel1.revalidate();
            }
        });

        menuChart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tab = " ";
                panel1.removeAll();
                panel1.repaint();
                panel1.add(timePanel, BorderLayout.NORTH);
                //add(charts, BorderLayout.EAST);
                panel1.add(menu,BorderLayout.CENTER);
                panel1.add(menuEdits, BorderLayout.SOUTH);
                panel1.revalidate();
            }
        });

        invChart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tab = "i";
                panel1.removeAll();
                panel1.repaint();
                panel1.add(timePanel, BorderLayout.NORTH);
                //add(charts, BorderLayout.EAST);
                //JPanel inventoryPanel = new InventoryUsage(conn, startWeek, endWeek, startDay, endDay);
                panel1.add(invData, BorderLayout.CENTER);
                panel1.revalidate();
            }
        });

        JPanel startUp = new JPanel();
        startUp.setBackground(Color.gray);
        JLabel none = new JLabel("[No Data Selected]");
        startUp.add(none);

        panel1.add(timePanel, BorderLayout.NORTH);
        panel1.add(startUp,BorderLayout.CENTER);

        JTabbedPane mainUI = new JTabbedPane();

        mainUI.addTab("Home", panel1);

        add(mainUI);
    

        // Mark one more active window when this one closes, and synchronize to avoid race condition
        GUI.mutex.lock();
        GUI.activeWindows+=1;
        GUI.mutex.unlock();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                //Close connection when window closes
                try {
                conn.close();
                } catch(Exception e) {
                JOptionPane.showMessageDialog(null,"Connection NOT Closed.");
                }

                // Mark one less active window when this one closes, and synchronize to avoid race condition
                GUI.mutex.lock();
                GUI.activeWindows-=1;
                GUI.mutex.unlock();
            }
        });
        setVisible(true);
    }


    // if button is pressed
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
    }
}