import org.apache.log4j.Logger;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI extends JFrame {

    Logger logger = Logger.getLogger(UI.class);

    private static UI instance = new UI();

    // shows name if client is found
    private final JLabel name;
    // shows points available
    private final JLabel points;
    // input phone number here
    private final JTextField phone;
    // sum of purchase
    private final JTextField addSum;
    // number of receipt
    private final JTextField addNum;
    // sum of points to use
    private final JTextField usePnt;
    // sum of purchase
    private final JTextField useSum;
    // number of receipt
    private final JTextField useNum;

    private final Container statusPanel;
    private final Container searchPanel;
    private final Container updatePanel;
    private final Container createPanel;
    private final Container reportsPanel;

    private Model model = Model.getInstance();

    private UI () {
        setTitle("Программа Лояльности");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        setSize(dimension.width/4*3, dimension.height/2);
        setLocation(150,150);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(5,1));

        statusPanel = new JPanel(new FlowLayout());
        searchPanel = new JPanel(new GridBagLayout());
        updatePanel = new JPanel(new GridBagLayout());
        createPanel = new JPanel(new GridBagLayout());
        reportsPanel = new JPanel(new FlowLayout());

        Insets zeroInsets = new Insets(0,0,0,0);

        // status panel objects
        name = new JLabel("Empty");
        points = new JLabel("Empty");

        // searchPanel objects
        phone = new JTextField();
        phone.setPreferredSize(new Dimension(200,20));
        PromptSupport.setPrompt("Введите номер телефона", phone);
        GridBagConstraints phoneC = new GridBagConstraints(0,0,2,1,0.8,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,100,0);

        JButton search = new JButton("Найти");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.setPhone(phone.getText())) {
                    if (findClient()) {
                        showStatusPanel(true);
                        showUpdatePanel(true);
                        showCreatePanel(false);
                    } else {
                        showStatusPanel(false);
                        showUpdatePanel(false);
                        showCreatePanel(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Неправильный формат номера телефона!", "Ошибка!",JOptionPane.ERROR_MESSAGE);
                    resetSearch();
                }
            }
        });
        GridBagConstraints searchC = new GridBagConstraints(2,0,1,1,0.2,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        // updatePanel objects
        // adding
        addSum = new JTextField();
        addSum.setPreferredSize(new Dimension(200,20));
        PromptSupport.setPrompt("Сумма чека",addSum);
        GridBagConstraints addSumC = new GridBagConstraints(0,0,1,1,0.4,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        addNum = new JTextField();
        addNum.setPreferredSize(new Dimension(100,20));
        PromptSupport.setPrompt("Номер чека",addNum);
        GridBagConstraints addNumC = new GridBagConstraints(1,0,1,1,0.4,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        JButton add = new JButton("Зачислить");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setSumOfPurchaseAdding(addSum.getText());
                model.setReceiptAdding(addNum.getText());
                addOrUsePoints(true);
                resetAdding();
            }
        });
        GridBagConstraints addC = new GridBagConstraints(2,0,1,1,0.2,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        // using
        usePnt = new JTextField();
        usePnt.setPreferredSize(new Dimension(200,20));
        PromptSupport.setPrompt("Списать баллы",usePnt);
        GridBagConstraints useSumC = new GridBagConstraints(0,1,1,1,0.4,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        useSum = new JTextField();
        useSum.setPreferredSize(new Dimension(200,20));
        PromptSupport.setPrompt("Сумма чека",useSum);
        GridBagConstraints usePntC = new GridBagConstraints(1,1,1,1,0.4,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        useNum = new JTextField();
        useNum.setPreferredSize(new Dimension(100,20));
        PromptSupport.setPrompt("Номер чека",useNum);
        GridBagConstraints useNumC = new GridBagConstraints(2,1,1,1,0.4,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        JButton use = new JButton("Списать");
        use.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setSumOfPurchaseUsing(useSum.getText());
                model.setReceiptUsing(useNum.getText());
                model.setPointsUsing(usePnt.getText());
                addOrUsePoints(false);
            }
        });
        GridBagConstraints useC = new GridBagConstraints(3,1,1,1,0.2,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        // create panel objects
        JLabel notFound = new JLabel("Телефон не зарегестрирован. Создать запись?");
        GridBagConstraints notFoundC = new GridBagConstraints(0,0,1,1,0.8,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        JButton create = new JButton("Создать");
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Creating a client.");
                boolean validInput = false;
                String dialog = "Введите имя";
                String name = null;
                while (!validInput) {
                    name = JOptionPane.showInputDialog(dialog).trim();
                    logger.info("Entered: " + name);
                    if (name.length() <= 30 && !name.isEmpty()) {
                        validInput = true;
                        logger.info("Input accepted!");
                        model.setName(name);
                    } else {
                        logger.info("Invalid input!");
                        dialog = "Неправильный ввод! Повторите.";
                    }
                }
                if (createClient()) {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Клиент успешно создан!");
                    showCreatePanel(false);
                } else {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Не удалось создать клиента!","Ошибка!",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        GridBagConstraints createC = new GridBagConstraints(1,0,1,1,0.2,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,zeroInsets,0,0);

        // reports panel objects
        JButton dayReport = new JButton("Ежедневный отчёт");
        dayReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(dayReport()) {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Дневной отчёт успешно создан!", "Успешно!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Не удалось создать отчёт!", "Ошибка!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JButton clientReport = new JButton("Отчёт по клиенту");
        clientReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clientReport()) {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Отчёт по клиенту успешно создан!", "Успешно!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(UI.getInstance(),"Не удалось создать отчёт!", "Ошибка!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JButton generateRandom = new JButton("Сгенерировать рандомные покупки");
        generateRandom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateRandom();
            }
        });

        statusPanel.add(name);
        statusPanel.add(points);

        searchPanel.add(phone,phoneC);
        searchPanel.add(search,searchC);

        updatePanel.add(addSum,addSumC);
        updatePanel.add(addNum,addNumC);
        updatePanel.add(add,addC);
        updatePanel.add(usePnt,usePntC);
        updatePanel.add(useSum,useSumC);
        updatePanel.add(useNum,useNumC);
        updatePanel.add(use,useC);

        createPanel.add(notFound,notFoundC);
        createPanel.add(create,createC);

        reportsPanel.add(dayReport);
        reportsPanel.add(clientReport);
        reportsPanel.add(generateRandom);

        contentPane.add(statusPanel);
        contentPane.add(searchPanel);
        contentPane.add(updatePanel);
        contentPane.add(createPanel);
        contentPane.add(reportsPanel);

        showStatusPanel(false);
        showSearchPanel(true);
        showUpdatePanel(false);
        showCreatePanel(false);

        revalidate();
        setVisible(false);
    }

    public static UI getInstance() {
        if (instance == null) {
            instance = new UI();
        }
        return instance;
    }

    private void setClientName(String name) {
        this.name.setText(name);
    }
    private void setPoints(int points) {
        this.points.setText("Доступно " + points);
    }

    private void renewStatus() {
        setClientName(model.getName());
        setPoints(model.getPoints());
    }
    private void resetStatus() {
        setClientName("");
        setPoints(0);
        model.resetName();
        model.resetPoints();
    }
    private void resetSearch() {
        phone.setText("");
        model.resetPhone();
    }
    private void resetAdding() {
        addNum.setText("");
        addSum.setText("");
        model.resetSumOfPurchaseAdding();
        model.resetReceiptAdding();
    }
    private void resetUsing() {
        useNum.setText("");
        usePnt.setText("");
        useSum.setText("");
        model.resetSumOfPurchaseUsing();
        model.resetReceiptUsing();
        model.resetPointsUsing();
    }

    private void showStatusPanel(boolean display) {
        statusPanel.setVisible(display);
    }
    private void showSearchPanel(boolean display) {
        searchPanel.setVisible(display);
    }
    private void showUpdatePanel(boolean display) {
        updatePanel.setVisible(display);
    }
    private void showCreatePanel(boolean display) {
        createPanel.setVisible(display);
    }
    private boolean findClient() {
        if (ClientController.getInstance().findClient()) {
            renewStatus();
            return true;
        } else {
            JOptionPane.showMessageDialog(UI.getInstance(),"Клиент не найден!");
        }
        return false;
    }
    private void addOrUsePoints(boolean add) {
        try {
            // TODO: if insufficient points - program continues!
            model.updateClient(add);
             // TODO:
            if (false) {
                logger.error("FAILED TO DO AN OPERATION WITH POINTS!");
                JOptionPane.showMessageDialog(UI.getInstance(),"Не удалось провести операцию!", "Ошибка!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(UI.getInstance(),"Неправильный формат баллов!", "Ошибка!", JOptionPane.ERROR_MESSAGE);
            logger.error("POINTS INVALID FORMAT\n" + nfe.getMessage());
        } finally {
            resetAdding();
            resetUsing();
            // renewing the status panel
            findClient();
        }
    }
    private boolean createClient() {
        model.createClient();
        // TODO: do!
        return true;
    }
    private boolean dayReport() {
        // TODO: do
//        JOptionPane.showMessageDialog(this,"Function is disabled.", "Disabled function", JOptionPane.ERROR_MESSAGE);
//        for (Purchase p : PurchaseController.getInstance().getTodayPurchases()) {
//            System.out.println(p);
//        }
        java.util.List<Purchase> purchases = PurchaseController.getInstance().getTodayPurchases();
        DayReportRow.startNewReport();
        for (Purchase p : purchases) {
            DayReportRow.addNewRow(p);
        }
        for (DayReportRow row : DayReportRow.getRows()) {
            System.out.println(row);
        }

//        return PDFCreator.getInstance().createDayReport();
        return false;
    }
    private boolean clientReport() {
        // TODO: do
        JOptionPane.showMessageDialog(this,"Function is disabled.", "Disabled function", JOptionPane.ERROR_MESSAGE);
//        Interval[] options = Interval.getValues();
//        int choice = JOptionPane.showOptionDialog(this,"Выберите интервал","Отчёт по клиенту",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,0);
//        return PDFCreator.getInstance().createClientReport(options[choice]);
        return false;
    }
    private void generateRandom() {
        // TODO: do
        JOptionPane.showMessageDialog(this,"Random purchases generation is disabled.", "Disabled function",JOptionPane.ERROR_MESSAGE);
//        DBController.getInstance().generateRandomPurchases();
    }

    public void showError(String text, boolean exit) {
        JOptionPane.showMessageDialog(this,text,"Error!",JOptionPane.ERROR_MESSAGE);
        if (exit) System.exit(0);
    }

    void showWindow() {
        setVisible(true);
    }
}
