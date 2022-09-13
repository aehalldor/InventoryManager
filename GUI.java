import java.sql.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.*;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.time.LocalDate;
import static java.lang.System.out;

public class GUI extends JFrame implements ActionListener {

    public static final String db = "jdbc:postgresql://csce-315-db.engr.tamu.edu/csce315904_22db", user=
    "csce315904_22user", password = "droptable";
    public static final int SERVER_WIDTH=1200, SERVER_HEIGHT=800;
    public static final int MANAGER_WIDTH=1200, MANAGER_HEIGHT=800;
    private Connection conn;

    public static int activeWindows = 0;
    public static ReentrantLock mutex;

    private JPanel cartPanel;
    DefaultListModel<String> listModel;
    private JList<String> itemsJList;
    private ArrayList<String> cartIDList;

    JButton delete;
    JButton complete;
    JLabel priceLabel;
    float price;
    int week;
    String day;


    private ArrayList<String> mealIDs;
    private HashMap<String, Float> menuItemPrices;

    private void updateInventory() {
      for (String ID : cartIDList) { 
        ResultSet mealsCount = getData(conn, "SELECT COUNT(*) FROM menu WHERE mealid = '"+ID+"';");
        ResultSet meal = getData(conn, "SELECT * FROM menu WHERE mealid = '"+ID+"';");
        try {
            int count = -1;
            if(mealsCount.next()) {
              count = Integer.parseInt(mealsCount.getString("count"));
            }
            if(count<0) {
              System.out.println("Error getting order count!");
              System.exit(-1);
            }       
            for(int i=0;i<count;i++) {
                meal.next();

                Float convf = (float) 0.0;
                Float stock =(float) 0.0;
                ResultSet product = getData(conn, "SELECT * FROM inventory WHERE product_id = '"+meal.getString("productid")+"';");
                try{
                  product.next();
                  convf = Float.parseFloat(product.getString("order_convert"));
                  stock = Float.parseFloat(product.getString("stock"));
                }catch(Exception e) {
                  e.printStackTrace();
                }
                int prodCount = Integer.parseInt(meal.getString("count"));
                Float convCount= prodCount*convf;
                Float updatedStock = stock - convCount;
                sendData(conn, "UPDATE inventory SET stock = "+ updatedStock +" WHERE product_id = '"+meal.getString("productid")+"';");  
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
      }
      
    }

    private void updateOrders() throws SQLException{
      Statement stmt = conn.createStatement();
      HashMap<String, Integer> counts = new HashMap<String, Integer>();
      for(int i=0;i<mealIDs.size();i++) {
        counts.put(mealIDs.get(i), 0);
      }
      for(int i=0;i<cartIDList.size();i++) {
        String curID = cartIDList.get(i);
        counts.put(curID, counts.get(curID) + 1);
      }
      for(String mealID: counts.keySet()) {
        //String mealID = cartIDList.get(i);
        int cartCount = counts.get(mealID);
        if(cartCount==0)
          continue;
        String countQuery = String.format("SELECT COUNT(*) FROM orders WHERE mealid=\'%s\' AND week=%d and day=\'%s\'", mealID, week, day);
        ResultSet countResults = stmt.executeQuery(countQuery);
        countResults.next();
        int count = Integer.parseInt(countResults.getString("count"));
        String query = "";
        String rawString = "";
        if(count==0) {
          rawString = "INSERT INTO %s(week, day, mealid, numsold) VALUES(%d, \'%s\', \'%s\', %d);";
          query = String.format(rawString, "orders", week, day, mealID, counts.get(mealID));
        } else {
          String getNumSold = String.format("SELECT numsold FROM orders WHERE mealid=\'%s\' and week=%d and day=\'%s\';", mealID, week, day);
          ResultSet numSoldSet = stmt.executeQuery(getNumSold);
          numSoldSet.next();
          int numSold = Integer.parseInt(numSoldSet.getString("numsold"));
          //out.printf("%s, %d, %d, %d\n", mealID, count, numSold, cartCount);
          rawString = "UPDATE %s SET numsold=%d where mealid=\'%s\' and week=%d and day=\'%s\'";
          query = String.format(rawString, "orders", numSold+cartCount, mealID, week, day);
        }
        stmt.executeUpdate(query);
        updateRevenue2(rawString);
      }
    }

    private void updateRevenue() throws SQLException{
      Statement stmt = conn.createStatement();
      HashMap<String, Integer> counts = new HashMap<String, Integer>();
      for(int i=0;i<mealIDs.size();i++) {
        counts.put(mealIDs.get(i), 0);
      }
      for(int i=0;i<cartIDList.size();i++) {
        String curID = cartIDList.get(i);
        counts.put(curID, counts.get(curID) + 1);
      }
      for(String mealID: counts.keySet()) {
        //String mealID = cartIDList.get(i);
        int cartCount = counts.get(mealID);
        if(cartCount==0)
          continue;
        String countQuery = String.format("SELECT COUNT(*) FROM revenue WHERE mealid=\'%s\' AND week=%d and day=\'%s\'", mealID, week, day);
        ResultSet countResults = stmt.executeQuery(countQuery);
        countResults.next();
        int count = Integer.parseInt(countResults.getString("count"));
        String query = "";

        
        String price = String.format("SELECT * FROM menu WHERE mealid=\'%s\' ", mealID);
        ResultSet pricee = stmt.executeQuery(price);
        pricee.next();
        Float p = Float.parseFloat(pricee.getString("price"));
        Float total = counts.get(mealID)*p;
        if(count==0) {
          query = String.format("INSERT INTO revenue(week, day, mealid, count, revenue) VALUES(%d, \'%s\', \'%s\', %d, %f);", week, day, mealID, counts.get(mealID),total);
        } else {
          String getNumSold = String.format("SELECT * FROM revenue WHERE mealid=\'%s\' and week=%d and day=\'%s\';", mealID, week, day);
          ResultSet numSoldSet = stmt.executeQuery(getNumSold);
          numSoldSet.next();
          int numSold = Integer.parseInt(numSoldSet.getString("count"));
          Float revCurr = Float.parseFloat(numSoldSet.getString("revenue"));
          //out.printf("%s, %d, %d, %d\n", mealID, count, numSold, cartCount);
          query = String.format("UPDATE revenue SET count=%d, revenue =%f WHERE mealid=\'%s\' and week=%d and day=\'%s\'", numSold+cartCount, total+revCurr, mealID, week, day);
        }
        stmt.executeUpdate(query);
      }
    }

    public void updateRevenue2(String rawQuery) {

    }

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

    public GUI(Connection conn, int width, int height) {
        super("Server Interface");
        this.conn = conn;
        setSize(width, height);
        setLayout(new BorderLayout());

        // Left hand side menu selection
        JPanel menuPanel = new JPanel();
        menuPanel.setPreferredSize(new Dimension(500,700));
        menuPanel.setBackground(Color.gray);
        menuPanel.setLayout(new GridLayout(20,20));

        //Right hand side current cart
        cartPanel = new JPanel();
        cartPanel.setBackground(Color.gray);
        cartPanel.setPreferredSize(new Dimension(400, 600));

        JLabel cartLabel = new JLabel("Cart");
        cartLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        cartPanel.add(cartLabel);

        listModel = new DefaultListModel<String>();
        itemsJList = new JList<String>(listModel);
        cartPanel.add(itemsJList);

        JScrollPane cartScrollPane = new JScrollPane(itemsJList);
        cartScrollPane.setPreferredSize(new Dimension(400, 400));
        cartPanel.add(cartScrollPane);
        add(cartPanel, BorderLayout.EAST);
        
        //Middle area
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3,1));
        priceLabel = new JLabel("$0.00", SwingConstants.CENTER);
        delete = new JButton("Clear cart");
        complete = new JButton("Complete order");
        centerPanel.add(priceLabel);
        centerPanel.add(delete);
        centerPanel.add(complete);

        add(centerPanel, BorderLayout.SOUTH);

        JPanel southPanel = new JPanel();
        //DAY DROP DOWN
        String[] daysToChoose = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        JComboBox<String> dComboBox = new JComboBox<>(daysToChoose);

        JTextField weekField = new JTextField(3);
        weekField.setText("0");
        southPanel.add(weekField);
        southPanel.add(dComboBox);
        
        add(southPanel, BorderLayout.NORTH);

        delete.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            listModel.clear();
            cartIDList.clear();
            price = 0;
            priceLabel.setText("$0.00");
          }
        });

        complete.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            //TODO: Update inventory values, store order in orders table, update revenue table???
            week =Integer.parseInt(weekField.getText());
            day = dComboBox.getItemAt(dComboBox.getSelectedIndex());
            try {
              updateInventory();
              updateOrders();
              updateRevenue();
            } catch(SQLException sqle) {
              sqle.printStackTrace();
            }
            priceLabel.setText("$0.00");
            price = 0;
            listModel.clear();
            cartIDList.clear();
          }
        });

        mealIDs = new ArrayList<String>();
        menuItemPrices = new HashMap<String, Float>();
        cartIDList = new ArrayList<String>();
        ResultSet menuCount = getData(conn, "SELECT COUNT(*) FROM MENU;");
        ResultSet menuItems = getData(conn, "SELECT * FROM MENU;");
        try {
          int count = -1;
          if(menuCount.next()) {
            count = Integer.parseInt(menuCount.getString("count"));
          }
          if(count<0) {
            System.out.println("Error getting menu count!");
            System.exit(-1);
          }
          JButton[] buttons = new JButton[count];
          HashMap<String, Boolean> mealNames = new HashMap<String, Boolean>();
          
          for(int i=0;i<count;i++) {
            menuItems.next();
            if(mealNames.get(menuItems.getString("mealname"))==null) {
              String mealName = menuItems.getString("mealname");
              Float mealPrice = Float.parseFloat(menuItems.getString("price"));
              menuItemPrices.put(mealName, mealPrice);
              String mealID = menuItems.getString("mealid");
              mealIDs.add(mealID);
              mealNames.put(mealName, true);

              buttons[i] = new JButton(mealName);
              buttons[i].setPreferredSize(new Dimension(50, 50));
              buttons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  String labelString = String.format("%s - $%2.2f", mealName, mealPrice); 
                  price = price + mealPrice;
                  priceLabel.setText(String.format("$%3.2f", price));
                  listModel.addElement(labelString);
                  cartIDList.add(mealID);
                  cartPanel.revalidate();
                }
              });
              menuPanel.add(buttons[i]);
            }
          }
          
        } catch(Exception e) {
          e.printStackTrace();
        }
        

        /*JPanel menuPanel = new JPanel();
        menuPanel.setPreferredSize(new Dimension(100,1000));
        menuPanel.setBackground(Color.gray);
        menuPanel.setLayout(new GridLayout(3,1));
        

        JButton b1 = new JButton("Button 1");
        b1.setBounds(50,100,80,30);
        b1.setBackground(Color.green);
        menuPanel.add(b1);

        JButton b2 = new JButton("Button 2");
        b2.setBounds(50,100,80,30);
        b2.setBackground(Color.green);
        menuPanel.add(b2);*/

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        //scrollPane.setBounds(40,80,200,100);
        add(scrollPane, BorderLayout.WEST);

        // Mark one more active window when this one closes, and synchronize to avoid race condition
        mutex.lock();
        activeWindows+=1;
        mutex.unlock();

        addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent windowEvent) {
            try {
              conn.close();
            } catch(Exception e) {
            }
            // Mark one less active window when this one closes, and synchronize to avoid race condition
            mutex.lock();
            activeWindows-=1;
            mutex.unlock();
          }
        });
        setVisible(true);
    }
    public static void main(String[] args)
    {
      mutex = new ReentrantLock();

      System.out.println(Arrays.toString(args));
      System.out.println(args.length);

      if(args.length > 1) {
        System.out.println("Error: incorrect cmd args. 0 for server gui. 1 for manager gui.");
      }
      int arg = 0;
      if(args.length==1)
        arg = Integer.parseInt(args[0]);
      if(arg==0 || args.length==0) {
        Connection conn = null;
        try {
          Class.forName("org.postgresql.Driver");
          conn = DriverManager.getConnection(db, user, password);
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println(e.getClass().getName()+": "+e.getMessage());
          System.exit(0);
        }
        new GUI(conn, SERVER_WIDTH, SERVER_HEIGHT);
      } 
      else if(arg==1) {
        //Make a separate connection for the manager GUI avoid synchronization issues
        try {
          Class.forName("org.postgresql.Driver");
          Connection conn2 = DriverManager.getConnection(db, user, password);
          new ManagerGUI(conn2, MANAGER_WIDTH, MANAGER_HEIGHT);
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println(e.getClass().getName()+": "+e.getMessage());
          System.exit(0);
        }
      }

      //Busy spin until there are no active windows, then exit.
      while(true) {
        try {
          Thread.sleep(2000);
        } catch(InterruptedException e) {

        }
        GUI.mutex.lock();
        if(activeWindows==0)
          System.exit(0);
        GUI.mutex.unlock();
      }
    }

    // if button is pressed
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
    }

}