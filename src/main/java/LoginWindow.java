import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame {

    private final LoginWindow thisObject = this;
    private final DBController dbc = DBController.getInstance();
    private final UI ui = UI.getInstance();

    private final JLabel loginLabel = new JLabel("Enter login:");
    private final JLabel passwordLabel = new JLabel("Enter password:");
    private final JTextField login = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JButton cancel = new JButton("Cancel");
    private final JButton log = new JButton("Log in");

    LoginWindow () {

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Dimension screen = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        setBounds((int)screen.getWidth()/3, (int)screen.getHeight()/3, (int)screen.getWidth()/3,(int)screen.getHeight()/4);

        Container container = getContentPane();
        container.setLayout(new GridLayout(3,2));

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        log.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dbc.connect(login.getText().trim(),password.getText())) {
                    closeLoginWindow();
                } else {
                    resetFields();
                    JOptionPane.showMessageDialog(thisObject, "Incorrect login/password","Authentification Failed!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        container.add(loginLabel);
        container.add(login);
        container.add(passwordLabel);
        container.add(password);
        container.add(cancel);
        container.add(log);

        setVisible(true);
    }
    private void closeLoginWindow() {
        ui.showWindow();
        thisObject.dispose();
    }
    private void resetFields() {
        login.setText("");
        password.setText("");
    }
}
