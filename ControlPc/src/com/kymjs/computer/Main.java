package com.kymjs.computer;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * ���Զ˴���
 */
public class Main extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int port = 8899;

    private JTextField messagebox;
    private int menux = 0; // menux�źű�־ 0��ʾδ���� 1��ʾ���� 2��ʾ��ͣ
    private boolean btnIsStart = false;
    private ServerThread serverthread; // ����̬�߳�

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        super();
        initLayout();
        initIpAddress();
        initButton();
        initExplain();
        this.setVisible(true);
    }

    /**
     * ��ʼ�����ڲ���
     */
    private void initLayout() {
        setTitle("kymjs������");
        setSize(230, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = getToolkit().getScreenSize(); // ���Dimension����
        int screenHeight = dimension.height; // �����Ļ�Ŀ�߶�
        int screenWidth = dimension.width;
        int frm_Height = this.getHeight(); // ��ȡ������
        int frm_width = this.getWidth();
        setLocation((screenWidth - frm_width) / 2,
                (screenHeight - frm_Height) / 2); // ������ʾ����
        getContentPane().setLayout(null);
    }

    /**
     * ��ʼ��IP��Ϣ
     */
    private void initIpAddress() {
        String localIp = null;
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e1) {
        }
        final JLabel label = new JLabel();
        label.setFont(new Font("SimSun", Font.PLAIN, 16));
        if (localIp != null && localIp.startsWith("192.168.1.")) {
            label.setText("����IPΪ��" + localIp);
            label.setBounds(10, 20, 300, 25);
        } else {
            label.setText("��ȡIPʧ�ܣ����ֶ����");
            label.setBounds(30, 20, 300, 25);
        }
        getContentPane().add(label);
    }

    /**
     * ��/�ذ�ť
     */
    private void initButton() {
        final JButton mBtn = new JButton("����");
        mBtn.setBounds(10, 60, 190, 25);
        getContentPane().add(mBtn);
        mBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!btnIsStart) {
                    start();
                    mBtn.setText("�ر�");
                } else {
                    stop();
                    mBtn.setText("����");
                }
            }
        });
    }

    /**
     * ��ʼ��˵��Text
     */
    private void initExplain() {
        final JLabel label3 = new JLabel();
        label3.setText("�����ֻ������뱾��IP");
        label3.setBounds(40, 100, 280, 25);
        getContentPane().add(label3);

        messagebox = new JTextField();
        messagebox.setBounds(10, 130, 190, 25);
        messagebox.setEnabled(false);
        getContentPane().add(messagebox);
    }

    public void start() {
        btnIsStart = true;
        if (menux == 0) {
            serverthread = new ServerThread();
            serverthread.start();
            menux = 1;
            messagebox.setText("���Ʒ����ѿ���");
        }
        if (menux == 2) {
            serverthread.resume();
            menux = 1;
            messagebox.setText("���Ʒ����ѻָ�");
        }
    }

    public void stop() {
        btnIsStart = false;
        if (menux == 1) {
            serverthread.suspend();
            menux = 2;
            messagebox.setText("���Ʒ�������ͣ");
        }
    }

    public class ServerThread extends Thread {
        public void run() {
            connection();
        }

        private void connection() {
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(port);
                messagebox.setText("���Ʒ��������ɹ�");
            } catch (Exception e) {
                messagebox.setText(port + "�˿ڱ�ռ��");
                menux = 0;
                return;
            }
            byte data[] = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data,
                    data.length);
            try {
                while (true) {
                    socket.receive(packet);
                    String message = new String(packet.getData(),
                            packet.getOffset(), packet.getLength());
                    messagebox.setText(message);
                    String[] messages = message.split(":");
                    if (messages.length >= 2) {
                        String type = messages[0];
                        String info = messages[1];
                        switch (type) {
                        case "mouse":
                            mouseMove(info);
                            break;
                        case "leftButton":
                            leftButton(info);
                            break;
                        case "rightButton":
                            rightButton(info);
                            break;
                        case "mousewheel":
                            mouseWheel(info);
                            break;
                        case "keyboard":
                            keyBoard(info);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        }

        /**
         * ��������ƶ�
         */
        public void mouseMove(String info) {
            String args[] = info.split(",");
            String x = args[0];
            String y = args[1];
            float px = Float.valueOf(x);
            float py = Float.valueOf(y);

            PointerInfo pinfo = MouseInfo.getPointerInfo(); // �õ���������
            java.awt.Point p = pinfo.getLocation();
            double mx = p.getX(); // �õ���ǰ������������
            double my = p.getY();
            try {
                java.awt.Robot robot = new Robot();
                robot.mouseMove((int) (mx + px), (int) (my + py));
            } catch (AWTException e) {
            }
        }

        /**
         * ����¼�����
         */
        public void leftButton(String info) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            if (info.equals("down")) {
                robot.mousePress(InputEvent.BUTTON1_MASK);
            } else if (info.equals("release")) {
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            } else if (info.equals("up")) {
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            } else if (info.equals("click")) {
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            }
        }

        /**
         * �Ҽ��¼�����
         */
        public void rightButton(String info) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            if (info.equals("down")) {
                robot.mousePress(InputEvent.BUTTON3_MASK);
            } else if (info.equals("release")) {
                robot.mouseRelease(InputEvent.BUTTON3_MASK);
            } else if (info.equals("up")) {
                robot.mouseRelease(InputEvent.BUTTON3_MASK);
            } else if (info.equals("click")) {
                robot.mousePress(InputEvent.BUTTON3_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_MASK);
            }
        }

        /**
         * �������¼�����
         */
        public void mouseWheel(String info) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            if (info.startsWith("-")) {
                robot.mouseWheel(-1);
            } else {
                robot.mouseWheel(1);
            }
        }

        /**
         * �����¼�����
         */
        public void keyBoard(String info) {
            switch (info) {
            case "Space":
                keyBoardClick(KeyEvent.VK_SPACE);
                break;
            case "Up":
                keyBoardClick(KeyEvent.VK_UP);
                break;
            case "UpPress":
                keyBoardPress(KeyEvent.VK_UP);
                break;
            case "UpRelease":
                keyBoardRelease(KeyEvent.VK_UP);
                break;
            case "Down":
                keyBoardClick(KeyEvent.VK_DOWN);
                break;
            case "DownPress":
                keyBoardPress(KeyEvent.VK_DOWN);
                break;
            case "DownRelease":
                keyBoardRelease(KeyEvent.VK_DOWN);
                break;
            case "Left":
                keyBoardClick(KeyEvent.VK_LEFT);
                break;
            case "LeftPress":
                keyBoardPress(KeyEvent.VK_LEFT);
                break;
            case "LeftRelease":
                keyBoardRelease(KeyEvent.VK_LEFT);
                break;
            case "Right":
                keyBoardClick(KeyEvent.VK_RIGHT);
                break;
            case "RightPress":
                keyBoardPress(KeyEvent.VK_RIGHT);
                break;
            case "RightRelease":
                keyBoardRelease(KeyEvent.VK_RIGHT);
                break;
            case "W":
                keyBoardClick(KeyEvent.VK_W);
                break;
            case "WPress":
                keyBoardPress(KeyEvent.VK_W);
                break;
            case "WRelease":
                keyBoardRelease(KeyEvent.VK_W);
                break;
            case "S":
                keyBoardClick(KeyEvent.VK_S);
                break;
            case "SPress":
                keyBoardPress(KeyEvent.VK_S);
                break;
            case "SRelease":
                keyBoardRelease(KeyEvent.VK_S);
                break;
            case "A":
                keyBoardClick(KeyEvent.VK_A);
                break;
            case "APress":
                keyBoardPress(KeyEvent.VK_A);
                break;
            case "ARelease":
                keyBoardRelease(KeyEvent.VK_A);
                break;
            case "D":
                keyBoardClick(KeyEvent.VK_D);
                break;
            case "DPress":
                keyBoardPress(KeyEvent.VK_D);
                break;
            case "DRelease":
                keyBoardRelease(KeyEvent.VK_D);
                break;
            case "Ctrl+Z":
                keyBoardGroup(KeyEvent.VK_CONTROL, KeyEvent.VK_Z);
                break;
            case "Ctrl+C":
                keyBoardGroup(KeyEvent.VK_CONTROL, KeyEvent.VK_C);
                break;
            case "Ctrl+V":
                keyBoardGroup(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
                break;
            case "Ctrl+S":
                keyBoardGroup(KeyEvent.VK_CONTROL, KeyEvent.VK_S);
                break;
            }
        }

        /**
         * ����
         * 
         * @param key
         */
        private void keyBoardPress(int key) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            robot.keyPress(key);
        }

        /**
         * ̧��
         * 
         * @param key
         */
        private void keyBoardRelease(int key) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            robot.keyRelease(key);
        }

        /**
         * ����
         */
        private void keyBoardClick(int key) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            robot.keyPress(key);
            robot.keyRelease(key);
        }

        /**
         * ��ϼ�
         */
        private void keyBoardGroup(int keyPress, int key) {
            java.awt.Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                return;
            }
            robot.keyPress(keyPress);
            robot.keyPress(key);
            robot.keyRelease(key);
            robot.keyRelease(keyPress);
        }
    }
}
