package fr.liglab.adele.icasa.apps.jabber.chat.config.ui;

/**
 * Created by donatien on 24/04/14.
 */

import fr.liglab.adele.icasa.jabber.chat.config.configInt.ChatConfig;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.liglab.adele.icasa.apps.jabber.chat.config.ui.uiInt.ConfigDisplayer;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

@Component
@Provides
@Instantiate
public class ChatConfigUi extends JFrame implements ConfigDisplayer{

    @Requires(filter = "(factory.name=fr.liglab.adele.icasa.jabber.chat.config.functions.ChatConfigImpl)")
    private Factory factory;
    private  ComponentInstance config = null;
    @Requires(optional=true)
    private ChatConfig chatConfig;

    private final BundleContext context;

    private JPanel onglet1;
    private JPanel onglet2;
    private JButton save ;
    private JButton apply ;
    private JButton cancel ;
    private JTextField userText;
    private JTextField portText;
    private JTextField serviceText;
    private JTextField hostText;
    private JTextField passwordText;

    private static final Logger LOG= LoggerFactory.getLogger(ChatConfigUi.class);

    public ChatConfigUi(BundleContext pcontext) {
        super();
        context=pcontext;
        setTitle("Configuration");
        JTabbedPane tabbedPane = new JTabbedPane();
        onglet1 = new JPanel(new BorderLayout());
        setOnglet1();
        onglet2 = new JPanel(new BorderLayout());
        setOnglet2();
        //onglet2.setPreferredSize(new Dimension(200,200));
        tabbedPane.addTab("House message",onglet1);
        tabbedPane.addTab("House preferences",onglet2);
        setContentPane(tabbedPane);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(400, 250);
        setVisible(true);
    }

    private void setOnglet1(){
        onglet1.setPreferredSize(new Dimension(150, 200));
        save = new JButton("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Properties props = new Properties();
                props.put("config.host",hostText.getText());
                //props.put("config.port",parseInt(portText.getText()));
                props.put("config.user",userText.getText());
                props.put("config.pwd",passwordText.getText());
                props.put("config.service",serviceText.getText());
                try {
                    if(chatConfig!=null){
                        chatConfig=null;
                        config = factory.createComponentInstance(props);
                    }else{
                        config = factory.createComponentInstance(props);
                    }

                } catch (UnacceptableConfiguration unacceptableConfiguration) {
                    unacceptableConfiguration.printStackTrace();
                } catch (MissingHandlerException e1) {
                    e1.printStackTrace();
                } catch (ConfigurationException e1) {
                    e1.printStackTrace();
                }
            }});
        apply = new JButton("Apply");
        cancel = new JButton("Cancel");
        //String[] labels = {"Username: ", "Port: ", "Password: ", "Host: ", "Service :"};
        //int numPairs = labels.length;

//Create and populate the panel.
        JPanel milieu = new JPanel();
        milieu.setLayout(new GridLayout(5,0));
        JPanel user = new JPanel(new BorderLayout());
        JLabel username = new JLabel("Username :");
        user.add(username, BorderLayout.WEST);
        userText = new JTextField(10);
        username.setLabelFor(userText);
        user.add(userText);
        milieu.add(user);

        JPanel panel1 = new JPanel(new BorderLayout());
        JLabel port = new JLabel("Port :");
        panel1.add(port, BorderLayout.WEST);
        portText = new JTextField(10);
        username.setLabelFor(portText);
        panel1.add(portText);
        milieu.add(panel1);

        JPanel panel2 = new JPanel(new BorderLayout());
        JLabel host = new JLabel("Host :");
        panel2.add(host, BorderLayout.WEST);
        hostText = new JTextField(10);
        host.setLabelFor(hostText);
        panel2.add(hostText);
        milieu.add(panel2);

        JPanel panel3 = new JPanel(new BorderLayout());
        JLabel service = new JLabel("Service :");
        panel3.add(service, BorderLayout.WEST);
        serviceText = new JTextField(10);
        host.setLabelFor(serviceText);
        panel3.add(serviceText);
        milieu.add(panel3);

        JPanel panel4 = new JPanel(new BorderLayout());
        JLabel pwd = new JLabel("Password:");
        panel4.add(pwd, BorderLayout.WEST);
        passwordText = new JTextField(10);
        host.setLabelFor(passwordText);
        panel4.add(passwordText);
        milieu.add(panel4);

        JPanel bas=new JPanel();
        bas.setLayout(new BoxLayout(bas, BoxLayout.LINE_AXIS));
        bas.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bas.add(Box.createHorizontalGlue());
        bas.add(cancel);
        bas.add(Box.createRigidArea(new Dimension(10, 0)));
        bas.add(save);
        bas.add(Box.createRigidArea(new Dimension(10, 0)));
        bas.add(apply);
        onglet1.add(milieu);
        onglet1.add(bas,BorderLayout.SOUTH);
    }

    private void setOnglet2(){
        onglet2.setPreferredSize(new Dimension(150, 200));
    }

    @Validate
    public void start(){
        loadData();
        this.setVisible(false);
        LOG.info("Interface starts");
    }

    private void loadData() {
       /* LOG.info(chatConfig.toString());
       if(chatConfig!=null){
           ServiceReference<? extends ChatConfig> reference=context.getServiceReference(chatConfig.getClass());
           userText.setText(reference.getProperty(chatConfig.USER_PROPERTY).toString());
           portText.setText("5222");
           passwordText.setText(reference.getProperty(chatConfig.PASSWORD_PROPERTY).toString());
           serviceText.setText(reference.getProperty(chatConfig.SERVICE_PROPERTY).toString());
           hostText.setText(reference.getProperty(chatConfig.HOST_PROPERTY).toString());
       }else{

       }*/
    }

    @Invalidate
    public void stop(){
        if(config!=null)config.stop();
        this.dispose();
        LOG.info("Interface stopped");
    }


    public void showConfig() {
        this.setVisible(true);
    }
}